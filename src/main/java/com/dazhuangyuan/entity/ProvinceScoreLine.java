package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_province_score_line")
public class ProvinceScoreLine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String province;

    private Integer year;

    private String batch;

    /** 物理类/历史类 */
    private String category;

    private Integer score;

    private Integer rankCount;

    private Integer totalStudents;
}
