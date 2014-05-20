package de.kimrudolph.akkaflow.actors;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.extension.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * A sample supervisor which should handle exceptions and general feedback
 * for the actual {@link de.kimrudolph.akkaflow.actors.TaskActor}
 * <p/>
 * A router is configured at startup time, managing a pool of task actors.
 */
@Component
@org.springframework.context.annotation.Scope("prototype")
public class Supervisor extends UntypedActor {

    private final LoggingAdapter log = Logging
        .getLogger(getContext().system(), "Supervisor");

    @Autowired
    private SpringExtension springExtension;

    private Router router;

    @Override
    public void preStart() throws Exception {

        log.info("Starting up");

        List<Routee> routees = new ArrayList<Routee>();
        for (int i = 0; i < 100; i++) {
            ActorRef actor = getContext().actorOf(springExtension.props(
                "taskActor"));
            getContext().watch(actor);
            routees.add(new ActorRefRoutee(actor));
        }
        router = new Router(new SmallestMailboxRoutingLogic(), routees);
        super.preStart();
    }

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
            router.route(message, getSender());
        } else if (message instanceof Terminated) {
            // Readd task actors if one failed
            router = router.removeRoutee(((Terminated) message).actor());
            ActorRef actor = getContext().actorOf(springExtension.props
                ("taskActor"));
            getContext().watch(actor);
            router = router.addRoutee(new ActorRefRoutee(actor));
        } else {
            log.error("Unable to interpret message {}", message);
        }
    }

    @Override
    public void postStop() throws Exception {
        log.info("Shutting down");
        super.postStop();
    }
}