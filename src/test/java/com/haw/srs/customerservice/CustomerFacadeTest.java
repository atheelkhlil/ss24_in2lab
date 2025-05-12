package com.haw.srs.customerservice;

import com.haw.srs.customerservice.Repo.CustomerRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerFacadeTest {

    private final Log log = LogFactory.getLog(getClass());

    @LocalServerPort
    private int port;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        this.customerRepository.deleteAll();

        customer = this.customerRepository.save(new Customer("Stefan", "Sarstedt", Gender.MALE));

        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Test
    void getAllCustomersSuccess() {
        //@formatter:off
        given().log().all().
                // add this here to log request --> log().all().
        when().
                get("/customers").
        then().
                // add this here to log response --> log().all().
                statusCode(HttpStatus.OK.value()).
                body("lastName", hasItems("Sarstedt"));
        //@formatter:on
    }

    @Test
    void getCustomerSuccess() {
        //@formatter:off
        given().log().all().
        when().
                get("/customers/{id}", customer.getId()).
        then().
                statusCode(HttpStatus.OK.value()).
                body("lastName", equalTo("Sarstedt"));
        //@formatter:on
    }

    @Test
    void getCustomerFailBecauseOfNotFound() {
        //@formatter:off
        given().log().all().
        when().
                get("/customers/{id}", Integer.MAX_VALUE).
        then().
                statusCode(HttpStatus.NOT_FOUND.value());
        //@formatter:on
    }

    @Test
    void createCustomerSuccess() {
        //@formatter:off
        given().
                contentType(ContentType.JSON).
                body(new Customer("Stefan", "Sarstedt", Gender.MALE)).
        when().
                post("/customers").
        then().
                statusCode(HttpStatus.CREATED.value()).
                body("id", is(greaterThan(0)));
        //@formatter:on
    }

    @Test
    void updateCustomerSuccess() {
        customer.setFirstName("Stefanie");

        //@formatter:off
        given().
                contentType(ContentType.JSON).
                body(customer).
        when().
                put("/customers").
        then().
                statusCode(HttpStatus.OK.value());

        given().
        when().
                get("/customers/{id}", customer.getId()).
        then().
                statusCode(HttpStatus.OK.value()).
                body("firstName", is(equalTo("Stefanie")));
        //@formatter:on
    }
    @Test
    void updateCustomerFailBecauseCustNotFound() {

            // Angenommen, Kunde mit dieser ID existiert nicht
            long nonExistingId = 9999L;
            customer.setId(nonExistingId);
            customer.setFirstName("Stefanie");

            //@formatter:off
            // 1. PUT auf /customers/{id} für nicht vorhandenen Datensatz → 404
            given().
                    contentType(ContentType.JSON).
                    body(customer).
                    when().
                    put("/customers/{id}", nonExistingId).
                    then().
                    statusCode(HttpStatus.NOT_FOUND.value());

            // 2. GET auf /customers/{id} → ebenfalls 404
            given().
                    when().
                    get("/customers/{id}", nonExistingId).
                    then().
                    statusCode(HttpStatus.NOT_FOUND.value());
            //@formatter:on
        }



    @Test
    void deleteCustomerSuccess() {
        //@formatter:off
        given().
                delete("/customers/{id}", customer.getId()).
        then().
                statusCode(HttpStatus.OK.value());

        given().
        when().
                get("/customers/{id}", customer.getId()).
        then().
                statusCode(HttpStatus.NOT_FOUND.value());
        //@formatter:on
    }

    @Test
    void createCustomerFailsWithMalformedJson() {
        String badJson = "{ \"firstName\": \"Max\", "; // unvollständig
        given()
                .contentType(ContentType.JSON)
                .body(badJson)
                .when()
                .post("/customers")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
    @Test
    void deleteCustomerFailsWhenNotFound() {
        given()
                .when()
                .delete("/customers/{id}", 9999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
    @Test
    void getAllCustomersReturnsEmptyList() {
        customerRepository.deleteAll();
        given()
                .when()
                .get("/customers")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(0));
    }



}