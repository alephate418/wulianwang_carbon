package ynu.edu.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import ynu.edu.entity.CarbonWarning;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface WarningMapper {
    public List<CarbonWarning> selectWarningPage(Map<String, Integer> page, Long areaId, String warningType, Integer handleStatus, LocalDateTime startTime, LocalDateTime endTime);

    int countWarning(Long areaId, String warningType, Integer handleStatus, LocalDateTime startTime, LocalDateTime endTime);

    @Select("SELECT cw.*, ca.area_name " +
            "FROM carbon_warning cw " +
            "JOIN campus_area ca ON cw.area_id = ca.id " +
            "WHERE cw.id = #{id}")
    CarbonWarning selectById(Long id);

    int updateById(CarbonWarning warning);

    @Insert("INSERT INTO carbon_warning (area_id, warning_time, carbon_value, threshold, warning_type, handle_status)" +
            " VALUES ( #{areaId}, #{warningTime}, #{carbonValue}, #{threshold}, #{warningType}, #{handleStatus})")
    void insert(CarbonWarning warning);
}
