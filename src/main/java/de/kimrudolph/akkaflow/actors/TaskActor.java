package de.kimrudolph.akkaflow.actors;

import akka.actor.UntypedActor;
import de.kimrudolph.akkaflow.beans.Task;
import de.kimrudolph.akkaflow.services.TaskDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TaskActor extends UntypedActor {

    @Autowired
    private TaskDAO taskDAO;

    @Override
    public void onReceive(Object message) throws Exception {

        // Create the task using the DAO and tell the supervisor the result
        Long result = taskDAO.createTask((Task) message);
        sender().tell(result, getSelf());
    }
}
