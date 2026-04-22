package com.dazhuangyuan.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dazhuangyuan.entity.*;
import com.dazhuangyuan.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 广东高考志愿匹配核心算法
 * 
 * 算法核心：位次匹配法 + 选科过滤 + 冲稳保分类
 * 
 * 广东新高考规则：
 * - 采用"院校专业组"模式
 * - 本科批/专科批各可填45个平行志愿
 * - 每个专业组下最多6个专业
 * - 冲击型梯度: 16:14:15
 * - 稳妥型梯度: 4:25:16
 */
@Slf4j
@Component
public class VolunteerMatchingAlgorithm {

    @Autowired
    private AdmissionDataMapper admissionDataMapper;

    @Autowired
    private CollegeMajorGroupMapper groupMapper;

    @Autowired
    private CollegeMapper collegeMapper;

    @Autowired
    private MajorMapper majorMapper;

    @Autowired
    private CollegeMajorMapper collegeMajorMapper;

    @Autowired
    private ScoreRankMapper scoreRankMapper;

    @Autowired
    private ProvinceScoreLineMapper scoreLineMapper;

    // 年份权重：最新年权重最高
    private static final double[] YEAR_WEIGHTS = {0.5, 0.3, 0.2};

    /**
     * 生成冲稳保志愿方案
     * 
     * @param studentScore 高考分数
     * @param studentRank 位次
     * @param subjects 选科列表（如["物理","化学"]）
     * @param category 物理类/历史类
     * @param province 目标省份
     * @param batch 批次（本科批/专科批）
     * @param strategy 策略类型（attack冲击型/steady稳妥型）
     * @param maxTotal 最大推荐数量
     * @return 志愿方案
     */
    public List<VolunteerResult> generateVolunteers(
            Integer studentScore, Integer studentRank,
            List<String> subjects, String category,
            String province, String batch,
            String strategy, int maxTotal) {

        log.info("开始生成志愿方案: 分数={}, 位次={}, 选科={}, 类别={}, 省份={}, 批次={}, 策略={}, 最大数量={}",
                studentScore, studentRank, subjects, category, province, batch, strategy, maxTotal);

        // Step 1: 如果没有位次，根据分数推算位次
        if (studentRank == null || studentRank <= 0) {
            studentRank = estimateRank(province, studentScore, category);
            log.info("根据分数推算位次: {} -> {}", studentScore, studentRank);
        }

        // Step 2: 获取近3年的录取数据
        int currentYear = 2024; // 最新录取数据年份（系统数据年份由dy_system_config.db_year控制）
        List<Integer> years = new ArrayList<>();
        for (int i = 0; i < 3 && (currentYear - i) >= 2022; i++) {
            years.add(currentYear - i);
        }

        // Step 3: 获取符合选科要求的专业组
        List<CollegeMajorGroup> matchedGroups = filterGroupsBySubjects(subjects, batch, years);
        log.info("符合选科要求的专业组数量: {}", matchedGroups.size());

        // Step 4: 对每个专业组计算匹配度和录取概率
        List<VolunteerResult> allResults = new ArrayList<>();

        for (CollegeMajorGroup group : matchedGroups) {
            // 获取该专业组的历年录取数据
            List<AdmissionData> groupAdmissions = getGroupAdmissions(
                    group.getCollegeId().longValue(), group.getId(), province, category, years, batch);

            if (groupAdmissions.isEmpty()) {
                continue;
            }

            // 计算加权录取位次
            WeightedResult weightedResult = calculateWeightedAdmission(groupAdmissions, years);
            if (weightedResult == null) continue;

            // 计算匹配度和录取概率
            MatchResult matchResult = calculateMatch(studentRank, weightedResult, group.getCollegeId().longValue());

            // 获取专业组下的专业
            List<Major> groupMajors = getGroupMajors(group.getCollegeId().longValue(), group.getId(), group.getYear());

            // 获取院校信息
            College college = collegeMapper.selectById(group.getCollegeId());
            if (college == null) continue;

            // 本省院校加权（广东院校优先）
            double bonusFactor = 1.0;
            if ("广东".equals(college.getProvince()) && !"广东".equals(province)) {
                bonusFactor = 1.02;
            } else if (!"广东".equals(college.getProvince()) && "广东".equals(province)) {
                bonusFactor = 0.98; // 广东考生出省稍降
            }

            // 构建结果
            VolunteerResult result = new VolunteerResult();
            result.setCollege(college);
            result.setGroup(group);
            result.setMajors(groupMajors);
            result.setAdmissionDataList(groupAdmissions);
            result.setWeightedAvgRank(weightedResult.getWeightedAvgRank());
            result.setWeightedAvgScore(weightedResult.getWeightedAvgScore());
            result.setMatchRate(matchResult.getMatchRate() * bonusFactor);
            result.setProbability(matchResult.getProbability());
            result.setType(matchResult.getType());
            result.setRecommendMajors(recommendMajors(groupMajors, matchResult.getProbability()));

            allResults.add(result);
        }

        // Step 5: 冲稳保分类和排序
        return classifyAndSort(allResults, strategy, maxTotal);
    }

