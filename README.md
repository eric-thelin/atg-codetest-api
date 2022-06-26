# ATG Code Test Question 1

## Assignment

Write API tests for testing the "pet" endpoint of
[the petstore API](https://petstore.swagger.io).

**Notes:**

- Make sure that this test can run everyday
- Use Java, Maven

## Solution

The test is found in
[PetEndpointTest](src/test/java/com/zingtongroup/atg/codetest/api/petstore/PetEndpointTest.java).
I have tried to test all happy paths as well as invalid values and edge cases. I
am sure there are edge cases that I did not think of. Also, I have tried to
design the tests to be robust, but since this is a public API used by others
there is always the potential for false failures due to interference from other
processes.

You can run the test from an IDE or from the command line. To run the test from
the command line, you can use the command below:

```
mvn clean test
```
