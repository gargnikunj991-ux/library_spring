package com.nikunj.library.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nikunj.library.model.BookReservation;
import com.nikunj.library.model.ReservationStatus;

@Repository
public interface BookReservationRepository extends JpaRepository<BookReservation, Long> {

    Optional<BookReservation> findFirstByBookIdAndStatusOrderByReservedAtAsc(Long bookId, ReservationStatus status);

    List<BookReservation> findByBookIdAndStatusOrderByReservedAtAsc(Long bookId, ReservationStatus status);

    List<BookReservation> findByMemberMemberIdOrderByReservedAtDesc(Long memberId);

    boolean existsByBookIdAndMemberMemberIdAndStatusIn(Long bookId, Long memberId, Collection<ReservationStatus> statuses);

    Optional<BookReservation> findFirstByBookIdAndMemberMemberIdAndStatus(Long bookId, Long memberId, ReservationStatus status);

    List<BookReservation> findByStatusAndPickupDeadlineBefore(ReservationStatus status, LocalDateTime deadline);
}
