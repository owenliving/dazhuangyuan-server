package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_college")
public class College {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String collegeName;

    private String collegeCode;

    private String province;

    private String city;

    /** 院校类型: 985/211/双一流/普通本科/专科 */
    private String collegeType;

    /** 办学性质: 公办/民办/中外合作办学 */
    private String collegeNature;

    /** 办学层次: 本科/专科 */
    private String collegeLevel;

    private String belong;

    private String phone;

    private String website;

    private String address;

    private String logoUrl;

    private String intro;

    private String features;

    private String employmentRate;

    private String tuitionRange;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
