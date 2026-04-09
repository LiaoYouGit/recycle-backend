package com.recycle.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.recycle.common.entity.FinanceRecord;

import java.util.Map;

public interface FinanceService extends IService<FinanceRecord> {
    Page<FinanceRecord> recordList(Integer type, int page, int size);
    Map<String, Object> statistics(String startDate, String endDate);
}
