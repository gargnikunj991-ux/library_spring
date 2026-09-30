package com.nikunj.library.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nikunj.library.dto.FineResponse;
import com.nikunj.library.service.FineService;

@RestController
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    /**
     * View all fines across the entire library.
     * Restricted to ADMIN and LIBRARIAN only.
     */
    @GetMapping
    public ResponseEntity<List<FineResponse>> getAllFines() {
        return ResponseEntity.ok(fineService.getAllFines());
    }

    /**
     * View fines for a specific member.
     * Accessible to ADMIN, LIBRARIAN, and ASSISTANT (requires memberId).
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<FineResponse>> getFinesByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(fineService.getFinesByMember(memberId));
    }

    /**
     * Settle/pay an outstanding fine.
     * Accessible to ADMIN, LIBRARIAN, and ASSISTANT.
     */
    @PostMapping("/{fineId}/pay")
    public ResponseEntity<FineResponse> payFine(@PathVariable Long fineId) {
        return ResponseEntity.ok(fineService.payFine(fineId));
    }

    /**
     * Manually trigger overdue fine reconciliation on demand.
     * Restricted to ADMIN only.
     */
    @PostMapping("/reconcile")
    public ResponseEntity<String> triggerReconciliation() {
        int reconciledCount = fineService.reconcileOverdueFines();
        return ResponseEntity.ok("Reconciliation completed. Reconciled " + reconciledCount + " overdue loan(s).");
    }
}
