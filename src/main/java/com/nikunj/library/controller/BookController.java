package com.nikunj.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nikunj.library.dto.BookResponse;
import com.nikunj.library.dto.CreateBookRequest;
import com.nikunj.library.service.BookService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public List<BookResponse> getAllBook() {
        return bookService.displayBook();
    }

    @PostMapping
    public BookResponse addBook(@Valid @RequestBody CreateBookRequest request) {
        return bookService.addBook(request);
    }

    @GetMapping("/{id}")
    public BookResponse getBookId(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteBookId(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody CreateBookRequest request) {
        return bookService.updateBook(id, request);
    }
}
