package com.nikunj.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nikunj.library.dto.CreateBorrowRequest;
import com.nikunj.library.exception.BookUnavailableException;
import com.nikunj.library.model.Book;
import com.nikunj.library.model.Member;
import com.nikunj.library.repository.BookRepository;
import com.nikunj.library.repository.BorrowRecordRepository;
import com.nikunj.library.repository.MemberRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class BorrowConcurrencyIntegrationTest {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @BeforeEach
    void cleanUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("Concurrency: 10 simultaneous threads competing for 1 copy results in exactly 1 success and 9 rejections")
    void testConcurrentBorrowingForLastCopy() throws InterruptedException {
        // 1. Setup 1 Book with exactly 1 copy
        Book book = new Book();
        book.setTitle("Concurrency in Practice");
        book.setAuthor("Brian Goetz");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        Book savedBook = bookRepository.save(book);

        // 2. Setup 10 distinct members
        int threadCount = 10;
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= threadCount; i++) {
            Member member = new Member();
            member.setName("Member " + i);
            member.setEmail("member" + i + "@example.com");
            member.setPhoneNumber("987654321" + (i % 10));
            members.add(memberRepository.save(member));
        }

        // 3. Coordinate threads to trigger simultaneously
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final Member member = members.get(i);
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    // Wait for all threads to be ready at the gate
                    startLatch.await();

                    CreateBorrowRequest request = new CreateBorrowRequest();
                    request.setBookId(savedBook.getId());
                    request.setMemberId(member.getMemberId());

                    borrowService.borrowBook(request);
                    successCount.incrementAndGet();
                } catch (BookUnavailableException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // Wait until all 10 threads are ready
        readyLatch.await(5, TimeUnit.SECONDS);
        // Release the latch to fire all 10 requests at the exact same millisecond
        startLatch.countDown();

        executor.shutdown();
        boolean finished = executor.awaitTermination(15, TimeUnit.SECONDS);
        assertTrue(finished, "All threads should complete within timeout");

        // 4. Assert invariants
        assertEquals(1, successCount.get(), "Exactly 1 member should successfully borrow the only copy");
        assertEquals(threadCount - 1, failureCount.get(), "Remaining 9 members should be rejected with BookUnavailableException");

        Book updatedBook = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertEquals(0, updatedBook.getAvailableCopies(), "Available copies must be 0 (no underflow)");
        assertEquals(1, borrowRecordRepository.count(), "Exactly 1 borrow record should exist in database");
    }
}
