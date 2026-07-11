package com.nikunj.library.service;


import java.util.ArrayList;
import java .util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikunj.library.dto.CreateMemberRequest;
import com.nikunj.library.dto.MemberResponse;
import com.nikunj.library.exception.MemberNotFoundException;
import  com.nikunj.library.model.Member;
import  com.nikunj.library.repository.MemberRepository;

@Service
public class MemberService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    public MemberResponse addMember(CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhoneNumber(request.getPhoneNumber());
        Member savedMember = memberRepository.save(member);
      return mapToMemberResponse(savedMember);
    }

    public List<MemberResponse> displayMember(){
         List<Member> searchMember = memberRepository.findAll();
       List<MemberResponse> responses = new ArrayList<>();
       for(Member member : searchMember){
        responses.add(mapToMemberResponse(member));
       }
       return responses;
}
    public MemberResponse searchMember(Long  memberId){
          Member member =memberRepository.findById(memberId).orElseThrow(()-> new MemberNotFoundException(" Member not found"));
       return mapToMemberResponse(member);
    }

    private MemberResponse mapToMemberResponse(Member member){
        MemberResponse response = new MemberResponse();
        response.setEmail(member.getEmail());
        response.setName(member.getName());
        response.setMemberId(member.getMemberId());
        response.setPhoneNumber(member.getPhoneNumber());
        return response; 

    }
}
