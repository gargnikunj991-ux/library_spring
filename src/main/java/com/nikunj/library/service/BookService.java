package com.nikunj.library.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return bookRepository.findById(id).get();
    }
}