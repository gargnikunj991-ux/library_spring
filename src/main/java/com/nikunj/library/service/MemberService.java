package com.nikunj.library.service;


import java.util.ArrayList;
import java .util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nikunj.library.dto.BookResponse;
import com.nikunj.library.dto.CreateBookRequest;
import com.nikunj.library.dto.CreateMemberRequest;
import com.nikunj.library.dto.MemberResponse;
import com.nikunj.library.exception.BookNotFoundException;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.model.Book;
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
    public void deleteMember(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(()-> new MemberNotFoundException("Member not found"));
        memberRepository.delete(member); 
    }

    private MemberResponse mapToMemberResponse(Member member){
        MemberResponse response = new MemberResponse();
        response.setEmail(member.getEmail());
        response.setName(member.getName());
        response.setMemberId(member.getMemberId());
        response.setPhoneNumber(member.getPhoneNumber());
        return response; 

    }
        public MemberResponse updateMember(Long memberId ,CreateMemberRequest request){
         Member dbMember = memberRepository.findById(memberId).orElseThrow(()-> new  BookNotFoundException("Member not found"));
        
        dbMember.setName(request.getName());
        dbMember.setEmail(request.getEmail());
        dbMember.setPhoneNumber(request.getPhoneNumber());
        Member member =memberRepository.save(dbMember);
        
        return mapToMemberResponse(member);
}
}
