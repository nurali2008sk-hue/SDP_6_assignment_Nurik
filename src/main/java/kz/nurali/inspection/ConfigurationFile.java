package kz.nurali.inspection;

public class ConfigurationFile implements ProjectElement {
    private final String name;
    private final int linesOfCode;
    private final boolean exposedCredentials;
    private final boolean insecureSettings;

    public ConfigurationFile(String name, int linesOfCode,
                             boolean exposedCredentials,
                             boolean insecureSettings) {
        if (linesOfCode < 0) {
            throw new IllegalArgumentException(
                    "Lines of code cannot be negative"
            );
        }

        this.name = name;
        this.linesOfCode = linesOfCode;
        this.exposedCredentials = exposedCredentials;
        this.insecureSettings = insecureSettings;
    }

    public String getName() {
        return name;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }

    public boolean hasExposedCredentials() {
        return exposedCredentials;
    }

    public boolean hasInsecureSettings() {
        return insecureSettings;
    }

    @Override
    public void accept(ProjectVisitor visitor) {
        visitor.visit(this);
    }
}
