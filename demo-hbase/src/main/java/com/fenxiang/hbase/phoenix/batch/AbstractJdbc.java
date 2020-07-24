package com.fenxiang.hbase.phoenix.batch;

import java.io.Closeable;
import java.util.ResourceBundle;

public abstract class AbstractJdbc implements Closeable {
    protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("application");
     static {
         System.setProperty("hadoop.home.dir", "D:/Code/Phoenix/");
     }
}
