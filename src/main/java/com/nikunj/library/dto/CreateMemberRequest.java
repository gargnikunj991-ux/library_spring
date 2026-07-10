package com.nikunj.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public class CreateMemberRequest {
    @NotBlank(message="Name is mandary") 
    private String name;
    @Email(message="Email is must be in a proper format ")  
    private String email;
    @NotBlank(message="PhoneNumber is mandary")
    private String phoneNumber;

   

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
