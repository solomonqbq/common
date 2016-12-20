# common
常用工具汇总
## 多线程执行工具：（MultiTaskDispatcher，用countdownlatch实现）
```
public class Sample {
    public static void main(String[] args) {
        MultiTaskDispatcher multiTaskDispatcher = new MultiTaskDispatcher();//初始化
        List<TaskUnit> list  = new ArrayList<>();
        int count = 1;
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        list.add(new DemoTask(count++));
        multiTaskDispatcher.runTasks(list);//并行执行任务并等待，或使用runAsyncTasks不等待
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
```