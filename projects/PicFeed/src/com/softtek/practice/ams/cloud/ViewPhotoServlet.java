package com.softtek.practice.ams.cloud;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Servlet implementation class ViewPhotoServlet
 */
public class ViewPhotoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AmazonS3 s3;
	private Bucket bucket;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ViewPhotoServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String key = request.getParameter("photoId");
		
		if (s3 == null) {
			AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
			s3 = new AmazonS3Client(credentialsProvider);

		}

		//Search the Image by keyId
		S3Object object = s3.getObject(new GetObjectRequest(
				UploadS3Servlet.PHOTO_SHARING_BUCKET_NAME, key));
		
		ServletOutputStream out = null;
		
		// Set a response content type
		response.setContentType(object.getObjectMetadata().getContentType());

		// Setup the output stream for the return XML data
		out = response.getOutputStream();
		InputStream input = object.getObjectContent();
		OutputStream output = response.getOutputStream();

		try {

			response.setContentLength((int) object.getObjectMetadata()
					.getContentLength());

			byte[] buffer = new byte[10240];

			//Flush the Image Bytes
			for (int length = 0; (length = input.read(buffer)) > 0;) {
				output.write(buffer, 0, length);
			}

			

		} catch (Exception e) {

			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (IOException ignore) {
			}
			try {
				input.close();
			} catch (IOException ignore) {
			}
		}
	}

}
