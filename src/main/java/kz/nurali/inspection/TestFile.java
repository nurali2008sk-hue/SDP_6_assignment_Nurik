package kz.nurali.inspection;

public class TestFile implements ProjectElement {
    private final String name;
    private final int linesOfCode;
    private final int testCount;
    private final String testedSourceFile;

    public TestFile(String name, int linesOfCode, int testCount,
                    String testedSourceFile) {
        if (linesOfCode < 0 || testCount < 0) {
            throw new IllegalArgumentException(
                    "Lines of code and test count cannot be negative"
            );
        }

        this.name = name;
        this.linesOfCode = linesOfCode;
        this.testCount = testCount;
        this.testedSourceFile = testedSourceFile;
    }

    public String getName() {
        return name;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public int getTestCount() {
        return testCount;
    }

    public String getTestedSourceFile() {
        return testedSourceFile;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}