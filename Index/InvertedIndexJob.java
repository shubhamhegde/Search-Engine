import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class InvertedIndexJob {
    public static class InvertedIndexMapper extends Mapper<Object, Text, Text, LongWritable>
    {
        private Text word = new Text();
        private LongWritable id = new LongWritable();
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            String inputText = value.toString();
            String input[] = inputText.split("\t", 2);
            String document_id = input[0];
            id.set(Long.parseLong(document_id));
            String text_data = input[1].toLowerCase().replaceAll("[^a-z]+", " ");
            StringTokenizer tokenizer = new StringTokenizer(text_data);

            while (tokenizer.hasMoreTokens())
            {
                word.set(tokenizer.nextToken());
                context.write(word, id);
            }
        }
    }

    public static class InvertedIndexReducer extends Reducer<Text,LongWritable,Text,Text>
    {
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException
        {
            HashMap<Long, Integer> map = new HashMap<Long, Integer>();

            for (LongWritable value : values)
            {
                Long id = value.get();
                map.put(id, map.getOrDefault(id,0)+1);
            }

            String reducer_value = "";
            for (Map.Entry<Long, Integer> item : map.entrySet())
            {
                reducer_value += item.getKey() + ":" + item.getValue() + " ";
            }
            context.write(key, new Text(reducer_value));
        }
    }

    public static void main(String[] args) throws Exception
    {
        Job job = new Job();
        job.setJarByClass(InvertedIndexJob.class);
        job.setJobName("Inverted Index");
        job.setMapperClass(InvertedIndexMapper.class);
        job.setReducerClass(InvertedIndexReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}