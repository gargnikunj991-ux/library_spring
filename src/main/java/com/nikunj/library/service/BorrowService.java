package com.nikunj.library.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikunj.library.dto.BorrowResponse;
import com.nikunj.library.dto.CreateBorrowRequest;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.exception.BookUnavailableException;
import com.nikunj.library.exception.BorrowRecordNotFoundException;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.model.BookReservation;
import com.nikunj.library.model.BorrowRecord;
import com.nikunj.library.model.Member;
import com.nikunj.library.model.ReservationStatus;
import com.nikunj.library.repository.BookRepository;
import com.nikunj.library.repository.BookReservationRepository;
import com.nikunj.library.repository.BorrowRecordRepository;
import com.nikunj.library.repository.MemberRepository;

@Service
public class BorrowService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookReservationRepository bookReservationRepository;

    public BorrowService(BookRepository bookRepository,
                         MemberRepository memberRepository,
                         BorrowRecordRepository borrowRecordRepository,
                         BookReservationRepository bookReservationRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookReservationRepository = bookReservationRepository;
    }

    @Transactional
    public BorrowResponse borrowBook(CreateBorrowRequest request) {
        Long memberId = request.getMemberId();
        Long bookId   = request.getBookId();

        // Find member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        // Find book with PESSIMISTIC_WRITE lock
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        // Check if member has an active NOTIFIED_READY reservation for this book
        Optional<BookReservation> readyReservation = bookReservationRepository
                .findFirstByBookIdAndMemberMemberIdAndStatus(book.getId(), member.getMemberId(), ReservationStatus.NOTIFIED_READY);

        boolean claimingReservation = false;
        if (readyReservation.isPresent()) {
            BookReservation reservation = readyReservation.get();
            if (reservation.getPickupDeadline() != null && reservation.getPickupDeadline().isBefore(LocalDateTime.now())) {
                reservation.setStatus(ReservationStatus.EXPIRED);
                bookReservationRepository.save(reservation);
            } else {
                reservation.setStatus(ReservationStatus.CLAIMED);
                bookReservationRepository.save(reservation);
                claimingReservation = true;
            }
        }

        if (!claimingReservation) {
            // Check availability
            if (book.getAvailableCopies() <= 0) {
                throw new BookUnavailableException("Book is currently unavailable");
            }

            // Atomically decrement available copies
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);
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
                Book lockedBook = bookRepository.findByIdForUpdate(book.getId()).orElse(book);

                // Check FIFO waitlist queue for next waiting reservation
                Optional<BookReservation> nextInLine = bookReservationRepository
                        .findFirstByBookIdAndStatusOrderByReservedAtAsc(lockedBook.getId(), ReservationStatus.WAITING);

                if (nextInLine.isPresent()) {
                    BookReservation reservation = nextInLine.get();
                    reservation.setStatus(ReservationStatus.NOTIFIED_READY);
                    reservation.setPickupDeadline(LocalDateTime.now().plusHours(48));
                    bookReservationRepository.save(reservation);
                    // Do not increment availableCopies; copy is held for the reserved patron
                } else {
                    lockedBook.setAvailableCopies(Math.min(lockedBook.getTotalCopies(), lockedBook.getAvailableCopies() + 1));
                    bookRepository.save(lockedBook);
                }
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

