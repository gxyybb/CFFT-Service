package com.example.cfft.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cfft.beans.Post;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PostMapper extends BaseMapper<Post>{
    @Select("SELECT SUM(like_count) FROM post WHERE user_id = #{userId}")
    Integer getTotalLikesByUserId(@Param("userId") Integer userId);

}
