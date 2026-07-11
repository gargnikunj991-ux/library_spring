package com.nikunj.library.exception; 

public class MemberNotFoundException extends RuntimeException{
    
    public MemberNotFoundException(String message){
        super(message);
    }
}
