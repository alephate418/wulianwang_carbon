package ynu.edu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ynu.edu.entity.CarbonWarning;
import ynu.edu.entity.Result;
import ynu.edu.service.WarningService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警管理接口
 */
@RestController
@RequestMapping("/warning")
@Slf4j
public class WarningController {

    @Autowired
    private WarningService warningService;

    /**
     * 分页查询预警记录
     */
    @GetMapping("/list")
    public Result getWarningList(
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String warningType,
            @RequestParam(required = false) Integer handleStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        List<CarbonWarning> list = warningService.getWarningPage(pageNum, pageSize, areaId, warningType, handleStatus, startTime, endTime);
        int total = warningService.countWarning(areaId, warningType, handleStatus, startTime, endTime);

        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", list);
        pageResult.put("total", total);
        pageResult.put("pageNum", pageNum);
        pageResult.put("pageSize", pageSize);

        log.info("分页查询预警记录成功，结果：{}", pageResult);

        return Result.success(pageResult);
    }

    /**
     * 处理预警
     */
    @PutMapping("/handle/{id}")
    public Result handleWarning(
            @PathVariable Long id,
            @RequestParam String handleRemark) {
        boolean success = warningService.handleWarning(id, handleRemark);
        log.info("处理预警成功，结果：{}", success);
        return Result.success(success);
    }
}
