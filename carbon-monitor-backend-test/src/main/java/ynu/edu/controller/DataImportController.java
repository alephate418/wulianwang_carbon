package ynu.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ynu.edu.entity.Result;
import ynu.edu.util.CarbonDataImportUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 数据导入控制器
 */
@RestController
@RequestMapping("/import")
public class DataImportController {

    @Autowired
    private CarbonDataImportUtil importUtil;

    /**
     * 上传CSV文件并导入数据
     */
    @PostMapping("/carbon-data")
    public Result importCarbonData(@RequestParam("file") MultipartFile file) {
        try {
            // 创建临时文件
            File tempFile = File.createTempFile("carbon_import_", ".csv");
            file.transferTo(tempFile);
            
            // 调用导入工具
            Map<String, Object> result = importUtil.importCarbonData(tempFile.getAbsolutePath());
            
            // 删除临时文件
            tempFile.delete();
            
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件处理失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }
}