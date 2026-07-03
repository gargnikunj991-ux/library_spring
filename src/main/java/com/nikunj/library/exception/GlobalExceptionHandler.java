package com.nikunj.library.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(BookNotFoundException.class)
public ResponseEntity<String> handleException(BookNotFoundException ex){
    return new ResponseEntity<>(
        "Book Not Found",
       HttpStatus.NOT_FOUND 
    );
}
    
}
