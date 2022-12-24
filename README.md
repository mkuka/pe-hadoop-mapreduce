# pe-hadoop-mapreduce
Performance Evaluation of Hadoop MapReduce Framework
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
- Compile our project and Run mapreduce jobs
