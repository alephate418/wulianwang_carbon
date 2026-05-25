package ynu.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 碳排放时序数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarbonData {
    private Long id;                // 主键ID
    private Long areaId;            // 区域ID
    private Double totalCarbon;     // 总碳排放（kgCO₂）
    private Double sequestration;   // 固碳量（kgCO₂）
    private Double netCarbon;       // 净碳排放（kgCO₂）
    private LocalDateTime collectTime; // 采集时间
    private LocalDateTime createTime; // 创建时间

    // 关联的排放消耗明细
    private List<CarbonEmissionConsume> emissionConsumes;

    // 扩展字段
    private String areaName;
    private String areaType;
}
