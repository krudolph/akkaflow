package de.kimrudolph.akkaflow.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.services.TaskDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TaskActor extends UntypedActor {

    private final LoggingAdapter log = Logging
        .getLogger(getContext().system(), "TaskProcessor");

    @Autowired
    private TaskDAO taskDAO;

    @Override
    public void onReceive(Object message) throws Exception {

        Long result = taskDAO.createTask((Task) message);
        log.debug("Created task {}", result);
    }
}
