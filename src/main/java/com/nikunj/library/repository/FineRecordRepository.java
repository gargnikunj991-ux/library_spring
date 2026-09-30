package com.nikunj.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nikunj.library.model.FineRecord;

public interface FineRecordRepository extends JpaRepository<FineRecord, Long> {

    Optional<FineRecord> findByBorrowRecordBorrowIdAndPaidFalse(Long borrowId);

    List<FineRecord> findByMemberMemberId(Long memberId);

    List<FineRecord> findByMemberMemberIdAndPaidFalse(Long memberId);

    List<FineRecord> findByPaidFalse();

}
