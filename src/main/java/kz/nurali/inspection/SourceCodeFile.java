package kz.nurali.inspection;

public class SourceCodeFile implements ProjectElement {
    private final String name;
    private final int linesOfCode;
    private final int complexity;
    private final boolean hardcodedCredentials;
    private final int todoCount;

    public SourceCodeFile(String name, int linesOfCode, int complexity,
                          boolean hardcodedCredentials, int todoCount) {
        if (linesOfCode < 0 || complexity < 0 || todoCount < 0) {
            throw new IllegalArgumentException(
                    "Lines of code, complexity and TODO count cannot be negative"
            );
        }

        this.name = name;
        this.linesOfCode = linesOfCode;
        this.complexity = complexity;
        this.hardcodedCredentials = hardcodedCredentials;
        this.todoCount = todoCount;
    }

    public String getName() {
        return name;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public int getComplexity() {
        return complexity;
    }

    public boolean hasHardcodedCredentials() {
        return hardcodedCredentials;
    }

    public int getTodoCount() {
        return todoCount;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}