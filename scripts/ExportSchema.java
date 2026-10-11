import java.nio.file.*;
import java.util.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

/** Portfolio packaging utility, October 2026. Generates DDL without a database connection. */
public class ExportSchema {
    public static void main(String[] args) throws Exception {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect")
            .applySetting("hibernate.temp.use_jdbc_metadata_defaults", "false")
            .build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            try (java.util.stream.Stream<Path> entries = Files.list(Paths.get("target/classes/cn/com/dhc/Entity"))) {
                entries.filter(p -> p.toString().endsWith(".class") && !p.getFileName().toString().contains("$"))
                    .sorted().forEach(p -> {
                        try {
                            String n = p.getFileName().toString().replace(".class", "");
                            Class<?> c = Class.forName("cn.com.dhc.Entity." + n);
                            if (c.isAnnotationPresent(javax.persistence.Entity.class)) sources.addAnnotatedClass(c);
                        } catch (Exception e) { throw new RuntimeException(e); }
                    });
            }
            Metadata metadata = sources.getMetadataBuilder()
                .applyImplicitNamingStrategy(new SpringImplicitNamingStrategy())
                .applyPhysicalNamingStrategy(new SpringPhysicalNamingStrategy()).build();
            SchemaExport export = new SchemaExport().setDelimiter(";").setFormat(true)
                .setHaltOnError(true).setOutputFile(args[0]);
            export.createOnly(EnumSet.of(TargetType.SCRIPT), metadata);
            if (!export.getExceptions().isEmpty()) throw new IllegalStateException(export.getExceptions().toString());
            System.out.println("Exported entity mappings without connecting to a database.");
        } finally { StandardServiceRegistryBuilder.destroy(registry); }
    }
}
