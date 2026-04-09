package com.recycle.common;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AliyunSmsUtil {

    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;
    private final RedisTemplate<String, Object> redisTemplate;
    private IAcsClient acsClient;

    public AliyunSmsUtil(String accessKeyId, String accessKeySecret,
                         String signName, String templateCode,
                         RedisTemplate<String, Object> redisTemplate) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.redisTemplate = redisTemplate;
    }

    private IAcsClient getClient() {
        if (acsClient == null) {
            try {
                IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
                acsClient = new DefaultAcsClient(profile);
            } catch (Exception e) {
                log.error("创建阿里云短信客户端失败", e);
            }
        }
        return acsClient;
    }

    public String sendVerifyCode(String phone) {
        String code = String.format("%06d", (int) ((Math.random() * 9 + 1) * 100000));
        try {
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam(JSON.toJSONString(new HashMap<String, String>() {{
                put("code", code);
            }}));

            SendSmsResponse response = getClient().getAcsResponse(request);
            if ("OK".equals(response.getCode())) {
                redisTemplate.opsForValue().set("sms:verify:" + phone, code, 5, TimeUnit.MINUTES);
                log.info("短信验证码发送成功，手机号：{}", phone);
                return code;
            } else {
                log.error("短信发送失败，手机号：{}，错误：{}", phone, response.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("短信发送异常，手机号：{}", phone, e);
            return null;
        }
    }

    public boolean verifyCode(String phone, String code) {
        Object stored = redisTemplate.opsForValue().get("sms:verify:" + phone);
        if (stored != null && stored.toString().equals(code)) {
            redisTemplate.delete("sms:verify:" + phone);
            return true;
        }
        return false;
    }
}
