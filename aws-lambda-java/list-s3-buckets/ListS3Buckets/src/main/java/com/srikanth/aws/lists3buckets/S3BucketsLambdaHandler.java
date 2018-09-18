package com.srikanth.aws.lists3buckets;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

public class S3BucketsLambdaHandler implements RequestHandler<Object, String> {

	private AmazonS3 s3 = AmazonS3ClientBuilder.standard().build();

	public S3BucketsLambdaHandler() {}

	// Test purpose only.
	S3BucketsLambdaHandler(AmazonS3 s3) {
		this.s3 = s3;
	}

	@Override
	public String handleRequest(Object event, Context context) {
		context.getLogger().log("Received event: " + event);

		try {
			StringBuffer returnValue = new StringBuffer("Your Amazon S3 buckets are: ");
			List<Bucket> buckets = s3.listBuckets();
			for (Bucket b : buckets) {
				returnValue.append(b.getName());
				returnValue.append(", ");
			}
			return returnValue.substring(0, returnValue.length()-2);
		} catch (Exception e) {
			context.getLogger().log("Error listing buckets!");
			throw e;
		}
	}
}