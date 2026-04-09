package com.recycle.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.recycle.common.entity.RecycleCategory;

import java.util.List;

public interface RecycleCategoryService extends IService<RecycleCategory> {
    List<RecycleCategory> listEnabled();
    Page<RecycleCategory> manageList(int page, int size);
}
