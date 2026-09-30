package com.nikunj.library.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FineResponse {

    private Long fineId;
    private Long borrowId;
    private Long bookId;
    private String bookTitle;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private boolean paid;
    private LocalDateTime calculatedAt;
    private LocalDateTime paidAt;

    public FineResponse() {
    }

    public FineResponse(Long fineId, Long borrowId, Long bookId, String bookTitle,
                        Long memberId, String memberName, BigDecimal amount,
                        boolean paid, LocalDateTime calculatedAt, LocalDateTime paidAt) {
        this.fineId = fineId;
        this.borrowId = borrowId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.amount = amount;
        this.paid = paid;
        this.calculatedAt = calculatedAt;
        this.paidAt = paidAt;
    }

    public Long getFineId() {
        return fineId;
    }

    public void setFineId(Long fineId) {
        this.fineId = fineId;
    }

    public Long getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Long borrowId) {
        this.borrowId = borrowId;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
