package com.recycle.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("recycle_order")
public class RecycleOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contactName;
    private String contactPhone;
    private String itemName;
    private String itemDescription;
    private BigDecimal estimatedPrice;
    private BigDecimal actualPrice;
    private String itemImages;
    /** 0-待接单 1-已接单 2-已取货 3-已完成 4-已取消 */
    private Integer status;
    private Long recyclerId;
    private String recyclerName;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    private LocalDateTime completeTime;
    @TableLogic
    private Integer deleted;
}
