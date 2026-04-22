package com.dazhuangyuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dazhuangyuan.common.BusinessException;
import com.dazhuangyuan.common.PageResult;
import com.dazhuangyuan.common.Result;
import com.dazhuangyuan.entity.*;
import com.dazhuangyuan.mapper.*;
import com.dazhuangyuan.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认证控制器 - 手机号验证码登录
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SmsLogMapper smsLogMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${sms.expire-minutes}")
    private int smsExpireMinutes;

    @Value("${sms.interval-seconds}")
    private int smsIntervalSeconds;

    /**
     * 发送短信验证码
     */
    @PostMapping("/sendSms")
    public Result<?> sendSms(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String phone = params.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error(400, "请输入正确的手机号");
        }

        // 检查发送频率（60秒内不能重复发送）
        LambdaQueryWrapper<SmsLog> freqWrapper = new LambdaQueryWrapper<>();
        freqWrapper.eq(SmsLog::getPhone, phone)
                .ge(SmsLog::getCreatedAt, LocalDateTime.now().minusSeconds(smsIntervalSeconds));
        Long recentCount = smsLogMapper.selectCount(freqWrapper);
        if (recentCount > 0) {
            return Result.error(429, "发送太频繁，请" + smsIntervalSeconds + "秒后重试");
        }

        // 生成6位验证码
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        // 保存验证码记录
        SmsLog smsLog = new SmsLog();
        smsLog.setPhone(phone);
        smsLog.setCode(code);
        smsLog.setType("login");
        smsLog.setIp(request.getRemoteAddr());
        smsLog.setStatus(0);
        smsLog.setCreatedAt(LocalDateTime.now());
        smsLog.setExpiredAt(LocalDateTime.now().plusMinutes(smsExpireMinutes));
        smsLogMapper.insert(smsLog);

        // TODO: 调用阿里云SMS SDK发送短信
        // 实际部署时需替换为真实短信发送逻辑
        log.info("【大状元】验证码: {} -> {}", code, phone);

        // 开发环境直接返回验证码，生产环境不返回
        if (true) { // TODO: 改为环境变量判断
            Map<String, Object> result = new HashMap<>();
            result.put("code", code);
            result.put("expireMinutes", smsExpireMinutes);
            return Result.ok("验证码已发送", result);
        }

        return Result.ok("验证码已发送");
    }

    /**
     * 手机号+验证码登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");

        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error(400, "请输入正确的手机号");
        }
        if (code == null || code.length() != 6) {
            return Result.error(400, "请输入6位验证码");
        }

        // 验证验证码
        LambdaQueryWrapper<SmsLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsLog::getPhone, phone)
                .eq(SmsLog::getCode, code)
                .eq(SmsLog::getStatus, 0)
                .ge(SmsLog::getExpiredAt, LocalDateTime.now())
                .orderByDesc(SmsLog::getCreatedAt)
                .last("LIMIT 1");
        SmsLog smsLog = smsLogMapper.selectOne(wrapper);

        if (smsLog == null) {
            return Result.error(401, "验证码错误或已过期");
        }

        // 标记验证码已使用
        smsLog.setStatus(1);
        smsLogMapper.updateById(smsLog);

        // 查找或创建用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(userWrapper);

        if (user == null) {
            // 新用户注册
            user = new User();
            user.setPhone(phone);
            user.setNickname("考生" + phone.substring(7));
            user.setStatus(1);
            user.setVipLevel(0);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }

        // 更新登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 检查VIP是否过期
        if (user.getVipLevel() > 0 && user.getVipExpireAt() != null 
                && user.getVipExpireAt().isBefore(LocalDateTime.now())) {
            user.setVipLevel(0);
            userMapper.updateById(user);
        }

        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getPhone());

        // 返回用户信息和token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("phone", user.getPhone());
        result.put("nickname", user.getNickname());
        result.put("vipLevel", user.getVipLevel());
        result.put("isNewUser", user.getCreatedAt().isEqual(user.getLastLoginAt()) || 
                          user.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1)));

        return Result.ok("登录成功", result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userInfo")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.ok(user);
    }

    /**
     * 更新用户资料（分数、位次、选科等）
     */
    @PostMapping("/updateProfile")
    public Result<?> updateProfile(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (params.containsKey("nickname")) {
            user.setNickname((String) params.get("nickname"));
        }
        if (params.containsKey("studentScore")) {
            user.setStudentScore((Integer) params.get("studentScore"));
        }
        if (params.containsKey("studentRank")) {
            user.setStudentRank((Integer) params.get("studentRank"));
        }
        if (params.containsKey("studentSubjects")) {
            user.setStudentSubjects(com.alibaba.fastjson2.JSON.toJSONString(params.get("studentSubjects")));
        }
        if (params.containsKey("studentCategory")) {
            user.setStudentCategory((String) params.get("studentCategory"));
        }
        if (params.containsKey("targetProvince")) {
            user.setTargetProvince((String) params.get("targetProvince"));
        }
        if (params.containsKey("province")) {
            user.setProvince((String) params.get("province"));
        }
        if (params.containsKey("gender")) {
            user.setGender((Integer) params.get("gender"));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return Result.ok("更新成功", user);
    }
}
