package com.nikunj.library.dto;

import jakarta.validation.constraints.NotNull;

public class CreateReservationRequest {

    @NotNull(message = "Book ID is mandatory")
    private Long bookId;

    @NotNull(message = "Member ID is mandatory")
    private Long memberId;

    public CreateReservationRequest() {
    }

    public CreateReservationRequest(Long bookId, Long memberId) {
        this.bookId = bookId;
        this.memberId = memberId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
