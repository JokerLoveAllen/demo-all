package com.fenxiang.hbase.mapred.hadoop.reduce;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @ClassName SearchReduce
 * @Author lqs
 * @Date 2020/5/8 11:00
 */
public class SearchReduce extends Reducer<Text, LongWritable, Text, LongWritable> {
    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        long sum = 0;
        for (LongWritable value : values) {
            //求key出现的次数
            sum += value.get();
        }
        //将统计的结果进行输出
        context.write(key, new LongWritable(sum));
    }
}
