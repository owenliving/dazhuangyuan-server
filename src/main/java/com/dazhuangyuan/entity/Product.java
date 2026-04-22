package com.dazhuangyuan.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "dy_product", autoResultMap = true)
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** basic/professional/premium */
    private String productType;

    private String name;

    private java.math.BigDecimal originalPrice;

    private java.math.BigDecimal price;

    private String description;

    /** 功能列表JSON */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> features;

    private Integer sortOrder;

    /** 1上架 0下架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
