package com.recycle.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recycle.common.entity.FinanceRecord;
import com.recycle.business.mapper.FinanceRecordMapper;
import com.recycle.business.service.FinanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl extends ServiceImpl<FinanceRecordMapper, FinanceRecord> implements FinanceService {

    @Override
    public Page<FinanceRecord> recordList(Integer type, int page, int size) {
        Page<FinanceRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<FinanceRecord> wrapper = new LambdaQueryWrapper<>();
        if (type != null) wrapper.eq(FinanceRecord::getType, type);
        wrapper.orderByDesc(FinanceRecord::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> statistics(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<FinanceRecord> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(FinanceRecord::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(FinanceRecord::getCreateTime, LocalDate.parse(endDate).atTime(LocalTime.MAX));
        }
        List<FinanceRecord> records = list(wrapper);

        BigDecimal totalExpense = BigDecimal.ZERO, purchaseExpense = BigDecimal.ZERO;
        BigDecimal otherExpense = BigDecimal.ZERO, totalIncome = BigDecimal.ZERO;

        for (FinanceRecord r : records) {
            switch (r.getType()) {
                case 0: totalExpense = totalExpense.add(r.getAmount()); purchaseExpense = purchaseExpense.add(r.getAmount()); break;
                case 1: totalExpense = totalExpense.add(r.getAmount()); otherExpense = otherExpense.add(r.getAmount()); break;
                case 2: totalIncome = totalIncome.add(r.getAmount()); break;
            }
        }
        result.put("totalExpense", totalExpense);
        result.put("purchaseExpense", purchaseExpense);
        result.put("otherExpense", otherExpense);
        result.put("totalIncome", totalIncome);
        result.put("profit", totalIncome.subtract(totalExpense));
        result.put("recordCount", records.size());
        return result;
    }
}
