package com.zingtongroup.atg.codetest.api.petstore;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public enum Operation {
	PUT {
		@Override
		public Response execute(RequestSpecification request) {
			return request.put();
		}
	},
	POST {
		@Override
		public Response execute(RequestSpecification request) {
			return request.post();
		}
	};

	public abstract Response execute(RequestSpecification request);
}
