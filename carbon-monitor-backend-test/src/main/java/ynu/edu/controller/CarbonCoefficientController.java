package ynu.edu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ynu.edu.entity.CarbonCoefficient;
import ynu.edu.entity.Result;
import ynu.edu.service.CarbonCoefficientService;

import java.util.List;

/**
 * 碳排放系数管理接口
 */
@RestController
@RequestMapping("/coefficient")
@Slf4j
public class CarbonCoefficientController {

    @Autowired
    private CarbonCoefficientService coefficientService;

    /**
     * 获取所有系数配置
     */
    @GetMapping("/list")
    public Result getCoefficientList() {
        List<CarbonCoefficient> list = coefficientService.getAllCoefficients();
        log.info("获取所有系数配置成功，结果：{}", list);
        return Result.success(list);
    }

    /**
     * 根据类型获取系数值
     */
    @GetMapping("/{type}")
    public Result getCoefficientValue(@PathVariable String type) {
        Double value = coefficientService.getCoefficientValueByType(type);
        log.info("获取{}系数值成功，结果：{}", type, value);
        return Result.success(value);
    }

    /**
     * 更新系数值
     */
    @PutMapping("/update")
    public Result updateCoefficient(
            @RequestParam String type,
            @RequestParam Double value) {
        boolean success = coefficientService.updateCoefficientValue(type, value);
        log.info("更新{}系数值成功，结果：{}", type, success);
        return Result.success(success);
    }

    /**
     * 添加新系数
     */
    @PostMapping("/add")
    public Result addCoefficient(@RequestBody CarbonCoefficient coefficient) {
        boolean success = coefficientService.add(coefficient);
        log.info("添加新系数成功，结果：{}", success);
        return Result.success(success);
    }
}


