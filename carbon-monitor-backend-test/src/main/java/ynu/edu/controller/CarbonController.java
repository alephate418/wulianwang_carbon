package ynu.edu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ynu.edu.entity.CarbonData;
import ynu.edu.entity.Result;
import ynu.edu.entity.vo.CarbonStatVO;
import ynu.edu.service.CarbonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/carbon")
public class CarbonController {
    @Autowired
    private CarbonService carbonService;

    /**
     * 获取所有区域的实时数据
     * @return
     */
    @GetMapping("/real-time")
    public Result getRealTimeData(){
        List<CarbonData> data = carbonService.getRealTimeCarbonData();
        return Result.success(data);
    }

    /**
     * 获取指定区域的实时数据
     * @param areaId
     * @return
     */
    @GetMapping("/real-time/{areaId}")
    public Result getAreaRealTimeData(@PathVariable Long areaId) {
        CarbonData data = carbonService.getAreaRealTimeData(areaId);
        log.info("获取指定区域的实时数据成功：{}", data);
        return Result.success(data);
    }

    /**
     * 获取指定时间段的统计数据
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/statistic")
    public Result getStatistic(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        CarbonStatVO statVO = carbonService.getCarbonStatistic(startDate, endDate);
        log.info("获取指定时间段的统计数据成功：{}", statVO);
        return Result.success(statVO);
    }

    /**
     * 接收能耗数据并实时计算保存碳排放
     * @param areaId 区域ID
     * @param consumeData 能耗数据（key: 排放类型，value: 消耗量）
     * @return 计算结果
     */
    @PostMapping("/calculate")
    public Result calculateAndSaveCarbonData(
            @RequestParam Long areaId,
            @RequestBody Map<String, Double> consumeData) {

        CarbonData result = carbonService.calculateAndSaveCarbonData(areaId, consumeData);
        log.info("计算并保存碳排放成功：{}", result);
        return Result.success(result);
    }
}
