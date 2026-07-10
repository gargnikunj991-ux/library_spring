package com.nikunj.library.dto;


public class MemberResponse {

    private Long memberId;
    private String name;
    private String email;
    private String phoneNumber;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }
    public void setPhoneNumber(String  phoneNumber){
        this.phoneNumber =phoneNumber;
    }
}
