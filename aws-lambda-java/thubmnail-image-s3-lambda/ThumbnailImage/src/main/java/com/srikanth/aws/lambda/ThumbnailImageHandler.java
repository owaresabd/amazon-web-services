package com.srikanth.aws.lambda;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Handler Class for Thumbnail Image.
 *
 * @author  Srikanth
 * @version 1.0
 * @since   2018-08-15
 */
public class ThumbnailImageHandler implements RequestHandler<S3Event, String> {

	private AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();

	public ThumbnailImageHandler() {

	}

	// For Testing purpose only
	public ThumbnailImageHandler(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}

	private static final float MAX_WIDTH = 100;
	private static final float MAX_HEIGHT = 100;
	private final String JPG_TYPE = "jpg";
	private final String JPG_MIME = "image/jpeg";
	private final String PNG_TYPE = "png";
	private final String PNG_MIME = "image/png";

	public String handleRequest(S3Event s3event, Context context) {
		try {
			S3EventNotificationRecord record = s3event.getRecords().get(0);

			String srcBucket = record.getS3().getBucket().getName();
			// Object key may have spaces or unicode non-ASCII characters.
			String srcKey = record.getS3().getObject().getKey()
					.replace('+', ' ');
			srcKey = URLDecoder.decode(srcKey, "UTF-8");

			String dstBucket = srcBucket + "-resized";
			String dstKey = "resized-" + srcKey;

			// Sanity check: validate that source and destination are different
			// buckets.
			if (srcBucket.equals(dstBucket)) {
				context.getLogger().log("Destination bucket must not match source bucket.");
				return "FAIL";
			}

			// Infer the image type.
			Matcher matcher = Pattern.compile(".*\\.([^\\.]*)").matcher(srcKey);
			if (!matcher.matches()) {
				context.getLogger().log("Unable to infer image type for key " + srcKey);
				return "FAIL";
			}
			String imageType = matcher.group(1);
			if (!(JPG_TYPE.equals(imageType)) && !(PNG_TYPE.equals(imageType))) {
				context.getLogger().log("Skipping non-image " + srcKey);
				return "SKIP";
			}

			// Download the image from S3 into a stream
			S3Object s3Object = amazonS3.getObject(new GetObjectRequest(
					srcBucket, srcKey));
			InputStream objectData = s3Object.getObjectContent();

			// Read the source image
			BufferedImage srcImage = ImageIO.read(objectData);
			int srcHeight = srcImage.getHeight();
			int srcWidth = srcImage.getWidth();
			// Infer the scaling factor to avoid stretching the image
			// unnaturally
			float scalingFactor = Math.min(MAX_WIDTH / srcWidth, MAX_HEIGHT	/ srcHeight);
			int width = (int) (scalingFactor * srcWidth);
			int height = (int) (scalingFactor * srcHeight);

			BufferedImage resizedImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resizedImage.createGraphics();
			// Fill with white before applying semi-transparent (alpha) images
			g.setPaint(Color.white);
			g.fillRect(0, 0, width, height);
			// Simple bilinear resize
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(srcImage, 0, 0, width, height, null);
			g.dispose();

			// Re-encode image to target format
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(resizedImage, imageType, os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			// Set Content-Length and Content-Type
			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentLength(os.size());
			if (JPG_TYPE.equals(imageType)) {
				meta.setContentType(JPG_MIME);
			}
			if (PNG_TYPE.equals(imageType)) {
				meta.setContentType(PNG_MIME);
			}

			// Uploading to S3 destination bucket
			context.getLogger().log("Writing to: " + dstBucket + "/" + dstKey);
			amazonS3.putObject(dstBucket, dstKey, is, meta);
			context.getLogger().log("Successfully resized " + srcBucket + "/"
					+ srcKey + " and uploaded the resized image to " + dstBucket + "/" + dstKey);
			return "SUCCESS";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}