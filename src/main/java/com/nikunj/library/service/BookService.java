package com.nikunj.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikunj.library.dto.BookResponse;
import com.nikunj.library.dto.CreateBookRequest;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.repository.BookRepository;

import jakarta.security.auth.message.callback.PrivateKeyCallback;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    public BookResponse addBook(CreateBookRequest request) {

        Book book = new Book();
        book.setAuthor(request.getAuthor());
        book.setTitle(request.getTitle());
        book.setAvailable(request.isAvailable());
        
        Book savedBook = bookRepository.save(book);
        BookResponse response = new BookResponse();
        response.setId(savedBook.getId());
        response.setTitle(savedBook.getTitle());
        response.setAuthor(savedBook.getAuthor());
        response.setAvailable(savedBook.isAvailable());
        
        return response;
}
    public List<BookResponse> displayBook(){

       List<Book> serachBook = bookRepository.findAll();
       List<BookResponse> responses = new ArrayList<>();
       for(Book book : serachBook){
        BookResponse response = new BookResponse();

        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setAvailable(book.isAvailable());

        responses.add(response);
       }
       return responses;
    }

    public void deleteBook(Long id){
        bookRepository.deleteById(id);
    }
    public BookResponse getBookById(Long id){
        Book book =bookRepository.findById(id).orElseThrow(()-> new BookNotFoundException("Book not found"));
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setAvailable(book.isAvailable());

        return response;
    }
    public BookResponse updateBook(Long id ,CreateBookRequest request){
        Optional<Book> existingBook = bookRepository.findById(id);
        
        Book dbBook = existingBook.orElseThrow(()-> new BookNotFoundException("Book not found"));
        dbBook.setTitle(request.getTitle());
        dbBook.setAuthor(request.getAuthor());
        dbBook.setAvailable(request.isAvailable());
        Book updateBook =bookRepository.save(dbBook);
        
        BookResponse response = new BookResponse();
        response.setId(updateBook.getId());
        response.setTitle(updateBook.getTitle());
        response.setAuthor(updateBook.getAuthor());
        response.setAvailable(updateBook.isAvailable());

        return response;
}
}