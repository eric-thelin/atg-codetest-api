package com.zingtongroup.atg.codetest.api.petstore;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PetEndpointTest {

	@Test
	void acceptsNewPet() {
		// Given
		RequestSpecification request = given()
				.body(Map.of("name", "my-pet"));

		// When
		Response response = request.when().post();

		// Then
		response.then().statusCode(200)
				.body("name", is("my-pet"))
				.body("id", notNullValue());
	}

	private RequestSpecification given() {
		RequestSpecification result = RestAssured.given()
				.baseUri("https://petstore.swagger.io/v2/pet")
				.header("Content-Type", "application/json")
				.header("accept", "application/json")
				.log().ifValidationFails();

		result.response().log().ifValidationFails();

		return result;
	}
}
