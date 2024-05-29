package com.example.cfft.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cfft.beans.Comment;
import com.example.cfft.beans.Like;
import com.example.cfft.beans.Post;
import com.example.cfft.beans.User;
import com.example.cfft.beans.vo.CommentVO;

import com.example.cfft.common.utils.PathUtil;
import com.example.cfft.common.utils.TokenUtil;
import com.example.cfft.common.vo.ResultVO;
import com.example.cfft.service.CommentService;
import com.example.cfft.service.LikeService;
import com.example.cfft.service.PostService;
import com.example.cfft.service.UserService;
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

    /**
     * 创建帖子的评论
     * @param content
     * @param token
     * @param postId
     * @return
     */
    @CrossOrigin(origins = "*")
    @PostMapping
    @Transactional
    public ResultVO addComment(@RequestParam("content") String content, @RequestParam("token") String token, @RequestParam("postId") Integer postId) {
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        return Optional.ofNullable( userService.getById(userIdFromToken)).map(user -> {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setUserId(userIdFromToken);
            comment.setPostId(postId);
            comment.setParentCommentId(null);
            commentService.save(comment);

            // 更新帖子评论数量
            Post byId = postService.getById(postId);
            byId.setCommentCount(byId.getCommentCount() + 1);
            postService.updateById(byId);
            return ResultVO.success();
        }).orElseGet(ResultVO::failure);

    }


    /**
     * 添加评论的评论
     * @param content
     * @param token
     * @param postId
     * @param commentId
     * @return
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/comment")
    @Transactional
    public ResultVO addCommentComment(@RequestParam("content") String content, @RequestParam("token") String token, @RequestParam("postId") Integer postId, @RequestParam("commentId") Integer commentId) {
        // 创建评论
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUserId(userIdFromToken);
        comment.setPostId(postId);
        comment.setParentCommentId(commentId);
        commentService.save(comment);

        // 更新评论回复数量
        Comment byId = commentService.getById(commentId);
        byId.setReplyCount(byId.getReplyCount() + 1);
        commentService.updateById(byId);

        // 更新帖子评论数量
        Post postById = postService.getById(postId);
        postById.setCommentCount(postById.getCommentCount() + 1);
        postService.updateById(postById);

        return ResultVO.success();
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
    public ResultVO addLike(@RequestParam("token") String token, @RequestParam("commentId") Integer commentId) {
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
     * @param postId
     * @return
     */
    @CrossOrigin(origins = "*")
  // 通过GetMapping注解映射路径，获取指定postId的评论
    @GetMapping("/comment/{postId}")
    public ResultVO getComments(@PathVariable Integer postId) {
        // 创建查询包装器，用于查询评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId).isNull("parent_comment_id");

        // 查询评论
        List<Comment> comments = commentService.list(queryWrapper);

        // 将评论转换为 CommentVO 对象并返回
        List<CommentVO> collect = comments.stream()
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

        // 返回成功结果
        return ResultVO.success(collect);
    }

    /**
     * 根据帖子id和评论id获取评论
     * @param postId
     * @param commentId
     * @return
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/comment/{postId}/{commentId}")
    public ResultVO getComment(@PathVariable String postId,@PathVariable Integer commentId){
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id",postId)
                .eq("parent_comment_id",commentId);
        List<Comment> comments = commentService.list(queryWrapper);
        return ResultVO.success(comments);
    }

    /**
     * 删除评论
     * @param commentId
     * @param token
     * @return
     */
    @CrossOrigin(origins = "*")
    @DeleteMapping("/comment")
    public ResultVO deleteComment(@RequestParam Integer commentId,@RequestParam String token){
        Integer userIdFromToken = TokenUtil.getUserIdFromToken(token);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id",commentId)
                .eq("user_id",userIdFromToken);
        boolean remove = commentService.remove(queryWrapper);
        return remove?ResultVO.success():ResultVO.error("评论不存在");
    }





}
