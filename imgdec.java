import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class imgdec {
	
	 public  BasicSessionCredentials SessionCredentials() throws Exception
	{
	     String access_id, secret_key, session_token;
	      String str = new File(imgdec.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
	      File file = new File(str + "/credentials");
	     // String fileName = "/Users/sushantnarang/eclipse-workspace/AWSPROJ/credentials";

         // URI uri = this.getClass().getResource(fileName).toURI();

		  List<String> lines = Collections.emptyList(); 
	      lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8); 
	      access_id= lines.get(1).split("=")[1];
	      secret_key = lines.get(2).split("=")[1];
	      session_token = lines.get(3).split("=")[1];
	   
	      BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(access_id, secret_key, session_token);
	      
	      return sessionCredentials;

	}

   public static void main(String[] args) throws Exception {
	   
	   imgdec dec = new imgdec();
	   Set<String> indexes = new HashSet<String>();
	   
	   final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(dec.SessionCredentials())).withRegion("us-east-1").build();
      String bucket = "njit-cs-643";
      

      AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("us-east-1").build();
     // System.out.format("Objects in S3 bucket %s:\n", bucket);
      final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
      ListObjectsV2Result result = s3.listObjectsV2(bucket);
      List<S3ObjectSummary> objects = result.getObjectSummaries();
      for (S3ObjectSummary os : objects) {
         // System.out.println("* " + os.getKey());
    	 
      DetectLabelsRequest request = new DetectLabelsRequest()
           .withImage(new Image()
           .withS3Object(new S3Object()
           .withName(os.getKey()).withBucket(bucket)))
           .withMaxLabels(10)
           .withMinConfidence(75F);

      try {
         DetectLabelsResult result1 = rekognitionClient.detectLabels(request);
         List <Label> labels = result1.getLabels();

         for (Label label: labels) {
        	 if(label.getName().equals("Car")){
        		 System.out.println("Detected labels for " + os.getKey());
        	      indexes.add(os.getKey());

        		 System.out.println(label.getName() + ": " + label.getConfidence()+"\n");
         }}
      } catch(AmazonRekognitionException e) {
         e.printStackTrace();
      }
   }
      
      System.out.println(indexes.toString());
   // Send a message
      Iterator<String> it = indexes.iterator();
     // while(it.hasNext()){
for(String a:indexes) {

      System.out.println("Sending a message to MyFifoQueue.fifo.  " + a + "\n");
      final SendMessageRequest sendMessageRequest = new SendMessageRequest("https://sqs.us-east-1.amazonaws.com/043626042153/SQSQ1.fifo", a);

      // When you send messages to a FIFO queue, you must provide a non-empty MessageGroupId.
      sendMessageRequest.setMessageGroupId("message");

      // Uncomment the following to provide the MessageDeduplicationId
      sendMessageRequest.setMessageDeduplicationId(a);
      final SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
      final String sequenceNumber = sendMessageResult.getSequenceNumber();
      final String messageId = sendMessageResult.getMessageId();
      System.out.println("SendMessage succeed with messageId " + messageId + ", sequence number " + sequenceNumber + "\n");
  }
   }
}