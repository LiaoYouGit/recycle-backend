package com.recycle.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("finance_record")
public class FinanceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    /** 0-采购支出 1-其他支出 2-收入 */
    private Integer type;
    private BigDecimal amount;
    private String description;
    private Long operatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;
}
