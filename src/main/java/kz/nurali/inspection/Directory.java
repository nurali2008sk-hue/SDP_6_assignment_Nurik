package kz.nurali.inspection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Directory implements ProjectElement {
    private final String name;
    private final List<ProjectElement> children = new ArrayList<>();

    public Directory(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addElement(ProjectElement element) {
        Objects.requireNonNull(element, "Element cannot be null");

        if (element == this) {
            throw new IllegalArgumentException(
                    "A directory cannot contain itself"
            );
        }

        children.add(element);
    }

    public List<ProjectElement> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}