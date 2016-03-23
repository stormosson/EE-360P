import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import java.util.HashMap;
import java.util.StringTokenizer;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

    // Replace "?" with your own output key / value types
    // The four template data types are:
    // <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static class TextMapper extends Mapper<LongWritable, Text, Text, HashMap<Text, LongWritable>> {

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            private final static LongWritable one = new LongWritable(1);
            private Text contextWord = new Text();
            private Text queryWord = new Text();
            StringTokenizer context_itr = new StringTokenizer(value.toString());
            /* context word, query word, count */
            HashMap<Text, HashMap<Text, LongWritable>> hash = 
                new HashMap<Text, HashMap<Text, LongWritable>>();

            while (context_itr.hasMoreTokens()) {
                contextWord.set(context_itr.nextToken());

                StringTokenizer query_itr = new StringTokenizer(value.toString());
                while (context_itr.countTokens() >= query_itr.countTokens()) {
                    query_itr.nextToken();  /* burn a word */
                }
                while(query_itr.hasMoreTokens()){
                    queryWord.set(query_itr.nextToken());
                    if (hash[contextWord] == null) {
                        hash[contextWord] = new HashMap<Text, LongWritable>();
                    }
                    if (hash[contextWord][queryWord] == null) {
                        hash[contextWord][queryWord] = one;
                    } else {
                        hash[contextWord][queryWord] += 1;
                    }
                }
            }
            for (context : hash.keySet()) {
                context.write(context, hash.get(context));
            }
        }
    }

	// Replace "?" with your own input key / value types, i.e., the output
	// key / value types of your mapper function
    public static class TextReducer extends Reducer<Text, HashMap<Text, LongWritable>, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, HashMap<Text, LongWritable> queryHash, Context context)
            throws IOException, InterruptedException {

            // Write out the current context key
            context.write(key, emptyText);
            // Write out query words and their count
            for (String queryWord : map.keySet()) {
                String count = map.get(queryWord).toString() + ">";
                queryWordText.set("<" + queryWord + ",");
                context.write(queryWordText, new Text(count));
            }
            // Empty line for ending the current context key
            context.write(emptyText, emptyText);
        }
    }

	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();

		// Create job
		Job job = new Job(conf, "esc625_wsm443"); // Replace with your EIDs
		job.setJarByClass(TextAnalyzer.class);

		// Setup MapReduce job
		job.setMapperClass(TextMapper.class);
		// Uncomment the following line if you want to use Combiner class
		// job.setCombinerClass(TextCombiner.class);
		job.setReducerClass(TextReducer.class);

		// Specify key / value types (Don't change them for the purpose of this
		// assignment)
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		// If your mapper and combiner's output types are different from
		// Text.class,
		// then uncomment the following lines to specify the data types.
		// job.setMapOutputKeyClass(?.class);
		// job.setMapOutputValueClass(?.class);

		// Input
		FileInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(TextInputFormat.class);

		// Output
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setOutputFormatClass(TextOutputFormat.class);

		// Execute job and return status
		return job.waitForCompletion(true) ? 0 : 1;
	}

	// Do not modify the main method
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
		System.exit(res);
	}
}
