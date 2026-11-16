package edu.ifrs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.http.ContentType;

/**
 * Testes de sistema: validam a biblioteca como um todo, do ponto de vista do
 * usuário, com os DOIS serviços reais no ar (catalog na porta 9080 e loans
 * na porta 9081). O loans chama o catalog por REST Client, sem mocks.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibrarySystemTest {

    private static final String CATALOG = "http://localhost:9080";
    private static final String LOANS = "http://localhost:9081";

    private static String token;
    private static int bookId;
    private static int loanId;

    @BeforeAll
    static void registerAndLogin() {
        String email = "ana" + System.nanoTime() + "@email.com";

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Ana\",\"email\":\"" + email + "\",\"password\":\"123\"}")
                .when()
                .post(LOANS + "/users")
                .then()
                .statusCode(201);

        token = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + email + "\",\"password\":\"123\"}")
                .when()
                .post(LOANS + "/auth/login")
                .then()
                .statusCode(200)
                .extract().asString();
    }

    private static String auth() {
        return "Bearer " + token;
    }

    @Test
    @Order(1)
    void librarianRegistersBookInCatalog() {
        bookId = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"Dom Casmurro\",\"author\":\"Machado de Assis\"}")
                .when()
                .post(CATALOG + "/books")
                .then()
                .statusCode(201)
                .body("loaned", is(false))
                .extract().path("id");
    }

    @Test
    @Order(2)
    void userSeesBookThroughLoansService() {
        given()
                .header("Authorization", auth())
                .when()
                .get(LOANS + "/loans/books")
                .then()
                .statusCode(200)
                .body("id", hasItem(bookId));
    }

    @Test
    @Order(3)
    void userBorrowsBookAndCatalogMarksItAsLoaned() {
        loanId = given()
                .header("Authorization", auth())
                .contentType(ContentType.JSON)
                .body("{\"bookId\":" + bookId + ",\"borrower\":\"Ana\"}")
                .when()
                .post(LOANS + "/loans")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract().path("id");

        given()
                .when()
                .get(CATALOG + "/books/{id}", bookId)
                .then()
                .statusCode(200)
                .body("loaned", is(true));
    }

    @Test
    @Order(4)
    void secondUserCannotBorrowTheSameBook() {
        given()
                .header("Authorization", auth())
                .contentType(ContentType.JSON)
                .body("{\"bookId\":" + bookId + ",\"borrower\":\"Pedro\"}")
                .when()
                .post(LOANS + "/loans")
                .then()
                .statusCode(409);
    }

    @Test
    @Order(5)
    void userSeesOwnLoan() {
        given()
                .header("Authorization", auth())
                .queryParam("borrower", "Ana")
                .when()
                .get(LOANS + "/loans")
                .then()
                .statusCode(200)
                .body("id", hasItem(loanId));
    }

    @Test
    @Order(6)
    void userReturnsBookAndItBecomesAvailableAgain() {
        given()
                .header("Authorization", auth())
                .when()
                .put(LOANS + "/loans/{id}/return", loanId)
                .then()
                .statusCode(200)
                .body("returned", is(true));

        given()
                .when()
                .get(CATALOG + "/books/{id}", bookId)
                .then()
                .statusCode(200)
                .body("loaned", is(false));
    }

    @Test
    @Order(7)
    void requestsWithoutTokenAreRejected() {
        given()
                .when()
                .get(LOANS + "/loans")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(8)
    void loginWithWrongPasswordIsRejected() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"naoexiste@email.com\",\"password\":\"x\"}")
                .when()
                .post(LOANS + "/auth/login")
                .then()
                .statusCode(401);
    }
}
