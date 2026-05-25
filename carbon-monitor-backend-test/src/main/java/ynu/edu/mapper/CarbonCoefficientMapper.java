package ynu.edu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import ynu.edu.entity.CarbonCoefficient;

import java.util.List;

@Mapper
public interface CarbonCoefficientMapper {

    /**
     * 查询所有系数配置（按类型排序）
     * @return
     */
    @Select("select * from carbon_coefficient ORDER BY coefficient_type ASC")
    List<CarbonCoefficient> selectAllCoefficients();

    /**
     * 根据系数类型查询系数配置
     * @param type
     * @return
     */
    @Select("select * from carbon_coefficient where coefficient_type = #{type} LIMIT 1")
    CarbonCoefficient selectByType(String type);

    int updateById(CarbonCoefficient coefficient);

    int insert(CarbonCoefficient coefficient);
}
