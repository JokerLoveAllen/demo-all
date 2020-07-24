package com.fenxiang.hbase.mapred.hadoop.reduce;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import java.io.IOException;

/**
 * @ClassName SearchReduceWithHbase
 * @Author zy
 * @Date 2020/5/9 11:50
 */
public class SearchReduceWithHbase extends TableReducer<Text, LongWritable, NullWritable> {
    public static NullWritable OUT_PUT_KEY = NullWritable.get();
    public Put outputValue;
    public long sum;
    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
        sum = 0;
        for (LongWritable value : values) {
            sum += value.get();
        }
        outputValue = new Put(Bytes.toBytes(key.toString()));
        outputValue.addColumn(Bytes.toBytes("0"), Bytes.toBytes("CNT"), Bytes.toBytes(String.valueOf(sum)));
        context.write(OUT_PUT_KEY, outputValue);
    }
}
