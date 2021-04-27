import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.eventstreaming.Message;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.TextDetection;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.File;

public class texdec {
	
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

    public static void main(String[] args) throws IOException {
       String bucket = "njit-cs-643";
       texdec dec = new texdec();
       Set<String> indexes = new HashSet<String>();
        AmazonSQS sqs = null;
       try {
	   sqs = AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(dec.SessionCredentials())).withRegion("us-east-1").build();

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("us-east-1").build();
        final ReceiveMessageRequest receiveMessageRequest =
                new ReceiveMessageRequest("https://sqs.us-east-1.amazonaws.com/043626042153/SQSQ1.fifo");
        List<com.amazonaws.services.sqs.model.Message> messages =sqs.receiveMessage(receiveMessageRequest).getMessages();
        System.out.println("Final output, after receiving.");
        while(messages.size()>0) {
        
       // System.out.format("Objects in S3 bucket %s:\n", bucket);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
        ListObjectsV2Result result = s3.listObjectsV2(bucket);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        FileWriter fw = new FileWriter("DetectedText.txt",true);// testing
        BufferedWriter bw = new BufferedWriter(fw);
        for (final com.amazonaws.services.sqs.model.Message message : messages) {

       // for (S3ObjectSummary os : objects) {
           // System.out.println("* " + os.getKey());
            DetectTextRequest request = new DetectTextRequest()
                    .withImage(new Image()
                    .withS3Object(new S3Object()
                   // .withName(os.getKey())
                    .withName(message.getBody())
                    .withBucket(bucket)));
    		//System.out.println("* " + message.getBody());

                DetectTextResult textresult = rekognitionClient.detectText(request);
                List<TextDetection> textDetections = textresult.getTextDetections();
              //  System.out.println("Detected lines and words for " + os.getKey());
                for (TextDetection text: textDetections) {
                	//if(text.getConfidence()>90) {
                		//System.out.println("* " + message.getBody());
                		//System.out.println(text.getDetectedText());
                		indexes.add(message.getBody());
                		bw.write(message.getBody());
                		bw.write("\t");
                		bw.write(text.getDetectedText());
                		bw.write("\n");
                		//bw.write();
                        //System.out.println("Detected: " + text.getDetectedText());
                      //System.out.println("Confidence: " + text.getConfidence().toString());
                        //System.out.println("Id : " + text.getId());
                        //System.out.println("Confidence: " + text.getConfidence());
                      //System.out.println("Parent Id: " + text.getParentId());
                      //System.out.println("Type: " + text.getType());
                       // System.out.println();
                	//}
                }
      //  }

          sqs.deleteMessage("https://sqs.us-east-1.amazonaws.com/043626042153/SQSQ1.fifo", message.getReceiptHandle() );
          messages =sqs.receiveMessage(receiveMessageRequest).getMessages();

        }

        }
        System.out.println(indexes.toString());

       }
             catch(AmazonRekognitionException e) {
                e.printStackTrace();
             }
            catch(Exception e) {
            	e.printStackTrace();
            }

            

       
                    
                }  
            
            
        }
    
