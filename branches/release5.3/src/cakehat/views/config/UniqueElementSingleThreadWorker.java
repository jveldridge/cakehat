package cakehat.views.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An executor service like construct which uses a single internally managed thread to run provided {@link Runnable}s in
 * the order in which they are received. If a {@link Runnable} is offered for execution, but there is a {@link Runnable}
 * awaiting execution that is {@code equals(...)} to it then it will be ignored.
 *
 * @author jak2
 */
class UniqueElementSingleThreadWorker
{
    /**
     * Used internally to record the state of this worker.
     */
    private static enum ActivityState
    {
        /**
         * The initial state of the worker while it is being constructed.
         */
        INITIALIZING,
        
        /**
         * While the task thread is running, taking tasks off of the queue and running them.
         */
        RUNNING,
        
        /**
         * Shutdown has commenced, remaining tasks will be executed, but no more tasks can be submitted.
         */
        SHUTTING_DOWN,
        
        /**
         * Shutdown has completed, there are no more remaining tasks to be executed, and no more tasks can be submitted.
         */
        SHUT_DOWN;
    }
    
    /**
     * The state of the worker.
     */
    private volatile ActivityState _state;
    
    /**
     * The thread safe queue of unique tasks to execute.
     */
    private final UniqueTagQueue<String, Runnable> _taskQueue;
    
    /**
     * The thread that will run the tasks.
     */
    private final Thread _taskThread;
    
    /**
     * A special task that is enqueued in order to shut down the worker thread.
     */
    private static final Runnable SHUTDOWN_TASK = new Runnable() { @Override public void run() { } };
    
    /**
     * For debugging purposes to give threads unique names.
     */
    private static final AtomicLong COUNTER = new AtomicLong();
    
    /**
     * Private constructor for the worker. It will initialize the worker, but will not start the thread it uses to
     * execute tasks.
     */
    private UniqueElementSingleThreadWorker()
    {
        _state = ActivityState.INITIALIZING;
        _taskQueue = new UniqueTagQueue<String, Runnable>();
        
        _taskThread = new Thread("UniqueElementSingleThreadWorker-" + COUNTER.incrementAndGet())
        {
            @Override
            public void run()
            {
                while(true)
                {
                    try
                    {
                        Runnable task = _taskQueue.blockingPop();
                        if(task == SHUTDOWN_TASK)
                        {
                            break;
                        }
                        task.run();
                    }
                    catch(InterruptedException ex) { }
                    //Catch run time exceptions that the runnable might throw so that the thread does not die
                    catch(RuntimeException ex) { ex.printStackTrace(); } //TODO: Log/report this somehow
                }
                
                _state = ActivityState.SHUT_DOWN;
            }
        };
    }
    
    /**
     * Returns a new instance of {@code UniqueElementSingleThreadWorker}.
     * 
     * @return 
     */
    public static UniqueElementSingleThreadWorker newInstance()
    {
        //This is done in order to start the worker without an external call, but not from the constructor as that would
        //cause issues because the thread needs to reference the _taskQueue variable and until an instance has completed
        //construction there is no guarantee it will be seen in a proper state from another thread
        
        UniqueElementSingleThreadWorker worker = new UniqueElementSingleThreadWorker();
        worker.start();
        
        return worker;
    }
    
    /**
     * Starts the thread up.
     */
    private void start()
    {
        _taskThread.start();
        _state = ActivityState.RUNNING;
    }
    
    /**
     * Executes all remaining tasks, not returning from the method invocation until it has done so. After this method
     * has been called, calls to {@link #submit(java.lang.Runnable)} will result in an exception being thrown.
     * 
     * @throws InterruptedException if interrupted while waiting for shutdown to complete
     */
    public void blockingShutdown() throws InterruptedException
    {
        _state = ActivityState.SHUTTING_DOWN;
        
        //Enqueue a task that will cause the task thread to terminate
        _taskQueue.offer(null, SHUTDOWN_TASK);
        
        //Wait for the task thread to terminate
        _taskThread.join();
    }
    
    /**
     * Whether submissions can be made. Submissions can only be made while the working is running; once shutdown has
     * been initiated submissions will result in a runtime exception.
     * 
     * @return 
     */
    public boolean canSubmit()
    {
        return (_state == ActivityState.RUNNING);
    }
    
