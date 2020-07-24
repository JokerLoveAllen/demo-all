"""
hdfs dfs -put /data/hbase/batch_file/20191224/20191224_2.csv /bulk/input/
"""
import os, stat, sys, datetime, time, random
from io import open

baseCmd = "hdfs dfs -put {} /bulk/input/"
baseCmd1 = """
HADOOP_CLASSPATH=/opt/cloudera/parcels/CDH/lib/hbase/lib/hbase-protocol-1.2.0-cdh5.16.2.jar:/opt/cloudera/parcels/CDH/lib/hbase/conf hadoop jar /opt/cloudera/parcels/CLABS_PHOENIX/lib/phoenix/./phoenix-4.7.0-clabs-phoenix1.3.0-client.jar org.apache.phoenix.mapreduce.CsvBulkLoadTool -t MY_SCHEMA.GOODS_PUSH -i /bulk/input/{} -z hadoop01,hadoop02,hadoop03
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

def bulkload(pathname):
    os.system(baseCmd1.format(str(pathname)[pathname.rindex("/")+1:]))
    print("({})upload to HBASE finished".format(str(pathname)[pathname.rindex("/")+1:]))

def changeId(pathname):
    offset = [0]
    filename = str(pathname)[pathname.rindex("/") + 1:]
    # filename = str(pathname)[pathname.rindex("\\") + 1:]
    print(filename)
    currDate, batchId = filename[:filename.index("_")], filename[filename.index("_")+1:filename.index(".")]
    currDate = currDate[6:] + currDate[4:6] + currDate[:4]
    batchId = int(batchId) + 1
    batchId = "0" + str(batchId) if batchId < 10 else str(batchId)
    def genId():
        offset[0] += 1
        return int("{}{}{}000000".format(random.randint(1, 60), batchId, currDate)) + offset[0]
    with open(pathname, "r+",encoding="utf8") as f:
        ls = f.readlines()
        f.seek(0, 0)
        for l in ls:
            l = str(genId()) + l[l.index(","):]
            f.writelines(l)
## get batch file name and execute it
if __name__ == '__main__':
    p = datetime.datetime.now()
    curr = sys.argv
    assert len(curr) > 1, "must have param"
    mode = str(curr[1])
    if mode == "1":
        walktree(curr[2], up2HDFS)
    elif mode == "2":
        walktree(curr[2], bulkload)
    else:
        print("not suppoted")
        #walktree(curr[2], changeId)
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
