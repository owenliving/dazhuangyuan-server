package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("dy_volunteer_item")
public class VolunteerItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Long collegeId;

    private Long groupId;

    /** 专业ID列表JSON */
    private String majorIds;

    private Integer sortOrder;

    /** 冲/稳/保 */
    private String type;

    /** 录取概率(%) */
    private java.math.BigDecimal admissionProbability;

    /** 是否服从调剂 */
    private Integer isAdjust;

    private String note;
}
