package com.fenxiang.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
object SparkSQLTest {

  @throws(classOf[Exception])
  def main(args: Array[String]): Unit = {
    // 构建 spark conf 配置类
    val conf = new SparkConf().setMaster("local").setAppName("SQLWordCount")
    // 获取spark 上下文信息对象
    val sparkContext = new SparkContext(conf)
    //spark 1.x 版本入口
    val sqlContext = new SQLContext(sparkContext)
    // 用于包含RDD到DataFrame隐式转换操作
    import sqlContext.implicits._

    //    对于2.0版本以后，入口变成了SparkSession，使用SparkSession.builder()来构建
    //    Spark2.0引入SparkSession的目的是内建支持Hive的一些特性，
    //    包括使用HiveQL查询，访问Hive UDFs,从Hive表中读取数据等，使用这些你不需要已存在的Hive配置。
    //    而在此之前，你需要引入HiveContext的依赖，并使用HiveContext来支持这些特性。
    //import org.apache.spark.sql.SparkSession;



  }
}
