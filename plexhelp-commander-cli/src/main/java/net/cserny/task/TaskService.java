package net.cserny.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final List<TaskRunner> taskRunners;

    public void runTask(TaskType taskType) {
        for (TaskRunner taskRunner : taskRunners) {
            if (taskRunner.supports(taskType)) {
                taskRunner.run();
            }
        }
    }
}
