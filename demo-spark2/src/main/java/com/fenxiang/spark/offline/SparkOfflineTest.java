package com.fenxiang.spark.offline;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName SparkOnlineTest
 * @Author lqs
 * @Date 2020/5/28 15:21
 */
public class SparkOfflineTest {
    private static JavaRDD<String> javaRDD = null;
    public static void main(String[] args) throws Exception{
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local").setAppName("WordCnt");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        //Test java spark task

        {
            String flag;
            if(args == null || args.length==0){
                flag = "1";
            } else {
                flag = args[0];
            }
            switch (flag){
                case "1":
                    javaRDD = sparkContext.textFile("hdfs://hadoop01:8020/spark/test/test.txt");
                    wordCnt();
                    break;
                case "2":
                    javaRDD = sparkContext.textFile("hdfs://hadoop01:8020/spark/test/test.txt").sample(false,0.9);
                    filterMost();
                    break;
                case "3":
                    javaRDD = sparkContext.textFile("hdfs://hadoop01:8020/spark/test/topN.txt");
                    topN2(5);
                    break;
                case "4":
                    javaRDD = sparkContext.textFile("hdfs://hadoop01:8020/spark/test/topN.txt");
                    saveAsHadoopFile(10);
                    break;
                case "5":
                    javaRDD = sparkContext.textFile("hdfs://hadoop01:8020/spark/test/friend.txt");
                    friendCircle();
                    break;
                case "6":
                    javaRDD = sparkContext.textFile("hdfs://hadoop01:8020/fenxiang/search/input");
                    wordsCnt();
                    break;
                default:
                    break;
            }
        }
        sparkContext.stop();
    }

    //统计单词数量
    // => collections.Counter(list)
    private static void wordCnt(){
        javaRDD.mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s,1);
            }
        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<Integer, String> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return new Tuple2<>(stringIntegerTuple2._2,stringIntegerTuple2._1);
            }
        }).sortByKey(false)//false 降序
                .mapToPair(new PairFunction<Tuple2<Integer, String>, String, Integer>() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Tuple2<String, Integer> call(Tuple2<Integer, String> integerStringTuple2) throws Exception {
                        return new Tuple2<>(integerStringTuple2._2,integerStringTuple2._1);
                    }
                }).foreach(new VoidFunction<Tuple2<String, Integer>>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                System.out.println(stringIntegerTuple2);
            }
        });
//        javaRDD.mapToPair(s -> new Tuple2<>(s, 1))
//                .reduceByKey(Integer::sum)
//                .mapToPair(x -> new Tuple2<>(x._2, x._1))
//                .sortByKey(false)//false 降序
//                .mapToPair(x -> new Tuple2<>(x._2, x._1))
//                .foreach(System.out::println);
    }

    //过滤最多的单词
    //1 查找出现最多的，2 过滤
    private static void filterMost(){

        final String result = javaRDD.mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = -1;

            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s.split(" ")[1], 1);
            }
        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = -1;

            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
            private static final long serialVersionUID = -1;

            @Override
            public Tuple2<Integer, String> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return new Tuple2<>(stringIntegerTuple2._2, stringIntegerTuple2._1);
            }
        }).sortByKey(false)//desc
                .first()
                ._2;

        javaRDD.filter(new Function<String, Boolean>() {
            private static final long serialVersionUID = -1;
            @Override
            public Boolean call(String s) throws Exception {
                return !s.contains(result);
            }
        }).foreach(new VoidFunction<String>() {
            private static final long serialVersionUID = -1;
            @Override
            public void call(String s) throws Exception {
                System.out.println("s = " + s);
            }
        });
    }

    // Top N 问题(1)
    // => error错误的例子, 广播变量只能在主driver定义、修改.不能通过executor修改!!!!!!!
    private static void topN1(final int n){
        final Object[][] top = new Object[n][2];
        for(int i = 0; i < n; i++){
            top[i] = new Object[]{"", Integer.MIN_VALUE};
        }
        javaRDD.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String s) throws Exception {
                String[] arr;
                if((arr = s.split(" ")).length==2 && StringUtils.isNumeric(arr[1])){
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        }).mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] strings = s.split(" ");
                return new Tuple2<>(strings[0], Integer.parseInt(strings[1]));
            }
        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> t) throws Exception {
                Integer m1 = t._2, m2;
                String s1 = t._1;
                for(int i = 0; i < n; i++){
                    m2 = (Integer) top[i][1];
                    System.out.println(t._1 + ":::::::::::::::::::" + t._2 +"::::::" +i+":"+ Arrays.toString(top[i]));
                    if(m1 > m2){
                        top[i][0] = s1;
                        top[i][1] = m1;
                        s1 = (String) top[i][0];
                        m1 = m2;
                    }
                }
            }
        });

        for (Object[] objects : top) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>." + Arrays.toString(objects));
        }
    }

    // Top N 问题(2)
    // => 正确的例子, 广播变量只能在driver定义、修改. 只有在executor定义的变量才在DAG遍历过程中生效
    //每一项下面最多的N个
    private static void topN2(int n){
        javaRDD.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String s) throws Exception {
                String[] arr;
                return (arr = s.split(" ")).length==2 && StringUtils.isNumeric(arr[1]);
            }
        }).mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] strings = s.split(" ");
                return new Tuple2<>(strings[0], Integer.parseInt(strings[1]));
            }
        }).groupByKey()
                .sortByKey(true)
                .foreach(new VoidFunction<Tuple2<String, Iterable<Integer>>>() {
            @Override
            public void call(Tuple2<String, Iterable<Integer>> t) throws Exception {
                int[] top = new int[n];
                Arrays.fill(top,Integer.MIN_VALUE);
                for (int integer : t._2) {
                    int m1 = integer, m2;
                    for (int i = 0; i < n; i++) {
                        m2 = top[i];
                        if (m1 > m2) {
                            top[i] = m1;
                            m1 = m2;
                        }
                    }
                }
                System.out.println("class:" + t._1);
                System.out.println(Arrays.toString(top));
            }
        });
    }

    //Spark计算生成的数据存放在HDFS
    private static void saveAsHadoopFile(final int n){
        //base !!!!!!!
        //saveAsHadoopFile(outputDir, IntWritable.class, Text.class, SequenceFileOutputFormat.class, DefaultCodec.class);

        javaRDD.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String s) throws Exception {
                String[] arr;
                if((arr = s.split(" ")).length==2 && StringUtils.isNumeric(arr[1])){
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        }).mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] strings = s.split(" ");
                return new Tuple2<>(strings[0], Integer.parseInt(strings[1]));
            }
        }).groupByKey()
                .sortByKey(true)
                .mapToPair(new PairFunction<Tuple2<String, Iterable<Integer>>, Text, Text>() {
                    @Override
                    public Tuple2<Text, Text> call(Tuple2<String, Iterable<Integer>> t) throws Exception {
                        int[] top = new int[n];
                        Arrays.fill(top,Integer.MIN_VALUE);
                        for (int integer : t._2) {
                            int m1 = integer, m2;
                            for (int i = 0; i < n; i++) {
                                m2 = top[i];
                                if (m1 > m2) {
                                    top[i] = m1;
                                    m1 = m2;
                                }
                            }
                        }
                        return new Tuple2<>(new Text(t._1.replaceAll(",","")), new Text(Arrays.toString(top)));
                    }
                })
                // new
                .saveAsNewAPIHadoopFile("hdfs://hadoop01:8020/spark/res/tmp_new", Text.class, Text.class, org.apache.hadoop.mapreduce.lib.output.TextOutputFormat.class);
                // old
