package com.nikunj.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nikunj.library.model.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

}