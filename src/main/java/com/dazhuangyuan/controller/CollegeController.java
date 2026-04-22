package com.dazhuangyuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dazhuangyuan.common.PageResult;
import com.dazhuangyuan.common.Result;
import com.dazhuangyuan.entity.*;
import com.dazhuangyuan.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 院校查询控制器
 */
@RestController
@RequestMapping("/api/college")
public class CollegeController {

    @Autowired
    private CollegeMapper collegeMapper;

    @Autowired
    private CollegeMajorGroupMapper groupMapper;

    @Autowired
    private MajorMapper majorMapper;

    @Autowired
    private CollegeMajorMapper collegeMajorMapper;

    @Autowired
    private AdmissionDataMapper admissionDataMapper;

    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String collegeType,
            @RequestParam(required = false) String collegeNature,
            @RequestParam(required = false) String collegeLevel,
            @RequestParam(required = false) String keyword) {

        Page<College> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();

        if (province != null && !province.isEmpty()) wrapper.eq(College::getProvince, province);
        if (collegeType != null && !collegeType.isEmpty()) wrapper.like(College::getCollegeType, collegeType);
        if (collegeNature != null && !collegeNature.isEmpty()) wrapper.eq(College::getCollegeNature, collegeNature);
        if (collegeLevel != null && !collegeLevel.isEmpty()) wrapper.eq(College::getCollegeLevel, collegeLevel);
        if (keyword != null && !keyword.isEmpty()) wrapper.like(College::getCollegeName, keyword);

        wrapper.orderByAsc(College::getCollegeCode);
        Page<College> result = collegeMapper.selectPage(pageParam, wrapper);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        College college = collegeMapper.selectById(id);
        if (college == null) return Result.error(404, "院校不存在");
        return Result.ok(college);
    }

    @GetMapping("/search")
    public Result<?> search(@RequestParam String keyword, @RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(College::getCollegeName, keyword).or().like(College::getCollegeCode, keyword).last("LIMIT " + limit);
        return Result.ok(collegeMapper.selectList(wrapper));
    }

    @GetMapping("/{id}/groups")
    public Result<?> groups(@PathVariable Long id, @RequestParam(defaultValue = "2024") Integer year) {
        LambdaQueryWrapper<CollegeMajorGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollegeMajorGroup::getCollegeId, id).eq(CollegeMajorGroup::getYear, year);
        return Result.ok(groupMapper.selectList(wrapper));
    }

    @GetMapping("/{id}/admission")
    public Result<?> admission(@PathVariable Long id,
            @RequestParam(defaultValue = "广东") String province,
            @RequestParam(required = false) String category) {
        LambdaQueryWrapper<AdmissionData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionData::getCollegeId, id)
                .eq(AdmissionData::getProvince, province)
                .eq(category != null, AdmissionData::getCategory, category)
                .orderByDesc(AdmissionData::getYear);
        return Result.ok(admissionDataMapper.selectList(wrapper));
    }

    @GetMapping("/compare")
    public Result<?> compare(@RequestParam List<Long> ids) {
        if (ids == null || ids.size() < 2 || ids.size() > 4) return Result.error(400, "请选择2-4所院校进行对比");
        return Result.ok(collegeMapper.selectBatchIds(ids));
    }

    @GetMapping("/hot")
    public Result<?> hotColleges(@RequestParam(defaultValue = "广东") String province) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getProvince, province)
                .and(w -> w.like(College::getCollegeType, "985")
                        .or().like(College::getCollegeType, "211")
                        .or().like(College::getCollegeType, "双一流"))
                .last("LIMIT 10");
        return Result.ok(collegeMapper.selectList(wrapper));
    }
}
