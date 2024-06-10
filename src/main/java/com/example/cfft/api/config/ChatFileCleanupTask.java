package com.example.cfft.api.config;

import com.example.cfft.common.utils.Static;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class ChatFileCleanupTask {

    // 每天凌晨1点执行一次清理任务
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanUpOldFiles() {
        try {
            Path directory = Paths.get(Static.AUDIO_TEMPORARY_URL);
            Files.list(directory).forEach(file -> {
                try {
                    // 获取文件的最后修改时间
                    File fileToCheck = file.toFile();
                    Instant fileLastModifiedTime = Instant.ofEpochMilli(fileToCheck.lastModified());

                    // 如果文件的最后修改时间早于当前时间的24小时之前，则删除该文件
                    if (fileLastModifiedTime.isBefore(Instant.now().minus(1, ChronoUnit.DAYS))) {
                        Files.delete(file);
                        System.out.println("Deleted old file: " + fileToCheck.getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
