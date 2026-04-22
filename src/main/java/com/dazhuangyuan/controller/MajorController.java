package com.dazhuangyuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dazhuangyuan.common.PageResult;
import com.dazhuangyuan.common.Result;
import com.dazhuangyuan.entity.*;
import com.dazhuangyuan.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业查询控制器
 */
@RestController
@RequestMapping("/api/major")
public class MajorController {

    @Autowired
    private MajorMapper majorMapper;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        Page<Major> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();

        if (category != null && !category.isEmpty()) wrapper.eq(Major::getCategory, category);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Major::getMajorName, keyword);
        wrapper.orderByDesc(Major::getIsHot).orderByAsc(Major::getMajorCode);

        Page<Major> result = majorMapper.selectPage(pageParam, wrapper);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Major major = majorMapper.selectById(id);
        if (major == null) return Result.error(404, "专业不存在");
        return Result.ok(major);
    }

    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword, @RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Major::getMajorName, keyword).last("LIMIT " + limit);
        return Result.ok(majorMapper.selectList(wrapper));
    }

    @GetMapping("/categories")
    public Result<?> categories() {
        // 返回所有学科门类
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Major::getCategory).groupBy(Major::getCategory).isNotNull(Major::getCategory);
        List<Major> list = majorMapper.selectList(wrapper);
        return Result.ok(list.stream().map(Major::getCategory).distinct().toList());
    }

    @GetMapping("/hot")
    public Result<?> hotMajors() {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getIsHot, 1).last("LIMIT 20");
        return Result.ok(majorMapper.selectList(wrapper));
    }
}
