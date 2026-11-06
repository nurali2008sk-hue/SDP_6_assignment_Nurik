package kz.nurali.inspection;

import java.util.Locale;

public class ProjectStatisticsVisitor implements ProjectVisitor {
    private int totalFiles;
    private int totalLinesOfCode;
    private int sourceFileCount;
    private int testFileCount;
    private int totalTests;
    private int totalDependencies;
    private int totalComplexity;

    @Override
    public void visit(SourceCodeFile file) {
        totalFiles++;
        sourceFileCount++;
        totalLinesOfCode += file.getLinesOfCode();
        totalComplexity += file.getComplexity();
    }

    @Override
    public void visit(TestFile file) {
        totalFiles++;
        testFileCount++;
        totalLinesOfCode += file.getLinesOfCode();
        totalTests += file.getTestCount();
    }

    @Override
    public void visit(ConfigurationFile file) {
        totalFiles++;
        totalLinesOfCode += file.getLinesOfCode();
    }

    @Override
    public void visit(DependencyFile file) {
        totalFiles++;
        totalLinesOfCode += file.getLinesOfCode();
        totalDependencies += file.getDependencies().size();
    }

    @Override
    public void visit(Directory directory) {
        for (ProjectElement element : directory.getChildren()) {
            element.accept(this);
        }
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public int getSourceFileCount() {
        return sourceFileCount;
    }

    public int getTestFileCount() {
        return testFileCount;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public int getTotalDependencies() {
        return totalDependencies;
    }

    public double getAverageComplexity() {
        if (sourceFileCount == 0) {
            return 0.0;
        }

        return (double) totalComplexity / sourceFileCount;
    }

    public double getTestSourceRatio() {
        if (sourceFileCount == 0) {
            return 0.0;
        }

        return (double) testFileCount / sourceFileCount;
    }

    public void printReport() {
        System.out.println("=== PROJECT STATISTICS ===");
        System.out.println("Total files: " + getTotalFiles());
        System.out.println("Total lines of code: " + getTotalLinesOfCode());
        System.out.println("Source files: " + getSourceFileCount());
        System.out.println("Test files: " + getTestFileCount());
        System.out.println("Number of tests: " + getTotalTests());
        System.out.println("Dependencies: " + getTotalDependencies());

        System.out.printf(
                Locale.ROOT,
                "Average complexity: %.2f%n",
                getAverageComplexity()
        );

        System.out.printf(
                Locale.ROOT,
                "Test/source ratio: %.2f%n",
                getTestSourceRatio()
        );

        System.out.println();
    }
}
