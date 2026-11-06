package kz.nurali.inspection;

import java.util.List;

public class DependencyFile implements ProjectElement {
    private final String name;
    private final int linesOfCode;
    private final List<Dependency> dependencies;

    public DependencyFile(String name, int linesOfCode,
                          List<Dependency> dependencies) {
        if (linesOfCode < 0) {
            throw new IllegalArgumentException(
                    "Lines of code cannot be negative"
            );
        }

        this.name = name;
        this.linesOfCode = linesOfCode;
        this.dependencies = List.copyOf(dependencies);
    }

    public String getName() {
        return name;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}