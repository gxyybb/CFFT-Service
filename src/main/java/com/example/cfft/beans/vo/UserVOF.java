package com.example.cfft.beans.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户视图对象详细信息")
public class UserVOF {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像URL")
    private String userImage;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "文本列表")
    private List<String> texts;

    @Schema(description = "时间")
    private Integer time;
}
