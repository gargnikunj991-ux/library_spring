package com.nikunj.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private BookReservationRepository bookReservationRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Member sampleMember;
    private Book sampleBook;
    private CreateReservationRequest reservationRequest;

    @BeforeEach
    void setUp() {
        sampleMember = new Member();
        sampleMember.setMemberId(1L);
        sampleMember.setName("Alice Cooper");
        sampleMember.setEmail("alice@example.com");

        sampleBook = new Book();
        sampleBook.setId(10L);
        sampleBook.setTitle("Designing Data-Intensive Applications");
        sampleBook.setAuthor("Martin Kleppmann");
        sampleBook.setTotalCopies(1);
        sampleBook.setAvailableCopies(0); // All copies checked out

        reservationRequest = new CreateReservationRequest(10L, 1L);
    }

    @Test
    @DisplayName("Create Reservation: Success when copies are 0 and no active reservation")
    void testCreateReservation_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));
        when(bookReservationRepository.existsByBookIdAndMemberMemberIdAndStatusIn(eq(10L), eq(1L), any()))
                .thenReturn(false);

        BookReservation saved = new BookReservation(sampleBook, sampleMember, ReservationStatus.WAITING, LocalDateTime.now());
        saved.setId(100L);
        when(bookReservationRepository.save(any(BookReservation.class))).thenReturn(saved);

        ReservationResponse response = reservationService.createReservation(reservationRequest);

        assertNotNull(response);
        assertEquals(100L, response.getReservationId());
        assertEquals(ReservationStatus.WAITING, response.getStatus());
        assertEquals("Designing Data-Intensive Applications", response.getBookTitle());
        assertEquals("Alice Cooper", response.getMemberName());
        verify(bookReservationRepository, times(1)).save(any(BookReservation.class));
    }

    @Test
    @DisplayName("Create Reservation: Throws IllegalStateException when copies are still available")
    void testCreateReservation_ThrowsWhenCopiesAvailable() {
        sampleBook.setAvailableCopies(1);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));

        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(reservationRequest));
        verify(bookReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Reservation: Throws MemberNotFoundException when member missing")
    void testCreateReservation_MemberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> reservationService.createReservation(reservationRequest));
    }

    @Test
    @DisplayName("Create Reservation: Throws BookNotFoundException when book missing")
    void testCreateReservation_BookNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> reservationService.createReservation(reservationRequest));
    }

    @Test
    @DisplayName("Create Reservation: Throws DuplicateReservationException when member is already in queue")
    void testCreateReservation_DuplicateReservation() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));
        when(bookReservationRepository.existsByBookIdAndMemberMemberIdAndStatusIn(eq(10L), eq(1L), any()))
                .thenReturn(true);

        assertThrows(DuplicateReservationException.class, () -> reservationService.createReservation(reservationRequest));
    }

    @Test
    @DisplayName("Cancel Reservation: Successfully cancels WAITING reservation")
    void testCancelReservation_Waiting() {
        BookReservation reservation = new BookReservation(sampleBook, sampleMember, ReservationStatus.WAITING, LocalDateTime.now());
        reservation.setId(100L);

        when(bookReservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(bookReservationRepository.save(any(BookReservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(100L);

        assertNotNull(response);
        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
        verify(bookReservationRepository, times(1)).save(reservation);
    }

    @Test
    @DisplayName("Cancel Reservation: NOTIFIED_READY reservation reassigns held copy to next waiting patron")
    void testCancelReservation_NotifiedReady_ReassignsToNext() {
        BookReservation currentReservation = new BookReservation(sampleBook, sampleMember, ReservationStatus.NOTIFIED_READY, LocalDateTime.now());
        currentReservation.setId(100L);

        Member nextMember = new Member();
        nextMember.setMemberId(2L);
        nextMember.setName("Bob Dylan");

        BookReservation nextWaiting = new BookReservation(sampleBook, nextMember, ReservationStatus.WAITING, LocalDateTime.now());
        nextWaiting.setId(101L);

        when(bookReservationRepository.findById(100L)).thenReturn(Optional.of(currentReservation));
        when(bookReservationRepository.findFirstByBookIdAndStatusOrderByReservedAtAsc(10L, ReservationStatus.WAITING))
                .thenReturn(Optional.of(nextWaiting));
        when(bookReservationRepository.save(any(BookReservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(100L);

        assertNotNull(response);
        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
        assertEquals(ReservationStatus.NOTIFIED_READY, nextWaiting.getStatus());
        assertNotNull(nextWaiting.getPickupDeadline());

        verify(bookReservationRepository, times(1)).save(nextWaiting);
        verify(bookReservationRepository, times(1)).save(currentReservation);
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancel Reservation: NOTIFIED_READY reservation returns copy to available inventory when queue is empty")
    void testCancelReservation_NotifiedReady_ReturnsToInventoryWhenQueueEmpty() {
        BookReservation currentReservation = new BookReservation(sampleBook, sampleMember, ReservationStatus.NOTIFIED_READY, LocalDateTime.now());
        currentReservation.setId(100L);

        when(bookReservationRepository.findById(100L)).thenReturn(Optional.of(currentReservation));
        when(bookReservationRepository.findFirstByBookIdAndStatusOrderByReservedAtAsc(10L, ReservationStatus.WAITING))
                .thenReturn(Optional.empty());
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(sampleBook));
        when(bookReservationRepository.save(any(BookReservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(100L);

        assertNotNull(response);
        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
        assertEquals(1, sampleBook.getAvailableCopies());
        verify(bookRepository, times(1)).save(sampleBook);
    }

    @Test
    @DisplayName("Cancel Reservation: Throws IllegalStateException when already CANCELLED or CLAIMED")
    void testCancelReservation_AlreadyCancelled() {
        BookReservation reservation = new BookReservation(sampleBook, sampleMember, ReservationStatus.CANCELLED, LocalDateTime.now());
        reservation.setId(100L);

        when(bookReservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(100L));
    }

    @Test
    @DisplayName("Cancel Reservation: Throws ReservationNotFoundException when ID missing")
    void testCancelReservation_NotFound() {
        when(bookReservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> reservationService.cancelReservation(999L));
    }

    @Test
    @DisplayName("Get Reservations: Returns active waiting reservations by book ID")
    void testGetReservationsByBook() {
        BookReservation reservation = new BookReservation(sampleBook, sampleMember, ReservationStatus.WAITING, LocalDateTime.now());
        reservation.setId(100L);

        when(bookRepository.findById(10L)).thenReturn(Optional.of(sampleBook));
        when(bookReservationRepository.findByBookIdAndStatusOrderByReservedAtAsc(10L, ReservationStatus.WAITING))
                .thenReturn(List.of(reservation));

        List<ReservationResponse> responses = reservationService.getReservationsByBook(10L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getReservationId());
    }

    @Test
    @DisplayName("Get Reservations: Returns reservation history by member ID")
    void testGetReservationsByMember() {
        BookReservation reservation = new BookReservation(sampleBook, sampleMember, ReservationStatus.WAITING, LocalDateTime.now());
        reservation.setId(100L);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        when(bookReservationRepository.findByMemberMemberIdOrderByReservedAtDesc(1L))
                .thenReturn(List.of(reservation));

        List<ReservationResponse> responses = reservationService.getReservationsByMember(1L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getReservationId());
    }
}
