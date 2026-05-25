package ynu.edu.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import ynu.edu.entity.CarbonData;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CarbonMapper {
    /**
     * 获取指定区域的最新数据
     * @param areaId
     * @return
     */
    @Select("SELECT cd.*, ca.area_name, ca.area_type " +
            "FROM carbon_data cd " +
            "JOIN campus_area ca ON cd.area_id = ca.id " +
            "WHERE cd.area_id = #{areaId} " +
            "ORDER BY cd.collect_time DESC LIMIT 1")
    CarbonData selectLatestByAreaId(Long areaId);

    /**
     * 获取指定时间段的总碳排放
     */
    @Select("SELECT SUM(total_carbon) FROM carbon_data WHERE collect_time BETWEEN #{startTime} AND #{endTime}")
    Double selectTotalCarbonByTime(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取指定区域和时间段的数据
     * @return
     */
    List<CarbonData> selectByAreaAndTime(Long areaId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 插入carbon_data主表
     */
//    int insertData(CarbonData carbonData);


    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO carbon_data (area_id, total_carbon, sequestration, net_carbon, collect_time) VALUES (#{areaId}, #{totalCarbon}, #{sequestration}, #{netCarbon}, #{collectTime})")
    void insert(CarbonData carbonData);
}
