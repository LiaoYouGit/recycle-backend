package com.recycle.auth.controller;

import com.recycle.common.JwtUtil;
import com.recycle.common.Result;
import com.recycle.common.entity.SysUser;
import com.recycle.auth.service.SysUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    @Data
    public static class LoginDTO {
        private String username;
        private String password;
    }

    @PostMapping("/auth/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        SysUser user = sysUserService.login(dto.getUsername(), dto.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("avatar", user.getAvatar());
        return Result.success(data);
    }
}
