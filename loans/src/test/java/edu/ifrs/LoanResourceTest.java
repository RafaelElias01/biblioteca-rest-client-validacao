package edu.ifrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.ifrs.client.IBookCatalog;
import edu.ifrs.model.Book;
import edu.ifrs.model.Loan;
import edu.ifrs.model.LoanRequest;
import jakarta.ws.rs.core.Response;

/**
 * Testes unitários de {@link LoanResource}: o catálogo é simulado com Mockito.
 */
@ExtendWith(MockitoExtension.class)
class LoanResourceTest {

    // Simula o REST Client do catálogo
    @Mock
    IBookCatalog bookCatalog;

    // O Mockito cria o LoanResource e injeta o mock
    @InjectMocks
    LoanResource resource;

    private static final Book FREE_BOOK = new Book(1L, "Dom Casmurro", "Machado de Assis", false);
    private static final Book LOANED_BOOK = new Book(2L, "O Cortiço", "Aluísio Azevedo", true);

    private Loan newLoan(Long bookId, String borrower) {
        when(bookCatalog.getBook(bookId)).thenReturn(new Book(bookId, "Livro", "Autor", false));
        Response response = resource.newLoan(new LoanRequest(bookId, borrower));
        return (Loan) response.getEntity();
    }

    @Test
    void shouldListBooksFromCatalog() {
        when(bookCatalog.listBooks()).thenReturn(List.of(FREE_BOOK));

        assertEquals(List.of(FREE_BOOK), resource.listBooks());
        verify(bookCatalog).listBooks();
    }

    @Test
    void shouldCreateLoanAndMarkBookAsLoaned() {
        when(bookCatalog.getBook(1L)).thenReturn(FREE_BOOK);

        Response response = resource.newLoan(new LoanRequest(1L, "Ana"));
        Loan loan = (Loan) response.getEntity();

        assertEquals(201, response.getStatus());
        assertNotNull(loan.id());
        assertEquals(1L, loan.bookId());
        assertEquals("Ana", loan.borrower());
        assertFalse(loan.returned());
        verify(bookCatalog).markAsLoaned(1L);
    }

    @Test
    void shouldReturn409WhenBookIsAlreadyLoaned() {
        when(bookCatalog.getBook(2L)).thenReturn(LOANED_BOOK);

        Response response = resource.newLoan(new LoanRequest(2L, "Pedro"));

        assertEquals(409, response.getStatus());
        verify(bookCatalog, never()).markAsLoaned(anyLong());
    }

    @Test
    void shouldListAllLoansWhenNoFilter() {
        newLoan(1L, "Ana");
        newLoan(2L, "Pedro");

        assertEquals(2, resource.listLoans(null).size());
    }

    @Test
    void shouldFilterLoansByBorrowerIgnoringCase() {
        newLoan(1L, "Ana");
        newLoan(2L, "Pedro");

        List<Loan> result = resource.listLoans("ANA");

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).borrower());
    }

    @Test
    void shouldReturnLoanAndMarkBookAsReturned() {
        Loan loan = newLoan(1L, "Ana");

        Response response = resource.returnLoan(loan.id());

        assertEquals(200, response.getStatus());
        assertTrue(((Loan) response.getEntity()).returned());
        verify(bookCatalog).markAsReturned(1L);
    }

    @Test
    void shouldReturn404WhenReturningUnknownLoan() {
        Response response = resource.returnLoan(999L);

        assertEquals(404, response.getStatus());
        verify(bookCatalog, never()).markAsReturned(anyLong());
    }

    @Test
    void shouldReturn409WhenLoanWasAlreadyReturned() {
        Loan loan = newLoan(1L, "Ana");
        resource.returnLoan(loan.id());

        Response response = resource.returnLoan(loan.id());

        assertEquals(409, response.getStatus());
    }
}
