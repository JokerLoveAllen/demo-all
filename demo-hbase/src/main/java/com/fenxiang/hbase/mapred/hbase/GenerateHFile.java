package com.fenxiang.hbase.mapred.hbase;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 需求：用BulkLoad的方法导入数据
 *
 * # hdfs创建文件夹
 * hdfs dfs -mkdir -p /bulk/input
 *
 * # 上传文件到HDFS
 * hdfs dfs -put /data/hbase/bulkload.txt   /bulk/input
 *
 * # list
 * hdfs dfs -ls /bulk/input
 *
 * # cat
 * hdfs dfs -cat /bulk/input/bulkload.txt
 *
 * # rm -r
 * hdfs dfs -rm -r  /bulk/output
 *
 * # execute jar
 * hadoop jar /data/hbase/hbase_bulk-1.0.jar com.fenxiang.bigdata.hbase.GenerateHFileDriver
 *
 * @author jokerloveallen
 * @DataFormat 1       info:www.baidu.com      BaiDu
 *
 */
public class GenerateHFile extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put>{
	
	@Override
	protected void map(LongWritable Key, Text Value,
			Context context)
			throws IOException, InterruptedException {
		
		//切分导入的数据
		String Values=Value.toString();
		String[] Lines=Values.split("\t");
		String Rowkey=Lines[0];
		String ColumnFamily="0";//Lines[1].split(":")[0];
		String Qualifier=Lines[1].split(":")[1];
		String ColValue=Lines[2];
		
		//拼装rowkey和put
		ImmutableBytesWritable PutRowkey=new ImmutableBytesWritable(Rowkey.getBytes());
		Put put=new Put(Rowkey.getBytes());
		put.addColumn(ColumnFamily.getBytes(), Qualifier.getBytes(), ColValue.getBytes());
//		put.addColumn();
		context.write(PutRowkey,put);
	}
	
}