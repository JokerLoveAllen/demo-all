"""
hdfs dfs -put /data/hbase/batch_file/20191224/20191224_2.csv /bulk/input/
"""
import os, stat, sys, datetime, time, random
from io import open

baseCmd = "hdfs dfs -put {} /fenxiang/search/input/"
baseCmd1 = """
hadoop jar ./{} \
{} /fenxiang/search/input /fenxiang/search/output
"""

def walktree(top,callback):
        for f in os.listdir(top):
                pathname = os.path.join(top, f)
                mode = os.stat(pathname).st_mode
                if stat.S_ISDIR(mode):
                        walktree(pathname,callback)
                elif stat.S_ISREG(mode):
                        #if pathname.endswith("java"):
                        callback(pathname)
                else:
                        pass

def up2HDFS(pathname):
    #print("tell me why ?!?")
    os.system(baseCmd.format(pathname))
    print("({})upload to HDFS finished".format(str(pathname)[pathname.rindex("/")+1:]))

# get batch file name and execute it
if __name__ == '__main__':
    p = datetime.datetime.now()
    curr = sys.argv
    assert len(curr) > 1, "must have param"
    mode = str(curr[1])
    assert mode in ['1','2','3'], "only mode [1 upload2HDFS, 2 hadoop MR,3 hadoopMR with Hbase]"
    if mode == "1":
        assert len(curr) > 2, "must have data directory"
        os.system("hdfs dfs -rm /fenxiang/search/input/*")
        walktree(curr[2], up2HDFS)
    else:
        jarFile = 'hadoop_mapred-1.0-jar-with-dependencies.jar'
        mainClazz = 'com.fenxiang.bigdata.hadoop.SearchLogTask'
        #print curr
        if mode=='3':
           print 'use: hbase load'
           mainClazz = 'com.fenxiang.bigdata.hadoop.SearchLogWithHbaseTask'
           jarFile = 'hadoop_mapred-2.0-jar-with-dependencies.jar'
        os.system(baseCmd1.format(jarFile,mainClazz))
    #walktree("D:/Hbase_Release/batch_file", changeId)
    gap = datetime.datetime.now() - p
    sec = gap.seconds
    minute = 60
    hour = 60 * minute
    day = 24 * hour
    runDay = sec // day
    runHour = (sec % day) // hour
    runMinute = (sec % hour) // minute
    print("({})day({})hour({})minute".format(runDay, runHour, runMinute))
