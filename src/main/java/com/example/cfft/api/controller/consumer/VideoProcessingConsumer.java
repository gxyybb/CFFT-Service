package com.example.cfft.api.controller.consumer;

import com.example.cfft.api.config.RabbitMQConfig;
import com.example.cfft.beans.Video;
import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.service.VideoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class VideoProcessingConsumer {

    @Autowired
    private VideoService videoService;

    // 监听队列
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println(" [x] Received '" + message + "'");

        // 解析消息内容
        String[] parts = message.split("\\|");
        String title = parts[0];
        String description = parts[1];
        String filePath = parts[2];
        String originalFileName = parts[3];

        // 处理视频文件
        try {
            String coverImage = FileUtil.extractFrame(filePath);
            updateVideoInfo(title, description, filePath, originalFileName, coverImage);
        } catch (Exception e) {
            e.printStackTrace();
            // 处理失败逻辑
        }
    }

    private void updateVideoInfo(String title, String description, String filePath, String originalFileName, String coverImage) {
        Video videos = new Video();
        videos.setTitle(title);
        videos.setDescription(description);
        videos.setFilename(title);
        videos.setFilepath(filePath);
        videos.setFiletype(originalFileName);
        videos.setUploadtime(new Date());
        videos.setDuration(null); // 这里设置为 null，可能需要改成视频的实际时长
        videos.setCoverimage(coverImage);
        videos.setViews(0);
        videos.setLikes(0);
        videos.setComments(0);
        videoService.save(videos);
    }
}
