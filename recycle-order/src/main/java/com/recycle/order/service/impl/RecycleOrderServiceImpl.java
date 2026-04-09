package com.recycle.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recycle.common.BusinessException;
import com.recycle.common.entity.RecycleOrder;
import com.recycle.order.mapper.RecycleOrderMapper;
import com.recycle.order.service.RecycleOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RecycleOrderServiceImpl extends ServiceImpl<RecycleOrderMapper, RecycleOrder> implements RecycleOrderService {

    @Value("${service.business.url:http://localhost:8093}")
    private String businessServiceUrl;

    private final RestTemplate restTemplate;

    public RecycleOrderServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Page<RecycleOrder> orderList(Integer status, int page, int size) {
        Page<RecycleOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RecycleOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(RecycleOrder::getStatus, status);
        wrapper.orderByDesc(RecycleOrder::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public Page<RecycleOrder> listByPhone(String phone, int page, int size) {
        Page<RecycleOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RecycleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecycleOrder::getContactPhone, phone);
        wrapper.orderByDesc(RecycleOrder::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public void acceptOrder(Long orderId, Long recyclerId, String recyclerName) {
        RecycleOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 0) throw new BusinessException("该订单已被接单或已取消");
        order.setStatus(1);
        order.setRecyclerId(recyclerId);
        order.setRecyclerName(recyclerName);
        updateById(order);
    }

    @Override
    public void pickupOrder(Long orderId, BigDecimal actualPrice) {
        RecycleOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 1) throw new BusinessException("订单状态不正确");
        order.setStatus(2);
        order.setActualPrice(actualPrice);
        updateById(order);
    }

    @Override
    public void completeOrder(Long orderId) {
        RecycleOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 2) throw new BusinessException("订单状态不正确");

        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now());
        updateById(order);

        // 远程调用业务服务：入库 + 记录财务
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", orderId);
            params.put("itemName", order.getItemName());
            params.put("actualPrice", order.getActualPrice());
            params.put("contactName", order.getContactName());
            params.put("operatorId", order.getRecyclerId());

            restTemplate.postForObject(businessServiceUrl + "/api/internal/complete", params, Map.class);
        } catch (Exception e) {
            log.error("调用业务服务失败，订单{}完成入库异常", orderId, e);
            // 回滚订单状态
            order.setStatus(2);
            order.setCompleteTime(null);
            updateById(order);
            throw new BusinessException("入库失败，请稍后重试");
        }
    }

    @Override
    public void cancelOrder(Long orderId) {
        RecycleOrder order = getById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() == 3) throw new BusinessException("已完成的订单不能取消");
        if (order.getStatus() == 4) throw new BusinessException("订单已取消");
        order.setStatus(4);
        updateById(order);
    }
}
