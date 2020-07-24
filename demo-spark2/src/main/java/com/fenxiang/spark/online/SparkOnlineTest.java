package com.fenxiang.spark.online;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka.KafkaReceiver;
import org.apache.spark.streaming.receiver.Receiver;
import scala.Tuple2;

import java.util.Arrays;

/**
 * @ClassName SparkOnlineTest
 * @Author lqs
 * @Date 2020/6/3 15:21
 * 1.local的模拟线程数必须大于等于2,因为一条线程被receiver(接受数据的线程)占用，另外一个线程是job执行
 * 2.Durations时间的设置，就是我们能接受的延迟度，这个我们需要根据集群的资源情况以及监控，要考虑每一个job的执行时间
 * 3.创建StreamingContext有两种方式 (sparkconf、sparkcontext)
 *  3.x 注意 streamContext数据来源是 增量数据比如时间区间内增加的数据，新建的文件(更新的文件和启动任务时既有的文件不被受理!!!)，
 * 4.业务逻辑完成后，需要有一个output operator
 * 5.StreamingContext.start(),straming框架启动之后是不能在次添加业务逻辑
 * 6.StreamingContext.stop()无参的stop方法会将sparkContext一同关闭，如果只想关闭StreamingContext,在stop()方法内传入参数false
 * 7.StreamingContext.stop()停止之后是不能在调用start
 */
public class SparkOnlineTest {

    private static JavaStreamingContext javaStreamingContext;

    private static void init() {
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local");
        sparkConf.setAppName("WordCount_OL");
        //使用sparkConf 配置资源并且每隔10s进行数据流的分析
        javaStreamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(10));
    }

    private static void stop(){
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
        javaStreamingContext.stop();
    }

    public static void main(String[] args) {
        init();
        //Test java SparkStreaming task
        {
            String flag;
            if (args == null || args.length == 0) {
                flag = "1";
            } else {
                flag = args[0];
            }
            switch (flag) {
                case "1":
                    wordCountWithSparkStreaming();
                    break;
                case "2":
                    break;
                case "3":
                    break;
                default:
                    break;
            }
        }
        stop();
    }

    private static void wordCountWithSparkStreaming() {
        JavaDStream<String> javaDStream;
//        javaDStream = javaStreamingContext.socketTextStream("", 9999, StorageLevel.MEMORY_AND_DISK());

        javaDStream = javaStreamingContext.textFileStream("hdfs://hadoop01:8020/spark/ol");

        final JavaDStream<String> wordJavaDStream = javaDStream.flatMap(new FlatMapFunction<String, String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterable<String> call(String s) throws Exception {
                return Arrays.asList(s.split(" "));
            }
        });

        final JavaPairDStream<String, Integer> javaPairDStream = wordJavaDStream.mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });

        final JavaPairDStream<String, Integer> resultJavaDStream = javaPairDStream.reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Integer call(Integer _1, Integer _2) throws Exception {
                return _1 + _2;
            }
        });

        final int[] counter = {0};
        resultJavaDStream.foreach(new Function<JavaPairRDD<String, Integer>, Void>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Void call(JavaPairRDD<String, Integer> rdd) throws Exception {
                System.out.println("---------------------------------->" + rdd.hashCode() + "::::" + counter[0]++);
                rdd.foreach(new VoidFunction<Tuple2<String, Integer>>() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                        System.out.println("counter:" + counter[0] + " string:" + stringIntegerTuple2._1 + " cnt:" + stringIntegerTuple2._2);
                    }
                });
                return null;
            }
        });
        resultJavaDStream.print();
    }
}
