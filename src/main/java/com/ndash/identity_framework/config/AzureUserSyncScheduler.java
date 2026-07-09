package com.ndash.identity_framework.config;

import com.ndash.identity_framework.services.AzureSyncService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AzureUserSyncScheduler {

    private final AzureSyncService azureSyncService;
    private final MeterRegistry meterRegistry;

    @Async
    @Scheduled(fixedRate = 600000)
    public void syncUsers() {
        final Timer.Sample sample = Timer.start(meterRegistry);
        try {
            azureSyncService.syncUsersFromAzure();
            meterRegistry.counter("azure.sync.success").increment();
            log.info("[identity-framework] - SYNC: Azure user sync completed");
        } catch (Exception ex) {
            meterRegistry.counter("azure.sync.failure").increment();
            log.error("[identity-framework] - SYNC: Azure user sync failed: {}", ex.getMessage(), ex);
        } finally {
            sample.stop(meterRegistry.timer("azure.sync.duration"));
        }
    }
}
