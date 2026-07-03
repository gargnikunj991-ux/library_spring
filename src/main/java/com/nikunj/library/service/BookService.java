package com.nikunj.library.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.repository.BookRepository;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    public Book addBook(Book book) {
     return bookRepository.save(book);
}
    public List<Book> displayBook(){
        return bookRepository.findAll();
    }

    public void deleteBook(Long id){
        bookRepository.deleteById(id);
    }
    public Book getBookById(Long id){
        return bookRepository.findById(id).orElseThrow(()-> new BookNotFoundException("Book not found"));
    }
    public Book updateBook(Long id ,Book book){
        Optional<Book> existingBook = bookRepository.findById(id);
        Book dbBook = existingBook.orElseThrow(()-> new BookNotFoundException("Book not found"));
        dbBook.setTitle(book.getTitle());
        dbBook.setAuthor(book.getAuthor());
        dbBook.setAvailable(book.isAvailable());
        return bookRepository.save(dbBook);
}
}