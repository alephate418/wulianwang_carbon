package ynu.edu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ynu.edu.entity.CampusArea;
import ynu.edu.entity.Result;
import ynu.edu.service.CampusAreaService;

import java.util.List;

/**
 * 校园区域管理接口
 */
@RestController
@RequestMapping("/area")
@Slf4j
public class CampusAreaController {
    @Autowired
    private CampusAreaService campusAreaService;
    /**
     * 查询所有区域
     */
    @GetMapping("/list")
    public Result getAreaList() {
        List<CampusArea> list = campusAreaService.getEnabledAreaList();
        log.info("查询所有区域：{}", list);
        return Result.success(list);
    }
    /**
     * 添加区域
     */
    @PostMapping("/add")
    public Result addArea(@RequestBody CampusArea area) {
        boolean success = campusAreaService.addArea(area);
        log.info("添加区域：{}", success);
        return Result.success(success);
    }
    /**
     * 修改区域
     */
    @PutMapping("/update")
    public Result updateArea(@RequestBody CampusArea area) {
        boolean success = campusAreaService.updateArea(area);
        log.info("修改区域：{}", success);
        return Result.success(success);
    }
}
