package ynu.edu.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarbonStatVO {
    // 时间维度统计（如：近7天）
    private List<String> timeLabels;
    private List<Double> totalCarbonList; // 每日总碳排放
    private List<Double> netCarbonList;   // 每日净碳排放

    // 排放源占比
    private List<String> sourceLabels; // 排放源（电力/燃气/交通）
    private List<Double> sourceValues; // 占比（%）

    // 区域排名
    private List<String> areaLabels;   // 区域名称
    private List<Double> areaValues;   // 碳排放总量
}
