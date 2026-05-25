package ynu.edu.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ynu.edu.service.CarbonCoefficientService;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class CarbonDataSimulator {
    private final Random random = new Random();

    @Autowired
    private CarbonCoefficientService coefficientService;

    // ================== 1. 基础配置数据 ==================

    // 区域类型-基准排放映射（单位：kgCO₂/次采集）
    private final Map<String, Double> areaBaseCarbonMap = new HashMap<String, Double>() {{
        put("教学", 60.0);
        put("宿舍", 50.0);
        put("食堂", 80.0);
        put("绿化", 0.0);     // 绿化区本身无能源消耗排放
        put("交通", 15.0);
    }};

    // 区域类型-固碳基准（所有区域都有固碳值）
    private final Map<String, Double> areaSequestrationMap = new HashMap<String, Double>() {{
        put("教学", 5.0);     // 楼宇周边绿化
        put("宿舍", 3.0);     // 宿舍区少量绿化
        put("食堂", 2.0);     // 食堂周边
        put("绿化", 50.0);    // 核心固碳区
        put("交通", 12.0);    // 道路两侧行道树（固碳量较高）
    }};

    // 能源类型占比（排放拆分）
    private final Map<String, Map<String, Double>> energyTypeRatioMap = new HashMap<String, Map<String, Double>>() {{
        put("教学", Map.of("ELECTRICITY", 0.95, "GAS", 0.05));
        put("宿舍", Map.of("ELECTRICITY", 0.85, "GAS", 0.15));
        put("食堂", Map.of("ELECTRICITY", 0.30, "GAS", 0.70));
        put("交通", Map.of("ELECTRICITY", 0.40, "FUEL", 0.60));
        put("绿化", Map.of()); // 绿化区无能源消耗
    }};

    // 预警阈值配置
    private final Map<String, Map<String, Double>> warningThresholdMap = new HashMap<String, Map<String, Double>>() {{
        put("教学", Map.of("ELECTRICITY", 100.0));
        put("宿舍", Map.of("ELECTRICITY", 80.0));
        put("食堂", Map.of("GAS", 120.0));
        put("交通", Map.of("FUEL", 25.0));
        put("绿化", Map.of("TREE", 30.0)); // 绿化区仅关注固碳过低
    }};

    // ================== 2. 核心模拟逻辑 ==================

    public SimulatedResult generateSimulatedData(Long areaId, String areaType) {
        LocalDateTime now = LocalDateTime.now();
        boolean isGreenArea = "绿化".equals(areaType);

        // 5% 概率触发异常
        boolean isAbnormal = random.nextDouble() < 0.05;

        // --- A. 计算固碳量 (所有区域都有) ---
        double baseSeq = areaSequestrationMap.getOrDefault(areaType, 0.0);
        double seasonFactor = getSeasonFactor(now, areaType);

        // 固碳波动：受季节和随机微扰影响
        double currentSeq = baseSeq * seasonFactor * (1.0 + (random.nextDouble() * 0.2 - 0.1));

        // 根据是否异常进行【强制干预】
        if (isGreenArea) {
            double threshold = warningThresholdMap.get("绿化").get("TREE"); // 30.0

            if (isAbnormal) {
                // 【异常情况】：强制低于阈值 (生成 0.5~0.9 倍阈值的数据)
                // 结果范围：15.0 ~ 27.0，必报警
                currentSeq = threshold * (0.5 + random.nextDouble() * 0.4);
            } else {
                // 【正常情况】：强制兜底，确保高于阈值
                // 如果自然计算结果低于阈值（例如冬季），强制拉高到 1.1~1.3 倍阈值
                if (currentSeq <= threshold) {
                    currentSeq = threshold * (1.1 + random.nextDouble() * 0.2);
                }
            }
        }

        double totalSequestration = Math.max(currentSeq, 0.0);

        // --- B. 计算排放量 (功能区计算，绿化区为0) ---
        double totalEmission = 0.0;
        Map<String, Double> emissionMap = new HashMap<>();
        Map<String, Double> consumeMap = new HashMap<>();

        if (!isGreenArea) {
            String dominantType = getDominantType(areaType);
            double threshold = warningThresholdMap.get(areaType).getOrDefault(dominantType, 9999.0);
            double ratio = energyTypeRatioMap.get(areaType).getOrDefault(dominantType, 0.5);

            if (isAbnormal) {
                // 异常模式：功能区排放飙升（逆向推导，必超标）
                double targetEmission = threshold * (1.1 + random.nextDouble() * 0.2);
                totalEmission = targetEmission / ratio;
                log.info("生成了一个功能区异常数据");
            } else {
                // 正常模式：计算
                double base = areaBaseCarbonMap.getOrDefault(areaType, 50.0);
                double timeFactor = getTimePeriodFactor(now);
                double weekdayFactor = getWeekdayFactor(now, areaType);

                totalEmission = base * timeFactor * weekdayFactor * seasonFactor * (1 + random.nextDouble() * 0.1 - 0.05);

                // 正常模式下的“天花板”保护，防止误报
                if (totalEmission * ratio > threshold) {
                    totalEmission = (threshold * 0.9) / ratio;
                }
            }

            // 拆分能源明细
            Map<String, Double> ratios = energyTypeRatioMap.get(areaType);
            for (Map.Entry<String, Double> entry : ratios.entrySet()) {
                String eType = entry.getKey();
                Double eRatio = entry.getValue();

                double typeEmission = totalEmission * eRatio;
                emissionMap.put(eType, typeEmission);

                // 计算消耗量 (排放量 / 系数)
                Double coef = coefficientService.getCoefficientValueByType(eType);
                double consume = (coef != null && coef != 0) ? typeEmission / coef : 0;
                consumeMap.put(eType, consume);
            }
        }

        // 将固碳数据也加入到 emissionMap/consumeMap 中
        // 约定：TREE 类型的 emission 记为负数（表示减少碳），consume 记为正数（表示生物量增长或固碳量）
        if (totalSequestration > 0) {
            emissionMap.put("TREE", -totalSequestration);
            consumeMap.put("TREE", totalSequestration);
        }

        // 构造结果及预警检查
        WarningResult warningResult = checkWarning(areaType, emissionMap, totalSequestration);

        return new SimulatedResult(totalEmission, totalSequestration, consumeMap, emissionMap, warningResult);
    }

    // ================== 3. 辅助逻辑 ==================

    // 检查预警
    private WarningResult checkWarning(String areaType, Map<String, Double> emissionMap, double sequestration) {
        WarningResult result = new WarningResult();
        Map<String, Double> thresholds = warningThresholdMap.get(areaType);
        if (thresholds == null) return result;

        // 绿化区：只关心固碳是否过低
        if ("绿化".equals(areaType)) {
            Double minTree = thresholds.get("TREE");
            if (minTree != null && sequestration < minTree) {
                result.setTriggered(true);
                result.setWarningType("固碳不足");
                result.setCarbonValue(sequestration);
                result.setThreshold(minTree);
            }
            return result;
        }

        // 功能区：只关心排放是否过高 (忽略该区域的微量固碳波动)
        for (Map.Entry<String, Double> entry : emissionMap.entrySet()) {
            String type = entry.getKey();
            // 排除固碳类型(TREE)的检查，因为功能区的固碳不设预警线
            if ("TREE".equals(type)) continue;

            double val = entry.getValue();
            if (thresholds.containsKey(type) && val > thresholds.get(type)) {
                result.setTriggered(true);
                result.setWarningType(type);
                result.setCarbonValue(val);
                result.setThreshold(thresholds.get(type));
                return result;
            }
        }
        return result;
    }

    // 辅助方法：获取主导能源
    private String getDominantType(String areaType) {
        if ("食堂".equals(areaType)) return "GAS";
        if ("交通".equals(areaType)) return "FUEL";
        return "ELECTRICITY";
    }

    // 时间系数
    private double getTimePeriodFactor(LocalDateTime now) {
        int hour = now.getHour();
        if (hour < 6) return 0.15;
        if (hour < 8) return 0.8;
        if (hour < 18) return 1.0;
        if (hour < 23) return 1.2;
        return 0.3;
    }

    // 周末系数
    private double getWeekdayFactor(LocalDateTime now, String areaType) {
        boolean isWeekend = now.getDayOfWeek() == DayOfWeek.SATURDAY
                || now.getDayOfWeek() == DayOfWeek.SUNDAY;
        if (!isWeekend) return 1.0;
        return switch (areaType) {
            case "教学" -> 0.2;
            case "食堂" -> 0.6;
            case "宿舍" -> 1.3;
            case "交通" -> 0.5;
            default -> 1.0;
        };
    }

    // 季节系数
    private double getSeasonFactor(LocalDateTime now, String areaType) {
        int month = now.getMonthValue();
        boolean isSummer = month >= 6 && month <= 9;
        boolean isWinter = month >= 11 || month <= 2;

        // 季节对固碳的影响 (夏季高，冬季低)
        double seqFactor = 1.0;
        if (isSummer) seqFactor = 1.5;
        if (isWinter) seqFactor = 0.5;

        // 如果是计算排放的季节系数
        if (areaType.equals("绿化")) return seqFactor;

        // 功能区的季节系数（同时影响排放和自带的微量固碳，简单起见统一处理）
        if (isSummer) {
            if ("教学".equals(areaType)) return 1.3;
            if ("宿舍".equals(areaType)) return 1.4;
        }
        if (isWinter) {
            if ("食堂".equals(areaType)) return 1.2;
            if ("教学".equals(areaType)) return 1.1;
        }
        return 1.0;
    }

    // 内部类定义
    public static class SimulatedResult {
        private double totalCarbon;
        private double sequestration;
        private final Map<String, Double> energyConsumeMap;
        private final Map<String, Double> emissionMap;
        private final WarningResult warningResult;

        public SimulatedResult(double totalCarbon, double sequestration,
                               Map<String, Double> energyConsumeMap,
                               Map<String, Double> emissionMap,
                               WarningResult warningResult) {
            this.totalCarbon = totalCarbon;
            this.sequestration = sequestration;
            this.energyConsumeMap = energyConsumeMap;
            this.emissionMap = emissionMap;
            this.warningResult = warningResult;
        }

        public double getTotalCarbon() { return totalCarbon; }
        public double getSequestration() { return sequestration; }
        public Map<String, Double> getEnergyConsumeMap() { return energyConsumeMap; }
        public Map<String, Double> getEmissionMap() { return emissionMap; }
        public WarningResult getWarningResult() { return warningResult; }
    }

    public static class WarningResult {
        private boolean triggered = false;
        private String warningType;
        private double carbonValue;
        private double threshold;

        public boolean isTriggered() { return triggered; }
        public void setTriggered(boolean triggered) { this.triggered = triggered; }
        public String getWarningType() { return warningType; }
        public void setWarningType(String warningType) { this.warningType = warningType; }
        public double getCarbonValue() { return carbonValue; }
        public void setCarbonValue(double carbonValue) { this.carbonValue = carbonValue; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
    }
}

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import ynu.edu.service.CarbonCoefficientService;
//
//import java.time.LocalDateTime;
//import java.time.DayOfWeek;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;
//
//@Component
//public class CarbonDataSimulator {
//    private final Random random = new Random();
//
//    @Autowired
//    private CarbonCoefficientService coefficientService;
//
//    // 1. 区域类型-基准排放映射（单位：kgCO₂/次采集）
//    private final Map<String, Double> areaBaseCarbonMap = new HashMap<String, Double>() {{
//        put("教学", 80.0);    // 基准值
//        put("宿舍", 60.0);      // 基准值
//        put("食堂", 150.0);     // 基准值（燃气消耗高）
//        put("绿化", 5.0);     // 基准值（排放低，固碳高）
//        put("交通", 20.0);    // 基准值（交通排放）
//    }};
//    // 2. 区域类型-固碳系数映射（仅绿化区高）
//    private final Map<String, Double> areaSequestrationMap = new HashMap<String, Double>() {{
//        put("教学", 10.0);
//        put("宿舍", 10.0);
//        put("食堂", 0.0);
//        put("绿化", 50.0);    // 绿化区固碳基准值
//        put("交通", 10.0);
//    }};
//    // 3. 时段系数（不同时段排放波动）
//    private final Map<String, Double> timePeriodFactorMap = new HashMap<String, Double>() {{
//        put("凌晨", 0.2);   // 0-6点
//        put("早间", 1.0);   // 6-12点
//        put("午间", 1.0);   // 12-18点
//        put("晚间", 1.5);   // 18-24点
//    }};
//    // 4. 能源类型占比（按区域类型差异化）
//    private final Map<String, Map<String, Double>> energyTypeRatioMap = new HashMap<String, Map<String, Double>>() {{
//        // 教学楼：以电力为主，少量燃气（如热水/供暖）/树木，无燃油相关
//        put("教学", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 0.9);
//            put("GAS", 0.05);
//            put("FUEL", 0.0);
//            put("TREE", 0.05);
//        }});
//        // 宿舍：电力为主，少量燃气（热水）/树木，无燃油相关
//        put("宿舍", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 0.8);
//            put("GAS", 0.1);
//            put("FUEL", 0.0);
//            put("TREE", 0.1);
//        }});
//        // 食堂：燃气为主，电力为辅，无燃油/树木相关
//        put("食堂", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 0.3);
//            put("GAS", 0.7);
//            put("FUEL", 0.0);
//            put("TREE", 0.0);
//        }});
//        // 交通：电力为主（电动车行驶），少量燃油/树木，无燃气相关
//        put("交通", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 0.6);
//            put("GAS", 0.0);
//            put("FUEL", 0.3);
//            put("TREE", 0.1);
//        }});
//        // 绿化：无能源消耗，仅树木固碳（TREE占比100%，用于后续固碳计算关联）
//        put("绿化", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 0.0);
//            put("GAS", 0.0);
//            put("FUEL", 0.0);
//            put("TREE", 1.0);
//        }});
//    }};
//
//    // 预警阈值配置（区域-能源类型-阈值）
//    private final Map<String, Map<String, Double>> warningThresholdMap = new HashMap<String, Map<String, Double>>() {{
//        put("教学", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 100.0); // 教学楼电力排放超100kg触发预警
//        }});
//        put("宿舍", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 80.0);
//        }});
//        put("食堂", new HashMap<String, Double>() {{
//            put("GAS", 120.0);         // 食堂燃气排放超120kg触发预警
//        }});
//        put("交通", new HashMap<String, Double>() {{
//            put("ELECTRICITY", 35.0);
//            put("FUEL", 25.0);         // 主干道燃油排放超25kg触发预警
//        }});
//        put("绿化", new HashMap<String, Double>() {{
//            put("TREE", 30.0);         // 绿化区固碳量低于30kg触发预警（反向预警）
//        }});
//    }};
//
//    /**
//     * 重构：生成场景化模拟数据
//     * @param areaId 区域ID
//     * @param areaType 区域类型（新增参数）
//     * @return 模拟结果（总排放、固碳量、能源明细）
//     */
//    public SimulatedResult generateSimulatedData(Long areaId, String areaType) {
//        LocalDateTime now = LocalDateTime.now();
//        // 基础参数计算
//        double baseCarbon = areaBaseCarbonMap.getOrDefault(areaType, 50.0);
//       // double baseSequestration = areaSequestrationMap.getOrDefault(areaType, 0.0);
//
//        // 时间维度系数（时段+工作日/周末+季节）
//        double timeFactor = getTimePeriodFactor(now);
//        double weekdayFactor = getWeekdayFactor(now, areaType);
//        double seasonFactor = getSeasonFactor(now, areaType);
//        double totalFactor = timeFactor * weekdayFactor * seasonFactor;
//
//        // 5%概率触发异常排放（超阈值）
//        double abnormalFactor = 1.0;
//        boolean isAbnormal = random.nextDouble() < 0.05; // 5%概率异常
//        if (isAbnormal) {
//            abnormalFactor = 1.5 + random.nextDouble() * 0.5; // 异常排放为基准的1.5-2倍
//        }
//
//        // 最终排放/固碳计算（加入小幅随机波动，避免完全固定）
//        double totalConsume = baseCarbon * totalFactor * abnormalFactor * (1 + random.nextDouble() * 0.1 - 0.05); // ±5%波动
//      //  double sequestration = baseSequestration * seasonFactor * (1 + random.nextDouble() * 0.1 - 0.05);
//
//        // 能源类型明细计算（按区域类型占比拆分）
//        Map<String, Double> energyConsumeMap = calculateEnergyConsume(totalConsume, areaType);
//
//        // 根据系数计算碳排放量
//        Map<String, Double> emissionMap = calculateEmissions(energyConsumeMap);
//
//        // 计算总排放和固碳量
//        double totalCarbon = 0.0;
//        double sequestration = 0.0;
//        for (Map.Entry<String, Double> entry : emissionMap.entrySet()) {
//            String type = entry.getKey();
//            double emission = entry.getValue();
//            if (isSequestrationType(type)) {
//                sequestration += Math.abs(emission);
//            } else {
//                totalCarbon += emission;
//            }
//        }
//        // 判断是否触发预警
//        WarningResult warningResult = checkWarning(areaType, emissionMap, sequestration);
//
//        // 数据合理性约束
//        totalConsume = Math.max(totalConsume, 0.1); // 避免0值
//        sequestration = Math.max(sequestration, 0.0);
//
//        return new SimulatedResult(totalCarbon, sequestration, energyConsumeMap, emissionMap, warningResult);
//    }
//
//    // 检查是否触发预警
//    private WarningResult checkWarning(String areaType, Map<String, Double> emissionMap, double sequestration) {
//        WarningResult warningResult = new WarningResult();
//        Map<String, Double> thresholdMap = warningThresholdMap.getOrDefault(areaType, new HashMap<>());
//
//        // 检查各非绿化能源类型预警
//        for (Map.Entry<String, Double> entry : emissionMap.entrySet()) {
//            String energyType = entry.getKey();
//            double emissionValue = entry.getValue();
//            Double threshold = thresholdMap.get(energyType);
//
//            if (threshold != null && emissionValue > threshold) {
//                warningResult.setTriggered(true);
//                warningResult.setWarningType(energyType);
//                warningResult.setCarbonValue(emissionValue);
//                warningResult.setThreshold(threshold);
//                break;
//            }
//        }
//
//        return warningResult;
//    }
//
//    /**
//     * 根据消耗量和系数计算排放量
//     */
//    private Map<String, Double> calculateEmissions(Map<String, Double> consumeMap) {
//        Map<String, Double> emissionMap = new HashMap<>();
//        for (Map.Entry<String, Double> entry : consumeMap.entrySet()) {
//            String type = entry.getKey();
//            Double consume = entry.getValue();
//            Double coefficient = coefficientService.getCoefficientValueByType(type);
//
//            if (coefficient == null) {
//                throw new RuntimeException("未配置类型[" + type + "]的排放系数");
//            }
//
//            double emission = consume * coefficient;
//            // 固碳类型存储为负值
//            if (isSequestrationType(type)) {
//                emission = -emission;
//            }
//            emissionMap.put(type, emission);
//        }
//        return emissionMap;
//    }
//
//    /**
//     * 判断是否为固碳类型
//     */
//    private boolean isSequestrationType(String type) {
//        return "TREE".equals(type) || "SHRUB".equals(type) || "LAWN".equals(type);
//    }
//
//    // 辅助：获取时段系数（凌晨/早间/午间/晚间）
//    private double getTimePeriodFactor(LocalDateTime now) {
//        int hour = now.getHour();
//        if (hour >= 0 && hour < 6) return timePeriodFactorMap.get("凌晨");
//        if (hour >= 6 && hour < 12) return timePeriodFactorMap.get("早间");
//        if (hour >= 12 && hour < 18) return timePeriodFactorMap.get("午间");
//        return timePeriodFactorMap.get("晚间");
//    }
//
//    // 辅助：获取工作日/周末系数（周末教学楼/食堂排放降低）
//    private double getWeekdayFactor(LocalDateTime now, String areaType) {
//        DayOfWeek dayOfWeek = now.getDayOfWeek();
//        boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
//
//        // 不同区域周末系数不同
//        Map<String, Double> weekdayFactorMap = new HashMap<String, Double>() {{
//            put("教学", isWeekend ? 0.3 : 1.0); // 周末教学楼排放降70%
//            put("宿舍", isWeekend ? 1.2 : 1.0);   // 周末宿舍排放升20%
//            put("食堂", isWeekend ? 0.7 : 1.0);   // 周末食堂排放降30%
//            put("绿化", 1.0);                   // 绿化区不受周末影响
//            put("交通", isWeekend ? 0.8 : 1.0); // 周末主干道交通降20%
//        }};
//        return weekdayFactorMap.getOrDefault(areaType, 1.0);
//    }
//
//    // 辅助：获取季节系数（冬季燃气高，夏季电力高，绿化夏季固碳高）
//    private double getSeasonFactor(LocalDateTime now, String areaType) {
//        int month = now.getMonthValue();
//        // 冬季（11-2月）、夏季（6-9月）、春秋季
//        String season;
//        if (month >= 11 || month <= 2) season = "winter";
//        else if (month >= 6 && month <= 9) season = "summer";
//        else season = "spring_autumn";
//
//        Map<String, Map<String, Double>> seasonFactorMap = new HashMap<String, Map<String, Double>>() {{
//            put("winter", new HashMap<String, Double>() {{
//                put("教学", 1.1); // 冬季供暖，排放升10%
//                put("宿舍", 1.1);
//                put("食堂", 1.2);   // 冬季燃气消耗升20%
//                put("绿化", 0.5); // 冬季绿化固碳降50%
//                put("交通", 1.0);
//            }});
//            put("summer", new HashMap<String, Double>() {{
//                put("教学", 1.1); // 夏季空调，排放升10%
//                put("宿舍", 1.1);
//                put("食堂", 1.0);
//                put("绿化", 1.5); // 夏季绿化固碳升50%
//                put("交通", 1.0);
//            }});
//            put("spring_autumn", new HashMap<String, Double>() {{
//                put("教学", 1.0);
//                put("宿舍", 1.0);
//                put("食堂", 1.0);
//                put("绿化", 1.0);
//                put("交通", 1.0);
//            }});
//        }};
//        return seasonFactorMap.get(season).getOrDefault(areaType, 1.0);
//    }
//
//    // 辅助：按区域类型计算能源消耗明细
//    private Map<String, Double> calculateEnergyConsume(double totalCarbon, String areaType) {
//        Map<String, Double> ratioMap = energyTypeRatioMap.getOrDefault(areaType,
//                new HashMap<String, Double>() {{
//                    put("ELECTRICITY", 0.7);
//                    put("GAS", 0.2);
//                    put("TRANSPORT", 0.1);
//                    put("TREE", 0.0);
//                }});
//        Map<String, Double> consumeMap = new HashMap<>();
//        if ("绿化".equals(areaType)) {
//            consumeMap.put("TREE", totalCarbon);
//        } else {
//            consumeMap.put("ELECTRICITY", totalCarbon * ratioMap.get("ELECTRICITY"));
//            consumeMap.put("GAS", totalCarbon * ratioMap.get("GAS"));
//            consumeMap.put("FUEL", totalCarbon * ratioMap.get("FUEL"));
//            consumeMap.put("TREE", totalCarbon * ratioMap.get("TREE"));
//        }
//        return consumeMap;
//    }
//
//    // 模拟结果封装类
//    public static class SimulatedResult {
//        private double totalCarbon;
//        private double sequestration;
//        private final Map<String, Double> energyConsumeMap;  // 消耗量
//        private final Map<String, Double> emissionMap;       // 排放量
//        private final WarningResult warningResult;
//
//        public SimulatedResult(double totalCarbon, double sequestration,
//                               Map<String, Double> energyConsumeMap,
//                               Map<String, Double> emissionMap,
//                               WarningResult warningResult) {
//            this.totalCarbon = totalCarbon;
//            this.sequestration = sequestration;
//            this.energyConsumeMap = energyConsumeMap;
//            this.emissionMap = emissionMap;
//            this.warningResult = warningResult;
//        }
//
//        // getter方法
//        public double getTotalCarbon() { return totalCarbon; }
//        public double getSequestration() { return sequestration; }
//        public Map<String, Double> getEnergyConsumeMap() { return energyConsumeMap; }
//        public Map<String, Double> getEmissionMap() { return emissionMap; }
//        public WarningResult getWarningResult() { return warningResult; }
//    }
//
//    public static class WarningResult {
//        private boolean triggered = false;       // 是否触发预警
//        private String warningType;               // 预警类型（电力/燃气/总排放/固碳不足）
//        private double carbonValue;             // 超标碳排放量/固碳量
//        private double threshold;           // 阈值
//
//        // getter/setter
//        public boolean isTriggered() { return triggered; }
//        public void setTriggered(boolean triggered) { this.triggered = triggered; }
//        public String getWarningType() { return warningType; }
//        public void setWarningType(String warningType) { this.warningType = warningType; }
//        public double getCarbonValue() { return carbonValue; }
//        public void setCarbonValue(double carbonValue) { this.carbonValue = carbonValue; }
//        public double getThreshold() { return threshold; }
//        public void setThreshold(double threshold) { this.threshold = threshold; }
//
//    }
//}