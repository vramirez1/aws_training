package com.softtek.practice.ams.cloud;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

/**
 * Servlet implementation class UploadS3Servlet
 */
public class UploadS3Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AmazonS3 s3;
	private AmazonDynamoDBClient dynamoDB;
	private Bucket bucket;
	public static String PHOTO_SHARING_BUCKET_NAME = "photo-sharing-ams-demo";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadS3Servlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		
		if (s3 == null) {
			AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
			s3 = new AmazonS3Client(credentialsProvider);
			dynamoDB = new AmazonDynamoDBClient(credentialsProvider);
			Region usWest2 = Region.getRegion(Regions.US_WEST_2);
			dynamoDB.setRegion(usWest2);

			/*
			 * List the buckets in your account
			 */
			for (Bucket buck : s3.listBuckets()) {
				System.out.println(" - " + buck.getName());
				if (buck.getName().equals(PHOTO_SHARING_BUCKET_NAME)) {
					bucket = buck;
					break;
				}
			}
		}

		// Commons file upload classes are specifically instantiated
		FileItemFactory factory = new DiskFileItemFactory();

		ServletFileUpload upload = new ServletFileUpload(factory);
		ServletOutputStream out = null;

		try {
			// Parse the incoming HTTP request
			// Commons takes over incoming request at this point
			// Get an iterator for all the data that was sent
			List<FileItem> itemLst = upload.parseRequest(request);			

			/*
			 * Upload an object to your bucket - You can easily upload a file to
			 * S3, or upload directly an InputStream if you know the length of
			 * the data in the stream. You can also specify your own metadata
			 * when uploading to S3, which allows you set a variety of options
			 * like content-type and content-encoding, plus additional metadata
			 * specific to your applications.
			 */

			if (itemLst != null && !itemLst.isEmpty()) {
				String keyFile = "";

				for (FileItem item : itemLst) {
					if (item.isFormField()) {
						String title = item.getString();
						// Add an item
						Map<String, AttributeValue> itemDynamo = newItem(keyFile, title);
						PutItemRequest putItemRequest = new PutItemRequest(	PHOTO_SHARING_BUCKET_NAME, itemDynamo);
						PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
					} else {
						// FileItem item = itemLst.get(0);
						System.out.println("Uploading a new object to S3 from a file\n");
						keyFile = "Photo" + (new Date()).hashCode()	+ item.getName();
						File disk = new File(keyFile);
						item.write(disk);
						s3.putObject(new PutObjectRequest(PHOTO_SHARING_BUCKET_NAME, keyFile, disk));
						disk.delete();
					}

				}

				// out.println("File Uploaded Successfully");

			} else {
				// out.println("File Not Found");
			}

			response.sendRedirect("index.jsp");
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/*
	 * Create new Item for DynamoDB
	 */
	private static Map<String, AttributeValue> newItem(String photoId,
			String photoTitle) {
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("photoId", new AttributeValue(photoId));
		item.put("photoTitle", new AttributeValue(photoTitle));

		return item;
	}

}
