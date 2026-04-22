package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("dy_admission_data")
public class AdmissionData {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long collegeId;

    private Long groupId;

    private Long majorId;

    /** 招生省份 */
    private String province;

    private Integer year;

    /** 批次 */
    private String batch;

    /** 物理类/历史类 */
    private String category;

    private Integer minScore;

    private Integer avgScore;

    private Integer maxScore;

    /** 最低位次 */
    private Integer minRank;

    /** 平均位次 */
    private Integer avgRank;

    /** 最高位次 */
    private Integer maxRank;

    private Integer enrolledCount;

    private Integer planCount;

    /** 省控线 */
    private Integer scoreLine;

    /** 线差 */
    private Integer lineDiff;
}
