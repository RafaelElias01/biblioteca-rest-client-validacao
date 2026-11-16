package edu.ifrs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ifrs.client.IBookCatalog;
import edu.ifrs.model.Book;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Testes de componente da API REST do módulo loans (Quarkus + RestAssured).
 * O catálogo (REST Client) é simulado, para testar o loans de forma isolada.
 */
@QuarkusTest
class LoanResourceApiTest {

    @InjectMock
    @RestClient
    IBookCatalog catalog;

    private String token;

    @BeforeEach
    void login() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Ana\",\"email\":\"ana@email.com\",\"password\":\"123\"}")
                .post("/users");

        token = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"ana@email.com\",\"password\":\"123\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().asString();
    }

    private int createLoan(long bookId) {
        when(catalog.getBook(bookId)).thenReturn(new Book(bookId, "Dom Casmurro", "Machado", false));
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"bookId\":" + bookId + ",\"borrower\":\"Ana\"}")
                .when()
                .post("/loans")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    void shouldCreateUser() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Pedro\",\"email\":\"pedro@email.com\",\"password\":\"abc\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("email", is("pedro@email.com"));
    }

    @Test
    void shouldReturn401WhenLoginIsInvalid() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"ana@email.com\",\"password\":\"errada\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldReturn401WhenTokenIsMissing() {
        given()
                .when()
                .get("/loans")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldListBooksFromCatalog() {
        when(catalog.listBooks()).thenReturn(List.of(new Book(1L, "Dom Casmurro", "Machado", false)));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/loans/books")
                .then()
                .statusCode(200)
                .body("title", hasItem("Dom Casmurro"));
    }

    @Test
    void shouldCreateLoan() {
        when(catalog.getBook(1L)).thenReturn(new Book(1L, "Dom Casmurro", "Machado", false));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"bookId\":1,\"borrower\":\"Ana\"}")
                .when()
                .post("/loans")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("bookId", is(1))
                .body("borrower", is("Ana"))
                .body("returned", is(false));
    }

    @Test
    void shouldReturn409WhenBookIsAlreadyLoaned() {
        when(catalog.getBook(2L)).thenReturn(new Book(2L, "O Cortiço", "Aluísio", true));

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("{\"bookId\":2,\"borrower\":\"Pedro\"}")
                .when()
                .post("/loans")
                .then()
                .statusCode(409);
    }

    @Test
    void shouldListLoansFilteringByBorrower() {
        createLoan(3L);

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("borrower", "ana")
                .when()
                .get("/loans")
                .then()
                .statusCode(200)
                .body("borrower", hasItem("Ana"));
    }

    @Test
    void shouldReturnLoan() {
        int id = createLoan(4L);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .put("/loans/{id}/return", id)
                .then()
                .statusCode(200)
                .body("returned", is(true));
    }

    @Test
    void shouldReturn404WhenReturningUnknownLoan() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .put("/loans/99999/return")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn409WhenReturningLoanTwice() {
        int id = createLoan(5L);
        given().header("Authorization", "Bearer " + token)
                .put("/loans/{id}/return", id).then().statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .put("/loans/{id}/return", id)
                .then()
                .statusCode(409);
    }
}
