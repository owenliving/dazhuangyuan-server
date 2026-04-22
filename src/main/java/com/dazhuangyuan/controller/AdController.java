package com.dazhuangyuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dazhuangyuan.common.Result;
import com.dazhuangyuan.entity.*;
import com.dazhuangyuan.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 广告/运营位控制器
 */
@RestController
@RequestMapping("/api/ad")
public class AdController {

    @Autowired
    private AdConfigMapper adConfigMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 获取广告列表（按位置）
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) String position) {
        LambdaQueryWrapper<AdConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdConfig::getIsActive, 1);
        if (position != null) {
            wrapper.eq(AdConfig::getPosition, position);
        }
        wrapper.orderByAsc(AdConfig::getSortOrder);

        // 检查有效期
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        List<AdConfig> list = adConfigMapper.selectList(wrapper).stream()
                .filter(ad -> {
                    if (ad.getStartAt() != null && now.isBefore(ad.getStartAt())) return false;
                    if (ad.getEndAt() != null && now.isAfter(ad.getEndAt())) return false;
                    return true;
                })
                .collect(Collectors.toList());

        return Result.ok(list);
    }

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public Result<?> getConfig(@RequestParam String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        return Result.ok(config != null ? config.getConfigValue() : "");
    }

    /**
     * 获取所有公开配置
     */
    @GetMapping("/publicConfig")
    public Result<?> publicConfig() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SystemConfig::getConfigKey, 
                "site_name", "site_description", "contact_phone", "contact_wechat",
                "ad_enabled", "current_province", "db_year");
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        
        java.util.Map<String, String> result = new java.util.HashMap<>();
        for (SystemConfig c : configs) {
            result.put(c.getConfigKey(), c.getConfigValue());
        }
        return Result.ok(result);
    }
}
