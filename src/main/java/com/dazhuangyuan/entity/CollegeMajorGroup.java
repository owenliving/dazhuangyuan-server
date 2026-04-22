package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "dy_college_major_group", autoResultMap = true)
public class CollegeMajorGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeId;

    private String groupCode;

    private String groupName;

    /** 选科要求JSON */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> requireSubjects;

    /** 录取批次: 提前批/本科批/专科批 */
    private String batch;

    private Integer year;

    /** 招生人数 */
    private Integer planCount;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
