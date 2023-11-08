import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndexBigrams {
    public static class BigramMapper extends Mapper<Object, Text, Text, LongWritable>
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
            String[] text_list = text_data.split(" ");

            for(int i=0; i<text_list.length-1; i++)
            {
                word.set(text_list[i] + " " + text_list[i+1]);
                context.write(word, id);
            }
        }
    }

    public static class BigramReducer extends Reducer<Text,LongWritable,Text,Text>
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
        job.setJarByClass(InvertedIndexBigrams.class);
        job.setJobName("Inverted Index Bigrams");
        job.setMapperClass(BigramMapper.class);
        job.setReducerClass(BigramReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
