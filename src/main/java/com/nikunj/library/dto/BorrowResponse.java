package com.nikunj.library.dto;

import java.time.LocalDate;

public class BorrowResponse {
    private Long borrowId;
    private Long bookId;
    private String memberName;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

  public Long getBorrowId(){
    return borrowId;
  }
  public void setBorrowId(Long borrowId){
    this.borrowId=borrowId;
  }
  public Long getBookId(){
    return bookId;
  }
  public void setBookId(Long bookId){
    this.bookId=bookId;
  }
  public String getMemberName(){
    return memberName;
  }
  public void setMemberName(String memberName){
    this.memberName = memberName;
  }
  public String getBookTitle(){
    return bookTitle;
  }
  public void setBookTitle(String bookTitle){
    this.bookTitle = bookTitle;
  }
  public LocalDate getBorrowDate(){
    return borrowDate;
  }
  public void setBorrowDate(LocalDate borrowDate){
    this.borrowDate =borrowDate;
  }
  public LocalDate getDueDate(){
    return dueDate;
  }
  public void setDueDate(LocalDate dueDate){
    this.dueDate=dueDate;
  }
  public boolean getReturned(){
    return returned;
  }
  public void setReturned(boolean returned){
    this.returned = returned;
  }
 
}
