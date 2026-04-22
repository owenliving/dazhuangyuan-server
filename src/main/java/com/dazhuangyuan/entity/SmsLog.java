package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_sms_log")
public class SmsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String code;

    /** login/register */
    private String type;

    private String ip;

    /** 0未用 1已用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime expiredAt;
}
