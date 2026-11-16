package edu.ifrs;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasItem;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Testes de componente da API REST {@code /books} (Quarkus + RestAssured).
 */
@QuarkusTest
class CatalogResourceApiTest {

    private static final String BOOK_JSON = """
            {"title": "Dom Casmurro", "author": "Machado de Assis"}
            """;

    private int createBook() {
        return given()
                .contentType(ContentType.JSON)
                .body(BOOK_JSON)
                .when()
                .post("/books")
                .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    void shouldReturn201WhenAddingBook() {
        given()
                .contentType(ContentType.JSON)
                .body(BOOK_JSON)
                .when()
                .post("/books")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("title", is("Dom Casmurro"))
                .body("author", is("Machado de Assis"))
                .body("loaned", is(false));
    }

    @Test
    void shouldListBooks() {
        createBook();

        given()
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("title", hasItem("Dom Casmurro"));
    }

    @Test
    void shouldFilterBooksByTitle() {
        createBook();

        given()
                .queryParam("title", "casmurro")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("title", hasItem("Dom Casmurro"));
    }

    @Test
    void shouldReturnEmptyListWhenFilterDoesNotMatch() {
        given()
                .queryParam("title", "livro-que-nao-existe")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void shouldGetBookById() {
        int id = createBook();

        given()
                .when()
                .get("/books/{id}", id)
                .then()
                .statusCode(200)
                .body("id", is(id))
                .body("title", is("Dom Casmurro"));
    }

    @Test
    void shouldReturn404WhenBookDoesNotExist() {
        given()
                .when()
                .get("/books/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldLoanAndReturnBook() {
        int id = createBook();

        given().when().put("/books/{id}/loan", id)
                .then().statusCode(200).body("loaned", is(true));

        given().when().put("/books/{id}/return", id)
                .then().statusCode(200).body("loaned", is(false));
    }

    @Test
    void shouldReturn409WhenLoaningAlreadyLoanedBook() {
        int id = createBook();
        given().when().put("/books/{id}/loan", id).then().statusCode(200);

        given().when().put("/books/{id}/loan", id)
                .then().statusCode(409);
    }

    @Test
    void shouldReturn404WhenLoaningOrReturningUnknownBook() {
        given().when().put("/books/99999/loan").then().statusCode(404);
        given().when().put("/books/99999/return").then().statusCode(404);
    }
}