    /**
     * 根据选科要求过滤专业组
     */
    private List<CollegeMajorGroup> filterGroupsBySubjects(List<String> subjects, String batch, List<Integer> years) {
        LambdaQueryWrapper<CollegeMajorGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CollegeMajorGroup::getYear, years)
                .eq(CollegeMajorGroup::getBatch, batch);

        List<CollegeMajorGroup> allGroups = groupMapper.selectList(wrapper);

        // 过滤选科要求
        return allGroups.stream()
                .filter(group -> {
                    if (group.getRequireSubjects() == null || group.getRequireSubjects().isEmpty()) {
                        return true; // 没有选科要求 = 不限选科
                    }
                    return subjects.containsAll(group.getRequireSubjects());
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取专业组的录取数据
     */
    private List<AdmissionData> getGroupAdmissions(Long collegeId, Long groupId, 
            String province, String category, List<Integer> years, String batch) {
        LambdaQueryWrapper<AdmissionData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionData::getCollegeId, collegeId)
                .eq(groupId != null, AdmissionData::getGroupId, groupId)
                .eq(AdmissionData::getProvince, province)
                .eq(AdmissionData::getCategory, category)
                .in(AdmissionData::getYear, years)
                .eq(AdmissionData::getBatch, batch)
                .isNotNull(AdmissionData::getAvgRank);
        return admissionDataMapper.selectList(wrapper);
    }

    /**
     * 计算加权录取位次和分数
     */
    private WeightedResult calculateWeightedAdmission(List<AdmissionData> data, List<Integer> years) {
        double weightedAvgRank = 0;
        double weightedAvgScore = 0;
        double totalWeight = 0;
        boolean hasRank = false;

        for (int i = 0; i < years.size() && i < YEAR_WEIGHTS.length; i++) {
            final int year = years.get(i);
            Optional<AdmissionData> yearData = data.stream()
                    .filter(d -> d.getYear() == year)
                    .max(Comparator.comparingInt(d -> d.getYear()));

            if (yearData.isPresent() && yearData.get().getAvgRank() != null) {
                weightedAvgRank += yearData.get().getAvgRank() * YEAR_WEIGHTS[i];
                if (yearData.get().getAvgScore() != null) {
                    weightedAvgScore += yearData.get().getAvgScore() * YEAR_WEIGHTS[i];
                }
                totalWeight += YEAR_WEIGHTS[i];
                hasRank = true;
            }
        }

        if (!hasRank) return null;

        return new WeightedResult(
                (long) (weightedAvgRank / totalWeight),
                (long) (weightedAvgScore / totalWeight)
        );
    }

    /**
     * 计算匹配度和录取概率
     */
    private MatchResult calculateMatch(Long studentRank, WeightedResult weighted, Long collegeId) {
        if (weighted.getWeightedAvgRank() == null || weighted.getWeightedAvgRank() <= 0) {
            return null;
        }

        // 匹配度计算：位次越低于录取位次 = 越安全
        double matchRate = (double) studentRank / weighted.getWeightedAvgRank();

        // 录取概率计算
        double probability;
        String type;

        if (matchRate <= 0.85) {
            // 位次远高于录取位次 → 冲刺
            probability = 25 + (0.85 - matchRate) * 50; // 25-68%
            probability = Math.min(probability, 60);
            type = "冲";
        } else if (matchRate <= 1.05) {
            // 位次在录取位次±15% → 稳妥
            probability = 60 + (1.05 - matchRate) / 0.20 * 25; // 60-85%
            type = "稳";
        } else {
            // 位次低于录取位次 → 保底
            probability = 85 + Math.min((matchRate - 1.05) * 20, 13); // 85-98%
            probability = Math.min(probability, 98);
            type = "保";
        }

        probability = Math.round(probability * 100.0) / 100.0;

        return new MatchResult(matchRate, probability, type);
    }

    /**
     * 推荐专业：根据录取概率推荐合适的专业
     */
    private List<Major> recommendMajors(List<Major> groupMajors, double probability) {
        if (groupMajors == null || groupMajors.isEmpty()) {
            return new ArrayList<>();
        }

        // 热门专业优先，最多推荐6个
        return groupMajors.stream()
                .sorted(Comparator.comparingInt(Major::getIsHot).reversed())
                .limit(6)
                .collect(Collectors.toList());
    }

    /**
     * 冲稳保分类和排序
     */
    private List<VolunteerResult> classifyAndSort(List<VolunteerResult> allResults, 
                                                   String strategy, int maxTotal) {
        // 分成冲/稳/保三类
        Map<String, List<VolunteerResult>> classified = allResults.stream()
                .collect(Collectors.groupingBy(VolunteerResult::getType));

        List<VolunteerResult> rushList = classified.getOrDefault("冲", new ArrayList<>());
        List<VolunteerResult> stableList = classified.getOrDefault("稳", new ArrayList<>());
        List<VolunteerResult> safeList = classified.getOrDefault("保", new ArrayList<>());

        // 根据策略确定比例
        int rushCount, stableCount, safeCount;
        if ("attack".equals(strategy)) {
            // 冲击型 16:14:15
            rushCount = (int) (maxTotal * 0.36);
            stableCount = (int) (maxTotal * 0.31);
            safeCount = maxTotal - rushCount - stableCount;
        } else {
            // 稳妥型 4:25:16（默认）
            rushCount = (int) (maxTotal * 0.09);
            stableCount = (int) (maxTotal * 0.55);
            safeCount = maxTotal - rushCount - stableCount;
        }

        // 排序
        // 冲刺：按录取概率从低到高（越难进的排前面）
        rushList.sort(Comparator.comparingDouble(VolunteerResult::getProbability));
        // 稳妥：按院校层次+匹配度综合排序
        stableList.sort(Comparator.comparingDouble(VolunteerResult::getMatchRate).reversed());
        // 保底：按录取概率从高到低
        safeList.sort(Comparator.comparingDouble(VolunteerResult::getProbability).reversed());

        // 截取指定数量
        List<VolunteerResult> finalResult = new ArrayList<>();
        finalResult.addAll(rushList.stream().limit(rushCount).collect(Collectors.toList()));
        finalResult.addAll(stableList.stream().limit(stableCount).collect(Collectors.toList()));
        finalResult.addAll(safeList.stream().limit(safeCount).collect(Collectors.toList()));

        // 设置排序号
        for (int i = 0; i < finalResult.size(); i++) {
            finalResult.get(i).setSortOrder(i + 1);
        }

        log.info("志愿方案生成完成: 冲{}所, 稳{}所, 保{}所", 
                finalResult.stream().filter(r -> "冲".equals(r.getType())).count(),
                finalResult.stream().filter(r -> "稳".equals(r.getType())).count(),
                finalResult.stream().filter(r -> "保".equals(r.getType())).count());

        return finalResult;
    }

    /**
     * 根据分数推算位次（使用一分一段表）
     */
    private Integer estimateRank(String province, Integer score, String category) {
        LambdaQueryWrapper<ScoreRank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRank::getProvince, province)
                .eq(ScoreRank::getCategory, category)
                .eq(ScoreRank::getScore, score)
                .eq(ScoreRank::getYear, 2024)
                .last("LIMIT 1");

        ScoreRank scoreRank = scoreRankMapper.selectOne(wrapper);
        return scoreRank != null ? scoreRank.getRankCount() : score * 1000; // 粗略估算
    }

