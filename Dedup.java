package HDFSToLocal;
import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Dedup {

	// map将输入中的value复制到输出数据的key上，并直接输出

	public static class Map extends Mapper<Object, Text, Text, Text> {

		private static Text line = new Text();// 每行数据

		// 实现map函数

		public void map(Object key, Text value, Context context)

		throws IOException, InterruptedException {

			line = value;

			context.write(line, new Text(""));

		}

	}

	// reduce将输入中的key复制到输出数据的key上，并直接输出

	public static class Reduce extends Reducer<Text, Text, Text, Text> {

		// 实现reduce函数

		public void reduce(Text key, Iterable<Text> values, Context context)

		throws IOException, InterruptedException {

			context.write(key, new Text(""));

		}
		public static IntWritable i=new IntWritable(0);

	}
	 

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
	//	conf.addResource("core-site.xml");
		 FileSystem hdfs=FileSystem.get(conf);
		System.out.println(conf.get("fs.defaultFS"));		// 这句话很关键
		System.out.println(conf.get("hadoop.tmp.dir"));	

		conf.set("mapred.job.tracker", "192.168.16.1:8021");

		String[] ioArgs = new String[] { "pl_in", "pl_out" };//目录是指usr/hadoop/pl_in

		String[] otherArgs = new GenericOptionsParser(conf, ioArgs)
				.getRemainingArgs();
		
		System.out.println(otherArgs[0]);
		System.out.println(otherArgs[1]);
		if (otherArgs.length != 2) {

			System.err.println("Usage: Data Deduplication <in> <out>");

			System.exit(2);

		}

		Job job = new Job(conf, "Data Deduplication");

		job.setJarByClass(Dedup.class);

		// 设置Map、Combine和Reduce处理类

		job.setMapperClass(Map.class);

		job.setCombinerClass(Reduce.class);

		job.setReducerClass(Reduce.class);

		// 设置输出类型

		job.setOutputKeyClass(Text.class);

		job.setOutputValueClass(Text.class);

		// 设置输入和输出目录

		FileInputFormat.addInputPath(job, new Path(
				"hdfs://192.168.16.1:8020/pl_test/pl_in"));

		FileOutputFormat.setOutputPath(job, new Path(
		"hdfs://192.168.16.1:8020/pl_test/pl_out"));
		//FileOutputFormat.setOutputPath(job, new Path(conf.get("fs.defaultFS")+"pl_test/"+otherArgs[1]));

		 System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
