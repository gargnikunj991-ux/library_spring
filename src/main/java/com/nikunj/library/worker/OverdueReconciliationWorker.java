package com.nikunj.library.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nikunj.library.service.FineService;

@Component
public class OverdueReconciliationWorker {

    private static final Logger log = LoggerFactory.getLogger(OverdueReconciliationWorker.class);

    private final FineService fineService;

    public OverdueReconciliationWorker(FineService fineService) {
        this.fineService = fineService;
    }

    /**
     * Nightly reconciliation job running every day at midnight (00:00:00).
     * Cron expression can be overridden via app.fine.cron property.
     */
    @Scheduled(cron = "${app.fine.cron:0 0 0 * * *}")
    public void runNightlyReconciliation() {
        log.info("Starting scheduled nightly overdue fine reconciliation worker...");
        try {
            int count = fineService.reconcileOverdueFines();
            log.info("Completed scheduled nightly overdue fine reconciliation. Total loans reconciled: {}", count);
        } catch (Exception e) {
            log.error("Error during scheduled overdue fine reconciliation worker execution", e);
        }
    }
}
