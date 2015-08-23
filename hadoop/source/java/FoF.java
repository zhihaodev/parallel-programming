import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class FoF {

	public static class FriendMapper extends Mapper<Object, Text, Text, Text>{

		private Text keyOutput = new Text();
		private Text valueOutput = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			int user = Integer.parseInt(itr.nextToken());
			ArrayList<Integer> friends = new ArrayList<Integer>();
			
			while (itr.hasMoreTokens()) {
				String friend = itr.nextToken();
				friends.add(Integer.parseInt(friend));
			}
			
			for (int i = 0; i < friends.size(); i++) {
				for (int j = i + 1; j < friends.size(); j++) {
					
					int a, b, c;
					
					if (friends.get(i) < friends.get(j)) {
						if (user < friends.get(i)) {
							a = user;
							b = friends.get(i);
							c = friends.get(j);
						}
						else if (user > friends.get(j)) {
							a = friends.get(i);
							b = friends.get(j);
							c = user;
						}
						else {
							a = friends.get(i);
							b = user;
							c = friends.get(j);
						}
					} else {
						if (user < friends.get(j)) {
							a = user;
							b = friends.get(j);
							c = friends.get(i);
						}
						else if (user > friends.get(i)) {
							a = friends.get(j);
							b = friends.get(i);
							c = user;
						}
						else {
							a = friends.get(j);
							b = user;
							c = friends.get(i);
						}
					}
					
					keyOutput.set(a + " " + b + " " + c);
					valueOutput.set(user + "");
					context.write(keyOutput, valueOutput);
					keyOutput.set(b + " " + a + " " + c);
					valueOutput.set(user + "");
					context.write(keyOutput, valueOutput);
					keyOutput.set(c + " " + a + " " + b);
					valueOutput.set(user + "");
					context.write(keyOutput, valueOutput);

				}
			}
			
		}
	}
	
	public static class FriendReducer extends Reducer<Text, Text, Text, Text> {

		private Text keyOutput = new Text();
		private Text valueOutput = new Text();
		
		public void reduce(Text key, Iterable<Text> values,
				Context context
				) throws IOException, InterruptedException {
			
			int num = 0;
			
			for (Text val : values) {
				num++;
			}
			
			if (num == 3) {
				StringTokenizer itr = new StringTokenizer(key.toString());
				String key1 = itr.nextToken();
				String key2 = itr.nextToken();
				String key3 = itr.nextToken();
				keyOutput.set(key1);
				valueOutput.set(key2 + " " + key3);
				context.write(keyOutput, valueOutput);		
			}
			
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		
//		conf.addResource(new Path("/Users/zhihao/hadoop-2.2.0/etc/hadoop/core-site.xml"));
//		conf.addResource(new Path("/Users/zhihao/hadoop-2.2.0/etc/hadoop/hdfs-site.xml"));
		
		Job job = Job.getInstance(conf, "Three's Company");
		job.setJarByClass(FoF.class);
		job.setMapperClass(FriendMapper.class);
		job.setReducerClass(FriendReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
		if (!job.waitForCompletion(true))
			return;
	}

}
