package com.fenxiang.hbase.phoenix.common_test;

import com.fenxiang.hbase.mapred.hbase.NaiveHbaseApi;
import com.fenxiang.hbase.phoenix.utils.TinySnowFlakeGenerator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @ClassName NaiveHbaseApi
 * @Author zy
 * @Date 2020/6/2 17:20
 */
public class NaiveHbaseTest {
    public static void main(String[] args) throws IOException {
//        new NaiveHbaseApi("hadoop01","2181","").getByRowKey("MY_SCHEMA.GOODS_PUSH","01000302202000016499414139054");
//        System.out.println(Long.toHexString(Long.MAX_VALUE));
        System.out.println(new TinySnowFlakeGenerator(-1, 1).nextId());
    }
}
