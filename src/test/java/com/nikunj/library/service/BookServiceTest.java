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
        sampleBook.setAvailable(true);

        createRequest = new CreateBookRequest();
        createRequest.setTitle("Design Patterns");
        createRequest.setAuthor("Gang of Four");
        createRequest.setAvailable(true);
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
        updateReq.setAvailable(false);

        BookResponse response = bookService.updateBook(1L, updateReq);

        assertNotNull(response);
        assertEquals("Design Patterns 2nd Edition", response.getTitle());
        assertFalse(response.isAvailable());
    }

    @Test
    @DisplayName("Delete Book: Successfully deletes existing book")
    void testDeleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        doNothing().when(bookRepository).delete(sampleBook);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(sampleBook);
    }
}
