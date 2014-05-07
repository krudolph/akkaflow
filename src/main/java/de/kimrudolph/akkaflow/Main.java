package de.kimrudolph.akkaflow;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.extension.SpringExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

/**
 * Tool to trigger messages passed to actors..
 */
public class Main {


    public static void main(String[] args) throws Exception {

        // create a producer context and scan the base configuration class
        AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext();
        context.scan("de.kimrudolph.akkaflow.configuration");
        context.refresh();

        ActorSystem system = context.getBean(ActorSystem.class);

        final LoggingAdapter log = Logging.getLogger(system, "Application");

        log.info("Starting up");

        SpringExtension ext = context.getBean(SpringExtension.class);

        // Use the Spring Extension to create props for a named actor bean
        ActorRef supervisor = system.actorOf(ext.props("supervisor")
            .withMailbox("akka.priority-mailbox"));

        for (int i = 1; i < 100000; i++) {
            Task task = new Task();
            task.setPayload("payload " + i);
            task.setPriority(new Random().nextInt(99));
            supervisor.tell(task, null);
        }

        // Poison pill will be queued with a priority of 100 as the last
        // message
        supervisor.tell(PoisonPill.getInstance(), null);

        while(!supervisor.isTerminated()) {
            Thread.sleep(100);
        }

        log.info("Created {} tasks", context.getBean(JdbcTemplate.class)
            .queryForObject
                ("SELECT COUNT(*) FROM tasks", Integer.class));

        log.info("Shutting down");

        system.shutdown();
        system.awaitTermination();
        context.close();
    }
}
