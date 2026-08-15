package com.nikunj.library.service;

import com.nikunj.library.dto.CreateMemberRequest;
import com.nikunj.library.dto.MemberResponse;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.model.Member;
import com.nikunj.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member sampleMember;
    private CreateMemberRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleMember = new Member();
        sampleMember.setMemberId(1L);
        sampleMember.setName("Alice");
        sampleMember.setEmail("alice@example.com");
        sampleMember.setPhoneNumber("9876543210");

        createRequest = new CreateMemberRequest();
        createRequest.setName("Alice");
        createRequest.setEmail("alice@example.com");
        createRequest.setPhoneNumber("9876543210");
    }

    @Test
    @DisplayName("Add Member: Successfully persists and returns member response")
    void testAddMember_Success() {
        when(memberRepository.save(any(Member.class))).thenReturn(sampleMember);

        MemberResponse response = memberService.addMember(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getMemberId());
        assertEquals("Alice", response.getName());
        assertEquals("alice@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Display Members: Returns list of members")
    void testDisplayMember_Success() {
        when(memberRepository.findAll()).thenReturn(List.of(sampleMember));

        List<MemberResponse> list = memberService.displayMember();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Alice", list.get(0).getName());
    }

    @Test
    @DisplayName("Search Member: Returns member when ID exists")
    void testSearchMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));

        MemberResponse response = memberService.searchMember(1L);

        assertNotNull(response);
        assertEquals("Alice", response.getName());
    }

    @Test
    @DisplayName("Search Member: Throws MemberNotFoundException when ID does not exist")
    void testSearchMember_NotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> memberService.searchMember(999L));
    }

    @Test
    @DisplayName("Delete Member: Successfully deletes member when ID exists")
    void testDeleteMember_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(sampleMember));
        doNothing().when(memberRepository).delete(sampleMember);

        memberService.deleteMember(1L);

        verify(memberRepository, times(1)).delete(sampleMember);
    }
}
