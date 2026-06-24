package com.nikunj.library.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nikunj.library.model.Book;
import com.nikunj.library.repository.BookRepository;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookRepository bookRepository;   
   
    @GetMapping
    public List<Book> getAllBook(){
        return bookRepository.findAll();
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }
    @GetMapping("/{id}")
    public Book getBookId(@PathVariable Long id){
        return bookRepository.findById(id).get();
    }
    @DeleteMapping("/{id}")
    public void deleteBookId(@PathVariable Long id){
         bookRepository.deleteById(id);
    }
}
