package ynu.edu.service;

import ynu.edu.entity.CarbonWarning;

import java.time.LocalDateTime;
import java.util.List;

public interface WarningService {
    List<CarbonWarning> getWarningPage(Integer pageNum, Integer pageSize, Long areaId, String warningType, Integer handleStatus, LocalDateTime startTime, LocalDateTime endTime);

    int countWarning(Long areaId, String warningType, Integer handleStatus, LocalDateTime startTime, LocalDateTime endTime);

    boolean handleWarning(Long id, String handleRemark);
}
