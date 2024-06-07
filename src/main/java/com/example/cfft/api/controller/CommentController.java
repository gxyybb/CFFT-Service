package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.*;
import com.example.cfft.beans.vo.CommentVO;

import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment")
public class CommentController{

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


    /**
     * 创建帖子的评论
     * @param content
     * @param token

     * @return
     */
    @CrossOrigin(origins = "*")
    @PostMapping
    @Transactional
    public ResultVO addComment(@RequestParam("content") String content, @RequestParam("token") String token, @RequestParam("postId") Integer typeId,@RequestParam(value = "type",required = false)String type) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        return Optional.ofNullable( userService.getById(userIdFromToken)).map(user -> {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setUserId(userIdFromToken);
            comment.setTypeId(typeId);
            comment.setParentCommentId(null);
            Optional.ofNullable(type).ifPresent( t ->{comment.setType(type);});
            commentService.save(comment);
            return getResultVOByType(typeId, type);
            // 更新帖子评论数量


        }).orElseGet(ResultVO::failure);

    }


    /**
     * 添加评论的评论
     * @param content
     * @param token
     * @param typeId
     * @param commentId
     * @return
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/comment")
    @Transactional
    public ResultVO addCommentComment(@RequestParam("content") String content, @RequestParam("token") String token, @RequestParam("postId") Integer typeId, @RequestParam("commentId") Integer commentId,@RequestParam(value = "type",required = false)String type) {
        // 创建评论
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUserId(userIdFromToken);
        comment.setTypeId(typeId);
        comment.setParentCommentId(commentId);
        Optional.ofNullable(type).ifPresent( t ->{comment.setType(type);});
        commentService.save(comment);

        // 更新评论回复数量
        Comment byId = commentService.getById(commentId);
        byId.setReplyCount(byId.getReplyCount() + 1);
        commentService.updateById(byId);
        return getResultVOByType(typeId, type);
        // 更新帖子评论数量


    }

    @NotNull
    private ResultVO getResultVOByType(@RequestParam("postId") Integer typeId, @RequestParam(value = "type", required = false) String type) {
        return Optional.ofNullable(type).map(t ->{
            Video video = videoService.getById(typeId);
            video.setComments(video.getComments() + 1);
            videoService.updateById(video);
            return ResultVO.success("视频评论创建成功");
        }).orElseGet(()->{
            Post post = postService.getById(typeId);
            post.setCommentCount(post.getCommentCount() + 1);
            postService.updateById(post);
            return ResultVO.success("帖子评论创建成功");
        });
    }


    /**
     * 点赞评论
     * @param token
     * @param commentId
     * @return
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/like")
    @Transactional
    public ResultVO addLike(@RequestParam("token") String token, @RequestParam("commentId") Integer commentId,@RequestParam(value = "type",required = false)String type) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);

        // 检查是否已经存在点赞记录
        QueryWrapper<Like> likeQueryWrapper = new QueryWrapper<>();
        likeQueryWrapper.eq("user_id", userIdFromToken)
                .eq("object_type", "Comment")
                .eq("object_id", commentId);
        Like existingLike = likeService.getOne(likeQueryWrapper);

        // 获取评论对象
        Comment comment = commentService.getById(commentId);

        if (existingLike != null) {
            // 如果存在点赞记录，则撤销点赞
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentService.updateById(comment);
            likeService.removeById(existingLike.getLikeId());
        } else {
            // 如果不存在点赞记录，则添加点赞
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


    /**
     * 获取1级评论

     * @return
     */
    @CrossOrigin(origins = "*")
  // 通过GetMapping注解映射路径，获取指定typeId的评论
    @GetMapping("/comment/{typeId}")
    public ResultVO getComments(@PathVariable Integer typeId,@RequestParam(value = "type",required = false)String type) {
        // 创建查询包装器，用于查询评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        type = (type==null||type.isEmpty())?"post":type;
        queryWrapper.eq("post_id", typeId).isNull("parent_comment_id").eq("type",type);

        // 查询评论
        List<Comment> comments = commentService.list(queryWrapper);

        // 将评论转换为 CommentVO 对象并返回
        List<CommentVO> collect = generateToComment(comments);

        // 返回成功结果
        return ResultVO.success(collect);
    }

    private List<CommentVO> generateToComment(List<Comment> comments) {
        return comments.stream()
                .distinct()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO();
                    // 使用 BeanUtils.copyProperties() 方法拷贝属性
                    BeanUtils.copyProperties(comment, commentVO);
                    // 获取评论的用户信息

                    Optional.ofNullable(userService.getById(comment.getUserId())).ifPresent(user -> {
                        commentVO.setUserImage(PathUtil.convertToHttpUrl(user.getUserImage()));
                        commentVO.setUsername(user.getUsername());
                    });
                    return commentVO;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据评论id获取评论

     * @param commentId
     * @return
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/comments/{commentId}")
    public ResultVO getComment(@PathVariable Integer commentId,@RequestParam(value = "type",required = false)String type){
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        type = (type==null||type.isEmpty())? "post":type;
        queryWrapper.eq("type",type)
                .eq("parent_comment_id",commentId);
        List<Comment> comments = commentService.list(queryWrapper);

        return ResultVO.success(generateToComment(comments));
    }

    /**
     * 删除评论
     * @param commentId
     * @param token
     * @return
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping("/comment")
    public ResultVO deleteComment(@RequestParam Integer commentId,@RequestParam String token,@RequestParam(value = "type",required = false)String type){
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        type = (type==null||type.isEmpty())? "post":type;
        queryWrapper.eq("comment_id",commentId)
                .eq("type",type)
                .eq("user_id",userIdFromToken);
        boolean remove = commentService.remove(queryWrapper);
        return remove?ResultVO.success():ResultVO.error("评论不存在");
    }





}
