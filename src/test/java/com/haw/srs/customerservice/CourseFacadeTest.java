package com.haw.srs.customerservice;

import com.haw.srs.customerservice.Repo.CourseRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourseFacadeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();

        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    void getAllCoursesReturnsEmptyList() {
        when()
                .get("/courses")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(0));
    }

    @Test
    void getAllCoursesReturnsCourses() {
        courseRepository.save(new Course("Mathe"));
        courseRepository.save(new Course("Physik"));

        when()
                .get("/courses")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("name", hasItems("Mathe", "Physik"));
    }

    @Test
    void getCourseByIdSuccess() {
        Course saved = courseRepository.save(new Course("Informatik"));

        when()
                .get("/courses/{id}", saved.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(saved.getId().intValue()))
                .body("name", equalTo("Informatik"));
    }

    @Test
    void getCourseByIdNotFound() {
        when()
                .get("/courses/{id}", 9999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void createCourseViaPostSuccess() {
        String json = "{ \"name\": \"Biologie\" }";

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/courses")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", greaterThan(0))
                .body("name", equalTo("Biologie"));
    }

    @Test
    void createCourseFailsWithoutBody() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/courses")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createCourseFailsWithMalformedJson() {
        String badJson = "{ \"name\": }";
        given()
                .contentType(ContentType.JSON)
                .body(badJson)
                .when()
                .post("/courses")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void updateCourseSuccess() {
        Course saved = courseRepository.save(new Course("Geschichte"));
        String updateJson = "{ \"name\": \"Neuere Geschichte\" }";

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/courses/{id}", saved.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(saved.getId().intValue()))
                .body("name", equalTo("Neuere Geschichte"));
    }

    @Test
    void updateCourseNotFound() {
        String json = "{ \"name\": \"X\" }";

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("/courses/{id}", 8888L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteCourseSuccess() {
        Course saved = courseRepository.save(new Course("Philosophie"));

        when()
                .delete("/courses/{id}", saved.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        when()
                .get("/courses/{id}", saved.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteCourseNotFound() {
        when()
                .delete("/courses/{id}", Long.MAX_VALUE)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getCourseFailsWithInvalidIdFormat() {
        when()
                .get("/courses/abc")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
