package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_college_major")
public class CollegeMajor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeId;

    private Long majorId;

    private Long groupId;

    private Integer enrollmentYear;

    private Integer planCount;
}
