package com.nikunj.library.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikunj.library.dto.BorrowResponse;
import com.nikunj.library.dto.CreateBorrowRequest;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.exception.BookUnavailableException;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.model.BorrowRecord;
import com.nikunj.library.model.Member;
import com.nikunj.library.repository.BookRepository;
import com.nikunj.library.repository.BorrowRecordRepository;
import com.nikunj.library.repository.MemberRepository;

@Service
public class BorrowService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    public BorrowResponse borrowBook(CreateBorrowRequest request) {
        Long memberId = request.getMemberId();
        Long bookId   = request.getBookId();

        // Find member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        // Find book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        // Check availability
        if (!book.isAvailable()) {
            throw new BookUnavailableException("Book is currently unavailable");
        }

        // Create BorrowRecord
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setMember(member);
        borrowRecord.setBook(book);

        // Set borrow date
        borrowRecord.setBorrowDate(LocalDate.now());

        // Set due date
        borrowRecord.setDueDate(LocalDate.now().plusDays(14));
        // Set returned = false
        borrowRecord.setReturned(false);

        // Mark book unavailable
        book.setAvailable(false);

        // Save Book
        bookRepository.save(book);

        // Save BorrowRecord
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);

        // Return BorrowResponse
        BorrowResponse response = new BorrowResponse();
        response.setBorrowId(savedRecord.getBorrowId());
        response.setBookId(book.getId());
        response.setMemberName(member.getName());
        response.setBookTitle(book.getTitle());
        response.setBorrowDate(savedRecord.getBorrowDate());
        response.setDueDate(savedRecord.getDueDate());
        response.setReturned(savedRecord.isReturned());

        return response;
    }
}
