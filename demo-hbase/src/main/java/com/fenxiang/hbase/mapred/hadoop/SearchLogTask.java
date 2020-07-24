package com.fenxiang.hbase.mapred.hadoop;

import com.fenxiang.hbase.mapred.hadoop.mapper.SearchMapper;
import com.fenxiang.hbase.mapred.hadoop.reduce.SearchReduce;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.util.Arrays;

/**
 * @ClassName SearchLogTask
 * @Author lqs
 * @Date 2020/5/8 10:57
 * use:
 * hadoop jar ${jar} com.fenxiang.bigdata.hadoop.SearchLogTask /fenxiang/search/input/${log} /fenxiang/search/output
 */
@Slf4j
public class SearchLogTask {

    private static final String HDFS_PREFIX = "hdfs://hadoop01:8020";

    public static void main1(String[] args) throws Throwable{
        if (args.length < 2) {
            log.error("必须添加输入和输出路径！!");
            System.exit(1);
        }
        args[1] = HDFS_PREFIX + args[1];
        args[2] = HDFS_PREFIX + args[2];
        log.info("输入输出目录信息:({})", Arrays.toString(args));
        //创建配置文件
        Configuration configuration = new Configuration();
        //设置mapper阶段的堆内存大小
//        configuration.set("mapreduce.admin.map.child.java.opts", "-Xmx1g");
//        configuration.set("mapred.map.child.java.opts", "-Xmx1g");
//
//        //设置reducer阶段的堆内存大小
//        configuration.set("mapreduce.admin.reduce.child.java.opts", "-Xmx1g");
//        configuration.set("mapred.reduce.child.java.opts", "-Xmx1g");

        //判断是否存在输出文件--有的话进行删除
        FileSystem fileSystem = FileSystem.get(configuration);

        Path outFilePath = new Path(args[2]);

        boolean is_exists = fileSystem.exists(outFilePath);

        //判断是否存在此文件--存在的话进行删除
        if (is_exists) {
            fileSystem.delete(outFilePath, true);
        }

        //创建job对象
        Job job = Job.getInstance(configuration, "searchLogTask");
        //设置job的处理类
        job.setJarByClass(SearchLogTask.class);
        //设置作业处理的输入路径
        FileInputFormat.setInputPaths(job, new Path(args[1]));

        //设置map相关参数
        job.setMapperClass(SearchMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        //设置reduce相关参数
        job.setReducerClass(SearchReduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        //设置作业处理的输出路径
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        if(job.waitForCompletion(true)){
            log.info("执行完成!!");
        }
    }
}
