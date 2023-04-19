package org.example.util;

import java.util.concurrent.*;

/**
 * 线程工具类
 */
public class ThreadUtil {


    public  static  String ThreadName (){
        return Thread.currentThread().getName();
    }

    /**
     * 线程休眠 time 毫秒
     * @param time
     */
    public static  void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程休眠 time 秒
     * @param time
     */
    public static  void sleepSend(long time){
        try {
            time = time * 1000;
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 线程休眠 time nanos
     * @param time
     */
    public  static  void sleepNanos(long time){
        try {
            Thread.sleep(time, (int) (time % 1000000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前线程信息
     */
    public static void Print(String msg){
        System.out.println("["+Thread.currentThread().getName()+"]: "+msg);
    }

    public static  void shutdownThreadPoolGracefully(ThreadPoolExecutor pool){
        //线程池已关闭返回
        if (!(pool instanceof ExecutorService) || pool.isTerminated()) {
            return;
        }
        try {
            //关闭线程池 不再接受新的任务
            pool.shutdown();
        } catch (SecurityException e) {
            return;
        }catch (NullPointerException e){
            return;
        }
        try {
            //等待线程池关闭
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                //超时的时候向线程池中所有的线程发出中断(interrupted)。
                pool.shutdownNow();
                //再次等待线程池关闭
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        if (!pool.isTerminated()) {
            try {
                for (int i = 0; i < 1000; i++) {
                    if (pool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                    pool.shutdownNow();
                }
            }catch (InterruptedException e){
                System.err.println(e.getMessage());
            }catch (Throwable throwable){
                System.err.println(throwable.getMessage());
            }
        }
    }

}
