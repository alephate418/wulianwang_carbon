package ynu.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ynu.edu.entity.CampusArea;
import ynu.edu.mapper.CampusAreaMapper;
import ynu.edu.service.CampusAreaService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CampusAreaServiceImpl implements CampusAreaService {

    @Autowired
    private CampusAreaMapper campusAreaMapper;

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public CampusArea getById(Long id) {
        return campusAreaMapper.selectById(id);
    }

    /**
     * 获取所有启用的区域
     * @return
     */
    @Override
    public List<CampusArea> getEnabledAreaList() {
        return campusAreaMapper.selectEnabledAreas();
    }

    @Override
    public boolean addArea(CampusArea area) {
        area.setCreateTime(LocalDateTime.now());
        area.setUpdateTime(LocalDateTime.now());
        area.setStatus(1);
        return campusAreaMapper.insertarea(area) > 0;
    }

    @Override
    public boolean updateArea(CampusArea area) {
        area.setUpdateTime(LocalDateTime.now());
        return campusAreaMapper.updateById(area) > 0;
    }
}
