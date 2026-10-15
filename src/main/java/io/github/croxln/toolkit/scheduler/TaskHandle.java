package io.github.croxln.toolkit.scheduler;

/**
 * Universal handle representing a scheduled task across Paper and Folia.
 */
public interface TaskHandle {

    /**
     * Cancels the scheduled task.
     */
    void cancel();

    /**
     * Checks if the task has been cancelled.
     */
    boolean isCancelled();
}
