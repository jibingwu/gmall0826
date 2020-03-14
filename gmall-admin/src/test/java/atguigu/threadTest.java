package atguigu;

import org.aspectj.weaver.ast.Var;

import java.io.IOException;
import java.util.concurrent.*;

public class threadTest {
    public static void main(String[] args) throws IOException {

        int corePoolSize = 3; //线程池中的常驻核心线程数量
        int maximumPoolSize = 5;//线程池中能够容纳同时执行的最大线程数量。
        long keepAliveTime = 3L; //线程保持存活时间
        TimeUnit unit = TimeUnit.SECONDS;  //线程存活的时间
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(3);//任务队列。被提交，尚未执行的任务
        ThreadFactory threadFactory = Executors.defaultThreadFactory(); //默认线程工厂
        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy(); //拒绝策越


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingDeque<>(3), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        //threadPoolExecutor.prestartAllCoreThreads(); // 预启动所有核心线程
        for (int i = 1; i <= 100; i++) {
            final int count = i;
            threadPoolExecutor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + count);

            });
        }
        threadPoolExecutor.shutdown();
        /**
         *
         *
         *
         *
         *
         *

         /*

         int corePoolSize = 2;
         int maximumPoolSize = 4;
         long keepAliveTime = 10;
         TimeUnit unit = TimeUnit.SECONDS;
         BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2);
         ThreadFactory threadFactory = new NameTreadFactory();
         RejectedExecutionHandler handler = new MyIgnorePolicy();
         ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
         workQueue, threadFactory, handler);
         executor.prestartAllCoreThreads(); // 预启动所有核心线程

         for (int i = 1; i <= 10; i++) {

         executor.execute(task);
         }

         System.in.read(); //阻塞主线程




         *
         *
         *
         *
         *
         */




    }
}











