package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.Comment;
import com.example.cfft.beans.Video;
import com.example.cfft.beans.vo.CommentVO;
import com.example.cfft.beans.vo.VideoDTO;
import com.example.cfft.beans.vo.VideoFirst;
import com.example.cfft.common.utils.FileUtil;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.Static;
import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.CommentService;
import com.example.cfft.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/video")
@Tag(name = "VideosController", description = "视频管理接口")
public class VideosController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CommentController commentController;

    @Autowired
    private CommentService commentService;

    /**
     * 获取视频列表
     * @return 视频列表
     */
    @Operation(summary = "获取视频列表", description = "获取所有视频的列表")
    @GetMapping("/list")
    @CrossOrigin("*")
    public ResultVO list() {
        List<Video> list = videoService.list();
        List<VideoFirst> videoFirsts = convertToVideoFirstList(list);
        return ResultVO.success(videoFirsts);
    }

    /**
     * 获取单个视频信息
     * @param id 视频ID
     * @return 视频信息
     */
    @Operation(summary = "获取单个视频信息", description = "根据视频ID获取视频的详细信息")
    @GetMapping
    @CrossOrigin("*")
    public ResultVO getOne(@Parameter(description = "视频ID") @RequestParam("id") Integer id) {
        Video byId = videoService.getById(id);
        VideoDTO videoDTO = convertToVideoDTO(byId);
        return ResultVO.success(videoDTO);
    }

    /**
     * 搜索视频
     * @param key 搜索关键词
     * @return 搜索结果
     */
    @Operation(summary = "搜索视频", description = "根据关键词搜索视频")
    @GetMapping("/search/{key}")
    @CrossOrigin("*")
    public ResultVO searchVideoFirst(@Parameter(description = "搜索关键词") @PathVariable String key) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(Video::getTitle, key)
                .or().like(Video::getDescription, key);

        List<Video> list = videoService.list(queryWrapper);
        List<VideoFirst> videoFirsts = convertToVideoFirstList(list);
        return ResultVO.success(videoFirsts);
    }

    /**
     * 保存视频
     * @param title 视频标题
     * @param description 视频描述
     * @param video 视频文件
     * @return 操作结果
     */
    @Operation(summary = "保存视频", description = "上传并保存视频文件")
    @PostMapping
    @CrossOrigin(origins = "*")
    public ResultVO saveVideo(
            @Parameter(description = "视频标题") @RequestParam("title") String title,
            @Parameter(description = "视频描述") @RequestParam("description") String description,
            @Parameter(description = "视频文件") @RequestParam("video") MultipartFile video) {

        String uniqueFolderName = title + "_" + UUID.randomUUID().toString();
        String baseDirectory = Static.VIDEO;
        String filePath = FileUtil.saveFile(baseDirectory + uniqueFolderName, video);

        if (filePath != null) {
            try {
                String coverImage = FileUtil.extractFrame(filePath);
                updateVideoInfo(title, description, filePath, video, coverImage);
                return ResultVO.success();
            } catch (Exception e) {
                e.printStackTrace();
                return ResultVO.error("视频处理失败");
            }
        } else {
            return ResultVO.error("请检查网络连接");
        }
    }

    /**
     * 更新视频信息
     * @param title 视频标题
     * @param description 视频描述
     * @param filePath 视频路径
     * @param video 视频文件
     * @param coverImage 封面图路径
     */
    private void updateVideoInfo(String title, String description, String filePath, MultipartFile video, String coverImage) {
        Video videos = new Video();
        videos.setTitle(title);
        videos.setDescription(description);
        videos.setFilename(title);
        videos.setFilepath(filePath);
        String originalFileName = video.getOriginalFilename();
        videos.setFiletype(originalFileName);
        videos.setUploadtime(new Date());
        videos.setDuration(null); // 这里设置为 null，可能需要改成视频的实际时长
        videos.setCoverimage(coverImage);
        videos.setViews(0);
        videos.setLikes(0);
        videos.setComments(0);
        videoService.save(videos);
    }

    /**
     * 删除视频
     * @param id 视频ID
     * @return 操作结果
     */
    @Operation(summary = "删除视频", description = "根据视频ID删除视频")
    @DeleteMapping("/{id}")
    @CrossOrigin(origins = "*")
    public ResultVO deleteVideo(@Parameter(description = "视频ID") @PathVariable Integer id) {
        Video video = videoService.getById(id);
        if (video == null) {
            return ResultVO.error("视频不存在");
        }

        FileUtil.deleteFile(video.getFilepath());
        FileUtil.deleteFile(video.getCoverimage());
        videoService.removeById(id);

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", id)
                .eq("type", "video");
        commentService.remove(queryWrapper);

        return ResultVO.success();
    }

    /**
     * 保存视频评论
     * @param token 用户token
     * @param content 评论内容
     * @param videoId 视频ID
     * @return 操作结果
     */
    @Operation(summary = "保存视频评论", description = "保存视频的评论")
    @PostMapping("/comment")
    @CrossOrigin("*")
    public ResultVO saveComment(
            @Parameter(description = "用户token") @RequestParam("token") String token,
            @Parameter(description = "评论内容") @RequestParam("content") String content,
            @Parameter(description = "视频ID") @RequestParam("videoId") Integer videoId) {
        return commentController.addComment(content, token, videoId, "video");
    }

    /**
     * 删除视频评论
     * @param videoId 视频ID
     * @return 操作结果
     */
    @Operation(summary = "删除视频评论", description = "根据视频ID删除视频的评论")
    @DeleteMapping("/comment")
    @CrossOrigin("*")
    public ResultVO deleteComment(@Parameter(description = "视频ID") @RequestParam("videoId") Integer videoId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", videoId)
                .eq("type", "video");
        commentService.remove(queryWrapper);
        return ResultVO.success();
    }

    /**
     * 将Video对象转换为VideoDTO对象
     * @param video Video对象
     * @return VideoDTO对象
     */
    private VideoDTO convertToVideoDTO(Video video) {
        VideoDTO videoDTO = new VideoDTO();
        BeanUtils.copyProperties(video, videoDTO);
        videoDTO.setFilepath(PathUtil.convertToHttpUrl(video.getFilepath()));
        videoDTO.setCoverimage(PathUtil.convertToHttpUrl(video.getCoverimage()));
        List<CommentVO> list = generateCommentVO(video.getVideoid());
        videoDTO.setCommentVOS(list);
        return videoDTO;
    }

    /**
     * 生成视频评论列表
     * @param videoId 视频ID
     * @return 评论列表
     */
    private List<CommentVO> generateCommentVO(Integer videoId) {
        return (List<CommentVO>) commentController.getComments(videoId, "video").getData();
    }

    /**
     * 将Video对象列表转换为VideoFirst对象列表
     * @param videosList Video对象列表
     * @return VideoFirst对象列表
     */
    private List<VideoFirst> convertToVideoFirstList(List<Video> videosList) {
        List<VideoFirst> videoFirstList = new ArrayList<>();
        for (Video videos : videosList) {
            VideoFirst videoFirst = new VideoFirst();
            BeanUtils.copyProperties(videos, videoFirst);
            videoFirst.setCoverimage(PathUtil.convertToHttpUrl(videos.getCoverimage()));
            videoFirstList.add(videoFirst);
        }
        return videoFirstList;
    }
}
