package com.nikunj.library.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikunj.library.dto.CreateReservationRequest;
import com.nikunj.library.dto.ReservationResponse;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.exception.DuplicateReservationException;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.exception.ReservationNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.model.BookReservation;
import com.nikunj.library.model.Member;
import com.nikunj.library.model.ReservationStatus;
import com.nikunj.library.repository.BookRepository;
import com.nikunj.library.repository.BookReservationRepository;
import com.nikunj.library.repository.MemberRepository;

@Service
public class ReservationService {

    private final BookReservationRepository bookReservationRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public ReservationService(BookReservationRepository bookReservationRepository,
                              BookRepository bookRepository,
                              MemberRepository memberRepository) {
        this.bookReservationRepository = bookReservationRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Long memberId = request.getMemberId();
        Long bookId = request.getBookId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        if (book.getAvailableCopies() > 0) {
            throw new IllegalStateException("Book has available copies. Please borrow directly.");
        }

        boolean alreadyReserved = bookReservationRepository.existsByBookIdAndMemberMemberIdAndStatusIn(
                bookId,
                memberId,
                List.of(ReservationStatus.WAITING, ReservationStatus.NOTIFIED_READY)
        );

        if (alreadyReserved) {
            throw new DuplicateReservationException("Member already has an active reservation for this book");
        }

        BookReservation reservation = new BookReservation();
        reservation.setBook(book);
        reservation.setMember(member);
        reservation.setStatus(ReservationStatus.WAITING);
        reservation.setReservedAt(LocalDateTime.now());

        BookReservation savedReservation = bookReservationRepository.save(reservation);
        return mapToResponse(savedReservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {
        BookReservation reservation = bookReservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.CLAIMED) {
            throw new IllegalStateException("Reservation cannot be cancelled in status: " + reservation.getStatus());
        }

        if (reservation.getStatus() == ReservationStatus.NOTIFIED_READY) {
            Book book = reservation.getBook();
            var nextInLine = bookReservationRepository
                    .findFirstByBookIdAndStatusOrderByReservedAtAsc(book.getId(), ReservationStatus.WAITING);

            if (nextInLine.isPresent()) {
                BookReservation next = nextInLine.get();
                next.setStatus(ReservationStatus.NOTIFIED_READY);
                next.setPickupDeadline(LocalDateTime.now().plusHours(48));
                bookReservationRepository.save(next);
            } else {
                Book lockedBook = bookRepository.findByIdForUpdate(book.getId()).orElse(book);
                lockedBook.setAvailableCopies(Math.min(lockedBook.getTotalCopies(), lockedBook.getAvailableCopies() + 1));
                bookRepository.save(lockedBook);
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        BookReservation saved = bookReservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByBook(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        return bookReservationRepository.findByBookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.WAITING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByMember(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found"));

        return bookReservationRepository.findByMemberMemberIdOrderByReservedAtDesc(memberId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long reservationId) {
        BookReservation reservation = bookReservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        return mapToResponse(reservation);
    }

    private ReservationResponse mapToResponse(BookReservation r) {
        ReservationResponse response = new ReservationResponse();
        response.setReservationId(r.getId());
        response.setBookId(r.getBook() != null ? r.getBook().getId() : null);
        response.setBookTitle(r.getBook() != null ? r.getBook().getTitle() : null);
        response.setMemberId(r.getMember() != null ? r.getMember().getMemberId() : null);
        response.setMemberName(r.getMember() != null ? r.getMember().getName() : null);
        response.setStatus(r.getStatus());
        response.setReservedAt(r.getReservedAt());
        response.setPickupDeadline(r.getPickupDeadline());
        return response;
    }
}
