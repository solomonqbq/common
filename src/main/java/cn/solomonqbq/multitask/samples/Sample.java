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

package cn.solomonqbq.multitask.samples;

import cn.solomonqbq.multitask.MultiTaskDispatcher;
import cn.solomonqbq.multitask.TaskUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by qinbaoqi on 2016/4/25.
 */
public class Sample {
    public static void main(String[] args) {
        MultiTaskDispatcher multiTaskDispatcher = new MultiTaskDispatcher();
        List<TaskUnit> list  = new ArrayList<TaskUnit>();
        int count = 1;
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        multiTaskDispatcher.runTasks(list);
        System.out.println("all tasks done");
    }
}


class DemoTask implements TaskUnit{

    private final int i;

    DemoTask(int i){
        this.i = i;
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(new Random().nextInt(3)*1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("task "+i+" finishied");
    }

    @Override
    public int getPriorityOfQueue() {
        return 0;
    }
}
