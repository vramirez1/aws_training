<%@ page contentType="text/html;charset=UTF-8" language="java"
  isELIgnored="false"%>

<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>

<%@ page import="com.amazonaws.AmazonClientException"%>
<%@ page import="com.amazonaws.AmazonServiceException"%>
<%@ page import="com.amazonaws.auth.AWSCredentials"%>
<%@ page import="com.amazonaws.auth.profile.ProfileCredentialsProvider"%>
<%@ page import="com.amazonaws.regions.Region"%>
<%@ page import="com.amazonaws.regions.Regions"%>
<%@ page import="com.amazonaws.services.s3.AmazonS3"%>
<%@ page import="com.amazonaws.services.s3.AmazonS3Client"%>
<%@ page import="com.amazonaws.services.s3.model.Bucket"%>
<%@ page import="com.amazonaws.services.s3.model.GetObjectRequest"%>
<%@ page import="com.amazonaws.services.s3.model.ListObjectsRequest"%>
<%@ page import="com.amazonaws.services.s3.model.ObjectListing"%>
<%@ page import="com.amazonaws.services.s3.model.PutObjectRequest"%>
<%@ page import="com.amazonaws.services.s3.model.S3Object"%>
<%@ page import="com.amazonaws.services.s3.model.S3ObjectSummary"%>

<%@ page import="com.amazonaws.auth.AWSCredentialsProvider"%>
<%@ page import="com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider"%>
<%@ page import="com.softtek.practice.ams.cloud.UploadS3Servlet"%>


<%@ page import="com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.AttributeDefinition"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.AttributeValue"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.ComparisonOperator"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.Condition"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.CreateTableRequest"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.DescribeTableRequest"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.KeySchemaElement"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.KeyType"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.PutItemRequest"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.PutItemResult"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.ScalarAttributeType"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.ScanRequest"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.ScanResult"%>
<%@ page import="com.amazonaws.services.dynamodbv2.model.TableDescription"%>
<%@ page import="com.amazonaws.services.dynamodbv2.util.Tables"%>

<%@ page import="com.amazonaws.regions.Region"%>
<%@ page import="com.amazonaws.regions.Regions"%>


<!DOCTYPE html>

<head>
<script type="text/javascript">
function onFileSelected() {
  filename = document.getElementById("input-file").value;
  if (filename == null || filename == "") {
    document.getElementById("btn-post").setAttribute("class", "inactive btn");
    document.getElementById("btn-post").disabled = true;
  } else {
    document.getElementById("btn-post").setAttribute("class", "active btn");
    document.getElementById("btn-post").disabled = false;
  }
}

function togglePhotoPost(expanded) {
  onFileSelected();
  if (expanded) {
    document.getElementById("btn-choose-image").style.display="none";
    document.getElementById("upload-form").style.display="block";
  } else {
    document.getElementById("btn-choose-image").style.display="inline-block";
    document.getElementById("upload-form").style.display="none";
  }
}

function onCommentChanged(id) {
  comment = document.getElementById("comment-input-" + id).value;
  if (comment == null || comment.trim() == "") {
    document.getElementById("comment-submit-" + id).setAttribute("class", "inactive btn");
    document.getElementById("comment-submit-" + id).disabled = true;
  } else {
    document.getElementById("comment-submit-" + id).setAttribute("class", "active btn");
    document.getElementById("comment-submit-" + id).disabled = false;
  }
}

function toggleCommentPost(id, expanded) {
  onCommentChanged(id);
  if (expanded) {
    document.getElementById("comment-input-" + id).setAttribute("class", "post-comment expanded");
    document.getElementById("comment-submit-" + id).style.display="inline-block";
    document.getElementById("comment-cancel-" + id).style.display="inline-block";
  } else {
    document.getElementById("comment-input-" + id).value = ""
    document.getElementById("comment-input-" + id).setAttribute("class", "post-comment collapsed");
    document.getElementById("comment-submit-" + id).style.display="none";
    document.getElementById("comment-cancel-" + id).style.display="none";
  }
}
</script>
<title>Photofeed</title>
<link rel="stylesheet" type="text/css" href="photofeed.css" />
</head>
<%
        
 
		Bucket bucket;
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
		AmazonS3 s3     = new AmazonS3Client(credentialsProvider);
		
		AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(credentialsProvider);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	    dynamoDB.setRegion(usWest2);
		
		/*
      	* List the buckets in your account
      	*/
		
      	/*
		for (Bucket buck : s3.listBuckets()) {
          System.out.println(" - " + buck.getName());
          if (buck.getName().equals(UploadS3Servlet.PHOTO_SHARING_BUCKET_NAME)){
         	 bucket = buck;
         	 break;
          }
      }			
*/

		ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
		.withBucketName(UploadS3Servlet.PHOTO_SHARING_BUCKET_NAME)
		.withPrefix("Photo"));
		
		
%>

