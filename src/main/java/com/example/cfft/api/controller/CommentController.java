package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.*;
import com.example.cfft.beans.vo.CommentVO;
import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论管理")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private PostService postService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private UserService userService;
    @Autowired
    private VideoService videoService;

    @Operation(summary = "创建帖子的评论", description = "根据传入的内容和token创建评论")
    @PostMapping
    @Transactional
    public ResultVO addComment(
            @Parameter(description = "评论内容", required = true) @RequestParam("content") String content,
            @Parameter(description = "用户token", required = true) @RequestParam("token") String token,
            @Parameter(description = "帖子ID", required = true) @RequestParam("typeId") Integer typeId,
            @Parameter(description = "类型", required = false) @RequestParam(value = "type", required = false) String type) {

        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        return Optional.ofNullable(userService.getById(userIdFromToken)).map(user -> {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setUserId(userIdFromToken);
            comment.setTypeId(typeId);
            comment.setParentCommentId(null);
            Optional.ofNullable(type).ifPresent(t -> comment.setType(t));
            commentService.save(comment);
            return getResultVOByType(typeId, type);
        }).orElseGet(ResultVO::failure);
    }

    @Operation(summary = "添加评论的评论", description = "根据传入的内容、token和评论ID添加回复评论")
    @PostMapping("/comment")
    @Transactional
    public ResultVO addCommentComment(
            @Parameter(description = "评论内容", required = true) @RequestParam("content") String content,
            @Parameter(description = "用户token", required = true) @RequestParam("token") String token,
            @Parameter(description = "帖子ID", required = true) @RequestParam("typeId") Integer typeId,
            @Parameter(description = "父评论ID", required = true) @RequestParam("commentId") Integer commentId,
            @Parameter(description = "类型", required = false) @RequestParam(value = "type", required = false) String type) {

        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUserId(userIdFromToken);
        comment.setTypeId(typeId);
        comment.setParentCommentId(commentId);
        Optional.ofNullable(type).ifPresent(t -> comment.setType(t));
        boolean save = commentService.save(comment);

        if (save) {
            Comment byId = commentService.getById(commentId);
            byId.setReplyCount(byId.getReplyCount() + 1);
            commentService.updateById(byId);
        }
        return getResultVOByType(typeId, type);
    }

    @Operation(summary = "点赞评论", description = "根据传入的token和评论ID对评论进行点赞或取消点赞")
    @PostMapping("/like")
    @Transactional
    public ResultVO addLike(
            @Parameter(description = "用户token", required = true) @RequestParam("token") String token,
            @Parameter(description = "评论ID", required = true) @RequestParam("commentId") Integer commentId,
            @Parameter(description = "类型", required = false) @RequestParam(value = "type", required = false) String type) {

        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);

        QueryWrapper<Like> likeQueryWrapper = new QueryWrapper<>();
        likeQueryWrapper.eq("user_id", userIdFromToken)
                .eq("object_type", "Comment")
                .eq("object_id", commentId);
        Like existingLike = likeService.getOne(likeQueryWrapper);

        Comment comment = commentService.getById(commentId);

        if (existingLike != null) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentService.updateById(comment);
            likeService.removeById(existingLike.getLikeId());
        } else {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentService.updateById(comment);
            Like newLike = new Like();
            newLike.setUserId(userIdFromToken);
            newLike.setObjectType("Comment");
            newLike.setObjectId(commentId);
            likeService.save(newLike);
        }

        return ResultVO.success();
    }

    @Operation(summary = "获取1级评论", description = "根据typeId获取对应的评论")
    @GetMapping("/comment/{typeId}")
    public ResultVO getComments(
            @Parameter(description = "帖子ID", required = true) @PathVariable Integer typeId,
            @Parameter(description = "类型", required = false) @RequestParam(value = "type", required = false) String type) {

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        type = (type == null || type.isEmpty()) ? "post" : type;
        queryWrapper.eq("post_id", typeId).isNull("parent_comment_id").eq("type", type);

        List<Comment> comments = commentService.list(queryWrapper);
        List<CommentVO> collect = generateToComment(comments);
        return ResultVO.success(collect);
    }

    @Operation(summary = "根据评论ID获取评论", description = "根据评论ID获取对应的回复评论")
    @GetMapping("/comments/{commentId}")
    public ResultVO getComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Integer commentId,
            @Parameter(description = "类型", required = false) @RequestParam(value = "type", required = false) String type) {

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        type = (type == null || type.isEmpty()) ? "post" : type;
        queryWrapper.eq("type", type).eq("parent_comment_id", commentId);

        List<Comment> comments = commentService.list(queryWrapper);
        return ResultVO.success(generateToComment(comments));
    }

    @Operation(summary = "删除评论", description = "根据评论ID和token删除评论")
    @DeleteMapping("/comment")
    public ResultVO deleteComment(
            @Parameter(description = "评论ID", required = true) @RequestParam Integer commentId,
            @Parameter(description = "用户token", required = true) @RequestParam String token,
            @Parameter(description = "类型", required = false) @RequestParam(value = "type", required = false) String type) {

        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        type = (type == null || type.isEmpty()) ? "post" : type;
        queryWrapper.eq("comment_id", commentId).eq("type", type).eq("user_id", userIdFromToken);

        boolean remove = commentService.remove(queryWrapper);
        return remove ? ResultVO.success() : ResultVO.error("评论不存在");
    }

    private List<CommentVO> generateToComment(List<Comment> comments) {
        return comments.stream()
                .distinct()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO();
                    BeanUtils.copyProperties(comment, commentVO);

                    Optional.ofNullable(userService.getById(comment.getUserId())).ifPresent(user -> {
                        commentVO.setUserImage(PathUtil.convertToHttpUrl(user.getUserImage()));
                        commentVO.setUsername(user.getUsername());
                    });
                    return commentVO;
                })
                .collect(Collectors.toList());
    }

    private ResultVO getResultVOByType(@Parameter(description = "类型ID", required = true) @RequestParam("typeId") Integer typeId, @Parameter(description = "类型", required = false) @RequestParam(value = "type", required = false) String type) {
        return Optional.ofNullable(type).map(t -> {
            Video video = videoService.getById(typeId);
            video.setComments(video.getComments() + 1);
            videoService.updateById(video);
            return ResultVO.success("视频评论创建成功");
        }).orElseGet(() -> {
            Post post = postService.getById(typeId);
            post.setCommentCount(post.getCommentCount() + 1);
            postService.updateById(post);
            return ResultVO.success("帖子评论创建成功");
        });
    }
}
