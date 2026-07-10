package com.nikunj.library.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikunj.library.dto.BookResponse;
import com.nikunj.library.dto.CreateBookRequest;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.repository.BookRepository;



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

       return mapToBookResponse(savedBook);
}
    public List<BookResponse> displayBook(){

       List<Book> searchBook = bookRepository.findAll();
       List<BookResponse> responses = new ArrayList<>();
       for(Book book : searchBook){
        responses.add(mapToBookResponse(book));
       }
       return responses;
    }

    public void deleteBook(Long id) {
     Book book = bookRepository.findById(id).orElseThrow(()-> new BookNotFoundException("Book Not found"));
       bookRepository.delete(book);
}
    public BookResponse getBookById(Long id){
        Book book =bookRepository.findById(id).orElseThrow(()-> new BookNotFoundException("Book not found"));
       return mapToBookResponse(book);
    }
    public BookResponse updateBook(Long id ,CreateBookRequest request){
         Book dbBook = bookRepository.findById(id).orElseThrow(()-> new  BookNotFoundException("Book not found"));
        
        dbBook.setTitle(request.getTitle());
        dbBook.setAuthor(request.getAuthor());
        dbBook.setAvailable(request.isAvailable());
        Book book =bookRepository.save(dbBook);
        
        return mapToBookResponse(book);
}

    private BookResponse mapToBookResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setAvailable(book.isAvailable());

        return response;     
    }

}