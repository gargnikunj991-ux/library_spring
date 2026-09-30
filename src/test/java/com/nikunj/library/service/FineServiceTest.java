package com.nikunj.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nikunj.library.dto.FineResponse;
import com.nikunj.library.exception.FineNotFoundException;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.model.Book;
import com.nikunj.library.model.BorrowRecord;
import com.nikunj.library.model.FineRecord;
import com.nikunj.library.model.Member;
import com.nikunj.library.repository.BorrowRecordRepository;
import com.nikunj.library.repository.FineRecordRepository;
import com.nikunj.library.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
public class FineServiceTest {

    @Mock
    private FineRecordRepository fineRecordRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FineService fineService;

    private Member testMember;
    private Book testBook;
    private BorrowRecord testBorrowRecord;
    private FineRecord testFineRecord;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setMemberId(1L);
        testMember.setName("Alice Smith");
        testMember.setEmail("alice@example.com");

        testBook = new Book();
        testBook.setId(10L);
        testBook.setTitle("Designing Data-Intensive Applications");
        testBook.setAuthor("Martin Kleppmann");
        testBook.setTotalCopies(3);
        testBook.setAvailableCopies(2);

        testBorrowRecord = new BorrowRecord();
        testBorrowRecord.setBorrowId(100L);
        testBorrowRecord.setMember(testMember);
        testBorrowRecord.setBook(testBook);
        testBorrowRecord.setBorrowDate(LocalDate.now().minusDays(24));
        testBorrowRecord.setDueDate(LocalDate.now().minusDays(10));
        testBorrowRecord.setReturned(false);

