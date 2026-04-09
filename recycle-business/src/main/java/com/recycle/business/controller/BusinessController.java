package com.recycle.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.entity.Inventory;
import com.recycle.common.entity.FinanceRecord;
import com.recycle.common.Result;
import com.recycle.business.service.InventoryService;
import com.recycle.business.service.FinanceService;
import com.recycle.business.service.RecycleCategoryService;
import com.recycle.common.entity.RecycleCategory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusinessController {

    private final InventoryService inventoryService;
    private final FinanceService financeService;
    private final RecycleCategoryService categoryService;

    // ========== 内部接口（供订单服务远程调用） ==========

    @Data
    public static class CompleteDTO {
        private Long orderId;
        private String itemName;
        private BigDecimal actualPrice;
        private String contactName;
        private Long operatorId;
    }

    /** 订单服务远程调用：完成入库 + 记录财务 */
    @PostMapping("/internal/complete")
    public Result<?> completeOrder(@RequestBody CompleteDTO dto) {
        // 入库
        Inventory inventory = new Inventory();
        inventory.setOrderId(dto.getOrderId());
        inventory.setItemName(dto.getItemName());
        inventory.setItemCategory("回收品");
        inventory.setQuantity(1);
        inventory.setUnit("件");
        inventory.setCostPrice(dto.getActualPrice());
        inventoryService.save(inventory);

        // 记录财务
        FinanceRecord record = new FinanceRecord();
        record.setOrderId(dto.getOrderId());
        record.setType(0);
        record.setAmount(dto.getActualPrice());
        record.setDescription("回收" + dto.getItemName() + " - " + dto.getContactName());
        record.setOperatorId(dto.getOperatorId());
        financeService.save(record);

        return Result.success("入库完成");
    }

    // ========== 库存接口 ==========

    @Data
    public static class InventoryDTO {
        private Long orderId;
        private String itemName;
        private String itemCategory;
        private Integer quantity;
        private String unit;
        private BigDecimal costPrice;
        private String stockLocation;
        private String remark;
    }

    @GetMapping("/inventory/list")
    public Result<?> inventoryList(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        Page<Inventory> result = inventoryService.page(new Page<>(page, size));
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return Result.success(data);
    }

    @GetMapping("/inventory/{id}")
    public Result<?> inventoryDetail(@PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }

    @PostMapping("/inventory/add")
    public Result<?> inventoryAdd(@RequestBody InventoryDTO dto) {
        Inventory inv = new Inventory();
        inv.setOrderId(dto.getOrderId());
        inv.setItemName(dto.getItemName());
        inv.setItemCategory(dto.getItemCategory());
        inv.setQuantity(dto.getQuantity());
        inv.setUnit(dto.getUnit());
        inv.setCostPrice(dto.getCostPrice());
        inv.setStockLocation(dto.getStockLocation());
        inv.setRemark(dto.getRemark());
        inventoryService.save(inv);
        return Result.success(inv.getId(), "添加成功");
    }

    @PutMapping("/inventory/update/{id}")
    public Result<?> inventoryUpdate(@PathVariable Long id, @RequestBody InventoryDTO dto) {
        Inventory inv = inventoryService.getById(id);
        if (inv == null) return Result.error("记录不存在");
        inv.setItemName(dto.getItemName());
        inv.setItemCategory(dto.getItemCategory());
        inv.setQuantity(dto.getQuantity());
        inv.setUnit(dto.getUnit());
        inv.setCostPrice(dto.getCostPrice());
        inv.setStockLocation(dto.getStockLocation());
        inv.setRemark(dto.getRemark());
        inventoryService.updateById(inv);
        return Result.success("更新成功");
    }

    @DeleteMapping("/inventory/{id}")
    public Result<?> inventoryDelete(@PathVariable Long id) {
        inventoryService.removeById(id);
        return Result.success("删除成功");
    }

    // ========== 财务接口 ==========

    @Data
    public static class FinanceDTO {
        private Long orderId;
        private Integer type;
        private BigDecimal amount;
        private String description;
    }

    @GetMapping("/finance/list")
    public Result<?> financeList(@RequestParam(required = false) Integer type,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Page<FinanceRecord> result = financeService.recordList(type, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return Result.success(data);
    }

    @PostMapping("/finance/add")
    public Result<?> financeAdd(@RequestBody FinanceDTO dto, HttpServletRequest request) {
        FinanceRecord record = new FinanceRecord();
        record.setOrderId(dto.getOrderId());
        record.setType(dto.getType());
        record.setAmount(dto.getAmount());
        record.setDescription(dto.getDescription());
        record.setOperatorId(Long.valueOf(request.getHeader("X-User-Id")));
        financeService.save(record);
        return Result.success(record.getId(), "添加成功");
    }

    @GetMapping("/finance/statistics")
    public Result<?> financeStatistics(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        return Result.success(financeService.statistics(startDate, endDate));
    }

    // ========== 品类价格配置接口 ==========

    @Data
    public static class CategoryDTO {
        private String name;
        private String unit;
        private java.math.BigDecimal price;
        private Integer sortOrder;
    }

    /** 获取启用的品类列表（公开） */
    @GetMapping("/category/list")
    public Result<?> categoryList() {
        return Result.success(categoryService.listEnabled());
    }

    /** 管理端：分页列表 */
    @GetMapping("/category/manage/list")
    public Result<?> categoryManageList(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Page<RecycleCategory> result = categoryService.manageList(page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return Result.success(data);
    }

    /** 新增品类 */
    @PostMapping("/category/manage/add")
    public Result<?> categoryAdd(@RequestBody CategoryDTO dto) {
        RecycleCategory cat = new RecycleCategory();
        cat.setName(dto.getName());
        cat.setUnit(dto.getUnit());
        cat.setPrice(dto.getPrice());
        cat.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        cat.setStatus(1);
        categoryService.save(cat);
        return Result.success(cat.getId(), "添加成功");
    }

    /** 更新品类 */
    @PutMapping("/category/manage/update/{id}")
    public Result<?> categoryUpdate(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        RecycleCategory cat = categoryService.getById(id);
        if (cat == null) return Result.error("品类不存在");
        cat.setName(dto.getName());
        cat.setUnit(dto.getUnit());
        cat.setPrice(dto.getPrice());
        cat.setSortOrder(dto.getSortOrder());
        categoryService.updateById(cat);
        return Result.success("更新成功");
    }

    /** 删除品类 */
    @DeleteMapping("/category/manage/delete/{id}")
    public Result<?> categoryDelete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success("删除成功");
    }
}
