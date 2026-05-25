package ynu.edu.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ynu.edu.entity.CampusArea;
import ynu.edu.entity.CarbonData;
import ynu.edu.entity.CarbonEmissionConsume;
import ynu.edu.entity.vo.CarbonStatVO;
import ynu.edu.mapper.CarbonEmissionConsumeMapper;
import ynu.edu.mapper.CarbonMapper;
import ynu.edu.service.CampusAreaService;
import ynu.edu.service.CarbonCoefficientService;
import ynu.edu.service.CarbonService;
import ynu.edu.util.CarbonCalculateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CarbonServiceImpl implements CarbonService {

    @Autowired
    private CampusAreaService campusAreaService;

    @Autowired
    private CarbonMapper carbonMapper;

    @Autowired
    private CarbonEmissionConsumeMapper emissionConsumeMapper;

    @Autowired
    private CarbonCoefficientService coefficientService;


    /**
     * 获取指定区域的实时数据
     * @param areaId
     * @return
     */
    @Override
    public CarbonData getAreaRealTimeData(Long areaId) {
        // 测试该区域是否存在
        CampusArea area = campusAreaService.getById(areaId);
        if (area == null) return null;

        // 查询最新数据
        CarbonData latestData = carbonMapper.selectLatestByAreaId(areaId);
        if (latestData == null) {
            return buildEmptyData(area);
        }

        // 查询关联的排放明细
        List<CarbonEmissionConsume> consumes = emissionConsumeMapper.selectByCdId(latestData.getId());
        latestData.setEmissionConsumes(consumes);
        latestData.setAreaName(area.getAreaName());
        latestData.setAreaType(area.getAreaType());

        return latestData;
    }

    private CarbonData buildEmptyData(CampusArea area) {
        CarbonData vo = new CarbonData();
        vo.setAreaId(area.getId());
        vo.setAreaName(area.getAreaName());
        vo.setAreaType(area.getAreaType());
        vo.setTotalCarbon(0.0);
        vo.setSequestration(0.0);
        vo.setNetCarbon(0.0);
        vo.setCollectTime(LocalDateTime.now());
        vo.setEmissionConsumes(new ArrayList<>());
        return vo;
    }

    /**
     * 获取所有区域的实时数据
     * @return
     */
    @Override
    public List<CarbonData> getRealTimeCarbonData() {
        List<CarbonData> result=new ArrayList<>();
        List<CampusArea> areaList=campusAreaService.getEnabledAreaList();

        for (CampusArea area : areaList) {
            CarbonData vo = getAreaRealTimeData(area.getId());
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public CarbonStatVO getCarbonStatistic(LocalDate startDate, LocalDate endDate) {
        CarbonStatVO statVO = new CarbonStatVO();
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(23, 59, 59);

        // 1. 时间趋势统计（按日聚合）
        List<String> timeLabels = new ArrayList<>();
        List<Double> totalCarbonList = new ArrayList<>();
        List<Double> netCarbonList = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            timeLabels.add(current.format(DateTimeFormatter.ofPattern("MM-dd")));
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);

            // 当日总排放
            Double dailyTotal = carbonMapper.selectTotalCarbonByTime(dayStart, dayEnd);
            totalCarbonList.add(dailyTotal == null ? 0.0 : dailyTotal);

            // 当日净排放
            List<CarbonData> dayData = carbonMapper.selectByAreaAndTime(null, dayStart, dayEnd);
            Double dailyNet = dayData.stream().mapToDouble(CarbonData::getNetCarbon).sum();
            netCarbonList.add(dailyNet);

            current = current.plusDays(1);
        }
        statVO.setTimeLabels(timeLabels);
        statVO.setTotalCarbonList(totalCarbonList);
        statVO.setNetCarbonList(netCarbonList);

        // 2. 排放源占比统计（从明细表聚合）
        List<CarbonEmissionConsume> allConsumes = emissionConsumeMapper.selectByAreaAndTime(null, startTime, endTime);



        // 按排放类型分组求和
        Map<String, Double> typeTotalMap = allConsumes.stream()
                .collect(Collectors.groupingBy(
                        CarbonEmissionConsume::getCoefficientType,
                        Collectors.summingDouble(CarbonEmissionConsume::getCarbonEmission)
                ));

        // 转换为前端需要的格式（类型名称映射）
        Map<String, String> typeNameMap = new HashMap<>();
        typeNameMap.put("ELECTRICITY", "电力");
        typeNameMap.put("GAS", "燃气");
        typeNameMap.put("FUEL", "交通");
        typeNameMap.put("TREE", "树木固碳");

        List<String> sourceLabels = new ArrayList<>();
        List<Double> sourceValues = new ArrayList<>();
        double total = typeTotalMap.values().stream().mapToDouble(Double::doubleValue).sum();

        if (total > 0) {
            typeTotalMap.forEach((type, value) -> {
                sourceLabels.add(typeNameMap.getOrDefault(type, type));
                sourceValues.add((value / total) * 100);
            });
        }
        statVO.setSourceLabels(sourceLabels);
        statVO.setSourceValues(sourceValues);
        // 3. 区域排名统计
        List<CampusArea> areaList = campusAreaService.getEnabledAreaList();
        Map<Long, Double> areaCarbonMap = new HashMap<>();

        for (CampusArea area : areaList) {
            List<CarbonData> areaData = carbonMapper.selectByAreaAndTime(area.getId(), startTime, endTime);
            Double areaTotal = areaData.stream().mapToDouble(CarbonData::getTotalCarbon).sum();
            areaCarbonMap.put(area.getId(), areaTotal);
        }
        // 按排放量降序排序
        List<Map.Entry<Long, Double>> sortedAreas = areaCarbonMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toList());

        List<String> areaLabels = new ArrayList<>();
        List<Double> areaValues = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : sortedAreas) {
            CampusArea area = campusAreaService.getById(entry.getKey());
            areaLabels.add(area.getAreaName());
            areaValues.add(entry.getValue());
        }

        statVO.setAreaLabels(areaLabels);
        statVO.setAreaValues(areaValues);
        return statVO;
    }

    /**
     * 计算并保存碳排放数据（核心实现）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarbonData calculateAndSaveCarbonData(Long areaId, Map<String, Double> consumeData) {
        // 1. 验证区域存在
        CampusArea area = campusAreaService.getById(areaId);
        if (area == null) {
            throw new IllegalArgumentException("区域不存在");
        }

        // 2. 初始化结果对象
        CarbonData carbonData = new CarbonData();
        carbonData.setAreaId(areaId);
        carbonData.setCollectTime(LocalDateTime.now());
        carbonData.setCreateTime(LocalDateTime.now());

        // 3. 计算各类型排放数据
        List<CarbonEmissionConsume> emissionItems = new ArrayList<>();
        double totalCarbon = 0.0;
        double totalSequestration = 0.0;

        for (Map.Entry<String, Double> entry : consumeData.entrySet()) {
            String type = entry.getKey();
            Double consumeAmount = entry.getValue();
            if (consumeAmount == null || consumeAmount <= 0) {
                continue; // 跳过无效数据
            }

            // 3.1 获取对应排放系数
            Double coefficient = coefficientService.getCoefficientValueByType(type);
            if (coefficient == null) {
                throw new IllegalArgumentException("未配置类型[" + type + "]的排放系数");
            }

            // 3.2 计算碳排放量
            Double emissionAmount = CarbonCalculateUtil.calculateCarbon(consumeAmount, coefficient);

            // 3.3 区分固碳和排放（固碳量为负值）
            if (isSequestrationType(type)) {
                totalSequestration += Math.abs(emissionAmount);
                emissionAmount = -emissionAmount; // 固碳量存储为负值
            } else {
                totalCarbon += emissionAmount;
            }

            // 3.4 构建排放明细项
            CarbonEmissionConsume item = new CarbonEmissionConsume();
            item.setCoefficientType(type);
            item.setConsumeAmount(consumeAmount);
            item.setCarbonEmission(emissionAmount);
            item.setCreateTime(LocalDateTime.now());
            emissionItems.add(item);
        }

        // 4. 计算汇总数据
        carbonData.setTotalCarbon(totalCarbon);
        carbonData.setSequestration(totalSequestration);
        carbonData.setNetCarbon(totalCarbon - totalSequestration);

        // 5. 保存主表数据
        carbonMapper.insert(carbonData);
        log.info("carbonData的id为：{}",carbonData.getId());

        // 6. 保存明细数据（关联主表ID）
        for (CarbonEmissionConsume item : emissionItems) {
            item.setCdId(carbonData.getId());
        }
        if (!emissionItems.isEmpty()) {
            emissionConsumeMapper.batchInsert(emissionItems);
        }

        // 7. 补充返回数据
        carbonData.setAreaName(area.getAreaName());
        carbonData.setAreaType(area.getAreaType());
        carbonData.setEmissionConsumes(emissionItems);

        return carbonData;
    }

    /**
     * 判断是否为固碳类型（可根据实际需求调整）
     */
    private boolean isSequestrationType(String type) {
        return type.equals("TREE") || type.equals("SHRUB") || type.equals("LAWN");
    }
}
