package ynu.edu.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import ynu.edu.entity.CarbonEmissionConsume;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CarbonEmissionConsumeMapper {
    /**
     * 批量插入排放消耗明细
     */
    int batchInsert(List<CarbonEmissionConsume> consumeList);

    /**
     * 根据carbon_data ID查询明细
     */
    List<CarbonEmissionConsume> selectByCdId(Long cdId);

    /**
     * 根据时间范围和区域查询排放明细
     */
    List<CarbonEmissionConsume> selectByAreaAndTime(Long areaId, LocalDateTime startTime, LocalDateTime endTime);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into carbon_emission_consume (cd_id, coefficient_type, consume_amount, carbon_emission, create_time) values (#{cdId}, #{coefficientType}, #{consumeAmount}, #{carbonEmission}, #{createTime})")
    void insert(CarbonEmissionConsume consume);
}