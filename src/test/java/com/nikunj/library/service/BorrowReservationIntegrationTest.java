package com.nikunj.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nikunj.library.dto.BorrowResponse;
import com.nikunj.library.dto.CreateBorrowRequest;
import com.nikunj.library.dto.CreateReservationRequest;
import com.nikunj.library.dto.ReservationResponse;
import com.nikunj.library.exception.BookUnavailableException;
import com.nikunj.library.model.Book;
import com.nikunj.library.model.BookReservation;
import com.nikunj.library.model.Member;
import com.nikunj.library.model.ReservationStatus;
import com.nikunj.library.repository.BookRepository;
import com.nikunj.library.repository.BookReservationRepository;
import com.nikunj.library.repository.BorrowRecordRepository;
import com.nikunj.library.repository.MemberRepository;

@SpringBootTest
public class BorrowReservationIntegrationTest {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private BookReservationRepository bookReservationRepository;

    @BeforeEach
    void cleanUp() {
        borrowRecordRepository.deleteAll();
        bookReservationRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Full FIFO Waitlist lifecycle — loan exhaustion, queueing, auto-assignment on return, and claim")
    void testFifoWaitlistEndToEndLifecycle() {
        // 1. Setup Book with 1 copy
        Book book = new Book();
        book.setTitle("Designing Distributed Systems");
        book.setAuthor("Brendan Burns");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        Book savedBook = bookRepository.save(book);

        // 2. Setup 3 Members
        Member memberA = memberRepository.save(new Member());
        memberA.setName("Alice");
        memberA.setEmail("alice@test.com");
        memberA.setPhoneNumber("1111111111");
        memberA = memberRepository.save(memberA);

        Member memberB = memberRepository.save(new Member());
        memberB.setName("Bob");
        memberB.setEmail("bob@test.com");
        memberB.setPhoneNumber("2222222222");
        memberB = memberRepository.save(memberB);

        Member memberC = memberRepository.save(new Member());
        memberC.setName("Charlie");
        memberC.setEmail("charlie@test.com");
        memberC.setPhoneNumber("3333333333");
        memberC = memberRepository.save(memberC);

        // 3. Member A borrows the only copy
        CreateBorrowRequest borrowA = new CreateBorrowRequest(savedBook.getId(), memberA.getMemberId());
        BorrowResponse borrowRespA = borrowService.borrowBook(borrowA);
        assertNotNull(borrowRespA);

        Book afterBorrowA = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(0, afterBorrowA.getAvailableCopies());

        // 4. Member B attempts to borrow and gets rejected
        CreateBorrowRequest borrowB = new CreateBorrowRequest(savedBook.getId(), memberB.getMemberId());
        assertThrows(BookUnavailableException.class, () -> borrowService.borrowBook(borrowB));

        // 5. Member B joins the FIFO waitlist
        ReservationResponse resB = reservationService.createReservation(
                new CreateReservationRequest(savedBook.getId(), memberB.getMemberId()));
        assertEquals(ReservationStatus.WAITING, resB.getStatus());

        // 6. Member C joins the FIFO waitlist behind Member B
        ReservationResponse resC = reservationService.createReservation(
                new CreateReservationRequest(savedBook.getId(), memberC.getMemberId()));
        assertEquals(ReservationStatus.WAITING, resC.getStatus());

        // Verify queue order
        List<ReservationResponse> queue = reservationService.getReservationsByBook(savedBook.getId());
        assertEquals(2, queue.size());
        assertEquals(memberB.getMemberId(), queue.get(0).getMemberId());
        assertEquals(memberC.getMemberId(), queue.get(1).getMemberId());

        // 7. Member A returns the book
        BorrowResponse returnRespA = borrowService.returnBook(borrowRespA.getBorrowId());
        assertTrue(returnRespA.isReturned());

        // The book must NOT be available to public yet! (availableCopies remains 0)
        Book afterReturnA = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(0, afterReturnA.getAvailableCopies());

        // Member B's reservation must now be NOTIFIED_READY with a 48h deadline
        BookReservation updatedResB = bookReservationRepository.findById(resB.getReservationId()).orElseThrow();
        assertEquals(ReservationStatus.NOTIFIED_READY, updatedResB.getStatus());
        assertNotNull(updatedResB.getPickupDeadline());

        // Member C is still WAITING
        BookReservation updatedResC = bookReservationRepository.findById(resC.getReservationId()).orElseThrow();
        assertEquals(ReservationStatus.WAITING, updatedResC.getStatus());

        // Member C tries to borrow, but copy is held for Member B -> rejected
        CreateBorrowRequest borrowC = new CreateBorrowRequest(savedBook.getId(), memberC.getMemberId());
        assertThrows(BookUnavailableException.class, () -> borrowService.borrowBook(borrowC));

        // 8. Member B claims their reserved copy by borrowing
        BorrowResponse borrowRespB = borrowService.borrowBook(borrowB);
        assertNotNull(borrowRespB);

        BookReservation claimedResB = bookReservationRepository.findById(resB.getReservationId()).orElseThrow();
        assertEquals(ReservationStatus.CLAIMED, claimedResB.getStatus());

        // 9. Member B returns the book -> Auto-assigns to Member C!
        borrowService.returnBook(borrowRespB.getBorrowId());

        Book afterReturnB = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(0, afterReturnB.getAvailableCopies()); // still 0, held for Member C

        BookReservation readyResC = bookReservationRepository.findById(resC.getReservationId()).orElseThrow();
        assertEquals(ReservationStatus.NOTIFIED_READY, readyResC.getStatus());
        assertNotNull(readyResC.getPickupDeadline());

        // 10. Member C decides to cancel their reservation -> copy is restored to public inventory!
        reservationService.cancelReservation(resC.getReservationId());

        Book afterCancelC = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(1, afterCancelC.getAvailableCopies());
        assertTrue(afterCancelC.isAvailable());
    }
}
