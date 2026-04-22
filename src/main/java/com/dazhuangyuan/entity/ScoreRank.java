package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_score_rank")
public class ScoreRank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String province;

    private Integer year;

    /** 物理类/历史类 */
    private String category;

    private Integer score;

    /** 累计人数/位次 */
    private Integer rankCount;

    /** 本分人数 */
    private Integer scoreCount;
}
