package com.zingtongroup.atg.codetest.api.petstore;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class PetEndpointTest {

	@Test
	void acceptsMinimalPet() {
		// Given
		RequestSpecification request = given()
				.body(Map.of("name", "my-pet"));

		// When
		Response response = request.when().post();

		// Then
		response.then().statusCode(200)
				.body("id", notNullValue())
				.body("name", is("my-pet"))
				.body("status", is(nullValue()))
				.body("photoUrls", is(empty()))
				.body("tags", is(empty()));
	}

	@Test
	void acceptsFullPet() {
		// Given
		RequestSpecification request = given()
				.body(Map.of(
						"name", "my-pet",
						"status", "pending",
						"photoUrls", List.of("p1", "p2", "p3"),
						"tags", List.of(
								Map.of("id", 1, "name", "t1"),
								Map.of("id", 2, "name", "t2"),
								Map.of("id", 3, "name", "t3")
						)
				));

		// When
		Response response = request.when().post();

		// Then
		response.then().statusCode(200)
				.body("id", notNullValue())
				.body("name", is("my-pet"))
				.body("status", is("pending"))
				.body("photoUrls", is(List.of("p1", "p2", "p3")))
				.body("tags", is(List.of(
						Map.of("id", 1, "name", "t1"),
						Map.of("id", 2, "name", "t2"),
						Map.of("id", 3, "name", "t3")
				)));
	}

	@Test
	void remembersPet() {
		// Given
		Long id = anExistingPet(Map.of(
				"name", "my-pet",
				"status", "pending",
				"photoUrls", List.of("p1", "p2", "p3"),
				"tags", List.of(
						Map.of("id", 1, "name", "t1"),
						Map.of("id", 2, "name", "t2"),
						Map.of("id", 3, "name", "t3")
				)
		));

		// When
		Response response = when().get(String.valueOf(id));

		// Then
		response.then().statusCode(200)
				.body("id", is(id))
				.body("name", is("my-pet"))
				.body("status", is("pending"))
				.body("photoUrls", is(List.of("p1", "p2", "p3")))
				.body("tags", is(List.of(
						Map.of("id", 1, "name", "t1"),
						Map.of("id", 2, "name", "t2"),
						Map.of("id", 3, "name", "t3")
				)));
	}

	@Test
	void acceptsMinimalUpdate() {
		// Given
		Long id = anExistingPet(Map.of(
				"name", "my-pet",
				"status", "pending",
				"photoUrls", List.of("p1", "p2", "p3"),
				"tags", List.of(
						Map.of("id", 1, "name", "t1"),
						Map.of("id", 2, "name", "t2"),
						Map.of("id", 3, "name", "t3")
				)
		));

		RequestSpecification request = given().body(Map.of("id", id));

		// When
		Response response = request.when().put();

		// Then
		response.then().statusCode(200)
				.body("id", is(id))
				.body("name", is(nullValue()))
				.body("status", is(nullValue()))
				.body("photoUrls", is(empty()))
				.body("tags", is(empty()));
	}

	@Test
	void acceptsFullUpdate() {
		// Given
		Long id = anExistingPet(Map.of(
				"name", "my-pet",
				"status", "pending",
				"photoUrls", List.of("p1", "p2", "p3"),
				"tags", List.of(
						Map.of("id", 1, "name", "t1"),
						Map.of("id", 2, "name", "t2"),
						Map.of("id", 3, "name", "t3")
				)
		));

		RequestSpecification request = given().body(Map.of(
				"id", id,
				"name", "new-name",
				"status", "sold",
				"photoUrls", List.of("p3", "p4"),
				"tags", List.of(
						Map.of("id", 3, "name", "t3"),
						Map.of("id", 4, "name", "t4")
				)
		));

		// When
		Response response = request.when().put();

		// Then
		response.then().statusCode(200)
				.body("id", is(id))
				.body("name", is("new-name"))
				.body("status", is("sold"))
				.body("photoUrls", is(List.of("p3", "p4")))
				.body("tags", is(List.of(
						Map.of("id", 3, "name", "t3"),
						Map.of("id", 4, "name", "t4")
				)));
	}

	private Long anExistingPet(Map<String, Object> data) {
		return given()
				.body(data)
				.when().post().then().statusCode(200).extract().path("id");
	}

	private RequestSpecification when() {
		return given().when();
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
