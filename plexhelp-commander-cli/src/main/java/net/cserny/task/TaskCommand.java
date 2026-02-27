package net.cserny.task;

import lombok.RequiredArgsConstructor;
import net.cserny.support.Features;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import picocli.CommandLine;
import picocli.CommandLine.Option;

@Component
@CommandLine.Command(name = "commander-tasks",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Runs a Commander task based on the provided argument.")
@RequiredArgsConstructor
public class TaskCommand implements Runnable {

    private final FeatureManager featureManager;
    private final TaskService taskService;

    @Option(names = {"-t", "--task"}, description = "Task to run: reduce_subs, etc...", required = true)
    private TaskType taskType;

    @Override
    public void run() {
        disableServerFeatures();
        taskService.runTask(taskType);
    }

    private void disableServerFeatures() {
        featureManager.setFeatureState(new FeatureState(Features.AUTOMOVE, false));
    }
}
