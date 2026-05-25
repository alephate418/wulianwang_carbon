package ynu.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 碳排放系数配置表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarbonCoefficient {
    private Long id;                // ID
    private String coefficientType; // 系数类型（ELECTRICITY/GAS/FUEL/TREE）
    private Double coefficientValue; // 系数值（kgCO₂/kWh 或 kgCO₂/m³ 等）
    private String remark;          // 备注（如：全国平均电力系数）
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
