package edu.ifrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.ifrs.model.Book;
import jakarta.ws.rs.core.Response;

/**
 * Testes unitários de {@link CatalogResource}, sem subir o Quarkus.
 */
class CatalogResourceTest {

    private CatalogResource resource;

    @BeforeEach
    void setUp() {
        resource = new CatalogResource();
    }

    private Book add(String title, String author) {
        Response response = resource.addBook(new Book(null, title, author, true));
        return (Book) response.getEntity();
    }

    @Test
    void shouldCreateBookNotLoanedWithGeneratedId() {
        Response response = resource.addBook(new Book(99L, "Dom Casmurro", "Machado de Assis", true));
        Book created = (Book) response.getEntity();

        assertEquals(201, response.getStatus());
        assertEquals(1L, created.id());
        assertEquals("Dom Casmurro", created.title());
        assertFalse(created.loaned());
    }

    @Test
    void shouldGenerateDifferentIds() {
        Book first = add("Livro 1", "Autor 1");
        Book second = add("Livro 2", "Autor 2");

        assertEquals(first.id() + 1, second.id());
    }

    @Test
    void shouldListAllBooksWhenNoFilter() {
        add("Dom Casmurro", "Machado de Assis");
        add("O Cortiço", "Aluísio Azevedo");

        assertEquals(2, resource.listBooks(null).size());
    }

    @Test
    void shouldFilterBooksByTitleIgnoringCase() {
        add("Dom Casmurro", "Machado de Assis");
        add("O Cortiço", "Aluísio Azevedo");

        List<Book> result = resource.listBooks("casmurro");

        assertEquals(1, result.size());
        assertEquals("Dom Casmurro", result.get(0).title());
    }

    @Test
    void shouldReturnEmptyListWhenTitleDoesNotMatch() {
        add("Dom Casmurro", "Machado de Assis");

        assertTrue(resource.listBooks("xyz").isEmpty());
    }

    @Test
    void shouldGetBookWhenItExists() {
        Book created = add("Dom Casmurro", "Machado de Assis");

        Response response = resource.getBook(created.id());

        assertEquals(200, response.getStatus());
        assertEquals(created, response.getEntity());
    }

    @Test
    void shouldReturn404WhenGettingUnknownBook() {
        assertEquals(404, resource.getBook(999L).getStatus());
    }

    @Test
    void shouldMarkBookAsLoaned() {
        Book created = add("Dom Casmurro", "Machado de Assis");

        Response response = resource.loanBook(created.id());

        assertEquals(200, response.getStatus());
        assertTrue(((Book) response.getEntity()).loaned());
    }

    @Test
    void shouldReturn409WhenBookIsAlreadyLoaned() {
        Book created = add("Dom Casmurro", "Machado de Assis");
        resource.loanBook(created.id());

        assertEquals(409, resource.loanBook(created.id()).getStatus());
    }

    @Test
    void shouldReturn404WhenLoaningUnknownBook() {
        assertEquals(404, resource.loanBook(999L).getStatus());
    }

    @Test
    void shouldMarkBookAsReturned() {
        Book created = add("Dom Casmurro", "Machado de Assis");
        resource.loanBook(created.id());

        Response response = resource.returnBook(created.id());

        assertEquals(200, response.getStatus());
        assertFalse(((Book) response.getEntity()).loaned());
    }

    @Test
    void shouldReturn404WhenReturningUnknownBook() {
        assertEquals(404, resource.returnBook(999L).getStatus());
    }
}
