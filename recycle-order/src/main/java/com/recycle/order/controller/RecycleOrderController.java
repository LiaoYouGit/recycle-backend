package com.recycle.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.Result;
import com.recycle.common.entity.RecycleOrder;
import com.recycle.order.service.RecycleOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class RecycleOrderController {

    private final RecycleOrderService recycleOrderService;

    @Data
    public static class CreateOrderDTO {
        private String contactName;
        private String contactPhone;
        private String itemName;
        private String itemDescription;
        private BigDecimal estimatedPrice;
        private String itemImages;
        private String remark;
    }

    @Data
    public static class PickupDTO {
        private BigDecimal actualPrice;
    }

    /** 卖方发起回收请求（公开） */
    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody CreateOrderDTO dto) {
        if (dto.getContactName() == null || dto.getContactName().isEmpty())
            return Result.error("联系人不能为空");
        if (dto.getContactPhone() == null || dto.getContactPhone().isEmpty())
            return Result.error("联系电话不能为空");
        if (dto.getItemName() == null || dto.getItemName().isEmpty())
            return Result.error("回收品名称不能为空");

        RecycleOrder order = new RecycleOrder();
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setItemName(dto.getItemName());
        order.setItemDescription(dto.getItemDescription());
        order.setEstimatedPrice(dto.getEstimatedPrice());
        order.setItemImages(dto.getItemImages());
        order.setRemark(dto.getRemark());
        order.setStatus(0);
        recycleOrderService.save(order);
        return Result.success(order.getId(), "回收请求提交成功");
    }

    /** 卖方查看订单详情（公开） */
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(recycleOrderService.getById(id));
    }

    /** 卖方按手机号查询自己的回收记录（公开） */
    @GetMapping("/listByPhone")
    public Result<?> listByPhone(@RequestParam String phone,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Page<RecycleOrder> result = recycleOrderService.listByPhone(phone, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return Result.success(data);
    }

    /** 查看回收池（管理端，网关已校验JWT） */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Integer status,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        Page<RecycleOrder> result = recycleOrderService.orderList(status, page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotal());
        data.put("list", result.getRecords());
        return Result.success(data);
    }

    /** 承接订单 */
    @PutMapping("/accept/{id}")
    public Result<?> accept(@PathVariable Long id, HttpServletRequest request) {
        Long userId = Long.valueOf(request.getHeader("X-User-Id"));
        String username = request.getHeader("X-Username");
        recycleOrderService.acceptOrder(id, userId, username);
        return Result.success("接单成功");
    }

    /** 确认取货 */
    @PutMapping("/pickup/{id}")
    public Result<?> pickup(@PathVariable Long id, @RequestBody PickupDTO dto) {
        if (dto.getActualPrice() == null) return Result.error("实际付款金额不能为空");
        recycleOrderService.pickupOrder(id, dto.getActualPrice());
        return Result.success("取货确认成功");
    }

    /** 完成入库 */
    @PutMapping("/complete/{id}")
    public Result<?> complete(@PathVariable Long id) {
        recycleOrderService.completeOrder(id);
        return Result.success("入库完成");
    }

    /** 取消订单 */
    @PutMapping("/cancel/{id}")
    public Result<?> cancel(@PathVariable Long id) {
        recycleOrderService.cancelOrder(id);
        return Result.success("订单已取消");
    }
}
