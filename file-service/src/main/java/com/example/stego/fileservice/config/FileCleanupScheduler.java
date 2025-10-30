package com.example.stego.fileservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileCleanupScheduler {

    private final GridFsTemplate gridFsTemplate;

    @Value("${pqcstego.cleanup.enabled:false}")
    private boolean cleanupEnabled;

    @Value("${pqcstego.cleanup.retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = "0 0 2 * * ?") // Runs daily at 2 AM
    public void cleanupOldFiles() {
        if (!cleanupEnabled) {
            log.debug("File cleanup is disabled.");
            return;
        }

        var cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        var cutoffDateAsDate = Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant());

        log.debug("Starting cleanup of GridFS files older than {} days (before {}).", retentionDays, cutoffDateAsDate);

        // Delete files older than configured retention period
        var query = new Query(
                Criteria.where("uploadDate").lt(cutoffDateAsDate)
        );
        gridFsTemplate.delete(query);
        log.info("Completed cleanup of GridFS files older than {} days.", retentionDays);
    }

}
