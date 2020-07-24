package com.fenxiang.hbase.mapred.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * @ClassName NaiveHbaseApi
 * @Author lqs
 * @Date 2020/6/2 17:20
 */
public class NaiveHbaseApi {
    Configuration config = HBaseConfiguration.create();
    Connection connection;
    Admin admin;
    
    //初始化提供  hbase ip 和 zk端口
    public NaiveHbaseApi(String ip, String port, String master) throws IOException {
        this.config.set("hbase.zookeeper.quorum", ip);
        this.config.set("hbase.zookeeper.property.clientPort", port);
//        this.config.set("hbase.master", master);
        this.connection = ConnectionFactory.createConnection(this.config);
        admin=connection.getAdmin();
    }


    //创建表（没查重）
    public void createTable(String tablename, String[] family) throws IOException {

        TableName tn = TableName.valueOf(tablename);

        if (!admin.tableExists(tn)) {
            HTableDescriptor htd = new HTableDescriptor(tn);
            String[] var5 = family;
            int var6 = family.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                String f = var5[var7];
                htd.addFamily(new HColumnDescriptor(f));
            }
            admin.createTable(htd);
            admin.close();
            //this.connection.close();  (再加关闭的函数)
            System.out.println("表：" + tablename + "创建成功");
        } else {
            System.out.println("表：" + tn + "已存在");
        }
    }

    //删除表
    public void deleteTable(String tablename) throws IOException {


        TableName tn = TableName.valueOf(tablename);
        try {
            if (admin.tableExists(tn)) {
                admin.disableTable(tn);
                admin.deleteTable(tn);
                System.out.println("删除成功");
            } else {

                System.out.println("删除失败 该表不存在");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    //查询所有表
    public TableName[] getTableList() throws IOException {

        TableName[] tableNames = admin.listTableNames();
        for (TableName tn : tableNames
        ) {
            System.out.println(tn);

        }

        return tableNames;

    }


    //查询数据 by  rowkey
    public Result getByRowKey(String tablename, String rowkey) throws IOException {

        TableName tn = TableName.valueOf(tablename);

        if (admin.tableExists(tn)) {
            System.out.println(tn+"表存在");
            Table table = connection.getTable(tn);
            Result result = table.get(new Get(Bytes.toBytes(rowkey)));
            System.out.println("开始查询");
            for (Cell cell:result.listCells()
            ) {
                System.out.println("rowkey:"+new String( CellUtil.cloneRow( cell ) ));
                System.out.println("family:"+new String( CellUtil.cloneFamily( cell ) ));
                System.out.println("qualifier:" + new String(CellUtil.cloneQualifier(cell)));

                System.out.println("value:"+new String( CellUtil.cloneValue( cell ) ));
                System.out.println();
            }
            return result;
        } else {
            System.out.println("表不存在");
            return null;
        }


    }

//    //插入数据
//    public void putData(TableArr arr) throws IOException {
//
//        TableName tn = TableName.valueOf(arr.getTablename());
//
//            Table table = connection.getTable(tn);
//            Put put =new Put(Bytes.toBytes(arr.getRowkey()));
//
//            put.addColumn(Bytes.toBytes(arr.getFamilyname()),Bytes.toBytes(arr.getColnumname()),Bytes.toBytes(arr.getValue()));
//            table.put(put);
//
//            System.out.println("插入成功");
//
//            //checkAndPut 方法   传入值与服务器上的数据比较  如果为真 就插入
//
//    }
//
//// 插入数据
//    public void incrData(TableArr arr) throws IOException {
//        TableName tn = TableName.valueOf(arr.getTablename());
//
//        Table table = connection.getTable(tn);
//        Long resut= table.incrementColumnValue( Bytes.toBytes(arr.getRowkey()), Bytes.toBytes(arr.getFamilyname()), Bytes.toBytes(arr.getColnumname()), arr.getValue() );
//          System.out.println( resut );
//
//
//    }
    
}
