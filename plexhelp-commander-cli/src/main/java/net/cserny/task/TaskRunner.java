package net.cserny.task;

public interface TaskRunner {

    boolean supports(TaskType taskType);
    void run();
}
