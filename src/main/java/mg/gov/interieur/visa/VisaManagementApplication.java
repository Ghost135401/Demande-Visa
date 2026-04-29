package mg.gov.interieur.visa;

import mg.gov.interieur.visa.config.DatabaseBootstrap;
import mg.gov.interieur.visa.config.PortCleanupConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.IOException;
import java.util.Properties;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
@EnableJpaAuditing
public class VisaManagementApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws IOException {

        // Lecture des paramètres admin depuis application.properties
        Properties props = new Properties();
        props.load(new ClassPathResource("application.properties").getInputStream());

        String adminUrl  = props.getProperty("app.db.admin.url");
        String adminUser = props.getProperty("app.db.admin.username");
        String adminPass = props.getProperty("app.db.admin.password", "");

        // ⚡ Bootstrap AVANT Spring : crée visa_user + visa_management si absents
        DatabaseBootstrap.initialize(adminUrl, adminUser, adminPass);

        // ⚡ Nettoyage du port AVANT Spring : tue le processus en cours sur le port de déploiement
        PortCleanupConfig.cleanupPort(props.getProperty("server.port", "8080"));

        SpringApplication.run(VisaManagementApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(VisaManagementApplication.class);
    }
}