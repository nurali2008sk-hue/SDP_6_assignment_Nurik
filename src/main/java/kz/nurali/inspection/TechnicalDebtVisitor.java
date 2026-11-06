package kz.nurali.inspection;

import java.util.Locale;

public class TechnicalDebtVisitor implements ProjectVisitor {
    private static final int MINUTES_PER_TODO = 15;
    private static final int COMPLEXITY_THRESHOLD = 10;
    private static final int MINUTES_PER_EXCESS_COMPLEXITY = 30;

    private int totalTodoCount;
    private int complexityDebtMinutes;

    @Override
    public void visit(SourceCodeFile file) {
        totalTodoCount += file.getTodoCount();

        int excessComplexity = Math.max(
                0,
                file.getComplexity() - COMPLEXITY_THRESHOLD
        );

        complexityDebtMinutes +=
                excessComplexity * MINUTES_PER_EXCESS_COMPLEXITY;
    }

    @Override
    public void visit(TestFile file) {
        // This estimate considers only source code complexity and TODOs.
    }

    @Override
    public void visit(ConfigurationFile file) {
        // Configuration issues are handled by other visitors.
    }

    @Override
    public void visit(DependencyFile file) {
        // Dependency issues are handled by other visitors.
    }

    @Override
    public void visit(Directory directory) {
        for (ProjectElement element : directory.getChildren()) {
            element.accept(this);
        }
    }

    public int getTotalTodoCount() {
        return totalTodoCount;
    }

    public int getTodoDebtMinutes() {
        return totalTodoCount * MINUTES_PER_TODO;
    }

    public int getComplexityDebtMinutes() {
        return complexityDebtMinutes;
    }

    public int getTotalDebtMinutes() {
        return getTodoDebtMinutes() + getComplexityDebtMinutes();
    }

    public double getEstimatedHours() {
        return getTotalDebtMinutes() / 60.0;
    }

    public void printReport() {
        System.out.println("=== TECHNICAL DEBT ===");
        System.out.println("TODOs: " + getTotalTodoCount());

        System.out.println(
                "TODO debt: " + getTodoDebtMinutes() + " minutes"
        );

        System.out.println(
                "Complexity debt: "
                        + getComplexityDebtMinutes() + " minutes"
        );

        System.out.println(
                "Total estimated debt: "
                        + getTotalDebtMinutes() + " minutes"
        );

        System.out.printf(
                Locale.ROOT,
                "Estimated remediation time: %.2f hours%n",
                getEstimatedHours()
        );

        System.out.println();
    }
}