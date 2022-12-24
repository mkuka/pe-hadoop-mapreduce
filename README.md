# pe-hadoop-mapreduce
Performance Evaluation of Hadoop MapReduce Framework
## Setup Hadoop on Windows
`#RRGGBB`
```
Download Hadoop, [Hadoop 3.2.1](https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.2.1/hadoop-3.2.1.tar.gz), unzip, and store it.
Download Hadoop native IO binary from the [`winutils` repository](https://github.com/cdarlint/winutils) and copy the contents of `hadoop-3.2.1/bin` into the previously  extracted Hadoop binary package.
Configure environment variables: Create new user variable `JAVA_HOME` and `HADOOP_HOME` and set the Java Jdk and Hadoop binary paths respectively. And set the paths of Jdk/bin and Hadoop/bin folders to the enironment variable `PATH`.
Configure Hadoop: Configure files `core-site.xml`, `hdfs-site.xml`, and `mapred-site.xml` that are exsit in `%HADOOP_HOME%\etc\hadoop`.




```
