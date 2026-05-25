package ynu.edu.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import ynu.edu.entity.CarbonData;
import ynu.edu.entity.CarbonEmissionConsume;
import ynu.edu.mapper.CarbonMapper;
import ynu.edu.mapper.CarbonEmissionConsumeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 碳排放数据导入工具
 */
@Component
@Slf4j
public class CarbonDataImportUtil {

    @Autowired
    private CarbonMapper carbonMapper;

    @Autowired
    private CarbonEmissionConsumeMapper carbonEmissionConsumeMapper;

    // 日期时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导入碳排放数据
     * @param filePath CSV文件路径
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importCarbonData(String filePath) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] nextLine;
            // 跳过表头（如果有的话）
            // reader.readNext();

            // 按area_id分组存储数据
            Map<Long, List<ImportData>> areaDataMap = new HashMap<>();

            // 1. 解析CSV数据并按area_id分组
            while ((nextLine = reader.readNext()) != null) {
                try {
                    // 解析单行数据
                    ImportData data = parseImportData(nextLine);
                    // 按area_id分组
                    areaDataMap.computeIfAbsent(data.getAreaId(), k -> new ArrayList<>()).add(data);
                } catch (Exception e) {
                    failCount++;
                    errorMessages.add("解析数据失败：" + Arrays.toString(nextLine) + "，错误信息：" + e.getMessage());
                }
            }


            // 2. 处理每个area_id的数据
            for (Map.Entry<Long, List<ImportData>> entry : areaDataMap.entrySet()) {
                Long areaId = entry.getKey();
                List<ImportData> dataList = entry.getValue();

                try {
                    // 3. 计算carbon_data记录
                    CarbonData carbonData = buildCarbonData(areaId, dataList);
                    // 4. 插入carbon_data，获取自增ID
                    carbonMapper.insert(carbonData);
                    Long cdId = carbonData.getId(); // 这里需要确保mapper配置了useGeneratedKeys

                    // 5. 插入carbon_emission_consume记录
                    for (ImportData data : dataList) {
                        CarbonEmissionConsume consume = buildCarbonEmissionConsume(data, cdId);
                        carbonEmissionConsumeMapper.insert(consume);
                    }

                    successCount += dataList.size();
                } catch (Exception e) {
                    failCount += dataList.size();
                    errorMessages.add("处理area_id=" + areaId + "的数据失败，错误信息：" + e.getMessage());
                }
            }

        } catch (IOException | CsvValidationException e) {
            failCount++;
            errorMessages.add("读取CSV文件失败：" + e.getMessage());
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errorMessages", errorMessages);
        return result;
    }

    /**
     * 解析CSV行数据为ImportData对象
     */
    private ImportData parseImportData(String[] line) {
        ImportData data = new ImportData();
        data.setId(Long.parseLong(line[0]));
        data.setAreaId(Long.parseLong(line[1]));
        data.setAreaName(line[2]);
        data.setCoefficientType(line[3]);
        data.setConsumeAmount(Double.parseDouble(line[4]));
        data.setCarbonEmission(Double.parseDouble(line[5]));
        data.setConsumeTime(LocalDateTime.parse(line[6], DATE_TIME_FORMATTER));
        data.setCreateTime(LocalDateTime.parse(line[7], DATE_TIME_FORMATTER));
        data.setUpdateTime(LocalDateTime.parse(line[8], DATE_TIME_FORMATTER));
        return data;
    }

    /**
     * 构建CarbonData对象
     */
    private CarbonData buildCarbonData(Long areaId, List<ImportData> dataList) {
        CarbonData carbonData = new CarbonData();
        carbonData.setAreaId(areaId);

        // 计算totalCarbon（所有排放的总和，固碳为负，所以直接求和）
        double totalCarbon = dataList.stream()
                .mapToDouble(ImportData::getCarbonEmission)
                .sum();
        carbonData.setTotalCarbon(totalCarbon);

        // 计算sequestration（固碳量，取绝对值之和）
        double sequestration = dataList.stream()
                .filter(d -> d.getCarbonEmission() < 0)
                .mapToDouble(d -> Math.abs(d.getCarbonEmission()))
                .sum();
        carbonData.setSequestration(sequestration);

        // 计算netCarbon（净排放 = 总排放 - 固碳量）
        carbonData.setNetCarbon(totalCarbon - sequestration);

        // 最早的create_time
        LocalDateTime earliestCreateTime = dataList.stream()
                .map(ImportData::getCreateTime)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        carbonData.setCreateTime(earliestCreateTime);

        // 最晚的update_time
        LocalDateTime latestUpdateTime = dataList.stream()
                .map(ImportData::getUpdateTime)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        // 假设collect_time使用最晚的update_time
        carbonData.setCollectTime(latestUpdateTime);

        return carbonData;
    }

    /**
     * 构建CarbonEmissionConsume对象
     */
    private CarbonEmissionConsume buildCarbonEmissionConsume(ImportData data, Long cdId) {
        CarbonEmissionConsume consume = new CarbonEmissionConsume();
        consume.setCdId(cdId);
        consume.setCoefficientType(data.getCoefficientType());
        consume.setConsumeAmount(data.getConsumeAmount());
        consume.setCarbonEmission(data.getCarbonEmission());
        consume.setCreateTime(data.getCreateTime());
        return consume;
    }

    /**
     * 内部类：用于临时存储解析的CSV数据
     */
    private static class ImportData {
        private Long id;
        private Long areaId;
        private String areaName;
        private String coefficientType;
        private Double consumeAmount;
        private Double carbonEmission;
        private LocalDateTime consumeTime;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        // getter和setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getAreaId() {
            return areaId;
        }

        public void setAreaId(Long areaId) {
            this.areaId = areaId;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public String getCoefficientType() {
            return coefficientType;
        }

        public void setCoefficientType(String coefficientType) {
            this.coefficientType = coefficientType;
        }

        public Double getConsumeAmount() {
            return consumeAmount;
        }

        public void setConsumeAmount(Double consumeAmount) {
            this.consumeAmount = consumeAmount;
        }

        public Double getCarbonEmission() {
            return carbonEmission;
        }

        public void setCarbonEmission(Double carbonEmission) {
            this.carbonEmission = carbonEmission;
        }

        public LocalDateTime getConsumeTime() {
            return consumeTime;
        }

        public void setConsumeTime(LocalDateTime consumeTime) {
            this.consumeTime = consumeTime;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }
    }
}