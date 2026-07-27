package com.nikunj.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nikunj.library.dto.BorrowResponse;
import com.nikunj.library.dto.CreateBorrowRequest;
import com.nikunj.library.service.BorrowService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/borrow")
public class Borrowcontroller {

    @Autowired
    private BorrowService borrowService;

    @PostMapping
    public ResponseEntity<BorrowResponse> borrowBook(@Valid @RequestBody CreateBorrowRequest request) {
        BorrowResponse response = borrowService.borrowBook(request);
        return ResponseEntity.ok(response);
    }
}
