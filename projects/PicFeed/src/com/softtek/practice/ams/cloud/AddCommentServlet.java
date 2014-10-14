package com.softtek.practice.ams.cloud;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3Client;


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
 * Servlet implementation class AddCommentServlet
 */
public class AddCommentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	  private AmazonDynamoDBClient dynamoDB;
	  
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddCommentServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String photoId = request.getParameter("photoId");
		String comment = request.getParameter("comment");
		
		if (dynamoDB== null){
			AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
		
			 dynamoDB = new AmazonDynamoDBClient(credentialsProvider);
			 Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		        dynamoDB.setRegion(usWest2);
		        
		}
		
		
		//Add new Item as Photo Comments
		 Map<String, AttributeValue> itemDynamo = newItem(photoId,comment);
         PutItemRequest putItemRequest = new PutItemRequest(UploadS3Servlet.PHOTO_SHARING_BUCKET_NAME, itemDynamo);
         PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
         response.sendRedirect("index.jsp");
	}
	
	 private static Map<String, AttributeValue> newItem(String photoId,String photoTitle) {
	        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
	        item.put("photoId", new AttributeValue(photoId));
	        item.put("photoTitle", new AttributeValue(photoTitle));
	       

	        return item;
	    }


}
