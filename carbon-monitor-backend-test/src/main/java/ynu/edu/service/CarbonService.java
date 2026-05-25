package ynu.edu.service;

import ynu.edu.entity.CarbonData;
import ynu.edu.entity.vo.CarbonStatVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CarbonService {
    public CarbonData getAreaRealTimeData(Long areaId);

    public List<CarbonData> getRealTimeCarbonData();

    CarbonStatVO getCarbonStatistic(LocalDate startDate, LocalDate endDate);

    CarbonData calculateAndSaveCarbonData(Long areaId, Map<String, Double> consumeData);
}
