# pe-hadoop-mapreduce
Performance evaluation of [Hadoop MapReduce](https://hadoop.apache.org/docs/r1.2.1/mapred_tutorial.html) framework using different test cases on Windows platform. MapReduce framework with [HDFS](https://csup.soc.pdn.ac.lk/) (Hadoop Distributed File System) is one of the ways to deal with big data storage and retrieval, it can be used in different platforms/cloud services such as [AWS](https://aws.amazon.com/). For this experiment we use [k-means](https://en.wikipedia.org/wiki/K-means_clustering) algorithm using MapReduce framework implemented in Java and compare the result of clustering for varying size of datasets. The result demonstrates that proposed k-means obtains higher performance and outperformed while clustering large datasets. Here we have used different sizes of the data to analysis the performance of MapReduce framework using a single node cluster in a local machine. We also deployed the tool on AWS.
## Setup Hadoop on Windows

- Download Hadoop, [Hadoop 3.2.1](https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.2.1/hadoop-3.2.1.tar.gz), unzip, and store it.
- Download Hadoop native IO binary from the [`winutils` repository](https://github.com/cdarlint/winutils) and copy the contents of `hadoop-3.2.1/bin` into the previously  extracted Hadoop binary package.
- Configure environment variables: Create new user variable `JAVA_HOME` and `HADOOP_HOME` and set the Java Jdk and Hadoop binary paths respectively. And set the paths of Jdk/bin and Hadoop/bin folders to the enironment variable `PATH`.
- Configure Hadoop: Configure files `core-site.xml`, `hdfs-site.xml`, and `mapred-site.xml` that are exsit in `%HADOOP_HOME%\etc\hadoop`
- - Update `configuration` element of the `core-site.xml` as follows,
```
<configuration>
  <property>
    <name>fs.default.name</name>
    <value>hdfs://0.0.0.0:19000</value>
  </property>
</configuration>
```
- - Update `configuration` element of the `mapred-site.xml` as follows,
```
<configuration>
  <property>
    <name>mapreduce.framework.name</name>
    <value>yarn</value>
  </property>
  <property> 
    <name>mapreduce.application.classpath</name>
    <value>
%HADOOP_HOME%/share/hadoop/mapreduce/*,%HADOOP_HOME%/share/hadoop/mapreduce/lib/*,%HADOOP_HOME%/share/hadoop/common/*,%HADOOP_HOME%/share/hadoop/common/lib/*,%HADOOP_HOME%/share/hadoop/yarn/*,%HADOOP_HOME%/share/hadoop/yarn/lib/*,%HADOOP_HOME%/share/hadoop/hdfs/*,%HADOOP_HOME%/share/hadoop/hdfs/lib/*
    </value>
  </property>
</configuration>
```
- - Update `configuration` element of the `yarn-site.xml` as follows,
```
<configuration>
  <property>
    <name>yarn.resourcemanager.hostname</name>
    <value>localhost</value>
  </property>
  <property>
    <name>yarn.nodemanager.aux-services</name>
    <value>mapreduce_shuffle</value>
  </property>
  <property>
    <name>yarn.nodemanager.env-whitelist</name>
    <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
  </property>
</configuration>
```
- - Create two directories for `namenode directory` and `data directory` and update `configuration` element of the `hdfs-site.xml` as follows to configure those directories,
```
<configuration>
   <property>
    <name>dfs.replication</name>
    <value>1</value>
  </property>
  <property>
    <name>dfs.namenode.name.dir</name>
    <value>file:///C:/hadoop-3.2.1/data/dfs/namenode</value>
  </property>
  <property>
    <name>dfs.datanode.data.dir</name>
    <value>file:///C:/hadoop-3.2.1/data/dfs/data</value>
  </property>
</configuration>
```
- Start services
```
Initialize HDFS:
$ hdfs namenode -format
Start HDFS daemons:
$ %HADOOP_HOME%\sbin\start-dfs.cmd
Start YARN daemons:
$ %HADOOP_HOME%\sbin\start-yarn.cmd
```
## Compile our project and Run mapreduce jobs
### Set environment variables `HADOOP_CP` and `HDFS_LOC`
```
1. Get hadoop classpaths
$ hadoop classpath
2. Copy the paths obtained from the previous command and set to the environment variables `HADOOP_CP`
3. Create an environment variable `HDFS_LOC` and set the value as `hdfs://localhost:19000` 
```
### Compile the k-means test codes and run on hadoop-mapreduce
```
$ cd pe-hadoop-mapreduce\src\k-means
$ hadoop fs -ls %HDFS_LOC%/
$ hadoop fs -copyFromLocal C:\pe-hadoop-mapreduce\data\k-means %HDFS_LOC%/df_250
$ hadoop fs -ls %HDFS_LOC%/df_250
$ javac -cp %HADOOP_CP% C:\pe-hadoop-mapreduce\src\k-means\*.java -d classes
$ cd classes
$ jar -cvf Kmeans.jar *.class
$ dir
$ hadoop jar Kmeans.jar Kmeans %HDFS_LOC%/df_250 %HDFS_LOC/output_df_250
$ hadoop fs -cat %HDFS_LOC%/output_df_250/centroids.txt
$ hadoop fs -copyToLocal %HDFS_LOC%/output_df_250 C:\pe-hadoop-mapreduce\results\k-means\
```

### Sample Result
```
7.94537,0.58977,0.18093,2.13241,0.08138,8.9537,26.53704,0.99685,3.35426,0.62824,9.79908
7.73247,0.53065,0.24974,2.1013,0.12039,16.98701,58.67533,0.99657,3.3161,0.7087,9.87273
```
