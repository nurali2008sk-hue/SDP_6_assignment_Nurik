package kz.nurali.inspection;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InspectionVisitorsTest {

    @Test
    void demoStatisticsIncludeAllFileTypes() {
        Directory project = InspectionDemo.createDemoProject();
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();

        project.accept(visitor);

        assertEquals(5, visitor.getTotalFiles());
        assertEquals(500, visitor.getTotalLinesOfCode());
        assertEquals(2, visitor.getSourceFileCount());
        assertEquals(1, visitor.getTestFileCount());
        assertEquals(4, visitor.getTotalTests());
        assertEquals(2, visitor.getTotalDependencies());
        assertEquals(16.0, visitor.getAverageComplexity(), 0.0001);
        assertEquals(0.5, visitor.getTestSourceRatio(), 0.0001);
    }

    @Test
    void statisticsTraverseNestedDirectories() {
        Directory root = new Directory("root");
        Directory src = new Directory("src");
        Directory services = new Directory("services");

        root.addElement(new SourceCodeFile("App.java", 10, 4, false, 0));
        services.addElement(
                new SourceCodeFile("Service.java", 20, 8, false, 0)
        );

        src.addElement(services);
        root.addElement(src);

        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        root.accept(visitor);

        assertEquals(2, visitor.getTotalFiles());
        assertEquals(30, visitor.getTotalLinesOfCode());
        assertEquals(6.0, visitor.getAverageComplexity(), 0.0001);
    }

    @Test
    void emptyDirectoryHasZeroStatistics() {
        Directory root = new Directory("empty");
        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();

        root.accept(visitor);

        assertEquals(0, visitor.getTotalFiles());
        assertEquals(0, visitor.getTotalLinesOfCode());
        assertEquals(0, visitor.getSourceFileCount());
        assertEquals(0, visitor.getTestFileCount());
        assertEquals(0, visitor.getTotalTests());
        assertEquals(0, visitor.getTotalDependencies());
        assertEquals(0.0, visitor.getAverageComplexity(), 0.0001);
        assertEquals(0.0, visitor.getTestSourceRatio(), 0.0001);
    }

    @Test
    void statisticsPreserveFractionalResults() {
        Directory root = new Directory("root");

        root.addElement(new SourceCodeFile("A.java", 10, 1, false, 0));
        root.addElement(new SourceCodeFile("B.java", 10, 2, false, 0));
        root.addElement(new SourceCodeFile("C.java", 10, 2, false, 0));
        root.addElement(new TestFile("ATest.java", 10, 1, "A.java"));

        ProjectStatisticsVisitor visitor = new ProjectStatisticsVisitor();
        root.accept(visitor);

        assertEquals(5.0 / 3.0, visitor.getAverageComplexity(), 0.0001);
        assertEquals(1.0 / 3.0, visitor.getTestSourceRatio(), 0.0001);
    }

    @Test
    void securityDetectsCredentialsAndHighComplexity() {
        SourceCodeFile file =
                new SourceCodeFile("Risky.java", 100, 21, true, 0);

        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        file.accept(visitor);

        assertEquals(1, visitor.getCriticalIssueCount());
        assertEquals(1, visitor.getWarningCount());
        assertEquals(65, visitor.getSecurityScore());

        assertTrue(visitor.getCriticalIssues().contains(
                "Hardcoded credentials in Risky.java"
        ));

        assertTrue(visitor.getWarnings().contains(
                "High complexity in Risky.java: 21"
        ));
    }

    @Test
    void securityAcceptsComplexityAtThreshold() {
        SourceCodeFile file =
                new SourceCodeFile("Safe.java", 100, 20, false, 0);

        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        file.accept(visitor);

        assertEquals(0, visitor.getCriticalIssueCount());
        assertEquals(0, visitor.getWarningCount());
        assertEquals(100, visitor.getSecurityScore());
    }

    @Test
    void securityDetectsBothConfigurationProblems() {
        ConfigurationFile file =
                new ConfigurationFile("app.properties", 20, true, true);

        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        file.accept(visitor);

        assertEquals(2, visitor.getCriticalIssueCount());
        assertEquals(50, visitor.getSecurityScore());

        assertTrue(visitor.getCriticalIssues().contains(
                "Exposed credentials in app.properties"
        ));

        assertTrue(visitor.getCriticalIssues().contains(
                "Insecure settings in app.properties"
        ));
    }

    @Test
    void securityDetectsVulnerableDependencies() {
        DependencyFile file = new DependencyFile(
                "pom.xml",
                30,
                List.of(
                        new Dependency("safe-library", "2.0", false, false),
                        new Dependency("risky-library", "1.0", true, false)
                )
        );

        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        file.accept(visitor);

        assertEquals(1, visitor.getCriticalIssueCount());
        assertEquals(75, visitor.getSecurityScore());

        assertEquals(
                List.of("Vulnerable dependency in pom.xml: risky-library"),
                visitor.getCriticalIssues()
        );
    }

    @Test
    void securityScoreCannotBeNegative() {
        Directory root = new Directory("root");

        for (int i = 0; i < 5; i++) {
            root.addElement(
                    new SourceCodeFile("File" + i + ".java", 10, 1, true, 0)
            );
        }

        SecurityAuditVisitor visitor = new SecurityAuditVisitor();
        root.accept(visitor);

        assertEquals(5, visitor.getCriticalIssueCount());
        assertEquals(0, visitor.getSecurityScore());
    }

    @Test
    void qualityGatePassesAtExactThresholds() {
        Directory root = new Directory("release-project");

        for (int i = 0; i < 5; i++) {
            root.addElement(
                    new SourceCodeFile("S" + i + ".java", 100, 10, false, 0)
            );
        }

        root.addElement(new TestFile("S0Test.java", 20, 1, "S0.java"));
        root.addElement(new TestFile("S1Test.java", 20, 1, "S1.java"));

        QualityGateVisitor visitor = new QualityGateVisitor();
        root.accept(visitor);

        assertTrue(visitor.isSecurityPassed());
        assertTrue(visitor.isComplexityPassed());
        assertTrue(visitor.isTestsPassed());
        assertTrue(visitor.isPassed());
    }

    @Test
    void qualityGateFailsForSecurityOnly() {
        Directory root = createQualityProject(5, true, true);

        QualityGateVisitor visitor = new QualityGateVisitor();
        root.accept(visitor);

        assertFalse(visitor.isSecurityPassed());
        assertTrue(visitor.isComplexityPassed());
        assertTrue(visitor.isTestsPassed());
        assertFalse(visitor.isPassed());
    }

    @Test
    void qualityGateFailsForComplexityOnly() {
        Directory root = createQualityProject(11, false, true);

        QualityGateVisitor visitor = new QualityGateVisitor();
        root.accept(visitor);

        assertTrue(visitor.isSecurityPassed());
        assertFalse(visitor.isComplexityPassed());
        assertTrue(visitor.isTestsPassed());
        assertFalse(visitor.isPassed());
    }

    @Test
    void qualityGateFailsForInsufficientTestsOnly() {
        Directory root = createQualityProject(5, false, false);

        QualityGateVisitor visitor = new QualityGateVisitor();
        root.accept(visitor);

        assertTrue(visitor.isSecurityPassed());
        assertTrue(visitor.isComplexityPassed());
        assertFalse(visitor.isTestsPassed());
        assertFalse(visitor.isPassed());
    }

    @Test
    void complianceReportsAllDemoViolationsWithoutDuplicates() {
        Directory root = InspectionDemo.createDemoProject();
        ComplianceVisitor visitor = new ComplianceVisitor();

        root.accept(visitor);

        List<String> expected = List.of(
                "Hardcoded credentials in PaymentService.java",
                "Missing version for dependency: unapproved-sdk",
                "Prohibited dependency: unapproved-sdk",
                "Missing tests for source file: UserService.java"
        );

        assertEquals(expected, visitor.getViolations());
        assertEquals(expected, visitor.getViolations());
        assertEquals(4, visitor.getViolationCount());
        assertFalse(visitor.isCompliant());
    }

    @Test
    void complianceAcceptsTestBeforeSource() {
        Directory root = new Directory("root");

        root.addElement(new TestFile("AppTest.java", 20, 2, "App.java"));
        root.addElement(new SourceCodeFile("App.java", 50, 5, false, 0));

        root.addElement(new DependencyFile(
                "pom.xml",
                20,
                List.of(new Dependency("approved-library", "1.0", false, false))
        ));

        ComplianceVisitor visitor = new ComplianceVisitor();
        root.accept(visitor);

        assertTrue(visitor.isCompliant());
        assertEquals(0, visitor.getViolationCount());
        assertTrue(visitor.getViolations().isEmpty());
    }

    @Test
    void complianceDetectsExposedConfigurationCredentials() {
        ConfigurationFile file =
                new ConfigurationFile("app.properties", 20, true, false);

        ComplianceVisitor visitor = new ComplianceVisitor();
        file.accept(visitor);

        assertFalse(visitor.isCompliant());

        assertEquals(
                List.of("Exposed credentials in app.properties"),
                visitor.getViolations()
        );
    }

    @Test
    void emptyTestFileDoesNotCoverSource() {
        Directory root = new Directory("root");

        root.addElement(new SourceCodeFile("App.java", 50, 5, false, 0));
        root.addElement(new TestFile("AppTest.java", 0, 0, "App.java"));

        ComplianceVisitor visitor = new ComplianceVisitor();
        root.accept(visitor);

        assertFalse(visitor.isCompliant());

        assertEquals(
                List.of("Missing tests for source file: App.java"),
                visitor.getViolations()
        );
    }

    @Test
    void technicalDebtMatchesDemoEstimate() {
        Directory root = InspectionDemo.createDemoProject();
        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();

        root.accept(visitor);

        assertEquals(4, visitor.getTotalTodoCount());
        assertEquals(60, visitor.getTodoDebtMinutes());
        assertEquals(420, visitor.getComplexityDebtMinutes());
        assertEquals(480, visitor.getTotalDebtMinutes());
        assertEquals(8.0, visitor.getEstimatedHours(), 0.0001);
    }

    @Test
    void technicalDebtIsZeroForCleanSource() {
        SourceCodeFile file =
                new SourceCodeFile("Clean.java", 50, 8, false, 0);

        TechnicalDebtVisitor visitor = new TechnicalDebtVisitor();
        file.accept(visitor);

        assertEquals(0, visitor.getTotalTodoCount());
        assertEquals(0, visitor.getComplexityDebtMinutes());
        assertEquals(0, visitor.getTotalDebtMinutes());
        assertEquals(0.0, visitor.getEstimatedHours(), 0.0001);
    }

    @Test
    void negativeMetricsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SourceCodeFile("Bad.java", 10, -1, false, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new TestFile("BadTest.java", 10, -1, "Bad.java")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ConfigurationFile("bad.properties", -1, false, false)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new DependencyFile("pom.xml", -1, List.of())
        );
    }

    private Directory createQualityProject(
            int complexity,
            boolean credentials,
            boolean includeTest
    ) {
        Directory root = new Directory("project");

        root.addElement(
                new SourceCodeFile("App.java", 100, complexity, credentials, 0)
        );

        if (includeTest) {
            root.addElement(
                    new TestFile("AppTest.java", 50, 2, "App.java")
            );
        }

        return root;
    }
}