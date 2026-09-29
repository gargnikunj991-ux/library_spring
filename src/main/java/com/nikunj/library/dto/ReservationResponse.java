package com.nikunj.library.dto;

import java.time.LocalDateTime;

import com.nikunj.library.model.ReservationStatus;

public class ReservationResponse {

    private Long reservationId;
    private Long bookId;
    private String bookTitle;
    private Long memberId;
    private String memberName;
    private ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime pickupDeadline;

    public ReservationResponse() {
    }

    public ReservationResponse(Long reservationId, Long bookId, String bookTitle, Long memberId,
                               String memberName, ReservationStatus status,
                               LocalDateTime reservedAt, LocalDateTime pickupDeadline) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.status = status;
        this.reservedAt = reservedAt;
        this.pickupDeadline = pickupDeadline;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }

    public LocalDateTime getPickupDeadline() {
        return pickupDeadline;
    }

    public void setPickupDeadline(LocalDateTime pickupDeadline) {
        this.pickupDeadline = pickupDeadline;
    }
}
