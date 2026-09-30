package com.nikunj.library.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikunj.library.dto.FineResponse;
import com.nikunj.library.exception.FineNotFoundException;
import com.nikunj.library.exception.MemberNotFoundException;
import com.nikunj.library.model.BorrowRecord;
import com.nikunj.library.model.FineRecord;
import com.nikunj.library.repository.BorrowRecordRepository;
import com.nikunj.library.repository.FineRecordRepository;
import com.nikunj.library.repository.MemberRepository;

@Service
public class FineService {

    private static final Logger log = LoggerFactory.getLogger(FineService.class);

    private final FineRecordRepository fineRecordRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final MemberRepository memberRepository;

    public FineService(FineRecordRepository fineRecordRepository,
                       BorrowRecordRepository borrowRecordRepository,
                       MemberRepository memberRepository) {
        this.fineRecordRepository = fineRecordRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Tiered Overdue Penalty Formula:
     * - Days 1 to 5: ₹1 / day
     * - Days 6 to 15: ₹5 / day
     * - Day 16 onwards: ₹10 / day
     */
    public BigDecimal calculateTieredFine(long daysOverdue) {
        if (daysOverdue <= 0) {
            return BigDecimal.ZERO;
        }

        if (daysOverdue <= 5) {
            return BigDecimal.valueOf(daysOverdue * 1L);
        } else if (daysOverdue <= 15) {
            return BigDecimal.valueOf(5L * 1L)
                    .add(BigDecimal.valueOf((daysOverdue - 5) * 5L));
        } else {
            return BigDecimal.valueOf(5L * 1L)
                    .add(BigDecimal.valueOf(10L * 5L))
                    .add(BigDecimal.valueOf((daysOverdue - 15) * 10L));
        }
    }

    /**
     * Idempotent reconciliation of active overdue loans.
     */
    @Transactional
    public int reconcileOverdueFines() {
        return reconcileOverdueFines(LocalDate.now());
    }

    @Transactional
    public int reconcileOverdueFines(LocalDate targetDate) {
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findByReturnedFalseAndDueDateBefore(targetDate);
        log.info("Found {} active overdue borrow records for date {}", overdueRecords.size(), targetDate);

        int count = 0;
        for (BorrowRecord record : overdueRecords) {
            long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), targetDate);
            if (daysOverdue <= 0) {
                continue;
            }

            BigDecimal fineAmount = calculateTieredFine(daysOverdue);

            FineRecord fineRecord = fineRecordRepository
                    .findByBorrowRecordBorrowIdAndPaidFalse(record.getBorrowId())
                    .orElseGet(() -> new FineRecord(record, record.getMember(), fineAmount));

            fineRecord.setAmount(fineAmount);
            fineRecord.setCalculatedAt(LocalDateTime.now());
            fineRecordRepository.save(fineRecord);
            count++;
        }

        log.info("Reconciled {} fine records successfully", count);
        return count;
    }

    public List<FineResponse> getAllFines() {
        return fineRecordRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<FineResponse> getFinesByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException("Member not found with id: " + memberId);
        }
        return fineRecordRepository.findByMemberMemberId(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FineResponse payFine(Long fineId) {
        FineRecord fine = fineRecordRepository.findById(fineId)
                .orElseThrow(() -> new FineNotFoundException("Fine record not found with id: " + fineId));

        if (fine.isPaid()) {
            throw new IllegalStateException("Fine has already been settled");
        }

        fine.setPaid(true);
        fine.setPaidAt(LocalDateTime.now());
        FineRecord saved = fineRecordRepository.save(fine);

        log.info("Fine ID {} of ₹{} settled successfully for member ID {}",
                fine.getFineId(), fine.getAmount(), fine.getMember().getMemberId());

        return toResponse(saved);
    }

    public FineResponse toResponse(FineRecord fine) {
        Long borrowId = fine.getBorrowRecord() != null ? fine.getBorrowRecord().getBorrowId() : null;
        Long bookId = (fine.getBorrowRecord() != null && fine.getBorrowRecord().getBook() != null)
                ? fine.getBorrowRecord().getBook().getId() : null;
        String bookTitle = (fine.getBorrowRecord() != null && fine.getBorrowRecord().getBook() != null)
                ? fine.getBorrowRecord().getBook().getTitle() : null;

        Long memberId = fine.getMember() != null ? fine.getMember().getMemberId() : null;
        String memberName = fine.getMember() != null ? fine.getMember().getName() : null;

        return new FineResponse(
                fine.getFineId(),
                borrowId,
                bookId,
                bookTitle,
                memberId,
                memberName,
                fine.getAmount(),
                fine.isPaid(),
                fine.getCalculatedAt(),
                fine.getPaidAt()
        );
    }
}
