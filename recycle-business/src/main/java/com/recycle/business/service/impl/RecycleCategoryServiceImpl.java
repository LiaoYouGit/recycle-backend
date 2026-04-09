package com.recycle.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recycle.common.entity.RecycleCategory;
import com.recycle.business.mapper.RecycleCategoryMapper;
import com.recycle.business.service.RecycleCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecycleCategoryServiceImpl extends ServiceImpl<RecycleCategoryMapper, RecycleCategory> implements RecycleCategoryService {

    @Override
    public List<RecycleCategory> listEnabled() {
        return list(new LambdaQueryWrapper<RecycleCategory>()
                .eq(RecycleCategory::getStatus, 1)
                .orderByAsc(RecycleCategory::getSortOrder));
    }

    @Override
    public Page<RecycleCategory> manageList(int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<RecycleCategory>()
                        .orderByAsc(RecycleCategory::getSortOrder));
    }
}
