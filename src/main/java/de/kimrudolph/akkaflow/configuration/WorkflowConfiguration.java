package de.kimrudolph.akkaflow.configuration;

import akka.actor.ActorSystem;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.kimrudolph.akkaflow.extension.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.PropertyVetoException;

@Configuration
@Lazy
@ComponentScan(basePackages = { "de.kimrudolph.akkaflow.services",
    "de.kimrudolph.akkaflow.actors", "de.kimrudolph.akkaflow.extension" })
public class WorkflowConfiguration {

    // The application context is needed to initialize the Akka Spring
    // Extension
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringExtension springExtension;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {

        ActorSystem system = ActorSystem
            .create("AkkaTaskProcessing", akkaConfiguration());

        // Initialize the application context in the Akka Spring Extension
        springExtension.initialize(applicationContext);
        return system;
    }

    /**
     * Read configuration from application.conf file
     */
    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

    /**
     * Simple H2 based in memory backend using a connection pool.
     * Creates th only table needed.
     */
    @Bean
    public JdbcTemplate jdbcTemplate() throws PropertyVetoException {

        final ComboPooledDataSource source = new ComboPooledDataSource();
        source.setMaxPoolSize(100);
        source.setDriverClass("org.h2.Driver");
        source.setJdbcUrl("jdbc:h2:mem:taskdb");
        source.setUser("sa");
        source.setPassword("");

        JdbcTemplate template = new JdbcTemplate(source);
        template.update("CREATE TABLE tasks (id INT(11) AUTO_INCREMENT, " +
            "payload VARCHAR(255), updated DATETIME)");
        return template;
    }
}
