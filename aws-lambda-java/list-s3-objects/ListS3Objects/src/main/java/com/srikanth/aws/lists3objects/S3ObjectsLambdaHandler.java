package com.srikanth.aws.lists3objects;

import java.util.Iterator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3ObjectsLambdaHandler implements RequestHandler<Object, String> {

	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

	@Override
	public String handleRequest(Object input, Context context) {
		context.getLogger().log("Received input: " + input);
		String bucketName = "srikanth-naidu65";

		try {
			StringBuffer returnValue = new StringBuffer("Your Amazon S3 bucket objects: ");
			ObjectListing objectListing = s3.listObjects(bucketName);
			while (true) {
				for (Iterator<?> iterator = objectListing.getObjectSummaries().iterator(); iterator.hasNext();) {
					S3ObjectSummary summary = (S3ObjectSummary)iterator.next();
					returnValue.append(summary.getKey());
					returnValue.append(", ");
				}

				// more object_listing to retrieve?
				if (objectListing.isTruncated()) {
					objectListing = s3.listNextBatchOfObjects(objectListing);
				} else {
					break;
				}
			}
			return returnValue.substring(0, returnValue.length()-2);
		} catch (Exception e) {
			context.getLogger().log("Error listing bucket objects!");
			throw e;
		}
	}

}
