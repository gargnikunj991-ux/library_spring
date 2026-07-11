package com.nikunj.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nikunj.library.dto.CreateMemberRequest;
import com.nikunj.library.dto.MemberResponse;
import com.nikunj.library.service.MemberService;

import java.util.List;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping  
    public MemberResponse addMember(@Valid @RequestBody CreateMemberRequest request){
        return memberService.addMember(request);
    }

    @GetMapping
    public List<MemberResponse> displayMember(){
        return memberService.displayMember();
    }
}
