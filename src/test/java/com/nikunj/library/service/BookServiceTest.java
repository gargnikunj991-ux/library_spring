package com.nikunj.library.service;

import com.nikunj.library.dto.BookResponse;
import com.nikunj.library.dto.CreateBookRequest;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;
    private CreateBookRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Design Patterns");
        sampleBook.setAuthor("Gang of Four");
        sampleBook.setTotalCopies(5);
        sampleBook.setAvailableCopies(5);

        createRequest = new CreateBookRequest();
        createRequest.setTitle("Design Patterns");
        createRequest.setAuthor("Gang of Four");
        createRequest.setTotalCopies(5);
    }

    @Test
    @DisplayName("Add Book: Successfully persists and maps book")
    void testAddBook_Success() {
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.addBook(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Design Patterns", response.getTitle());
        assertEquals("Gang of Four", response.getAuthor());
        assertEquals(5, response.getTotalCopies());
        assertEquals(5, response.getAvailableCopies());
        assertTrue(response.isAvailable());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Display Books: Returns list of all books mapped to DTOs")
    void testDisplayBook_Success() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<BookResponse> books = bookService.displayBook();

        assertNotNull(books);
        assertEquals(1, books.size());
        assertEquals("Design Patterns", books.get(0).getTitle());
    }

    @Test
    @DisplayName("Get Book By ID: Returns book response when found")
    void testGetBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookResponse response = bookService.getBookById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Get Book By ID: Throws BookNotFoundException when ID does not exist")
    void testGetBookById_NotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(999L));
    }

    @Test
    @DisplayName("Update Book: Successfully updates existing book")
    void testUpdateBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        CreateBookRequest updateReq = new CreateBookRequest();
        updateReq.setTitle("Design Patterns 2nd Edition");
        updateReq.setAuthor("Gang of Four");
        updateReq.setTotalCopies(10);

        BookResponse response = bookService.updateBook(1L, updateReq);

        assertNotNull(response);
        assertEquals("Design Patterns 2nd Edition", response.getTitle());
    }

    @Test
    @DisplayName("Delete Book: Successfully deletes existing book")
    void testDeleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        doNothing().when(bookRepository).delete(sampleBook);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(sampleBook);
    }

    @Test
    @DisplayName("Add Book: Defaults totalCopies to 1 if null")
    void testAddBook_DefaultCopiesWhenNull() {
        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Refactoring");
        req.setAuthor("Martin Fowler");
        req.setTotalCopies(null);

        Book saved = new Book();
        saved.setId(2L);
        saved.setTitle("Refactoring");
        saved.setAuthor("Martin Fowler");
        saved.setTotalCopies(1);
        saved.setAvailableCopies(1);

        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        BookResponse resp = bookService.addBook(req);

        assertNotNull(resp);
        assertEquals(1, resp.getTotalCopies());
        assertEquals(1, resp.getAvailableCopies());
    }

    @Test
    @DisplayName("Update Book: Adjusts available copies when total copies increases")
    void testUpdateBook_AdjustCopies() {
        sampleBook.setTotalCopies(5);
        sampleBook.setAvailableCopies(2);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateBookRequest req = new CreateBookRequest();
        req.setTitle("Design Patterns");
        req.setAuthor("Gang of Four");
        req.setTotalCopies(7); // +2 copies

        BookResponse resp = bookService.updateBook(1L, req);

        assertNotNull(resp);
        assertEquals(7, resp.getTotalCopies());
        assertEquals(4, resp.getAvailableCopies()); // 2 + (7 - 5) = 4
    }
}
