package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.Comment;
import com.example.cfft.beans.User;
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
import com.example.cfft.service.UserService;

import com.example.cfft.service.VideoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("video")
public class VideosController {

    @Autowired
    private VideoService videosService;

    @Autowired
    private CommentController commentController;

    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;





    /**
     * 获取视频列表
     * @return 视频列表
     */
    @GetMapping("/list")
    @CrossOrigin("*")
    public ResultVO list() {
        List<Video> list = videosService.list();
        List<VideoFirst> videoFirsts = convertToVideoFirstList(list);
        return ResultVO.success(videoFirsts);
    }

    /**
     * 获取单个视频信息
     * @param id 视频ID
     * @return 视频信息
     */
    @GetMapping
    @CrossOrigin("*")
    public ResultVO getOne(@RequestParam("id") Integer id) {
        Video byId = videosService.getById(id);
        VideoDTO videoDTO = covertToVideDTO(byId);
        return ResultVO.success(videoDTO);
    }
    /**
     * 搜索视频
     * @param key 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search/{key}")
    @CrossOrigin("*")
    public ResultVO searchVideoFirst(@PathVariable String key) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(Video::getTitle, key)
                .or().like(Video::getDescription, key);

        List<Video> list = videosService.list(queryWrapper);
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
    @PostMapping
    @CrossOrigin(origins = "*")
    public ResultVO saveVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("video") MultipartFile video) {

        String uniqueFolderName = title + "_" + UUID.randomUUID().toString();
        String baseDirectory = Static.VIDEO;
        String s = FileUtil.saveFile(baseDirectory + uniqueFolderName, video);

        if (s != null) {
            try {
                String coverImage = FileUtil.extractFrame(s);
                // 更新视频信息
                updateVideoInfo(title, description, s, video, coverImage);
                return ResultVO.success();
            } catch (Exception e) {
                e.printStackTrace();
                // 处理异常
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
     * @param s 视频路径
     * @param video 视频文件
     * @param coverImage 封面图路径
     */
    private void updateVideoInfo(String title, String description, String s, MultipartFile video, String coverImage) {
        Video videos = new Video();
        videos.setTitle(title);
        videos.setDescription(description);
        videos.setFilename(title);
        videos.setFilepath(s);
        String originalFileName = video.getOriginalFilename();
        videos.setFiletype(originalFileName);
        videos.setUploadtime(new Date());
        videos.setDuration(null);
        videos.setCoverimage(coverImage);
        videos.setViews(0);
        videos.setLikes(0);
        videos.setComments(0);
        videosService.save(videos);
    }



    /**
     * 删除视频
     * @param id 视频ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @CrossOrigin(origins = "*")
    public ResultVO deleteVideo(@PathVariable Integer id) {
        // 检查视频是否存在
        Video video = videosService.getById(id);
        if (video == null) {
            return ResultVO.error("视频不存在");
        }

        // 删除视频文件和封面图（如果存在）
        FileUtil.deleteFile(video.getFilepath());
        FileUtil.deleteFile(video.getCoverimage());

        // 删除视频记录
        videosService.removeById(id);

        // 删除与视频相关的评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id",id)
                .eq("type","video");
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
    @PostMapping("/comment")
    @CrossOrigin("*")
    public ResultVO saveComment(@RequestParam("token") String token, @RequestParam("content") String content, @RequestParam("videoId") Integer videoId) {
       return commentController.addComment(content,token,videoId,"video");
    }

    /**
     * 删除视频评论
     * @param videoId 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/comment")
    @CrossOrigin("*")
    public ResultVO deleteComment(@RequestParam("videoId") Integer videoId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id",videoId)
                .eq("type","video");
        commentService.remove(queryWrapper);
        return ResultVO.success();
    }

    /**
     * 将Video对象转换为VideoDTO对象
     * @param byId Video对象
     * @return VideoDTO对象
     */
    private VideoDTO covertToVideDTO(Video byId) {
        VideoDTO videoDTO = new VideoDTO();
        BeanUtils.copyProperties(byId, videoDTO);
        videoDTO.setFilepath(PathUtil.convertToHttpUrl(byId.getFilepath()));
        videoDTO.setCoverimage(PathUtil.convertToHttpUrl(byId.getCoverimage()));
        List<CommentVO> list = generateCommentVO(byId.getVideoid());
        videoDTO.setCommentVOS(list);
        return videoDTO;
    }

    /**
     * 生成视频评论列表
     * @param videoId 视频ID
     * @return 评论列表
     */
    private List<CommentVO> generateCommentVO(Integer videoId) {
        return (List<CommentVO>) commentController.getComments(videoId,"video").getData();

    }



    /**
     * 将Video对象列表转换为VideoFirst对象列表
     * @param videosList Video对象列表
     * @return VideoFirst对象列表
     */
    public List<VideoFirst> convertToVideoFirstList(List<Video> videosList) {
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