<body>
  <div class="wrap">

    <div class="header group">
      <h1>
        <img src="img/photofeed.png" alt="Photofeed" />
      </h1>
      <div class="glow"></div>
    </div>

    <div id="upload-wrap">
      <div id="upload">
        <div class="account group">
          <div id="account-thumb">
            <img
              src="<%-- ServletUtils.getUserIconImageUrl(currentUser.getUserId()) --%>"
              alt="Unknown User Icon" />
          </div>
          <!-- /#account-thumb -->
          <div id="account-name">
            <h2><%--ServletUtils.getProtectedUserNickname(currentUser.getNickname())*/ --%></h2>
            <p><%--currentUser.getEmail()*/ --%>
              | <a
                href=<%-- userService.createLogoutURL(configManager.getMainPageUrl())--%>>Sign
                out</a>
            </p>
          </div>
          <!-- /#account-name -->
        </div>
        <!-- /.account -->
        <a id="btn-choose-image" class="active btn" onclick="togglePhotoPost(true)">Choose an image</a>
        <div id="upload-form" style="display:none">
          <form action="Upload" method="post"
            enctype="multipart/form-data">
            <input id="input-file" class="inactive file btn" type="file" name="photo"
              onchange="onFileSelected()">
            <textarea name="title" placeholder="Write a description"></textarea>
            <input id="btn-post" class="active btn" type="submit" value="Post">
            <a class="cancel" onclick="togglePhotoPost(false)">Cancel</a>
          </form>
        </div>
      </div>
      <!-- /#upload -->
    </div>
    <!-- /#upload-wrap -->

    <!-- KK -->
 <%
 int count = 0;
 for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
	 String firstClass = "";
     String lastClass = "";
     
     if (count == 0) {
       firstClass = "first";
     }
     if (count == objectListing.getObjectSummaries().size() - 1) {
       lastClass = "last";
     }
		System.out.println(" - " + objectSummary.getKey() + "  " + 	 "(size = " + objectSummary.getSize() + ")"); %>
    <div class="feed <%= firstClass %> <%= lastClass %>">
      <div class="post group">
        <div class="image-wrap">
          <img class="photo-image"
            src="ViewPhoto?photoId=<%= objectSummary.getKey()%>"
            alt="Photo Image" />
        </div>
        <div class="owner group">
          <img src="<%--ServletUtils.getUserIconImageUrl(photo.getOwnerId())--%>"
            alt="" />
          <div class="desc">
            <h3><%--ServletUtils.getProtectedUserNickname(photo.getOwnerNickname()) --%></h3>
            <p><!--  -->
            <p>
            <p class="timestamp"><%--ServletUtils.formatTimestamp(photo.getUploadTime()) --%></p>
          </div>
          <!-- /.desc -->
        </div>
        <!-- /.usr -->
      </div>
      <!-- /.post -->
      <%
   	// Scan items for movies with a year attribute greater than 1985
      HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
      Condition condition = new Condition()
          .withComparisonOperator(ComparisonOperator.EQ)
          .withAttributeValueList(new AttributeValue(objectSummary.getKey()));
      scanFilter.put("photoId", condition);
      ScanRequest scanRequest = new ScanRequest(UploadS3Servlet.PHOTO_SHARING_BUCKET_NAME).withScanFilter(scanFilter);
      ScanResult scanResult = dynamoDB.scan(scanRequest);
      
    
      System.out.println("Result: " + scanResult);
     // System.out.println("Items: " + scanResult.getItems() );
      
      List<Map<String, AttributeValue>> commentsLst =  scanResult.getItems();
      
      for(Map<String, AttributeValue> comment :commentsLst){
    	//  System.out.println("Comment:"+ comment.get("photoTitle").getS());
    	 /* for(AttributeValue textcomment: comment.values()){
    		  System.out.println("Values ->" + textcomment.getS());
    	  }
    	 */
      
    %>
      <div class="post group">
        <div class="usr">
          <img
            src="<%--ServletUtils.getUserIconImageUrl(comment.getCommentOwnerId()) --%>"
            alt="" />
          <div class="comment">
            <h3><%--ServletUtils.getProtectedUserNickname(comment.getCommentOwnerName()) --%></h3>
            <p><%= comment.get("photoTitle").getS() %>
            <p>
            <p class="timestamp"><%-- ServletUtils.formatTimestamp(comment.getTimestamp()) --%></p>
          </div>
          <!-- /.comment -->
        </div>
        <!-- /.usr -->
      </div>
      <!-- /.post -->

      <%
        }
    %>
      <div class="post group">
        <div class="usr last">
          <img
            src="<%--ServletUtils.getUserIconImageUrl(currentUser.getUserId())--%>"
            alt="" />
          <div class="comment">
            <h3><%--ServletUtils.getProtectedUserNickname(currentUser.getNickname()) --%></h3>
            <form action="AddComment"
              method="post">
              <input type="hidden" name="user" value="<%-- photo.getOwnerId()--%>" />
              <input type="hidden" name="photoId" value="<%= objectSummary.getKey()%>" />
              <textarea id="comment-input-<%=count%>" class="post-comment collapsed" name="comment"
                placeholder="Post a comment" onclick="toggleCommentPost(<%=count%>, true)"
                onchange="onCommentChanged(<%=count%>)"
                onkeyup="onCommentChanged(<%= count%>)" onPaste="onCommentChanged(<%= count%>)"></textarea>
              <input id="comment-submit-<%=count %>" class="inactive btn" style="display:none"
                type="submit" name="send" value="Post comment">
              <a id="comment-cancel-<%=count%>" class="cancel" style="display:none"
                onclick="toggleCommentPost(<%=count%>, false)">Cancel</a>
            </form>
          </div>
          <!-- /.comment -->
        </div>
        <!-- /.usr -->
      </div>
      <!-- /.post -->
    </div>
    <!-- /.feed -->
    <%
    	count++;
      }
    %>
  </div>
  <!-- /.wrap -->
</body>
</html>
