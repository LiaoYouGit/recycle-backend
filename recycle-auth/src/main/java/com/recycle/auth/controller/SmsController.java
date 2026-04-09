package com.recycle.auth.controller;

import com.recycle.common.AliyunSmsUtil;
import com.recycle.common.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SmsController {

    private final AliyunSmsUtil aliyunSmsUtil;

    @Data
    public static class SmsDTO {
        private String phone;
    }

    @PostMapping("/sms/send")
    public Result<?> sendCode(@RequestBody SmsDTO dto) {
        if (dto.getPhone() == null || dto.getPhone().length() != 11) {
            return Result.error("手机号格式不正确");
        }
        String code = aliyunSmsUtil.sendVerifyCode(dto.getPhone());
        if (code != null) return Result.success("验证码已发送");
        return Result.error("验证码发送失败，请稍后重试");
    }
}
