# AWS-Image-and-Text-Rekognition
Algorithm to detect Image and Text on it using AWS Rekognition, S3 and EC2.
![0_kbeKZGAZYZmf_WQi](https://user-images.githubusercontent.com/77020328/116316561-21397c00-a780-11eb-85f6-4d5d31035e32.png)

We start with creating and initializing ec2 instances that virtual machines from the AWS panel.

Once my two EC@ instances for up and running I proceeded to install Java on both my virtual.

The images on which we had to do image and text detection were in the form of a AWS S3 bucket. So to access the bucket I had to create my IM rolls for both EC2 instances so that they could both access the S3 bucket.

Once the I am roll for the bucket was set up I then proceeded to set up one more I am roll for both my ec2 instances so that they can access the functionality of AWS recognition services as well.

Finally once everything was set up then we use our primary instance which we call as easy to instance to use recognition services to detect all the images that have cars in them

We would then store index numbers for all the images with cars in our simple Q service that is AWS sqs

Once we had first the day to answer the index numbers of the images in argue that then our second instance which is easy to be instance we can use the squares to fetch images with cars in them from the S3 bucket

Once we fix those images from the sqs then again we use the AWS recognition services using same ec2 instance to extract the text from the number plates on those cars

Lastly on the console we would get the index number of image and the text on it as output

