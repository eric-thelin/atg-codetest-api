package se.ericthelin.atg.codetest.api.petstore;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
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

	@ParameterizedTest
	@EnumSource(names = {"PUT", "POST"})
	void acceptsMinimalUpdate(Operation operation) {
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
		Response response = operation.execute(request);

		// Then
		response.then().statusCode(200)
				.body("id", is(id))
				.body("name", is(nullValue()))
				.body("status", is(nullValue()))
				.body("photoUrls", is(empty()))
				.body("tags", is(empty()));
	}

	@ParameterizedTest
	@EnumSource(names = {"PUT", "POST"})
	void acceptsFullUpdate(Operation operation) {
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
		Response response = operation.execute(request);

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

	@ParameterizedTest
	@EnumSource(names = {"PUT", "POST"})
	void failsOnUpdateOfInvalidPetId(Operation operation) {
		// Given
		RequestSpecification request = given().body(Map.of(
				"id", "invalid"
		));

		// When
		Response response = operation.execute(request);

		// Then
		response.then().statusCode(500)
				.body("code", is(500))
				.body("type", is("unknown"))
				.body("message", is("something bad happened"));
	}

	@Test
	void acceptsDelete() {
		// Given
		Long id = anExistingPet();

		// When
		Response response = when().delete(String.valueOf(id));

		// Then
		response.then().statusCode(200)
				.body("code", is(200))
				.body("type", is("unknown"))
				.body("message", is(String.valueOf(id)));
	}

	@Test
	void forgetsDeletedPet() {
		// Given
		Long id = aDeletedPet();

		// When
		Response response = when().get(String.valueOf(id));

		// Then
		response.then().statusCode(404)
				.body("code", is(1))
				.body("type", is("error"))
				.body("message", is("Pet not found"));
	}

	@Test
	void rejectsDeleteForInvalidPetId() {
		// When
		Response response = when().delete("invalid");

		// Then
		response.then().statusCode(404)
				.body("code", is(404))
				.body("type", is("unknown"))
				.body("message", is(
						"java.lang.NumberFormatException: For input string: \"invalid\""
				));
	}

	@ParameterizedTest
	@CsvSource(value = {
			"                       | ",
			"available              | p1",
			"pending                | p2",
			"sold                   | p3",
			"invalid                |",

			// It is interesting that the order of the statuses affect the
			// response. It seems like only the first value is being used,
			// despite the fact that API documentation suggests this to be a
			// multi-value parameter.
			"available pending sold | p1",
			"pending sold available | p2",
			"sold available pending | p3",
			"invalid available      |",
	}, delimiter = '|')
	void findsPetsByStatus(String statuses, String expectedNames) {
		// Given
		Set<Long> pets = Set.of(
				anExistingPet(Map.of("name", "p1", "status", "available")),
				anExistingPet(Map.of("name", "p2", "status", "pending")),
				anExistingPet(Map.of("name", "p3", "status", "sold"))
		);

		// When
		Response response = when()
				.queryParam("status", parseParameter(statuses))
				.get("findByStatus");

		// Then
		response.then().statusCode(200).body(
				String.format("findAll {it.id in %s }.name", pets),
				is(parseParameter(expectedNames))
		);
	}

	@Test
	void acceptsFindingPetsByStatusWithoutStatusParameter() {
		// Given
		Set<Long> pets = Set.of(
				anExistingPet(Map.of("name", "p1", "status", "available")),
				anExistingPet(Map.of("name", "p2", "status", "pending")),
				anExistingPet(Map.of("name", "p3", "status", "sold"))
		);

		// When
		Response response = when().get("findByStatus");

		// Then
		response.then().statusCode(200).body(
				String.format("findAll {it.id in %s }.name", pets),
				is(empty())
		);
	}


	@ParameterizedTest
	@ValueSource(strings = {
			"available",
			"pending",
			"sold",
			"invalid",
			"lorem ipsum"
	})
	void acceptsAnyStatus(String status) {
		// Given
		RequestSpecification request = given()
				.body(Map.of(
						"status", status
				));

		// When
		Response response = request.when().post();

		// Then
		response.then().statusCode(200)
				.body("status", is(status));
	}

	@Test
	void acceptsNullPhotoUrls() {
		// Given
		RequestSpecification request = given()
				.body(Map.of(
						"photoUrls", Arrays.asList(null, null, null)
				));

		// When
		Response response = request.when().post();

		// Then
		response.then().statusCode(200)
				.body("id", notNullValue())
				.body("photoUrls", is(Arrays.asList(null, null, null)));
	}

	@Test
	void acceptsNullTags() {
		// Given
		RequestSpecification request = given()
				.body(Map.of("tags", Arrays.asList(null, null, null)));

		// When
		Response response = request.when().post();

		// Then
		response.then().statusCode(200)
				.body("id", notNullValue())
				.body("tags", is(Arrays.asList(null, null, null)));
	}

	@Test
	void acceptsNullTagFields() {
		// Given
		RequestSpecification request = given().body(Map.of("tags", List.of(
				mapOfNull("id", "name"),
				mapOfNull("id", "name"),
				mapOfNull("id", "name")
		)));

		// When
		Response response = request.when().post();

		// Then
		// Strange behavior...
		response.then().statusCode(200)
				.body("id", notNullValue())
				.body("tags", is(List.of(
						Map.of("id", 0),
						Map.of("id", 0),
						Map.of("id", 0)
				)));
	}

	@Test
	void acceptsImage() {
		// Given
		Long id = anExistingPet();

		RequestSpecification request = when()
				.contentType(ContentType.MULTIPART)
				.pathParam("petId", id)
				.multiPart("additionalMetadata", "foo=bar")
				.multiPart("file", "remote-file.png",
						getClass().getResourceAsStream("/test-image.png")
				);

		// When
		Response response = request.post("{petId}/uploadImage");

		// Then
		response.then().statusCode(200)
				.body("code", is(200))
				.body("type", is("unknown"))
				.body("message", is("" +
						"additionalMetadata: foo=bar\n" +
						"File uploaded to ./remote-file.png, 10169 bytes"
				));
	}

	@Test
	void acceptsImageWithoutMetadata() {
		// Given
		Long id = anExistingPet();

		RequestSpecification request = when()
				.contentType(ContentType.MULTIPART)
				.pathParam("petId", id)
				.multiPart("file", "remote-file.png",
						getClass().getResourceAsStream("/test-image.png")
				);

		// When
		Response response = request.post("{petId}/uploadImage");

		// Then
		response.then().statusCode(200)
				.body("code", is(200))
				.body("type", is("unknown"))
				.body("message", is("" +
						"additionalMetadata: null\n" +
						"File uploaded to ./remote-file.png, 10169 bytes"
				));
	}

	@Test
	void failsOnImageUploadWithoutFile() {
		// Given
		Long id = anExistingPet();

		RequestSpecification request = when()
				.contentType(ContentType.MULTIPART)
				.pathParam("petId", id)
				.multiPart("additionalMetadata", "foo=bar");

		// When
		Response response = request.post("{petId}/uploadImage");

		// Then
		response.then().statusCode(500)
				.contentType(ContentType.HTML)
				.body(containsString("Internal Server Error"));
	}

	@Test
	void rejectsImageUploadForInvalidPetId() {
		// Given
		RequestSpecification request = when()
				.contentType(ContentType.MULTIPART)
				.multiPart("file", "remote-file.png",
						getClass().getResourceAsStream("/test-image.png")
				);

		// When
		Response response = request.post("/invalid/uploadImage");

		// Then
		response.then().statusCode(404)
				.body("type", is("unknown"))
				.body("message", is(
						"java.lang.NumberFormatException: For input string: \"invalid\""
				));
	}

	@Test
	void acceptsImageUploadForDeletedPet() {
		// Given
		Long id = aDeletedPet();

		RequestSpecification request = when()
				.contentType(ContentType.MULTIPART)
				.pathParam("petId", id)
				.multiPart("file", "remote-file.png",
						getClass().getResourceAsStream("/test-image.png")
				);

		// When
		Response response = request.post("{petId}/uploadImage");

		// Then
		response.then().statusCode(200)
				.body("code", is(200))
				.body("type", is("unknown"))
				.body("message", is("" +
						"additionalMetadata: null\n" +
						"File uploaded to ./remote-file.png, 10169 bytes"
				));
	}

	private Long aDeletedPet() {
		Long id = anExistingPet();
		when().delete(String.valueOf(id));
		return id;
	}

	private Long anExistingPet() {
		return anExistingPet(Map.of("name", "my-pet"));
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

	private List<String> parseParameter(String statuses) {
		return Optional.ofNullable(statuses)
				.map(text -> List.of(text.split(" ")))
				.orElse(List.of());
	}

	private Map<String, Object> mapOfNull(String... keys) {
		Map<String, Object> result = new HashMap<>();

		for (String key : keys) {
			result.put(key, null);
		}

		return result;
	}
}
