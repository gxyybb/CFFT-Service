package com.example.cfft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cfft.beans.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CommentMapper extends BaseMapper<Comment>{
    @Select("SELECT SUM(like_count) FROM comment WHERE user_id = #{userId}")
    Integer getTotalLikesByUserId(@Param("userId") Integer userId);
    @Select("SELECT SUM(reply_count) FROM comment WHERE user_id = #{userId}")
    Integer getTotalCommentsByUserId(@Param("userId") Integer userId);
}
