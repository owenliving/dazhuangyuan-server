package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_major")
public class Major {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String majorName;

    private String majorCode;

    /** 学科门类 */
    private String category;

    /** 一级学科 */
    private String subCategory;

    /** 授予学位 */
    private String degreeType;

    /** 学制 */
    private String duration;

    private String description;

    /** 就业方向 */
    private String employmentDirection;

    /** 薪资范围 */
    private String salaryRange;

    /** 是否热门 */
    private Integer isHot;
}
