package com.example.cfft.beans;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName location
 */
@TableName(value ="location")
@Data
public class Location implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private String province;

    /**
     * 
     */
    private String city;

    /**
     * 
     */
    private BigDecimal latitude;

    /**
     * 
     */
    private BigDecimal longitude;
    private String description;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}