package de.kimrudolph.akkaflow;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.extension.SpringExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import scala.concurrent.Future;

import java.util.ArrayList;

import static akka.pattern.Patterns.ask;

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

        SpringExtension ext = context.getBean(SpringExtension.class);

        // Use the Spring Extension to create props for a named actor bean
        ActorRef supervisor = system.actorOf(ext.props("supervisor"));

        for (int i = 1; i < 10000; i++) {
            Task task = new Task();
            task.setPayload("payload " + i);
            supervisor.tell(task, null);
        }

        // Keep it running until terminated
        final ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>();
        Future<Object> result = ask(supervisor, "running", Integer.MAX_VALUE);
        futures.add(result);

        Thread.sleep(10000);
        log.info("Shutting down");

        system.shutdown();
        system.awaitTermination();
        context.close();
    }
}
