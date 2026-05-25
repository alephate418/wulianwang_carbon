package ynu.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 能源消耗与碳排放数据表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarbonEmissionConsume {
    private Long id;                // ID
    private Long cdId;              // 关联carbon_data的主键
    private String coefficientType; // 消耗类型（ELECTRICITY/GAS/FUEL/TREE/SHRUB/LAWN）
    private Double consumeAmount;   // 消耗量（kWh/m³/L/㎡）
    private Double carbonEmission;  // 碳排放量（kgCO₂，固碳为负数）
    private LocalDateTime createTime;
}
