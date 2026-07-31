package com.nikunj.library.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikunj.library.dto.BorrowResponse;
import com.nikunj.library.dto.CreateBorrowRequest;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.exception.BookUnavailableException;
import com.nikunj.library.exception.BorrowRecordNotFoundException;
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

    @Transactional
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

    @Transactional
    public BorrowResponse returnBook(Long borrowId) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new BorrowRecordNotFoundException("Borrow record not found"));

        if (!borrowRecord.isReturned()) {
            borrowRecord.setReturned(true);
            borrowRecord.setReturnDate(LocalDate.now());

            Book book = borrowRecord.getBook();
            if (book != null) {
                book.setAvailable(true);
                bookRepository.save(book);
            }

            borrowRecordRepository.save(borrowRecord);
        }

        BorrowResponse response = new BorrowResponse();
        response.setBorrowId(borrowRecord.getBorrowId());
        response.setBookId(borrowRecord.getBook() != null ? borrowRecord.getBook().getId() : null);
        response.setMemberName(borrowRecord.getMember() != null ? borrowRecord.getMember().getName() : null);
        response.setBookTitle(borrowRecord.getBook() != null ? borrowRecord.getBook().getTitle() : null);
        response.setBorrowDate(borrowRecord.getBorrowDate());
        response.setDueDate(borrowRecord.getDueDate());
        response.setReturned(borrowRecord.isReturned());

        return response;
    }
}

