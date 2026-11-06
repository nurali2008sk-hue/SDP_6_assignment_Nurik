package kz.nurali.inspection;

public class QualityGateVisitor implements ProjectVisitor {
    private static final double MAX_AVERAGE_COMPLEXITY = 10.0;
    private static final double MIN_TEST_SOURCE_RATIO = 0.40;

    private final SecurityAuditVisitor securityVisitor =
            new SecurityAuditVisitor();

    private final ProjectStatisticsVisitor statisticsVisitor =
            new ProjectStatisticsVisitor();

    @Override
    public void visit(SourceCodeFile file) {
        file.accept(securityVisitor);
        file.accept(statisticsVisitor);
    }

    @Override
    public void visit(TestFile file) {
        file.accept(securityVisitor);
        file.accept(statisticsVisitor);
    }

    @Override
    public void visit(ConfigurationFile file) {
        file.accept(securityVisitor);
        file.accept(statisticsVisitor);
    }

    @Override
    public void visit(DependencyFile file) {
        file.accept(securityVisitor);
        file.accept(statisticsVisitor);
    }

    @Override
    public void visit(Directory directory) {
        for (ProjectElement element : directory.getChildren()) {
            element.accept(this);
        }
    }

    public boolean isSecurityPassed() {
        return securityVisitor.getCriticalIssueCount() == 0;
    }

    public boolean isComplexityPassed() {
        return statisticsVisitor.getAverageComplexity()
                <= MAX_AVERAGE_COMPLEXITY;
    }

    public boolean isTestsPassed() {
        return statisticsVisitor.getTestSourceRatio()
                >= MIN_TEST_SOURCE_RATIO;
    }

    public boolean isPassed() {
        return isSecurityPassed()
                && isComplexityPassed()
                && isTestsPassed();
    }

    private String status(boolean passed) {
        return passed ? "PASS" : "FAIL";
    }

    public void printReport() {
        System.out.println("=== QUALITY GATE ===");
        System.out.println("Security: " + status(isSecurityPassed()));
        System.out.println("Complexity: " + status(isComplexityPassed()));
        System.out.println("Tests: " + status(isTestsPassed()));

        System.out.println(
                "FINAL RESULT: " + (isPassed() ? "PASSED" : "FAILED")
        );

        System.out.println();
    }
}