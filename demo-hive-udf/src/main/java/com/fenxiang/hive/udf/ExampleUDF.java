package com.fenxiang.hive.udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * @ClassName ExampleUDF
 * @Author lun qs
 * @Date 2020/6/30 15:33
 */
@Description(
        name = "ExampleUDF",
        value = "return higher case version of the input string",
        extended = "select ExampleUDF(Sname) from stu_buck limit 10;"
)
public class ExampleUDF extends UDF {
    public String evaluate(String va){
        if(va==null){
            return null;
        }
        return va.toUpperCase() + "|" + "GO";
    }
}