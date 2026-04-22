package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String nickname;

    private String avatar;

    /** 0未知 1男 2女 */
    private Integer gender;

    private String province;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    /** 0禁用 1正常 */
    private Integer status;

    /** VIP等级: 0免费 1基础 2专业 3尊享 */
    private Integer vipLevel;

    private LocalDateTime vipExpireAt;

    /** 高考分数 */
    private Integer studentScore;

    /** 位次 */
    private Integer studentRank;

    /** 选科JSON */
    private String studentSubjects;

    /** 物理类/历史类 */
    private String studentCategory;

    /** 意向省份 */
    private String targetProvince;
}
