package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dy_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long productId;

    private String productName;

    private java.math.BigDecimal amount;

    /** wechat/alipay */
    private String payType;

    /** 0待支付 1已支付 2已退款 3已过期 */
    private Integer payStatus;

    private LocalDateTime payTime;

    /** 第三方交易号 */
    private String transactionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime expireAt;
}
