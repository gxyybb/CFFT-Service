package com.example.cfft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cfft.beans.Like;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LikeMapper extends BaseMapper<Like>{
    @Select("SELECT DISTINCT l.user_id " +
            "FROM `like` l " +
            "JOIN post p ON l.object_id = p.post_id " +
            "WHERE p.user_id = #{inputUserId} " +
            "AND l.object_type = 'Post' " +
            "AND l.user_id != #{inputUserId}")
    List<Integer> getDistinctUserIdsByPostLikes(@Param("inputUserId") Integer inputUserId);
}