    /**
     * Submits {@code task} for execution on a separate thread.
     * 
     * @param tag
     * @param run
     * @return {@code true} if {@code task} was added to the execution queue
     * @throws NullPointerException if {@code task} is null
     * @throws IllegalStateException if shut down has commenced
     */
    public boolean submit(String tag, Runnable task)
    {
        if(!canSubmit())
        {
            throw new IllegalStateException("new tasks cannot be submitted");
        }
        
        return _taskQueue.offer(tag, task);
    }
    
    /**
     * Blocks the calling thread until all elements in the queue at the moment this method is invoked have been removed
     * from the queue and run.
     * 
     * @throws InterruptedException 
     */
    public void blockOnQueuedTasks() throws InterruptedException
    {
        final ReentrantLock lock = new ReentrantLock();
        final Condition waitingCompleteCondition = lock.newCondition();
        
        //Create a runnable which will signal the condition when it is run
        Runnable waitRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                lock.lock();
                try
                {
                    waitingCompleteCondition.signal();
                }
                finally
                {
                    lock.unlock();
                }
            }
        };
        
        //Acquire the lock, submit the runnable, and then wait for the runnable to be run by the worker thread where the
        //condition that is being waited on will be signaled
        lock.lock();
        try
        {
            this.submit(null, waitRunnable);
            waitingCompleteCondition.await();
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * Returns if the task is contained in the queue of pending execution. If the task is currently being executed,
     * {@code false} will be returned.
     * 
     * @param task
     * @return 
     */
    public boolean contains(Runnable task)
    {
        return _taskQueue.contains(task);
    }
    
    /**
     * Removes all tasks that were submitted with {@code tag}.
     * 
     * @param tasks
     * @return a set of the tasks canceled
     */
    public Set<Runnable> cancel(String tag)
    {
        return _taskQueue.remove(tag);
    }
    
    /**
     * A thread safe unbounded queue that guarantees uniqueness of its elements. When offering an element a tag may be
     * provided. Elements can be removed by providing the tag used while inserting them.
     * <br/><br/>
     * It is designed to be efficiently used by one producer and one consumer, however it is safe to use with any number
     * of producers and consumers. The blocking methods in this class allow a producer thread to offer items to the
     * queue, which in turns causes the blocking method to resume.
     * 
     * @param <E> 
     */
    private static class UniqueTagQueue<T, E>
    {
        /**
         * Guards access to {@link #_elements} and {@link #_tagMap}.
         */
        private final Lock _lock = new ReentrantLock();
        
        /**
         * The non-thread safe ordered hash set used as a queue.
         */
        private final LinkedHashSet<TaggedElement<T, E>> _elements = new LinkedHashSet<TaggedElement<T, E>>();
        
        /**
         * The non-thread safe map of tags to a set of tagged elements.
         */
        private final HashMap<T, Set<TaggedElement<T, E>>> _tagMap = new HashMap<T, Set<TaggedElement<T, E>>>();
        
        /**
         * Tracks if {@link #_elements} is empty without needing to obtain a lock in order to call a method on
         * {@link #_elements}.
         */
        private volatile boolean _empty = true;
        
        /**
         * The condition used for when the queue is no longer empty. Used to implement the blocking methods.
         */
        private final Condition _notEmptyCondition = _lock.newCondition();
        
        /**
         * Adds {@code element} to the end of the queue if {@code element} is not already in the queue. If
         * {@code element} is already in the queue nothing occurs.
         * <br/><br/>
         * This method will acquire and hold the lock until it returns.
         * 
         * @param tag may be {@code null}
         * @param element may not be {@code null}
         * @return {@code true} if this queue did not already contain the specified element
         * @throws NullPointerException if {@code element} is null
         */
        public boolean offer(T tag, E element)
        {
            if(element == null)
            {
                throw new NullPointerException("a null element may not be offered");
            }
            
            _lock.lock();
            try
            {
                TaggedElement<T, E> taggedElement = new TaggedElement<T, E>(tag, element);
                
                boolean wasAdded = _elements.add(taggedElement);
                if(wasAdded)
                {
                    //If there is a tag, keep track of the tag used
                    if(tag != null)
                    {
                        Set<TaggedElement<T, E>> tagged = _tagMap.get(tag);
                        if(tagged == null)
                        {
                            tagged = new HashSet<TaggedElement<T, E>>();
                            _tagMap.put(tag, tagged);
                        }
                        tagged.add(taggedElement);
                    }
                    
                    _empty = false;
                    _notEmptyCondition.signal();
                }
                
                return wasAdded;
            }
            finally
            {
                _lock.unlock();
            }
        }
        
        /**
         * Returns and removes the head of the queue, blocking the calling thread until there is one.
         * <br/><br/>
         * Calls to methods in this class, including calls to this method on other threads, will not be blocked while
         * waiting for this method to return - only the calling thread will be blocked. This method needs to acquire a
         * lock initially, but will release it while waiting, and then regain it once the queue is no longer empty.
         * 
         * @return head of the queue
         * @throws InterruptedException if interrupted while waiting
         */
        public E blockingPop() throws InterruptedException
        {
            _lock.lock();
            try
            {
                //Wait for an item to be offered to the queue
                try
                {
                    //This loop is necessary because it is possible to be awoken erroneously; however, empty will still
                    //be false and so the thread will go back to sleep
                    while(_empty)
                    {
                        _notEmptyCondition.await();
                    }
                }
                //Propogate to a non-interrupted thread
                //The situation of multiple consumer threads is not currently used in cakehat
                catch(InterruptedException e)
                {
                    _notEmptyCondition.signal();
                    throw e;
                }
                
                //Retrieve and remove next element
                Iterator<TaggedElement<T, E>> iterator = _elements.iterator();
                TaggedElement<T, E> taggedElement = iterator.next();
                iterator.remove();
                _empty = !iterator.hasNext();
                
                //If the element was tagged, remove it from the set of elements with that tag
                if(taggedElement.getTag() != null)
                {
                    _tagMap.get(taggedElement.getTag()).remove(taggedElement);
                }
                
                return taggedElement.getElement();
            }
            finally
            {
                _lock.unlock();
            }
        }
        
        /**
         * If {@code element} is in the queue.
         * 
         * @param element
         * @return 
         */
        public boolean contains(E element)
        {
            if(_empty)
            {
                return false;
            }
            else
            {
                TaggedElement<T, E> taggedElement = new TaggedElement<T, E>(null, element);
                _lock.lock();
                try
                {
                    return _elements.contains(taggedElement);
                }
                finally
                {
                    _lock.unlock();
                }
            }
        }
        
        /**
         * Removes all elements from the queue that was inserted with {@code tag}.
         * 
         * @param tag
         * @return a set of the elements removed
         * @throws NullPointerException if {@code tag} is null
         */
        public Set<E> remove(T tag)
        {
            if(tag == null)
            {
                throw new NullPointerException("tag may not be null");
            }
            
            if(_empty)
            {
                return Collections.<E>emptySet();
            }
            else
            {
                _lock.lock();
                
                try
                {
                    Set<TaggedElement<T, E>> taggedElements = _tagMap.get(tag);
                    _tagMap.remove(tag);
                    
                    Set<E> toRemove = new HashSet<E>();
                    for(TaggedElement<T, E> taggedElement : taggedElements)
                    {
                        toRemove.add(taggedElement.getElement());
                    }
                    _elements.removeAll(toRemove);
                    
                    return toRemove;
                }
                finally
                {
                    _lock.unlock();
                }
            }
        }
        
        /**
         * Wraps around a tag and an element. Equality is based on the element only.
         * 
         * @param <T>
         * @param <E> 
         */
        private static class TaggedElement<T, E>
        {
            private final T _tag;
            private final E _element;
            
            TaggedElement(T tag, E element)
            {
                _tag = tag;
                _element = element;
            }
            
            public T getTag()
            {
                return _tag;
            }
            
            public E getElement()
            {
                return _element;
            }
            
            @Override
            public boolean equals(Object other)
            {
                boolean equal = false;
                if(other instanceof TaggedElement)
                {
                    equal = _element.equals(((TaggedElement) other)._element);
                }
                
                return equal;
            }
            
            @Override
            public int hashCode()
            {
                return _element.hashCode();
            }
        }
    }
}