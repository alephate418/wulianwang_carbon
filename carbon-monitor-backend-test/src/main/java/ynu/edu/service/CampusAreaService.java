package ynu.edu.service;

import ynu.edu.entity.CampusArea;

import java.util.List;

public interface CampusAreaService {
    CampusArea getById(Long id);

    List<CampusArea> getEnabledAreaList();

    boolean addArea(CampusArea area);

    boolean updateArea(CampusArea area);
}