//                .saveAsHadoopFile("hdfs://hadoop01:8020/spark/res/tmp",Text.class, Text.class, org.apache.hadoop.mapred.TextOutputFormat.class);
    }

    //查询所有的朋友对
    //base 解析出所有的朋友对然后分组
    private static void friendCircle(){
        javaRDD.flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterable<Tuple2<String, Integer>> call(String s) throws Exception {
                String[] strings = s.split("\t");
                List<Tuple2<String, Integer>> res = new ArrayList<>();
                for(String item : strings[1].split(",")){
                    int cmp = strings[0].compareTo(item);
                    if(cmp < 0){
                        res.add(new Tuple2<>(strings[0] + ":" + item, 1));
                    }else{
                        res.add(new Tuple2<>(item + ":" + strings[0], 1));
                    }
                }
                return res;
            }
        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).foreach(new VoidFunction<Tuple2<String, Integer>>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                System.out.printf("朋友圈关系:%s, 关注关系:%d\n",stringIntegerTuple2._1,stringIntegerTuple2._2);
            }
        });
    }

    //查询所有单词搜索的数量然后转为文件
    private static void wordsCnt() throws Exception{
        final List<Tuple2<Integer, String>> res = javaRDD.filter(new Function<String, Boolean>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Boolean call(String line) throws Exception {
                //接收到的每一行数据
                if(line.contains("/goods/search")){
                    int startIdx = line.indexOf("request=");
                    int endIdx = line.indexOf(", response=");
                    if(endIdx>startIdx && startIdx > -1){
                        String json = line.substring(startIdx + 8, endIdx);
                        json = StringEscapeUtils.unescapeJava(json);
                        final Gson gson = new Gson();
                        final Type type = new TypeToken<Map<String, String>>(){}.getType();
                        Map<String, String> myMap = gson.fromJson(json,type);
                        return myMap.containsKey("keyword");
                    }
                }
                return Boolean.FALSE;
            }
        }).mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String line) throws Exception {
                int startIdx = line.indexOf("request=");
                int endIdx = line.indexOf(", response=");
                String json = line.substring(startIdx + 8, endIdx);
                json = StringEscapeUtils.unescapeJava(json);
                final Gson gson = new Gson();
                final Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> myMap = gson.fromJson(json,type);
                String kw =  myMap.get("keyword");
                return new Tuple2<>(kw, 1);
            }
        }).reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Integer call(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<Integer, String> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return new Tuple2<>(stringIntegerTuple2._2, stringIntegerTuple2._1);
            }
        }).sortByKey(false)//false 降序\
                .take(200);
        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Files.write(Paths.get("/root/keywords/",format + "-keyword.csv"), res.stream().map(e->String.format("%s,%d",e._2,e._1)).collect(Collectors.toList()),
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
}
