package ynu.edu.util;

import ynu.edu.entity.CampusArea;
import ynu.edu.entity.CarbonCoefficient;
import java.util.Map;

/**
 * 碳排放计算工具类
 */
public class CarbonCalculateUtil {

    /**
     * 通用碳排放计算方法
     * @param consumeAmount 消耗量
     * @param coefficient 排放系数
     * @return 碳排放量
     */
    public static Double calculateCarbon(Double consumeAmount, Double coefficient) {
        if (consumeAmount == null || coefficient == null || consumeAmount <= 0) {
            return 0.0;
        }
        return consumeAmount * coefficient;
    }

    /**
     * 计算总碳排放量
     */
    public static Double calculateTotalCarbon(
            Double electricityCarbon,
            Double gasCarbon,
            Double trafficCarbon,
            Double evChargeCarbon) {
        return (electricityCarbon == null ? 0 : electricityCarbon) +
                (gasCarbon == null ? 0 : gasCarbon) +
                (trafficCarbon == null ? 0 : trafficCarbon) +
                (evChargeCarbon == null ? 0 : evChargeCarbon);
    }

    /**
     * 计算绿化固碳量
     * @param area 区域信息(包含绿化面积和植物类型)
     * @param treeCoefficient 植物固碳系数(kgCO₂/㎡·天)
     * @return 固碳量(kgCO₂，负值表示减少碳排放)
     */
    public static Double calculateSequestration(CampusArea area, Double treeCoefficient) {
        if (area == null || treeCoefficient == null) {
            return 0.0;
        }
        
        // 根据植物类型调整系数(示例系数)
        double typeFactor = 1.0;
        if ("乔木".equals(area.getPlantType())) {
            typeFactor = 1.5;  // 乔木固碳能力更强
        } else if ("草地".equals(area.getPlantType())) {
            typeFactor = 0.8;  // 草地固碳能力较弱
        }
        
        return area.getGreenArea() * treeCoefficient * typeFactor;
    }



    /**
     * 计算净碳排放量
     */
    public static Double calculateNetCarbon(Double totalCarbon, Double sequestration) {
        return (totalCarbon == null ? 0 : totalCarbon) - (sequestration == null ? 0 : sequestration);
    }
}