package com.srikanth.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Handler Class for Hello World.
 *
 * @author  Srikanth
 * @version 1.0
 * @since   2018-08-15
 */
public class HelloLambdaHandler implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
		return "Hello World!";
	}

}
