package com.nikunj.library.worker;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nikunj.library.service.FineService;

@ExtendWith(MockitoExtension.class)
public class OverdueReconciliationWorkerTest {

    @Mock
    private FineService fineService;

    @InjectMocks
    private OverdueReconciliationWorker worker;

    @Test
    @DisplayName("Worker triggers fineService.reconcileOverdueFines() successfully")
    void testRunNightlyReconciliation_Success() {
        worker.runNightlyReconciliation();
        verify(fineService, times(1)).reconcileOverdueFines();
    }

    @Test
    @DisplayName("Worker catches and handles exceptions gracefully without crashing")
    void testRunNightlyReconciliation_HandlesExceptionGracefully() {
        doThrow(new RuntimeException("Database connection timeout")).when(fineService).reconcileOverdueFines();

        // Should not throw
        worker.runNightlyReconciliation();
        verify(fineService, times(1)).reconcileOverdueFines();
    }
}
