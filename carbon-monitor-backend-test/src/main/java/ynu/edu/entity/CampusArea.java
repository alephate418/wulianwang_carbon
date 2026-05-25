package ynu.edu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 校区区域信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampusArea {
    private Long id;                // 区域ID
    private String areaName;        // 区域名称
    private String areaType;        // 区域类型（教学/宿舍/食堂/绿化/交通）
    private Double areaSize;        // 面积（㎡）
    private String location;        // GIS坐标（纬度,经度）
    private Integer status;         // 状态（1-启用，0-禁用）
    private double greenArea;       // 绿化面积
    private String plantType;       // 种植类型
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
