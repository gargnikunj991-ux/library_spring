package com.nikunj.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nikunj.library.dto.CreateMemberRequest;
import com.nikunj.library.dto.MemberResponse;
import com.nikunj.library.repository.MemberRepository;
import com.nikunj.library.service.MemberService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;

    MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostMapping  
    public MemberResponse addMember(@Valid @RequestBody CreateMemberRequest request){
        return memberService.addMember(request);
    }

    @GetMapping
    public List<MemberResponse> displayMember(){
        return memberService.displayMember();
    }
    @GetMapping("/{memberId}")
    public MemberResponse searchMember(@PathVariable Long memberId){
         return memberService.searchMember(memberId);
    }
    
    @DeleteMapping("/{memberId}")
    public void deleteMemberById(@PathVariable Long memberId){
         memberService.deleteMember(memberId);
    }
    
    @PutMapping("/{memberId}")
    public MemberResponse updateMember(@PathVariable Long memberId, @Valid @RequestBody CreateMemberRequest request){
        return memberService.updateMember(memberId, request);
    }
}
