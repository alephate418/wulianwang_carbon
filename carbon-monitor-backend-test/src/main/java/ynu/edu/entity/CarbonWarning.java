package ynu.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 碳排放预警记录表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarbonWarning {
    private Long id;                // ID
    private Long areaId;            // 区域ID
    private LocalDateTime warningTime; // 预警时间
    private Double carbonValue;     // 超标碳排放量（kgCO₂）
    private Double threshold;       // 阈值（kgCO₂）
    private String warningType;     // 预警类型（电力/燃气/总排放）
    private Integer handleStatus;   // 处理状态（0-未处理，1-已处理）
    private LocalDateTime handleTime; // 处理时间
    private String handleRemark;    // 处理备注

    // 扩展字段
    private String areaName;
}
