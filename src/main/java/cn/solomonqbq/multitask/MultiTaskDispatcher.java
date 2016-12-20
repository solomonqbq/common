/*
 * Copyright [2016] [solomon qbq]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.solomonqbq.multitask;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多任务并行调度器
 *
 * @author qinbaoqi
 */
@Component
public class MultiTaskDispatcher {

    Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${threadpool.corepoolsize}")
    private int corePoolSize = 20;
    @Value("${threadpool.maximumpoolsize}")
    private int maximumPoolSize = 50;
    @Value("${threadpool.keepalivetime}")
    private int keepAliveTime = 10;//单位分钟
    @Value("${threadpool.queuesize}")
    private int queueSize = 100;

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public ThreadPoolExecutor getPool() {
        return pool;
    }

    public void setPool(ThreadPoolExecutor pool) {
        this.pool = pool;
    }

    public Comparator getTaskUnitComparator() {
        return taskUnitComparator;
    }

    public void setTaskUnitComparator(Comparator taskUnitComparator) {
        this.taskUnitComparator = taskUnitComparator;
    }

    /**
     * 线程池
     */
    private ThreadPoolExecutor pool = null;

    //初始化锁
    private ReentrantLock lock = new ReentrantLock();

    //任务优先级比较器
    private Comparator taskUnitComparator = new Comparator<TaskUnit>() {
        @Override
        public int compare(TaskUnit t1, TaskUnit t2) {
            return t1.getPriorityOfQueue() - t2.getPriorityOfQueue();
        }
    };

    /**
     * @param tasks
     */
    public void runTasks(List<TaskUnit> tasks) {
        if (pool == null) {
            lock.lock();
            initPool();
            lock.unlock();
        }
        //组织并对任务排队
        Collections.sort(tasks, taskUnitComparator);
        Integer currentPriority = null;
        //当前执行多线程任务集合
        Set<TaskUnit> batchSet = new HashSet<TaskUnit>();
        for (TaskUnit unit : tasks) {
            if (currentPriority == null) {
                currentPriority = unit.getPriorityOfQueue();
            }
            if (currentPriority == unit.getPriorityOfQueue()) {
                //同一优先级任务
                batchSet.add(unit);
            } else {
                //当前任务优先级较低,执行上一批高优先级任务
                if (!batchSet.isEmpty()) {
                    executeBatchTasks(batchSet);
                }
                batchSet.clear();
                currentPriority = unit.getPriorityOfQueue();
                batchSet.add(unit);
            }
        }
        if (!batchSet.isEmpty()) {
            executeBatchTasks(batchSet);
        }
    }

    /**
     * @param tasks
     */
    public void runAsyncTasks(List<TaskUnit> tasks) {
        if (pool == null) {
            lock.lock();
            initPool();
            lock.unlock();
        }
        for (final TaskUnit taskUnit : tasks) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    taskUnit.execute();
                }
            });
        }
    }

    /**
     * 执行一批任务，本批任务优先级相同
     *
     * @param set
     */
    private void executeBatchTasks(Set<TaskUnit> set) {
        try {
            CountDownLatch endSignal = new CountDownLatch(set.size());
            for (TaskUnit task : set) {
                pool.execute(new RunnableTask(task, endSignal));
            }
            endSignal.await();
        } catch (Exception e) {
            logger.error("执行批量任务出错:", e);
            return;
        }
    }

    class RunnableTask implements Runnable {
        TaskUnit task;
        CountDownLatch end;

        RunnableTask(TaskUnit task, CountDownLatch end) {
            this.task = task;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                task.execute();
            } catch (Exception e) {
                logger.error("子任务出错:{}", e);
            } finally {
                end.countDown();
            }
        }
    }

    @PostConstruct
    public void initPool() {
        pool = new ThreadPoolExecutor(this.corePoolSize, this.maximumPoolSize, this.keepAliveTime,
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(this.queueSize), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
