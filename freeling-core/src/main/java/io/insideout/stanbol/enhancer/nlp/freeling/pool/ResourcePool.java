/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.insideout.stanbol.enhancer.nlp.freeling.pool;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple ResourcePool implementation using a {@link Semaphore} to limit the
 * number of resources and a {@link Queue} to hold the Resources. A
 * {@link ResourceFactory} is used to allow lazzy initialization of the
 * Resources in the pool
 * @author Rupert Westenthaler
 *
 * @param <T> the type of the resource
 */
public class ResourcePool<T> {

    private final Logger log = LoggerFactory.getLogger(ResourcePool.class);
    
    public static final int DEFAULT_SIZE = 5;
    public static final int DEFAULT_MIN_QUEUE_SIZE = 1;

    private int size;

    private final Semaphore semaphore;
    private final Queue<T> resources;
    private final Queue<Future<? extends T>> creating; 
    private final ResourceFactory<? extends T> factory;
    private final Map<String,Object> context;
    private final int minQueueSize;

    private boolean closed;
    

    /**
     * 
     * @param maxSize
     * @param factory
     * @param context
     */
    @SuppressWarnings("unchecked")
    public ResourcePool(int maxSize, int minQueueSize,  ResourceFactory<? extends T> factory, Map<String,Object> context) {
        this.factory = factory;
        this.size = maxSize <= 0 ? DEFAULT_SIZE : maxSize;
        this.minQueueSize = minQueueSize < 0 ? DEFAULT_MIN_QUEUE_SIZE : 
            minQueueSize > this.size ? this.size : minQueueSize;
        this.context = context == null ? Collections.EMPTY_MAP : 
            Collections.unmodifiableMap(context);
        this.semaphore = new Semaphore(this.size, true);
        this.resources = new ConcurrentLinkedQueue<T>();
        this.creating = new ConcurrentLinkedQueue<Future<? extends T>>();
        if(this.minQueueSize > 0) {
            for(int i=0; i <= (this.minQueueSize) && i < this.size; i++){
                creating.add(factory.createResource(context));
            }
        }
    }

    public T getResource(long maxWaitMillis) throws PoolTimeoutException {
        if(closed){
            throw new IllegalStateException("This ResourcePool is already closed");
        }
        // First, get permission to take or create a resource
        try {
            if(!semaphore.tryAcquire(maxWaitMillis, TimeUnit.MILLISECONDS)){
                throw new PoolTimeoutException(maxWaitMillis, size ,
                    semaphore.getQueueLength());
            }
        } catch (InterruptedException e) {
            if(closed){
                throw new IllegalStateException("This ResourcePool is already closed");
            } else {
                log.warn(" ... interrupted!",e);
                return null;
            }
        }
        T res = null;
        Future<? extends T> future;
        synchronized (resources) {
            if(closed){
                throw new IllegalStateException("This ResourcePool is already closed");
            }
            //check if creating resources are ready and add them to the queue
            Iterator<Future<? extends T>> it = creating.iterator();
            while(it.hasNext()){
                Future<? extends T> f = it.next();
                if(f.isDone()){
                    it.remove();
                    if(!f.isCancelled()){
                        try {
                            resources.add(f.get());
                        } catch (InterruptedException e) {
                            log.warn("Interupted while creating resource!", e);
                        } catch (ExecutionException ee) {
                            log.warn("Unable to create a Resoruce because of a "
                                    + ee.getCause().getClass().getSimpleName()
                                    + "while creating the Resource using "
                                    + factory.getClass().getSimpleName() 
                                    + " (message: "+ee.getCause().getMessage()
                                    + ")!",ee);
                        }
                    } // else cancelled ... nothing to do
                } // else still creating ... nothing to do
            }
            //now get the resources (if available) from the queue
            res = resources.poll();
            //if queue is to small create additional resources
            if(resources.size() < minQueueSize || 
                    res == null){ //in case minQueueSize == 0
                creating.add(factory.createResource(context));
            }
            if(res == null){
                future = creating.poll(); //creating can not be empty!
            } else {
                future = null;
            }
        }
        if (res == null) { 
            try { //we need to wait for the next resource to be created
                return future.get();
            } catch (InterruptedException e) {
                // release this acquire as we do not deliver a resource
                semaphore.release();
                throw new IllegalStateException("Interupted",e);
            } catch (ExecutionException ee) {
                Throwable e = ee.getCause();
                // release this acquire as we do not deliver a resource
                semaphore.release();
                throw new IllegalStateException("Unable to provide a Resoruce "
                    + "because of a "+e.getClass().getSimpleName()
                    + "while creating the Resource using "+ factory.getClass().getSimpleName()
                    + " (message: "+e.getMessage()+")!",e);
            } catch (RuntimeException e) { //should not happen
                //so note this in the logs
                log.warn("Unexpected "+e.getClass().getSimpleName()+"!");
                semaphore.release(); //make sure we release the acquire
                throw e; //re throw
            }
        } else { //return the resource polled from the queue
            return res;
        } //else  still enough resources in the queue
    }

    public void returnResource(T res) {
        try {
            synchronized (resources) {
                if(closed){
                    factory.closeResource(res, context);
                } else {
                    resources.add(res); //return to the queue
                }
            }
        } finally {
           semaphore.release(); // and release the semaphore
       }
    }
    /**
     * Closes this resource pool
     */
    public void close() {
        this.closed = true;
        List<Future<? extends T>> toClose = new ArrayList<Future<? extends T>>();
        synchronized (resources) {
            for(Future<? extends T> f : creating){
                if(f.isDone()){
                    toClose.add(0, f); //add in the front
                }
                if(!f.cancel(false)){
                    toClose.add(f); //add to the end (as we might need to wait for those)
                } //cancelled before creation started - no need to close
                
            }
            creating.clear();
            for(T resource : resources){
                factory.closeResource(resource, context);
            }
            resources.clear();
        }
        //now close the resources
        for(Future<? extends T> f : toClose){
            try {
                factory.closeResource(f.get(), context);
            } catch (InterruptedException e) {
                log.warn("Interupted while closing resource ",e);
            } catch (ExecutionException e) {
                log.warn("Unable to close resource ",e);
            }
        }
    }
    /**
     * Responsible for creating instance for the {@link ResourcePool}. This
     * allows lazzy initialization of the Resources in the Pool
     * @author Rupert Westanthaler
     *
     * @param <T> the type of the resource
     * @param <E> the exception thrown by the Factory
     */
    public static interface ResourceFactory<T> {

        /**
         * Creates a {@link Future} used to get the created resource. The
         * Factory is responsible to manage the {@link ExecutorService} used
         * to create instances. The context can be used to read the state of
         * this {@link ResourcePool}.
         * @param context the context as parsed to the {@link ResourcePool}
         * @return the resource. MUST NOT be <code>null</code>
         * @throws IllegalArgumentException if the context is missing an
         * required information
         */
        Future<T> createResource(Map<String,Object> context);

        /**
         * Request the Factory to close this resource. This allows the factory
         * to free up System resources acquired by Resources.
         * @param resource An resource created by this ResourceFactory instance
         * that is no longer needed by the Pool.
         * @param context the context
         */
        void closeResource(Object resource, Map<String,Object> context);
    }
    
    
}
