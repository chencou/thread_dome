package org.example.ThreadLocal;

import com.sun.xml.internal.fastinfoset.tools.XML_SAX_StAX_FI;
import lombok.Data;
import org.example.util.ThreadUtil;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * threadLocal  线程threadlocal 使用情况
 */
public class ThreadLocalDemo{
    /**
     * 线程池最大线程数 为cpu核心数的一半
     */
    private static Integer  max = Runtime.getRuntime().availableProcessors()/2;
    /**
     * 默认队列大小 128
     */
    private static  final  Integer  DEFAULT_QUEUE_MAX= 128;

    private static  Integer queueMax;

     public  static void  setQueueMax(Integer queueMax1){
        queueMax = queueMax1;
    }

    public static Integer getQueueMax(){
        return queueMax==null||queueMax==0?DEFAULT_QUEUE_MAX:queueMax;
    }

    /**
     * ThreadLocal 用于线程内部的数据共享
     */
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 线程池
     */
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(
            max, //核心线程数
            max, //最大线程数
            100, //线程空闲时间
            TimeUnit.MILLISECONDS, //线程空闲时间单位
            new LinkedBlockingDeque<>(getQueueMax()) //线程队列
    ){

        /**
         * 钩子方法，在任务执行之前调用 结束使用ThreadLocal 释放资源 防止内存泄漏
         * @param r the runnable that has completed
         * @param t the exception that caused termination, or null if
         * execution completed normally
         */
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            THREAD_LOCAL.remove();
            super.afterExecute(r, t);
        }



    };

    static {
        //设置线程池允许核心线程超时
        pool.allowCoreThreadTimeOut(true);
        //设置钩子方法，在jvm关闭时关闭线程池
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ThreadUtil.shutdownThreadPoolGracefully(pool);
        }));
    }

    @Data
    static  class  Foo{

        static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

        int  index=0;

        int  bar =0;

        public Foo() {
            index = ATOMIC_INTEGER.getAndIncrement();
        }

        @Override
        public  String  toString(){
            return index+"@Foo{" +
                    ", bar=" + bar +
                    '}';
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            pool.execute(()->{
                if ( THREAD_LOCAL.get()== null) {
                    THREAD_LOCAL.set(new Foo());
                }
                ThreadUtil.Print("THREAD_LOCAL.get() = " + THREAD_LOCAL.get());
                for (int j = 0; j < 10; j++) {
                    Foo foo = (Foo) THREAD_LOCAL.get();
                    foo.setBar(foo.getBar()+1);
                }
                ThreadUtil.Print("本底值累计添加10次 当前值：" + THREAD_LOCAL.get());
                //删除当前线程变量
                THREAD_LOCAL.remove();
            });
        }
    }
}
