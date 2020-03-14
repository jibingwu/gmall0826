package com.atguigu.gmall.item;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class TestCompletableFuture {


    public static void main(String[] args) {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("初始化化CompletableFuture子任务");

            int i=1/0;
            return "hello supplyAsync";
            }).thenApply(s -> {
            System.out.println("s = " + s);
                return "hello  thenApply " ;

        }).whenCompleteAsync((t,u) ->{
            System.out.println("---------------------CompletableFuture-------------------------");
            System.out.println("上个任务的返回值 = " + t);
            System.out.println("上个任务的返回异常异常信息= " + u);

        }).exceptionally(t->{
            System.out.println("---------------------exceptionally-------------------------");
            System.out.println("t = " + t);
            return "Hello exceptionally";

        }).handleAsync((t, u) -> {

            System.out.println(".......................handleAsync...........................");
            System.out.println("上个任务的返回值 = " + t);
            System.out.println("上个任务的返回异常异常信息= " + u);
             return "hello handleAsync";

        });
        

    }
}
