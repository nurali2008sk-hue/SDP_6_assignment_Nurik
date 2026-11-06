package kz.nurali.inspection;

public class Dependency {
    private final String name;
    private final String version;
    private final boolean vulnerable;
    private final boolean prohibited;

    public Dependency(String name, String version,
                      boolean vulnerable, boolean prohibited) {
        this.name = name;
        this.version = version;
        this.vulnerable = vulnerable;
        this.prohibited = prohibited;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public boolean isProhibited() {
        return prohibited;
    }
}
