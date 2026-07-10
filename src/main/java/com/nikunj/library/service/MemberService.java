package com.nikunj.library.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikunj.library.dto.CreateMemberRequest;
import  com.nikunj.library.repository.MemberRepository;
import  com.nikunj.library.model.Member;
import jakarta.validation.Valid;

@Service
public class MemberService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    public Member addMember(CreateMemberRequest request){
        return memberRepository.save(request);
    }
}
