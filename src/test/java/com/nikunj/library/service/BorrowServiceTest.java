package com.nikunj.library.service;

import com.nikunj.library.dto.BorrowResponse;
import com.nikunj.library.dto.CreateBorrowRequest;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.exception.BookUnavailableException;
import com.nikunj.library.exception.BorrowRecordNotFoundException;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.model.BorrowRecord;
import com.nikunj.library.model.BookReservation;
import com.nikunj.library.model.Member;
import com.nikunj.library.model.ReservationStatus;
import com.nikunj.library.repository.BookRepository;
import com.nikunj.library.repository.BookReservationRepository;
import com.nikunj.library.repository.BorrowRecordRepository;
import com.nikunj.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BorrowServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookReservationRepository bookReservationRepository;

    @InjectMocks
    private BorrowService borrowService;

    private Member sampleMember;
    private Book sampleBook;
    private CreateBorrowRequest borrowRequest;

    @BeforeEach
    void setUp() {
        sampleMember = new Member();
        sampleMember.setMemberId(1L);
        sampleMember.setName("John Doe");
        sampleMember.setEmail("john@example.com");
        sampleMember.setPhoneNumber("1234567890");

        sampleBook = new Book();
        sampleBook.setId(10L);
        sampleBook.setTitle("Effective Java");
        sampleBook.setAuthor("Joshua Bloch");
        sampleBook.setTotalCopies(1);
        sampleBook.setAvailableCopies(1);

        borrowRequest = new CreateBorrowRequest();
        borrowRequest.setMemberId(1L);
        borrowRequest.setBookId(10L);
    }

    @Test
    @DisplayName("Borrow Book: Success when member and book exist and book is available")
    void testBorrowBook_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));

        BorrowRecord savedRecord = new BorrowRecord();
        savedRecord.setBorrowId(100L);
        savedRecord.setMember(sampleMember);
        savedRecord.setBook(sampleBook);
        savedRecord.setBorrowDate(LocalDate.now());
        savedRecord.setDueDate(LocalDate.now().plusDays(14));
        savedRecord.setReturned(false);

        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(savedRecord);

        BorrowResponse response = borrowService.borrowBook(borrowRequest);

        assertNotNull(response);
        assertEquals(100L, response.getBorrowId());
        assertEquals("Effective Java", response.getBookTitle());
        assertEquals("John Doe", response.getMemberName());
        assertFalse(response.isReturned());
        assertEquals(0, sampleBook.getAvailableCopies());
        assertFalse(sampleBook.isAvailable());

        verify(bookRepository, times(1)).save(sampleBook);
        verify(borrowRecordRepository, times(1)).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("Borrow Book: Throws MemberNotFoundException when member ID does not exist")
    void testBorrowBook_MemberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> borrowService.borrowBook(borrowRequest));
        verify(bookRepository, never()).save(any());
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Borrow Book: Throws BookNotFoundException when book ID does not exist")
    void testBorrowBook_BookNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> borrowService.borrowBook(borrowRequest));
        verify(bookRepository, never()).save(any());
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Borrow Book: Throws BookUnavailableException when book is already borrowed")
    void testBorrowBook_BookUnavailable() {
        sampleBook.setAvailableCopies(0);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));

        assertThrows(BookUnavailableException.class, () -> borrowService.borrowBook(borrowRequest));
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Return Book: Successfully marks record returned and resets book availability")
    void testReturnBook_Success() {
        sampleBook.setAvailableCopies(0);

        BorrowRecord record = new BorrowRecord();
        record.setBorrowId(100L);
        record.setMember(sampleMember);
        record.setBook(sampleBook);
        record.setBorrowDate(LocalDate.now().minusDays(5));
        record.setDueDate(LocalDate.now().plusDays(9));
        record.setReturned(false);

        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(record));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));

        BorrowResponse response = borrowService.returnBook(100L);

        assertNotNull(response);
        assertTrue(response.isReturned());
        assertEquals(1, sampleBook.getAvailableCopies());
        assertTrue(sampleBook.isAvailable());
        assertTrue(record.isReturned());
        assertEquals(LocalDate.now(), record.getReturnDate());

        verify(bookRepository, times(1)).save(sampleBook);
        verify(borrowRecordRepository, times(1)).save(record);
    }

    @Test
    @DisplayName("Return Book: Throws BorrowRecordNotFoundException when borrow ID does not exist")
    void testReturnBook_NotFound() {
        when(borrowRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BorrowRecordNotFoundException.class, () -> borrowService.returnBook(999L));
    }

    @Test
    @DisplayName("Borrow Book: Multi-copy book decrements available copies by 1 and remains available")
    void testBorrowBook_MultipleCopies_DecrementOnlyOne() {
        sampleBook.setTotalCopies(5);
        sampleBook.setAvailableCopies(5);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));

        BorrowRecord savedRecord = new BorrowRecord();
        savedRecord.setBorrowId(101L);
        savedRecord.setMember(sampleMember);
        savedRecord.setBook(sampleBook);
        savedRecord.setBorrowDate(LocalDate.now());
        savedRecord.setDueDate(LocalDate.now().plusDays(14));
        savedRecord.setReturned(false);

        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(savedRecord);

        BorrowResponse response = borrowService.borrowBook(borrowRequest);

        assertNotNull(response);
        assertEquals(4, sampleBook.getAvailableCopies());
        assertTrue(sampleBook.isAvailable());
        verify(bookRepository, times(1)).save(sampleBook);
    }

    @Test
    @DisplayName("Return Book: Available copies cannot exceed total copies")
    void testReturnBook_CannotExceedTotalCopies() {
        sampleBook.setTotalCopies(3);
        sampleBook.setAvailableCopies(3);

        BorrowRecord record = new BorrowRecord();
        record.setBorrowId(102L);
        record.setMember(sampleMember);
        record.setBook(sampleBook);
        record.setReturned(false);

        when(borrowRecordRepository.findById(102L)).thenReturn(Optional.of(record));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));

        BorrowResponse response = borrowService.returnBook(102L);

        assertNotNull(response);
        assertEquals(3, sampleBook.getAvailableCopies());
        verify(bookRepository, times(1)).save(sampleBook);
    }

    @Test
    @DisplayName("Return Book: When waiting reservation exists, locks book for reservation and leaves availableCopies untouched")
    void testReturnBook_AutoAssignsToWaitingReservation() {
        sampleBook.setAvailableCopies(0);

        BorrowRecord record = new BorrowRecord();
        record.setBorrowId(103L);
        record.setMember(sampleMember);
        record.setBook(sampleBook);
        record.setReturned(false);

        Member waitingMember = new Member();
        waitingMember.setMemberId(2L);
        waitingMember.setName("Bob Smith");

        BookReservation waitingReservation = new BookReservation();
        waitingReservation.setId(50L);
        waitingReservation.setBook(sampleBook);
        waitingReservation.setMember(waitingMember);
        waitingReservation.setStatus(ReservationStatus.WAITING);

        when(borrowRecordRepository.findById(103L)).thenReturn(Optional.of(record));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));
        when(bookReservationRepository.findFirstByBookIdAndStatusOrderByReservedAtAsc(10L, ReservationStatus.WAITING))
                .thenReturn(Optional.of(waitingReservation));

        BorrowResponse response = borrowService.returnBook(103L);

        assertNotNull(response);
        assertTrue(response.isReturned());
        assertEquals(0, sampleBook.getAvailableCopies()); // Still 0, held for Bob!
        assertEquals(ReservationStatus.NOTIFIED_READY, waitingReservation.getStatus());
        assertNotNull(waitingReservation.getPickupDeadline());

        verify(bookReservationRepository, times(1)).save(waitingReservation);
        verify(bookRepository, never()).save(sampleBook);
    }

    @Test
    @DisplayName("Borrow Book: Member with NOTIFIED_READY reservation claims copy even when availableCopies is 0")
    void testBorrowBook_ClaimReadyReservation() {
        sampleBook.setAvailableCopies(0);

        BookReservation readyReservation = new BookReservation();
        readyReservation.setId(51L);
        readyReservation.setBook(sampleBook);
        readyReservation.setMember(sampleMember);
        readyReservation.setStatus(ReservationStatus.NOTIFIED_READY);
        readyReservation.setPickupDeadline(LocalDateTime.now().plusHours(24));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));
        when(bookReservationRepository.findFirstByBookIdAndMemberMemberIdAndStatus(10L, 1L, ReservationStatus.NOTIFIED_READY))
                .thenReturn(Optional.of(readyReservation));

        BorrowRecord savedRecord = new BorrowRecord();
        savedRecord.setBorrowId(104L);
        savedRecord.setMember(sampleMember);
        savedRecord.setBook(sampleBook);
        savedRecord.setBorrowDate(LocalDate.now());
        savedRecord.setDueDate(LocalDate.now().plusDays(14));
        savedRecord.setReturned(false);

        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(savedRecord);

        BorrowResponse response = borrowService.borrowBook(borrowRequest);

        assertNotNull(response);
        assertEquals(ReservationStatus.CLAIMED, readyReservation.getStatus());
        verify(bookReservationRepository, times(1)).save(readyReservation);
        verify(borrowRecordRepository, times(1)).save(any(BorrowRecord.class));
    }
}
