package com.nikunj.library.dto;

import jakarta.validation.constraints.NotNull;
public class CreateBorrowRequest {
    @NotNull(message="BookId is mandary")
    private Long bookId;
    @NotNull(message="MemberId is mandatory")
    private Long memberId;

    public Long getMemberId(){
        return memberId;
    }
     public void setMemberId(Long memberId){
        this.memberId = memberId;
    }
    public Long getBookId(){
        return bookId;
    }
    public void setBookId(Long bookId){
        this.bookId= bookId;
    }
}
