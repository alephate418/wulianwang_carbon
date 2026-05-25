package ynu.edu.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ynu.edu.entity.CampusArea;
import ynu.edu.entity.CarbonData;
import ynu.edu.entity.CarbonEmissionConsume;
import ynu.edu.entity.CarbonWarning;
import ynu.edu.mapper.CarbonEmissionConsumeMapper;
import ynu.edu.mapper.CarbonMapper;
import ynu.edu.mapper.WarningMapper;
import ynu.edu.service.CampusAreaService;
import ynu.edu.util.CarbonDataSimulator;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class CarbonDataTask {

    @Autowired
    private CarbonDataSimulator simulator;
    @Autowired
    private WarningMapper warningMapper;
    @Autowired
    private CampusAreaService campusAreaService;
    @Autowired
    private CarbonEmissionConsumeMapper consumeMapper;
    @Autowired
    private CarbonMapper carbonMapper;

    // 每10秒执行一次
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void generateAndSaveRealTimeData() {
        // 降低日志级别，避免刷屏，只在生成数据时记录关键信息
        log.debug("开始执行模拟数据生成任务...");

        try {
            List<CampusArea> areas = campusAreaService.getEnabledAreaList();
            if (areas.isEmpty()) {
                log.warn("未查询到有效区域，跳过数据生成");
                return;
            }

            int warningCount = 0;

            for (CampusArea area : areas) {
                // 1. 调用模拟器生成数据
                CarbonDataSimulator.SimulatedResult result = simulator.generateSimulatedData(area.getId(), area.getAreaType());

                // 2. 保存主表 CarbonData
                CarbonData carbonData = new CarbonData();
                carbonData.setAreaId(area.getId());
                carbonData.setAreaName(area.getAreaName());
                // 保留两位小数，使数据库更整洁
                carbonData.setTotalCarbon(formatDouble(result.getTotalCarbon()));
                carbonData.setSequestration(formatDouble(result.getSequestration()));
                carbonData.setNetCarbon(formatDouble(result.getTotalCarbon() - result.getSequestration()));
                carbonData.setCollectTime(LocalDateTime.now());
                carbonData.setCreateTime(LocalDateTime.now());

                carbonMapper.insert(carbonData);

                // 3. 保存明细表 CarbonEmissionConsume
                List<CarbonEmissionConsume> consumeList = result.getEnergyConsumeMap().entrySet().stream()
                        .filter(entry -> Math.abs(entry.getValue()) > 0.001) // 过滤极小值
                        .map(entry -> {
                            CarbonEmissionConsume consume = new CarbonEmissionConsume();
                            consume.setCdId(carbonData.getId());
                            consume.setCoefficientType(entry.getKey());
                            consume.setConsumeAmount(formatDouble(entry.getValue()));
                            consume.setCarbonEmission(formatDouble(result.getEmissionMap().get(entry.getKey())));
                            consume.setCreateTime(LocalDateTime.now());
                            return consume;
                        }).toList();

                if (!consumeList.isEmpty()) {
                    consumeMapper.batchInsert(consumeList);
                }

                // 4. 处理预警
                CarbonDataSimulator.WarningResult warningResult = result.getWarningResult();
                if (warningResult.isTriggered()) {
                    warningCount++;
                    CarbonWarning warning = new CarbonWarning();
                    warning.setAreaId(area.getId());
                    warning.setWarningTime(LocalDateTime.now());
                    warning.setCarbonValue(formatDouble(warningResult.getCarbonValue()));
                    warning.setThreshold(formatDouble(warningResult.getThreshold()));
                    warning.setWarningType(warningResult.getWarningType()); // 例如 "ELECTRICITY" 或 "固碳不足"
                    warning.setHandleStatus(0); // 未处理

                    warningMapper.insert(warning);

                    log.info("【预警触发】区域:{} | 类型:{} | 当前值:{} > 阈值:{}",
                            area.getAreaName(),
                            warningResult.getWarningType(),
                            formatDouble(warningResult.getCarbonValue()),
                            formatDouble(warningResult.getThreshold()));
                }
            }
            log.info("本次任务完成，处理区域数：{}，生成预警数：{}", areas.size(), warningCount);

        } catch (Exception e) {
            log.error("模拟数据生成任务异常", e);
            // 事务回滚由 @Transactional 处理
            throw e;
        }
    }

    // 辅助方法：保留两位小数
    private Double formatDouble(Double val) {
        if (val == null) return 0.0;
        return Math.round(val * 100.0) / 100.0;
    }
}
//package ynu.edu.task;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import ynu.edu.entity.CampusArea;
//import ynu.edu.entity.CarbonData;
//import ynu.edu.entity.CarbonEmissionConsume;
//import ynu.edu.entity.CarbonWarning;
//import ynu.edu.mapper.CarbonEmissionConsumeMapper;
//import ynu.edu.mapper.CarbonMapper;
//import ynu.edu.mapper.WarningMapper;
//import ynu.edu.service.CampusAreaService;
//import ynu.edu.service.CarbonService;
//import ynu.edu.util.CarbonDataSimulator;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//// 定时任务组件
//@Component
//@Slf4j
//public class CarbonDataTask {
//
//    @Autowired
//    private CarbonDataSimulator simulator;
//    @Autowired
//    private WarningMapper warningMapper;
//    @Autowired
//    private CampusAreaService campusAreaService;
//    @Autowired
//    private CarbonEmissionConsumeMapper consumeMapper;
//    @Autowired
//    private CarbonMapper carbonMapper;
//
//    // 每隔10秒执行一次（fixedRate：以上一次任务开始时间计算间隔）
//    @Scheduled(fixedRate = 10000)
//    @Transactional
//    public void generateAndSaveRealTimeData() {
//        log.info("开始生成模拟实时数据...");
//
//        try {
//            List<CampusArea> areas = campusAreaService.getEnabledAreaList();
//            log.info("获取到有效区域数量：{}", areas.size());
//
//            for (CampusArea area : areas) {
//                CarbonDataSimulator.SimulatedResult result = simulator.generateSimulatedData(area.getId(), area.getAreaType());
//
//                // 1. 保存碳排放主数据
//                CarbonData carbonData = new CarbonData();
//                carbonData.setAreaId(area.getId());
//                carbonData.setAreaName(area.getAreaName());
//                carbonData.setTotalCarbon(result.getTotalCarbon());
//                carbonData.setSequestration(result.getSequestration());
//                carbonData.setNetCarbon(result.getTotalCarbon() - result.getSequestration());
//                carbonData.setCollectTime(LocalDateTime.now());
//                carbonData.setCreateTime(LocalDateTime.now());
//                carbonMapper.insert(carbonData);
//
//                // 2. 保存能源消耗明细
//                List<CarbonEmissionConsume> consumeList = result.getEnergyConsumeMap().entrySet().stream()
//                        .filter(entry -> entry.getValue() > 0) // 过滤0值
//                        .map(entry -> {
//                            CarbonEmissionConsume consume = new CarbonEmissionConsume();
//                            consume.setCdId(carbonData.getId());
//                            consume.setCoefficientType(entry.getKey());
//                            consume.setConsumeAmount(entry.getValue());
//                            consume.setCarbonEmission(result.getEmissionMap().get(entry.getKey()));
//                            consume.setCreateTime(LocalDateTime.now());
//                            return consume;
//                        }).toList();
//                if (!consumeList.isEmpty()) {
//                    consumeMapper.batchInsert(consumeList);
//                }
//
//                // 3. 保存预警数据（适配新CarbonWarning实体）
//                CarbonDataSimulator.WarningResult warningResult = result.getWarningResult();
//                if (warningResult.isTriggered()) {
//                    CarbonWarning warning = new CarbonWarning();
//                    warning.setAreaId(area.getId());                // 区域ID
//                    warning.setWarningTime(LocalDateTime.now());    // 预警时间
//                    warning.setCarbonValue(warningResult.getCarbonValue()); // 超标值
//                    warning.setThreshold(warningResult.getThreshold());     // 阈值
//                    warning.setWarningType(warningResult.getWarningType()); // 预警类型（中文）
//                    warning.setHandleStatus(0);                     // 处理状态：0-未处理
//                    // handleTime/handleRemark 初始为null（未处理）
//
//                    warningMapper.insert(warning);
//                    log.warn("区域[{}]触发预警：{}，超标值{}kgCO₂，阈值{}kgCO₂",
//                            area.getAreaName(),
//                            warningResult.getWarningType(),
//                            warningResult.getCarbonValue(),
//                            warningResult.getThreshold());
//                }
//
//                log.info("区域[{}]模拟数据生成完成，总排放：{}kgCO₂，固碳：{}kgCO₂",
//                        area.getAreaName(), result.getTotalCarbon(), result.getSequestration());
//            }
//        } catch (Exception e) {
//            log.error("定时任务执行失败", e);
//        }
////        // 获取所有有效区域
////        List<CampusArea> areas = campusAreaService.getEnabledAreaList();
////        for (CampusArea area : areas) {
////            // 1. 生成模拟数据
////            CarbonData simulatedData = simulator.generateSimulatedData(area.getId());
////
////            // 2. 保存主表数据
////            carbonMapper.insert(simulatedData); // 复用已有插入方法
////            Long cdId = simulatedData.getId();
////
////            // 3. 保存明细数据（关联主表ID）
////            List<CarbonEmissionConsume> consumes = simulatedData.getEmissionConsumes();
////            consumes.forEach(consume -> consume.setCdId(cdId));
////            consumeMapper.batchInsert(consumes); // 复用批量插入方法
////
////            log.info("区域[{}]模拟数据生成完成", area.getAreaName());
////        }
//    }
//}