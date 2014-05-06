package de.kimrudolph.akkaflow.actors;

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.extension.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

/**
 * A sample supervisor which should handle exceptions and general feedback
 * for the actual {@link de.kimrudolph.akkaflow.actors.TaskActor}
 */
@Component
@org.springframework.context.annotation.Scope("prototype")
public class Supervisor extends UntypedActor {

    private final LoggingAdapter log = Logging
        .getLogger(getContext().system(), "Supervisor");

    @Autowired
    private SpringExtension extension;

    /**
     * Configure a no-retry-allowed policy
     */
    private final SupervisorStrategy strategy = new OneForOneStrategy(0,
        Duration.Zero(),
        new Function<Throwable, SupervisorStrategy.Directive>() {

            @Override
            public SupervisorStrategy.Directive apply(
                Throwable type) throws Exception {

                if (type instanceof Exception) {
                    return SupervisorStrategy.stop();
                }

                return SupervisorStrategy.escalate();
            }
        });

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof Task) {
            getContext().actorOf(extension.props("taskActor")).tell
                (message, getSelf());
        } else if (message instanceof Long) {
            // Process answer...
        }
    }

    @Override
    public void postStop() throws Exception {
        log.info("Shutting down");
        super.postStop();
    }
}