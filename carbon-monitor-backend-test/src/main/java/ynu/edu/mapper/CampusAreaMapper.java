package ynu.edu.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import ynu.edu.entity.CampusArea;

import java.util.List;

@Mapper
public interface CampusAreaMapper {
    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Select("SELECT * FROM campus_area WHERE id = #{id}")
    CampusArea selectById(Long id);

    /**
     * 查询所有启用的区域
     * @return
     */
    @Select("SELECT * FROM campus_area WHERE status = 1 ORDER BY id ASC")
    List<CampusArea> selectEnabledAreas();

    /**
     * 添加区域
     * @param area
     * @return
     */
    @Insert("INSERT INTO campus_area (area_name, area_type, area_size, location, status, create_time, update_time) VALUES (#{areaName}, #{areaType}, #{areaSize}, #{location}, #{status}, #{createTime}, #{updateTime})")
    int insertarea(CampusArea area);

    int updateById(CampusArea area);
}
