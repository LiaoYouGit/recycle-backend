package com.recycle.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.recycle.common.BusinessException;
import com.recycle.common.entity.SysUser;
import com.recycle.auth.mapper.SysUserMapper;
import com.recycle.auth.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser login(String username, String password) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) throw new BusinessException("用户不存在");
        if (!password.equals(user.getPassword())) throw new BusinessException("密码错误");
        if (user.getStatus() != null && user.getStatus() == 0) throw new BusinessException("账号已被禁用");
        return user;
    }
}
