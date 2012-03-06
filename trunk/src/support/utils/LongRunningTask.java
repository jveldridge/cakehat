package support.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a long running task that can be canceled. The progress of the task can be listened to.
 *
 * @author jak2
 */
public abstract class LongRunningTask
{
    /**
     * Listeners for the progress of the long running task.
     * <br/><br/>
     * There are no guarantees which thread methods of the listener will be called on.
     */
    public static interface ProgressListener
    {
        /**
         * Called to notify the listener the task has started.
         */
        public void taskStarted();
        
        /**
         * Called after the task has started once it has been determined the number of steps the task will be broken
         * into. This method will not be called if the task does not have clearly defined steps.
         * 
         * @param totalSteps
         */
        public void taskDetermined(int totalSteps);
        
        /**
         * Called immediately prior to a task step starting to provide a description of what the task is. This method
         * may never be called even by tasks that do call {@link #taskStepCompleted(int)}.
         * 
         * @param description
         */
        public void taskStepStarted(String description);
        
        /**
         * Called to notify progress made on the task. This method may not be called if the task does not have clearly
         * defined tasks.
         * 
         * @param currStep
         */
        public void taskStepCompleted(int currStep);

        /**
         * Called to notify the listener that the task has completed.
         */
        public void taskCompleted();
        
        /**
         * Called to notify the listener that the task has been canceled.
         */
        public void taskCanceled();
        
        /**
         * Called to notify the listener that the task failed.
         * 
         * @param cause the exception that caused the export to fail, may be {@code null}
         */
        public void taskFailed(Exception cause);
    }

    //Holds on to the listeners in a thread safe data structure
    private final List<ProgressListener> _listeners = new CopyOnWriteArrayList<ProgressListener>();
    
    //Keeps track of the number of steps performed
    private final AtomicInteger _currStep = new AtomicInteger();
    
    //Used to guarantee that the task is started at most once
    private final ReentrantLock _startLock = new ReentrantLock();
    private volatile boolean _started = false;
    
    //Used to guarantee that the task is canceled at most once
    private final ReentrantLock _cancelLock = new ReentrantLock();
    private volatile boolean _cancelAttempted = false;

    /**
     * Constructor for subclasses.
     */
    protected LongRunningTask() { }

    /**
     * Adds the listener for progress events. Methods may be invoked on the listener from any thread.
     * 
     * @param listener 
     */
    public final void addProgressListener(ProgressListener listener)
    {
        _listeners.add(listener);
    }

    /**
     * Removes the listener for progress events.
     * 
     * @param listener 
     */
    public final void removeProgressListener(ProgressListener listener)
    {
        _listeners.remove(listener);
    }
    
    /**
     * Starts the task. Listeners will be notified immediately after the task has been started.
     * <br/><br/>
     * Only the first call to this method will have an effect, subsequent calls will be ignored.
     */
    public final void start()
    {
        _startLock.lock();
        try
        {
            if(!_started)
            {
                _started = true;
                startTask();
                notifyTaskStarted();
            }
        }
        finally
        {
            _startLock.unlock();
        }
    }
    
    /**
     * Starts the task. This method is guaranteed to be called at most once.
     */
    protected abstract void startTask();

    /**
     * Attempts to cancel the task. Listeners will be notified once the cancellation has actually occurred.
     * <br/><br/>
     * Only the first call to this method will have an effect, subsequent calls will be ignored.
     */
    public final void cancel()
    {
        _cancelLock.lock();
        try
        {
            if(!_cancelAttempted)
            {
                _cancelAttempted = true;
                cancelTask();
            }
        }
        finally
        {
            _cancelLock.unlock();
        }
    }
    
    /**
     * Attempts to cancel the task. This method is guaranteed to be called at most once. After the task has been
     * successfully canceled {@link #notifyCanceled()} should be called.
     */
    protected abstract void cancelTask();
    
    /**
     * If an attempt to cancel the task has occurred.
     * 
     * @return 
     */
    protected boolean isCancelAttempted()
    {
        return _cancelAttempted;
    }

    /**
     * Notifies listeners the task started.
     */
    private void notifyTaskStarted()
    {
        for(ProgressListener listener : _listeners)
        {
            listener.taskStarted();
        }
    }
    
    /**
     * Notifies listeners the work involved in the task has been determined.
     * 
     * @param totalSteps 
     */
    protected void notifyTaskDetermined(int totalSteps)
    {
        for(ProgressListener listener : _listeners)
        {
            listener.taskDetermined(totalSteps);
        }
    }
    
    /**
     * Notifies listeners a step in progressing towards completion is about to start.
     * 
     * @param description 
     */
    protected void notifyTaskStepStarted(String description)
    {
        for(ProgressListener listener : _listeners)
        {
            listener.taskStepStarted(description);
        }
    }
    
    /**
     * Notifies listeners a step in progressing towards completion has occurred.
     */
    protected void notifyTaskStepCompleted()
    {   
        int step = _currStep.incrementAndGet();
        for(ProgressListener listener : _listeners)
        {
            listener.taskStepCompleted(step);
        }
    }
    
    /**
     * Notifies listeners the task has completed.
     */
    protected void notifyTaskCompleted()
    {
        for(ProgressListener listener : _listeners)
        {
            listener.taskCompleted();
        }
    }

    /**
     * Notifies listeners of cancellation.
     */
    protected void notifyTaskCanceled()
    {
        for(ProgressListener listener : _listeners)
        {
            listener.taskCanceled();
        }
    }
    
    /**
     * Notifies listeners of failure to complete the task.
     * 
     * @param cause may be {@code null} 
     */
    protected void notifyTaskFailed(Exception cause)
    {
        for(ProgressListener listener : _listeners)
        {
            listener.taskFailed(cause);
        }
    }
}