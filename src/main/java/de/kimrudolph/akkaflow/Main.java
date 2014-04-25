package de.kimrudolph.akkaflow;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.extension.SpringExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

        SpringExtension ext = context.getBean(SpringExtension.class);

        for (int i = 1; i < 100000; i++) {

            // Use the Spring Extension to create props for a named actor bean
            ActorRef taskActor = system.actorOf(ext.props("supervisor"));
            Task task = new Task();
            task.setPayload("payload " + i);
            taskActor.tell(task, null);
        }

        system.awaitTermination();
    }
}
