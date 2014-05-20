package de.kimrudolph.akkaflow;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.extension.SpringExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

/**
 * Tool to trigger messages passed to actors.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("de.kimrudolph.akkaflow.configuration")
public class AkkaApplication {


    public static void main(String[] args) throws Exception {

        ApplicationContext context =
            SpringApplication.run(AkkaApplication.class, args);

        ActorSystem system = context.getBean(ActorSystem.class);

        final LoggingAdapter log = Logging.getLogger(system, "Application");

        log.info("Starting up");

        SpringExtension ext = context.getBean(SpringExtension.class);

        // Use the Spring Extension to create props for a named actor bean
        ActorRef supervisor = system.actorOf(
            ext.props("supervisor").withMailbox("akka.priority-mailbox"));

        for (int i = 1; i < 1000; i++) {
            Task task = new Task();
            task.setPayload("payload " + i);
            task.setPriority(new Random().nextInt(99));
            supervisor.tell(task, null);
        }

        // Poison pill will be queued with a priority of 100 as the last
        // message
        supervisor.tell(PoisonPill.getInstance(), null);

        while (!supervisor.isTerminated()) {
            Thread.sleep(100);
        }

        log.info("Created {} tasks", context.getBean(JdbcTemplate.class)
            .queryForObject("SELECT COUNT(*) FROM tasks", Integer.class));

        log.info("Shutting down");

        system.shutdown();
        system.awaitTermination();
    }
}
