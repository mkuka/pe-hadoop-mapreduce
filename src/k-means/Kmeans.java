import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Kmeans {
	
	public static class Kmeans_map extends Mapper<LongWritable, Text, IntWritable, DataPoint> {

	    private DataPoint[] centroids;
	    private final DataPoint dataPoint = new DataPoint();
	    private final IntWritable centroid = new IntWritable();

	    public void setup(Context context) {
	    int k = 2; 

	    this.centroids = new DataPoint[k];
	    for(int i = 0; i < k; i++) {
	    String[] centroid = context.getConfiguration().getStrings("centroid." + i);
	    //System.out.println(centroid);
	     this.centroids[i] = new DataPoint(centroid);
	        }
	    }

	    public void map(LongWritable key, Text value, Context context) 
	     throws IOException, InterruptedException {
	        
	        String[] dataString = value.toString().split("[|,|]");
	        dataPoint.set(dataString);

	        float minDist = Float.POSITIVE_INFINITY;
	        float distance = 0.0f;
	        int nearest = -1;

	        for (int i = 0; i < centroids.length; i++) {
	            distance = dataPoint.findDist(centroids[i]);
	            if(distance < minDist) {
	                nearest = i;
	                minDist = distance;
	            }
	        }

	        centroid.set(nearest);
	        context.write(centroid, dataPoint);
	    }
	}
	
	public static class Kmeans_combine extends Reducer<IntWritable, DataPoint, IntWritable, DataPoint> {
	
	    public void reduce(IntWritable centroid, Iterable<DataPoint> DataPoints, Context context)throws IOException, InterruptedException {
	    //Sum the DataPoints
	    DataPoint point_sum = DataPoint.copy(DataPoints.iterator().next());
	    while (DataPoints.iterator().hasNext()) {
	    point_sum.sum(DataPoints.iterator().next());
	        }
	        
	    context.write(centroid, point_sum);
	    }
	}

	public static class Kmeans_reduce extends Reducer<IntWritable, DataPoint, Text, Text> {

	    private final Text centroidId = new Text();
	    private final Text centroidValue = new Text();
	    
	    public void reduce(IntWritable centroid, Iterable<DataPoint> partialSums, Context context)throws IOException, InterruptedException {
	        
	        DataPoint sum = DataPoint.copy(partialSums.iterator().next());
	        while (partialSums.iterator().hasNext()) {
	            sum.sum(partialSums.iterator().next());
	        }
	        sum.average();
	        
	        centroidId.set(centroid.toString());
	        centroidValue.set(sum.toString());
	        context.write(centroidId, centroidValue);
	    }
	}

	private static boolean terminatingCondition(DataPoint[] oldCentroids, DataPoint[] newCentroids,float threshold) {
		boolean check = true;
		for(int i = 0; i < oldCentroids.length; i++) {
			check = oldCentroids[i].findDist(newCentroids[i]) <= threshold;
			if(!check) {
				return false;
			}
		}
		return true;
	}

	private static DataPoint[] centroidsInit(Configuration conf, String pathString, int k, int dataSetSize) 
	  throws IOException {
		DataPoint[] dataPoints = new DataPoint[k];

		List<Integer> positions = new ArrayList<Integer>();
		Random random = new Random();
		int pos;
		while(positions.size() < k) {
			pos = random.nextInt(dataSetSize);
			if(!positions.contains(pos)) {
				positions.add(pos);
			}
		}
		Collections.sort(positions);
	
		Path path = new Path(pathString);
		FileSystem hdfs = FileSystem.get(path.toUri(), conf);
		FSDataInputStream in = hdfs.open(path);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		int row = 0;
		int i = 0;
		int position;
		while(i < positions.size()) {
			position = positions.get(i);
			String DataPoint = br.readLine();
			if(row == position) {    
				dataPoints[i] = new DataPoint(DataPoint.split("[|,|]"));  
				i++;
			}
			row++;
		}   
		br.close();
		
		return dataPoints;
	} 

	private static DataPoint[] readCentroids(Configuration conf, int k, String pathString)throws IOException, FileNotFoundException {
		DataPoint[] dataPoints = new DataPoint[k];
		Path path = new Path(pathString);
		FileSystem hdfs = FileSystem.get(path.toUri(),conf);
		FileStatus[] status = hdfs.listStatus(path);	
		
		for (int i = 0; i < status.length; i++) {
			if(!status[i].getPath().toString().endsWith("_SUCCESS")) {
				BufferedReader br = new BufferedReader(new InputStreamReader(hdfs.open(status[i].getPath())));
				String[] keyValueSplit = br.readLine().split("[|\t|]"); 
				int centroidId = Integer.parseInt(keyValueSplit[0]);
				String[] dataPoint = keyValueSplit[1].split("[|,|]");
				dataPoints[centroidId] = new DataPoint(dataPoint);
				br.close();
			}
		}
		hdfs.delete(path, true); 

		return dataPoints;
	}

	    private static void finalize(Configuration conf, DataPoint[] centroids, String output) throws IOException {
	    	Path path=new Path(output + "/centroids.txt");
	        FileSystem hdfs = FileSystem.get(path.toUri(),conf);
	        FSDataOutputStream dos = hdfs.create(path, true);
	        BufferedWriter br = new BufferedWriter(new OutputStreamWriter(dos));

	        for(int i = 0; i < centroids.length; i++) {
	            br.write(centroids[i].toString());
	            br.newLine();
	        }

	        br.close();
	        hdfs.close();
	    }

	    public static void main(String[] args) throws Exception {
	      
	        long start_time = 0;
	        long end_time = 0;
	        
	       
	        Configuration conf = new Configuration();
	        
	        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

	        if (otherArgs.length != 2) {
	            System.err.println("Usage: <input> <output>");
	            System.exit(1);
	        }

	        final String OUTPUT_FILE_NAME = "/clusters.txt";
	    	final String INPUT_FILE_NAME = "/data.csv";
	    	
	    	final String INPUT = otherArgs[0] +INPUT_FILE_NAME;
	        final String OUTPUT = otherArgs[1] +OUTPUT_FILE_NAME;
	    	
	        final int DIAMENTION = 11;
	        final int K = 2;
	        final float THRESHOLD = (float)0.01;
	        final int MAX_ITERATIONS = 10; 

	        DataPoint[] oldCentroids = new DataPoint[K];
	        DataPoint[] newCentroids = new DataPoint[K];

	        newCentroids = centroidsInit(conf, INPUT, K, DIAMENTION);
	        
	        for(int i = 0; i < K; i++) {
	            conf.set("centroid." + i, newCentroids[i].toString());
	        }

	        // --- MapReduce workflow --- //
	        boolean terminate = false;
	        boolean job_done = true;
	        int i = 0;
			start_time = System.currentTimeMillis();
	        while(!terminate) {
	            i++;

	            //--- Job configuration --- //
	            Job clustering_job = Job.getInstance(conf, "iter_" + i);
	            clustering_job.setJarByClass(Kmeans.class);
	            clustering_job.setMapperClass(Kmeans_map.class);
	            clustering_job.setCombinerClass(Kmeans_combine.class);
	            clustering_job.setReducerClass(Kmeans_reduce.class);  
	            clustering_job.setNumReduceTasks(K);             
	            clustering_job.setOutputKeyClass(IntWritable.class);
	            clustering_job.setOutputValueClass(DataPoint.class);
	            FileInputFormat.addInputPath(clustering_job, new Path(INPUT));
	            FileOutputFormat.setOutputPath(clustering_job, new Path(OUTPUT));
	            clustering_job.setInputFormatClass(TextInputFormat.class);
	            clustering_job.setOutputFormatClass(TextOutputFormat.class);

	            job_done = clustering_job.waitForCompletion(true);

	            if(!job_done) {        
	                System.err.println("job" + i + "failed.");
	                System.exit(1);
	            }

	            for(int id = 0; id < K; id++) {
	                oldCentroids[id] = DataPoint.copy(newCentroids[id]);
	            }                        
	            newCentroids = readCentroids(conf, K, OUTPUT);

	            terminate = terminatingCondition(oldCentroids, newCentroids,THRESHOLD);

	            if(terminate || i == (MAX_ITERATIONS -1)) {
	                finalize(conf, newCentroids, otherArgs[1]);
	            } else {
	                for(int d = 0; d < K; d++) {
	                    conf.unset("centroid." + d);
	                    conf.set("centroid." + d, newCentroids[d].toString());
	                }
	            }
	        }

	        end_time = System.currentTimeMillis();
	        end_time -= start_time;
	        
	        System.out.println("execution time: " + end_time + " ms");	     
	        System.out.println("n_iter: " + i);
	        System.exit(0);
	    }
}



	
	
	
	

