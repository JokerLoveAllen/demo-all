package com.fenxiang.spark

import java.nio.file.{Files, Paths, StandardOpenOption}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.lang3.StringEscapeUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 * fix: scala maven 工程不能打包(OOM)
 * 在bin目录下mvn文件有:
 *      -- MAVEN_OPTS="`concat_lines "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config"` $MAVEN_OPTS"
 *  因此在 Maven的安装目录内{和bin目录同级}添加 .mvn文件夹并新建 jvm.config
 *  jvm.config 增加构建使用的内存  export MAVEN_OPTS="-Xmx4g -XX:MaxPermSize=1g"
 */
object SparkTest {

  @throws(classOf[Exception])
  def main(args: Array[String]) : Unit = {
    val size = args.length
    if(size < 1){
      println("缺少必填参数, 退出.....")
      return
    }
    val info = args(0)
    if("1".equals(info)) {
      //大文本 统计
      wordCountAnalysis()
    } else if("2".equals(info)){
      topN()
    } else if("3".equals(info)){

    } else if("4".equals(info)){

    } else if("5".equals(info)){

    } else if("6".equals(info)){

    } else if("7".equals(info)){

    } else if("8".equals(info)){

    } else if("9".equals(info)){

    } else {
      println("没有[%s]模式".format(info))
    }
  }

  /**
   * 大文件统计前n条数据
   * @param takeCount 需要统计前n数量
   */
  def wordCountAnalysis(takeCount:Int = 100000, searchCountLimit:Int = 30): Unit ={
    val before = System.currentTimeMillis()
    println("%s => %s ".format("fxxk it! ","this work !!!"))
    val conf = new SparkConf()
    //    conf.setMaster("standalone").setAppName("WordCount")
    conf.setMaster("local").setAppName("WordCount")
    val sc = new SparkContext(conf)
    try{
      val rdd : RDD[String] = sc.textFile("hdfs://hadoop01:8020/fenxiang/search/input")
      //    val sampleRDD : RDD[String] = rdd.sample(false, 0.9) //采样
      val result = rdd
        .filter{ line => {
          var res = false
          if(line.contains("/goods/search")){
            val startIdx: Int = line.indexOf("request=")
            val endIdx: Int= line.indexOf(", response=")
            if(endIdx>startIdx && startIdx > -1){
              var json : String = line.substring(startIdx + 8, endIdx)
              json = StringEscapeUtils.unescapeJava(json)
              val gson = new Gson()
              val tpe = new TypeToken[util.Map[String, String]](){}.getType
              val myMap : util.Map[String, String]  = gson.fromJson(json,tpe)
              res = myMap.containsKey("keyword")
            }
          }
          res
        }}
        //.map { x => (x.split(" ")(1),1) }
        .map { line =>{
          val startIdx: Int = line.indexOf("request=")
          val endIdx: Int= line.indexOf(", response=")
          var json : String = line.substring(startIdx + 8, endIdx)
          json = StringEscapeUtils.unescapeJava(json)
          val tpe = new TypeToken[util.Map[String, String]](){}.getType
          val myMap : util.Map[String, String]  = new Gson().fromJson(json, tpe)
          (myMap.get("keyword"), 1)
        }}
        .reduceByKey(_+_)
        .filter(_._2 >= searchCountLimit)
        .map { x => {(x._2, x._1)}}
        .sortByKey(false)
        .take(takeCount)

      //    rdd
      //      .filter {(!_.contains(result))}
      //      .foreach(println)
      val format = LocalDateTime.now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
      val arr = new util.ArrayList[String](200)
      result.map(e=>"%s,%d".format(e._2, e._1)).foreach(e=>arr.add(e))
      Files.write(Paths.get("/root/keywords/", format + "-keyword.csv"), arr,StandardOpenOption.APPEND, StandardOpenOption.CREATE)
    }catch {
      case e : Throwable => println("shit it,have error !!!", e)
    }finally {
      sc.stop()
      val now = System.currentTimeMillis()
      val sec = (now-before)/1000
      val minute = sec / 60
      println("after %d minute %d second finish !".format(minute, sec % 60))
      println("%s => %s".format("yes!!","it finished"))
    }
  }

  def topN(n : Int = 3): Unit ={
    val before = System.currentTimeMillis()
    println("%s => %s ".format("fxxk it! ","this work !!!"))
    val conf = new SparkConf()
    conf.setMaster("local").setAppName("topN")
    val sc = new SparkContext(conf)

    // 判断是否为数字组成的字符串
    def isNumeric(string: String): Boolean ={
      val charArray = string.toCharArray
      for(idx <- 0 to charArray.length){
        if(charArray(idx) < '0' || charArray(idx) > '9'){
          return false
        }
      }
      true
    }

    try{
      val rdd : RDD[String] = sc.textFile("hdfs://hadoop01:8020/spark/test/topN.txt")
      rdd
        .filter{ line => {
          val arr = line.split(" ")
          arr.length==2 && isNumeric(arr(1))
        }}
        //.map { x => (x.split(" ")(1),1) }
        .map { line =>{
          val arr = line.split(" ")
          (arr(0), Integer.valueOf(arr(1)))
        }}
        .groupByKey()
        .sortByKey(true)
        .foreach(voidFunc => {
          val topN = new Array[Int](n)
          util.Arrays.fill(topN, Integer.MIN_VALUE)
          voidFunc._2.foreach(e=>{
            var m1 = e
            var m2 = -1
            for(i <- 0 to n){
              m2 = topN(i)
              if(m1>m2){
                topN(i) = m1
                m1 = m2
              }
            }
          })
          println("==============> %s top[%d] is %s <==============".format(voidFunc._1, n, util.Arrays.toString(topN)))
        })
    }catch {
      case e : Throwable => println("shit it,have error !!!", e)
    }finally {
      sc.stop()
      val now = System.currentTimeMillis()
      val sec = (now-before)/1000
      val minute = sec / 60
      println("after %d minute %d second finish !".format(minute, sec % 60))
      println("%s => %s".format("yes!!","it finished"))
    }
  }
}
