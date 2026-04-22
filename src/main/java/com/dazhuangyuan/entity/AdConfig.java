package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_ad_config")
public class AdConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** home_banner/home_mid/profile_top/about_page */
    private String position;

    private String title;

    private String subTitle;

    private String imageUrl;

    private String linkUrl;

    /** 升学规划/学业评估/咨询/外部 */
    private String adType;

    private String contactInfo;

    private Integer sortOrder;

    /** 1启用 0禁用 */
    private Integer isActive;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