    /**
     * 获取专业组下的专业
     */
    private List<Major> getGroupMajors(Long collegeId, Long groupId, Integer year) {
        LambdaQueryWrapper<CollegeMajor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollegeMajor::getCollegeId, collegeId)
                .eq(CollegeMajor::getGroupId, groupId)
                .eq(CollegeMajor::getEnrollmentYear, year);

        List<CollegeMajor> collegeMajors = collegeMajorMapper.selectList(wrapper);

        List<Long> majorIds = collegeMajors.stream()
                .map(CollegeMajor::getMajorId)
                .collect(Collectors.toList());

        if (majorIds.isEmpty()) {
            return new ArrayList<>();
        }

        return majorMapper.selectBatchIds(majorIds);
    }

    // ==================== 内部类 ====================

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class WeightedResult {
        private Long weightedAvgRank;
        private Long weightedAvgScore;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MatchResult {
        private double matchRate;
        private double probability;
        private String type;
    }

    @lombok.Data
    public static class VolunteerResult {
        private int sortOrder;
        private College college;
        private CollegeMajorGroup group;
        private List<Major> majors;
        private List<AdmissionData> admissionDataList;
        private Long weightedAvgRank;
        private Long weightedAvgScore;
        private double matchRate;
        private double probability;
        private String type; // 冲/稳/保
        private List<Major> recommendMajors;
    }
}
