package kz.nurali.inspection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ComplianceVisitor implements ProjectVisitor {
    private final List<String> violations = new ArrayList<>();

    private final Set<String> sourceFiles = new LinkedHashSet<>();
    private final Set<String> testedSourceFiles = new LinkedHashSet<>();

    @Override
    public void visit(SourceCodeFile file) {
        sourceFiles.add(file.getName());

        if (file.hasHardcodedCredentials()) {
            violations.add(
                    "Hardcoded credentials in " + file.getName()
            );
        }
    }

    @Override
    public void visit(TestFile file) {
        if (file.getTestCount() > 0) {
            testedSourceFiles.add(file.getTestedSourceFile());
        }
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.hasExposedCredentials()) {
            violations.add(
                    "Exposed credentials in " + file.getName()
            );
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.getDependencies()) {
            String version = dependency.getVersion();

            if (version == null || version.isBlank()) {
                violations.add(
                        "Missing version for dependency: "
                                + dependency.getName()
                );
            }

            if (dependency.isProhibited()) {
                violations.add(
                        "Prohibited dependency: "
                                + dependency.getName()
                );
            }
        }
    }

    @Override
    public void visit(Directory directory) {
        for (ProjectElement element : directory.getChildren()) {
            element.accept(this);
        }
    }

    public List<String> getViolations() {
        List<String> result = new ArrayList<>(violations);

        for (String sourceFile : sourceFiles) {
            if (!testedSourceFiles.contains(sourceFile)) {
                result.add(
                        "Missing tests for source file: " + sourceFile
                );
            }
        }

        return List.copyOf(result);
    }

    public int getViolationCount() {
        return getViolations().size();
    }

    public boolean isCompliant() {
        return getViolations().isEmpty();
    }

    public void printReport() {
        List<String> reportViolations = getViolations();

        System.out.println("=== COMPLIANCE CHECK ===");
        System.out.println("Violations: " + reportViolations.size());

        for (String violation : reportViolations) {
            System.out.println("[VIOLATION] " + violation);
        }

        System.out.println(
                "COMPLIANCE RESULT: "
                        + (reportViolations.isEmpty() ? "PASS" : "FAIL")
        );

        System.out.println();
    }
}