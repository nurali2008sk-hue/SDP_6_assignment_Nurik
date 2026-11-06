package kz.nurali.inspection;

import java.util.ArrayList;
import java.util.List;

public class SecurityAuditVisitor implements ProjectVisitor {
    private static final int HIGH_RISK_COMPLEXITY = 20;

    private final List<String> criticalIssues = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    @Override
    public void visit(SourceCodeFile file) {
        if (file.hasHardcodedCredentials()) {
            criticalIssues.add(
                    "Hardcoded credentials in " + file.getName()
            );
        }

        if (file.getComplexity() > HIGH_RISK_COMPLEXITY) {
            warnings.add(
                    "High complexity in " + file.getName()
                            + ": " + file.getComplexity()
            );
        }
    }

    @Override
    public void visit(TestFile file) {
        // The current model does not define security flags for test files.
    }

    @Override
    public void visit(ConfigurationFile file) {
        if (file.hasExposedCredentials()) {
            criticalIssues.add(
                    "Exposed credentials in " + file.getName()
            );
        }

        if (file.hasInsecureSettings()) {
            criticalIssues.add(
                    "Insecure settings in " + file.getName()
            );
        }
    }

    @Override
    public void visit(DependencyFile file) {
        for (Dependency dependency : file.getDependencies()) {
            if (dependency.isVulnerable()) {
                criticalIssues.add(
                        "Vulnerable dependency in " + file.getName()
                                + ": " + dependency.getName()
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

    public int getCriticalIssueCount() {
        return criticalIssues.size();
    }

    public int getWarningCount() {
        return warnings.size();
    }

    public List<String> getCriticalIssues() {
        return List.copyOf(criticalIssues);
    }

    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }

    public int getSecurityScore() {
        int penalty = getCriticalIssueCount() * 25
                + getWarningCount() * 10;

        return Math.max(0, 100 - penalty);
    }

    public void printReport() {
        System.out.println("=== SECURITY AUDIT ===");
        System.out.println("Critical issues: " + getCriticalIssueCount());
        System.out.println("Warnings: " + getWarningCount());
        System.out.println("Security score: " + getSecurityScore() + "/100");

        for (String issue : criticalIssues) {
            System.out.println("[CRITICAL] " + issue);
        }

        for (String warning : warnings) {
            System.out.println("[WARNING] " + warning);
        }

        System.out.println();
    }
}
