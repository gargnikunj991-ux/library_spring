package com.nikunj.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nikunj.library.model.BorrowRecord;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

}