        testFineRecord = new FineRecord(testBorrowRecord, testMember, BigDecimal.valueOf(30));
        testFineRecord.setFineId(500L);
    }

    @Test
    @DisplayName("Tiered Fine: 0 or negative days overdue should return 0")
    void testCalculateTieredFine_ZeroOrNegative() {
        assertEquals(BigDecimal.ZERO, fineService.calculateTieredFine(0));
        assertEquals(BigDecimal.ZERO, fineService.calculateTieredFine(-5));
    }

    @Test
    @DisplayName("Tiered Fine: Tier 1 (1 to 5 days) at ₹1/day")
    void testCalculateTieredFine_Tier1() {
        // 3 days overdue = 3 * 1 = ₹3
        assertEquals(BigDecimal.valueOf(3L), fineService.calculateTieredFine(3));
        // 5 days overdue = 5 * 1 = ₹5
        assertEquals(BigDecimal.valueOf(5L), fineService.calculateTieredFine(5));
    }

    @Test
    @DisplayName("Tiered Fine: Tier 2 (6 to 15 days) first 5 days @ ₹1, next days @ ₹5")
    void testCalculateTieredFine_Tier2() {
        // 6 days overdue = (5 * 1) + (1 * 5) = 5 + 5 = ₹10
        assertEquals(BigDecimal.valueOf(10L), fineService.calculateTieredFine(6));
        // 10 days overdue = (5 * 1) + (5 * 5) = 5 + 25 = ₹30
        assertEquals(BigDecimal.valueOf(30L), fineService.calculateTieredFine(10));
        // 15 days overdue = (5 * 1) + (10 * 5) = 5 + 50 = ₹55
        assertEquals(BigDecimal.valueOf(55L), fineService.calculateTieredFine(15));
    }

    @Test
    @DisplayName("Tiered Fine: Tier 3 (16+ days) first 5 @ ₹1, next 10 @ ₹5, remainder @ ₹10")
    void testCalculateTieredFine_Tier3() {
        // 16 days overdue = 5 + 50 + (1 * 10) = ₹65
        assertEquals(BigDecimal.valueOf(65L), fineService.calculateTieredFine(16));
        // 20 days overdue = 5 + 50 + (5 * 10) = 55 + 50 = ₹105
        assertEquals(BigDecimal.valueOf(105L), fineService.calculateTieredFine(20));
    }

    @Test
    @DisplayName("Reconcile: Creates new FineRecord when none exists")
    void testReconcileOverdueFines_CreatesNewRecord() {
        LocalDate today = LocalDate.now();
        when(borrowRecordRepository.findByReturnedFalseAndDueDateBefore(today))
                .thenReturn(List.of(testBorrowRecord));
        when(fineRecordRepository.findByBorrowRecordBorrowIdAndPaidFalse(100L))
                .thenReturn(Optional.empty());
        when(fineRecordRepository.save(any(FineRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int reconciled = fineService.reconcileOverdueFines(today);

        assertEquals(1, reconciled);
        verify(fineRecordRepository, times(1)).save(any(FineRecord.class));
    }

    @Test
    @DisplayName("Reconcile: Idempotently updates existing unpaid FineRecord")
    void testReconcileOverdueFines_UpdatesExistingRecord() {
        LocalDate today = LocalDate.now();
        when(borrowRecordRepository.findByReturnedFalseAndDueDateBefore(today))
                .thenReturn(List.of(testBorrowRecord));
        when(fineRecordRepository.findByBorrowRecordBorrowIdAndPaidFalse(100L))
                .thenReturn(Optional.of(testFineRecord));
        when(fineRecordRepository.save(any(FineRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int reconciled = fineService.reconcileOverdueFines(today);

        assertEquals(1, reconciled);
        assertEquals(BigDecimal.valueOf(30L), testFineRecord.getAmount());
        verify(fineRecordRepository, times(1)).save(testFineRecord);
    }

    @Test
    @DisplayName("Get all fines returns mapped responses")
    void testGetAllFines() {
        when(fineRecordRepository.findAll()).thenReturn(List.of(testFineRecord));

        List<FineResponse> result = fineService.getAllFines();

        assertEquals(1, result.size());
        assertEquals(500L, result.get(0).getFineId());
        assertEquals("Designing Data-Intensive Applications", result.get(0).getBookTitle());
        assertEquals("Alice Smith", result.get(0).getMemberName());
    }

    @Test
    @DisplayName("Get fines by member returns records if member exists")
    void testGetFinesByMember_Success() {
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(fineRecordRepository.findByMemberMemberId(1L)).thenReturn(List.of(testFineRecord));

        List<FineResponse> result = fineService.getFinesByMember(1L);

        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(30), result.get(0).getAmount());
    }

    @Test
    @DisplayName("Get fines by member throws MemberNotFoundException if member absent")
    void testGetFinesByMember_NotFound() {
        when(memberRepository.existsById(999L)).thenReturn(false);

        assertThrows(MemberNotFoundException.class, () -> fineService.getFinesByMember(999L));
    }

    @Test
    @DisplayName("Pay fine settles fine and sets paidAt timestamp")
    void testPayFine_Success() {
        when(fineRecordRepository.findById(500L)).thenReturn(Optional.of(testFineRecord));
        when(fineRecordRepository.save(any(FineRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(testFineRecord.isPaid());
        FineResponse response = fineService.payFine(500L);

        assertTrue(response.isPaid());
        assertNotNull(response.getPaidAt());
        verify(fineRecordRepository, times(1)).save(testFineRecord);
    }

    @Test
    @DisplayName("Pay fine throws IllegalStateException if already paid")
    void testPayFine_AlreadyPaid() {
        testFineRecord.setPaid(true);
        testFineRecord.setPaidAt(LocalDateTime.now());
        when(fineRecordRepository.findById(500L)).thenReturn(Optional.of(testFineRecord));

        assertThrows(IllegalStateException.class, () -> fineService.payFine(500L));
        verify(fineRecordRepository, never()).save(any(FineRecord.class));
    }

    @Test
    @DisplayName("Pay fine throws FineNotFoundException if fine not found")
    void testPayFine_NotFound() {
        when(fineRecordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(FineNotFoundException.class, () -> fineService.payFine(999L));
    }
}
