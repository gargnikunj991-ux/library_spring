package com.nikunj.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import java.util.List;
import java.util.ArrayList;
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

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<?> handleException(MethodArgumentNotValidException ex){
    var errors = ex.getBindingResult().getFieldErrors();
    List<String> messages  = new ArrayList<>();
    for(FieldError error : errors){
        messages.add(error.getDefaultMessage());
    }
   return new ResponseEntity<>(messages, HttpStatus.BAD_REQUEST);
}

@ExceptionHandler(MemberNotFoundException.class)
public ResponseEntity<String> handleException(MemberNotFoundException ex){
    return new ResponseEntity<>(
        "Member Not Found",
       HttpStatus.NOT_FOUND 
    );
}

    
}
