package com.recycle.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.recycle.common.entity.RecycleOrder;

public interface RecycleOrderService extends IService<RecycleOrder> {
    Page<RecycleOrder> orderList(Integer status, int page, int size);
    Page<RecycleOrder> listByPhone(String phone, int page, int size);
    void acceptOrder(Long orderId, Long recyclerId, String recyclerName);
    void pickupOrder(Long orderId, java.math.BigDecimal actualPrice);
    void completeOrder(Long orderId);
    void cancelOrder(Long orderId);
}
