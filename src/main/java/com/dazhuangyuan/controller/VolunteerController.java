package com.dazhuangyuan.controller;

import com.dazhuangyuan.algorithm.VolunteerMatchingAlgorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dazhuangyuan.common.BusinessException;
import com.dazhuangyuan.common.Result;
import com.dazhuangyuan.entity.*;
import com.dazhuangyuan.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 志愿填报控制器 - 核心功能
 */
@Slf4j
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    @Autowired
    private VolunteerMatchingAlgorithm algorithm;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserVolunteerMapper volunteerMapper;

    @Autowired
    private VolunteerItemMapper volunteerItemMapper;

    @Autowired
    private CollegeMapper collegeMapper;

    @Autowired
    private MajorMapper majorMapper;

    @Autowired
    private ScoreRankMapper scoreRankMapper;

    @Autowired
    private ProvinceScoreLineMapper scoreLineMapper;

    @Autowired
    private AdmissionDataMapper admissionDataMapper;

    /**
     * 生成冲稳保志愿方案（核心接口）
     * 
     * 请求参数：
     * - score: 高考分数（必填）
     * - rank: 位次（可选，不填则根据分数推算）
     * - subjects: 选科列表（必填，如["物理","化学"]）
     * - category: 物理类/历史类（必填）
     * - province: 意向省份（默认广东）
     * - batch: 本科批/专科批（默认本科批）
     * - strategy: attack(冲击型)/steady(稳妥型)（默认稳妥型）
     */
    @PostMapping("/generate")
    public Result<?> generateVolunteer(@RequestBody Map<String, Object> params, 
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);

        // 获取参数
        Integer score = (Integer) params.get("score");
        Integer rank = params.get("rank") != null ? (Integer) params.get("rank") : null;
        List<String> subjects = (List<String>) params.get("subjects");
        String category = (String) params.get("category");
        String province = (String) params.getOrDefault("province", "广东");
        String batch = (String) params.getOrDefault("batch", "本科批");
        String strategy = (String) params.getOrDefault("strategy", "steady");

        // 参数校验
        if (score == null || score < 100 || score > 750) {
            return Result.error(400, "请输入正确的高考分数（100-750）");
        }
        if (subjects == null || subjects.isEmpty()) {
            return Result.error(400, "请选择考试科目");
        }
        if (category == null || (!"物理类".equals(category) && !"历史类".equals(category))) {
            return Result.error(400, "请选择考生类别（物理类/历史类）");
        }

        // 根据VIP等级确定最大推荐数量
        int maxTotal;
        boolean isPreview = false;
        if (user.getVipLevel() == 0) {
            // 免费用户：预览3所
            maxTotal = 3;
            isPreview = true;
        } else if (user.getVipLevel() == 1) {
            maxTotal = 10;
        } else {
            maxTotal = 45;
        }

        // 执行匹配算法
        List<VolunteerMatchingAlgorithm.VolunteerResult> results = algorithm.generateVolunteers(
                score, rank, subjects, category, province, batch, strategy, maxTotal);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("inputParams", params);
        data.put("total", results.size());

        long rushCount = results.stream().filter(r -> "冲".equals(r.getType())).count();
        long stableCount = results.stream().filter(r -> "稳".equals(r.getType())).count();
        long safeCount = results.stream().filter(r -> "保".equals(r.getType())).count();
        data.put("rushCount", rushCount);
        data.put("stableCount", stableCount);
        data.put("safeCount", safeCount);
        data.put("isPreview", isPreview);
        data.put("maxTotal", maxTotal);

        // 转换结果为前端需要的格式
        List<Map<String, Object>> volunteerList = results.stream().map(result -> {
            Map<String, Object> item = new HashMap<>();
            item.put("sortOrder", result.getSortOrder());
            item.put("type", result.getType());
            item.put("probability", result.getProbability());
            item.put("collegeId", result.getCollege().getId());
            item.put("collegeName", result.getCollege().getCollegeName());
            item.put("collegeCode", result.getCollege().getCollegeCode());
            item.put("province", result.getCollege().getProvince());
            item.put("city", result.getCollege().getCity());
            item.put("collegeType", result.getCollege().getCollegeType());
            item.put("collegeNature", result.getCollege().getCollegeNature());
            item.put("collegeLevel", result.getCollege().getCollegeLevel());
            item.put("logoUrl", result.getCollege().getLogoUrl());
            item.put("groupId", result.getGroup().getId());
            item.put("groupCode", result.getGroup().getGroupCode());
            item.put("groupName", result.getGroup().getGroupName());
            item.put("requireSubjects", result.getGroup().getRequireSubjects());
            item.put("weightedAvgRank", result.getWeightedAvgRank());
            item.put("weightedAvgScore", result.getWeightedAvgScore());
            
            // 推荐专业
            List<Map<String, Object>> majorList = result.getRecommendMajors().stream().map(m -> {
                Map<String, Object> mItem = new HashMap<>();
                mItem.put("majorId", m.getId());
                mItem.put("majorName", m.getMajorName());
                mItem.put("majorCode", m.getMajorCode());
                mItem.put("category", m.getCategory());
                mItem.put("isHot", m.getIsHot());
                return mItem;
            }).collect(Collectors.toList());
            item.put("recommendMajors", majorList);

            // 历年录取数据
            List<Map<String, Object>> admissionList = result.getAdmissionDataList().stream().map(ad -> {
                Map<String, Object> adItem = new HashMap<>();
                adItem.put("year", ad.getYear());
                adItem.put("minScore", ad.getMinScore());
                adItem.put("avgScore", ad.getAvgScore());
                adItem.put("maxScore", ad.getMaxScore());
                adItem.put("minRank", ad.getMinRank());
                adItem.put("avgRank", ad.getAvgRank());
                return adItem;
            }).collect(Collectors.toList());
            item.put("admissionHistory", admissionList);

            return item;
        }).collect(Collectors.toList());

        data.put("volunteers", volunteerList);

        // VIP升级提示
        if (isPreview) {
            data.put("upgradeTip", "免费版仅展示3所推荐院校，升级VIP可查看完整45所方案");
        } else if (user.getVipLevel() == 1 && results.size() >= 10) {
            data.put("upgradeTip", "基础版最多推荐10所，升级专业版可查看完整45所方案");
        }

        return Result.ok(data);
    }

    /**
     * 保存志愿方案
     */
    @PostMapping("/save")
    public Result<?> savePlan(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        String title = (String) params.getOrDefault("title", "我的志愿方案");
        List<Map<String, Object>> volunteers = (List<Map<String, Object>>) params.get("volunteers");

        if (volunteers == null || volunteers.isEmpty()) {
            return Result.error(400, "志愿方案不能为空");
        }

        // 创建方案
        UserVolunteer plan = new UserVolunteer();
        plan.setUserId(userId);
        plan.setTitle(title);
        plan.setTotalCount(volunteers.size());
        plan.setRushCount((int) volunteers.stream().filter(v -> "冲".equals(v.get("type"))).count());
        plan.setStableCount((int) volunteers.stream().filter(v -> "稳".equals(v.get("type"))).count());
        plan.setSafeCount((int) volunteers.stream().filter(v -> "保".equals(v.get("type"))).count());
        volunteerMapper.insert(plan);

        // 保存明细
        for (Map<String, Object> v : volunteers) {
            VolunteerItem item = new VolunteerItem();
            item.setPlanId(plan.getId());
            item.setCollegeId(toLong(v.get("collegeId")));
            item.setGroupId(toLong(v.get("groupId")));
            item.setSortOrder((Integer) v.getOrDefault("sortOrder", 0));
            item.setType((String) v.get("type"));
            item.setAdmissionProbability(new BigDecimal(String.valueOf(v.get("probability"))));
            item.setIsAdjust((Integer) v.getOrDefault("isAdjust", 1));
            item.setNote((String) v.get("note"));
            volunteerItemMapper.insert(item);
        }

        return Result.ok("保存成功", plan);
    }

    /**
     * 我的志愿方案列表
     */
    @GetMapping("/myPlans")
    public Result<?> myPlans(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        LambdaQueryWrapper<UserVolunteer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVolunteer::getUserId, userId)
                .orderByDesc(UserVolunteer::getCreatedAt);

        List<UserVolunteer> plans = volunteerMapper.selectList(wrapper);
        return Result.ok(plans);
    }

    /**
     * 方案详情
     */
    @GetMapping("/{planId}")
    public Result<?> planDetail(@PathVariable Long planId) {
        UserVolunteer plan = volunteerMapper.selectById(planId);
        if (plan == null) {
            return Result.error(404, "方案不存在");
        }

        LambdaQueryWrapper<VolunteerItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VolunteerItem::getPlanId, planId)
                .orderByAsc(VolunteerItem::getSortOrder);

        List<VolunteerItem> items = volunteerItemMapper.selectList(wrapper);

        // 填充院校和专业信息
        List<Map<String, Object>> detailedItems = items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("sortOrder", item.getSortOrder());
            map.put("type", item.getType());
            map.put("probability", item.getAdmissionProbability());
            map.put("isAdjust", item.getIsAdjust());
            map.put("note", item.getNote());

            College college = collegeMapper.selectById(item.getCollegeId());
            if (college != null) {
                map.put("collegeId", college.getId());
                map.put("collegeName", college.getCollegeName());
                map.put("province", college.getProvince());
                map.put("city", college.getCity());
                map.put("collegeType", college.getCollegeType());
                map.put("collegeNature", college.getCollegeNature());
                map.put("logoUrl", college.getLogoUrl());
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("items", detailedItems);

        return Result.ok(result);
    }

    /**
     * 查询一分一段表
     */
    @GetMapping("/scoreRank")
    public Result<?> queryScoreRank(
            @RequestParam(defaultValue = "广东") String province,
            @RequestParam(defaultValue = "2024") Integer year,
            @RequestParam(defaultValue = "物理类") String category,
            @RequestParam(required = false) Integer score) {

        LambdaQueryWrapper<ScoreRank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRank::getProvince, province)
                .eq(ScoreRank::getYear, year)
                .eq(ScoreRank::getCategory, category);

        if (score != null) {
            wrapper.eq(ScoreRank::getScore, score);
        }

        wrapper.orderByDesc(ScoreRank::getScore);

        List<ScoreRank> list = scoreRankMapper.selectList(wrapper);

        // 如果查询单个分数，返回位次
        if (score != null && !list.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("rank", list.get(0).getRankCount());
            result.put("scoreCount", list.get(0).getScoreCount());
            result.put("category", category);
            result.put("year", year);
            return Result.ok(result);
        }

        // 限制返回数量
        if (list.size() > 200) {
            list = list.subList(0, 200);
        }

        return Result.ok(list);
    }

    /**
     * 查询批次线
     */
    @GetMapping("/scoreLine")
    public Result<?> queryScoreLine(
            @RequestParam(defaultValue = "广东") String province,
            @RequestParam(defaultValue = "2024") Integer year) {

        LambdaQueryWrapper<ProvinceScoreLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProvinceScoreLine::getProvince, province)
                .eq(ProvinceScoreLine::getYear, year)
                .orderByAsc(ProvinceScoreLine::getBatch);

        List<ProvinceScoreLine> list = scoreLineMapper.selectList(wrapper);
        return Result.ok(list);
    }

    /**
     * 导出志愿表为Excel
     */
    @GetMapping("/export/{planId}")
    public void exportPlan(@PathVariable Long planId, HttpServletResponse response) {
        try {
            UserVolunteer plan = volunteerMapper.selectById(planId);
            if (plan == null) return;

            LambdaQueryWrapper<VolunteerItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VolunteerItem::getPlanId, planId)
                    .orderByAsc(VolunteerItem::getSortOrder);
            List<VolunteerItem> items = volunteerItemMapper.selectList(wrapper);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                    java.net.URLEncoder.encode(plan.getTitle() + ".xlsx", "UTF-8"));

            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("志愿方案");

            // 表头
            String[] headers = {"序号", "类型", "院校名称", "专业组", "录取概率", "是否服从调剂", "备注"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 数据行
            int rowIdx = 1;
            for (VolunteerItem item : items) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                College college = collegeMapper.selectById(item.getCollegeId());

                row.createCell(0).setCellValue(item.getSortOrder());
                row.createCell(1).setCellValue(item.getType());
                row.createCell(2).setCellValue(college != null ? college.getCollegeName() : "");
                row.createCell(3).setCellValue("");
                row.createCell(4).setCellValue(item.getAdmissionProbability() + "%");
                row.createCell(5).setCellValue(item.getIsAdjust() == 1 ? "是" : "否");
                row.createCell(6).setCellValue(item.getNote() != null ? item.getNote() : "");
            }

            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (Exception e) {
            log.error("导出Excel失败", e);
        }
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try { return Long.valueOf(obj.toString()); } catch (Exception e) { return null; }
    }
}
