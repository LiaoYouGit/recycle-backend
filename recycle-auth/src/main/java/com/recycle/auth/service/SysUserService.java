package com.recycle.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.recycle.common.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser login(String username, String password);
}
