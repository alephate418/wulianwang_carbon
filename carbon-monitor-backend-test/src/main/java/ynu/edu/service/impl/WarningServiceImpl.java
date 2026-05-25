package ynu.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ynu.edu.entity.CarbonWarning;
import ynu.edu.mapper.WarningMapper;
import ynu.edu.service.WarningService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarningServiceImpl implements WarningService {

    @Autowired
    private WarningMapper warningMapper;

    /**
     * 获取 CarbonWarning 分页数据
     * @param pageNum
     * @param pageSize
     * @param areaId
     * @param warningType
     * @param handleStatus
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<CarbonWarning> getWarningPage(Integer pageNum, Integer pageSize, Long areaId, String warningType, Integer handleStatus, LocalDateTime startTime, LocalDateTime endTime) {
        int offset = (pageNum - 1) * pageSize;
        Map<String, Integer> page = new HashMap<>();
        page.put("pageSize", pageSize);
        page.put("offset", offset);

        // 调用 mapper 方法，传递所有筛选条件
        return warningMapper.selectWarningPage(
                page,
                areaId,
                warningType,
                handleStatus,
                startTime,
                endTime
        );
    }

    @Override
    public int countWarning(Long areaId, String warningType, Integer handleStatus, LocalDateTime startTime, LocalDateTime endTime) {
        return warningMapper.countWarning(areaId, warningType, handleStatus, startTime, endTime);
    }

    @Override
    public boolean handleWarning(Long id, String handleRemark) {
        CarbonWarning warning = warningMapper.selectById(id);
        if (warning == null) {
            return false;
        }
        warning.setHandleStatus(1);
        warning.setHandleTime(LocalDateTime.now());
        warning.setHandleRemark(handleRemark);
        return warningMapper.updateById(warning) > 0;
    }
}
