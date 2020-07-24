package com.fenxiang.spark.sql;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SparkSession;

/**
 * @ClassName SparkSessionTest
 * @Author lun qs
 * @Date 2020/7/1 13:58
 * base:
 *         <!--with use CDH5 SparkSession must v2.0 or higher-->
 *         <dependency>
 *             <groupId>org.apache.spark</groupId>
 *             <artifactId>spark-core_2.11</artifactId>
 *             <version>2.0.0-cloudera1-SNAPSHOT</version>
 *         </dependency>
 *         <dependency>
 *             <groupId>org.apache.spark</groupId>
 *             <artifactId>spark-sql_2.11</artifactId>
 *             <version>2.0.0-cloudera1-SNAPSHOT</version>
 *         </dependency>
 * spark_sql&&spark_core 1.6.0-cdh5.16.2/
 */
public class SparkSessionTest {
    public static void main(String[] args) {

        final SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("WordCnt")
                .getOrCreate();
        final DataFrame dataFrame = spark.range(100L).toDF();
       dataFrame.where("number % 2 = 0").as("mod").sort("mod");

    }
}
