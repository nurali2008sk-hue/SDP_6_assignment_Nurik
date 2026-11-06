package kz.nurali.inspection;

import java.util.List;

public class InspectionDemo {

    public static Directory createDemoProject() {
        Directory project = new Directory("fintech-project");

        Directory src = new Directory("src");

        src.addElement(new SourceCodeFile(
                "PaymentService.java",
                200,
                24,
                true,
                3
        ));

        src.addElement(new SourceCodeFile(
                "UserService.java",
                140,
                8,
                false,
                1
        ));

        Directory tests = new Directory("tests");

        tests.addElement(new TestFile(
                "PaymentServiceTest.java",
                100,
                4,
                "PaymentService.java"
        ));

        Directory config = new Directory("config");

        config.addElement(new ConfigurationFile(
                "application.properties",
                25,
                false,
                true
        ));

        DependencyFile dependencies = new DependencyFile(
                "pom.xml",
                35,
                List.of(
                        new Dependency(
                                "legacy-payment-sdk",
                                "1.0",
                                true,
                                false
                        ),
                        new Dependency(
                                "unapproved-sdk",
                                "",
                                false,
                                true
                        )
                )
        );

        project.addElement(src);
        project.addElement(tests);
        project.addElement(config);
        project.addElement(dependencies);

        return project;
    }

    public static void main(String[] args) {
        Directory project = createDemoProject();

        SecurityAuditVisitor securityVisitor = new SecurityAuditVisitor();

        project.accept(securityVisitor);

        securityVisitor.printReport();
    }
}