//package ynu.edu;
//
//import ynu.edu.CarbonMonitorBackendTestApplication;
//import ynu.edu.entity.CarbonData;
//import ynu.edu.entity.CarbonEmissionConsume;
//import ynu.edu.entity.CampusArea;
//import ynu.edu.service.CampusAreaService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.util.Assert;
//import ynu.edu.util.CarbonDataSimulator;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//
///**
// * CarbonDataSimulator 工具类测试
// */
//@SpringBootTest(classes = CarbonMonitorBackendTestApplication.class)
//public class CarbonDataSimulatorTest {
//
//    // 注入待测试的工具类
//    @Autowired
//    private CarbonDataSimulator carbonDataSimulator;
//
//    // 模拟 CampusAreaService（避免依赖数据库实际数据）
//    @MockBean
//    private CampusAreaService campusAreaService;
//
//    // 测试用区域ID
//    private static final Long TEST_AREA_ID = 1L;
//
//    /**
//     * 测试前置准备：模拟区域数据
//     */
//    @BeforeEach
//    void setUp() {
//        // 构建模拟区域对象
//        CampusArea testArea = new CampusArea();
//        testArea.setId(TEST_AREA_ID);
//        testArea.setAreaName("测试教学楼");
//        testArea.setAreaType("教学");
//        testArea.setStatus(1); // 启用状态
//
//        // 模拟查询有效区域列表的返回结果
//        List<CampusArea> areaList = new ArrayList<>();
//        areaList.add(testArea);
//        when(campusAreaService.getEnabledAreaList()).thenReturn(areaList);
//
//        // 手动触发工具类初始化（因@PostConstruct依赖真实Service，此处手动初始化基准数据）
//        carbonDataSimulator.init();
//    }
//
//    /**
//     * 测试核心功能：生成的模拟数据是否符合业务规则
//     */
//    @Test
//    void testGenerateSimulatedData() {
//        // 1. 执行模拟数据生成
//        CarbonData simulatedData = carbonDataSimulator.generateSimulatedData(TEST_AREA_ID);
//
//        // 2. 验证核心字段完整性
//        Assert.notNull(simulatedData, "模拟数据不能为null");
//        Assert.isTrue(TEST_AREA_ID.equals(simulatedData.getAreaId()), "区域ID匹配错误");
//        Assert.notNull(simulatedData.getCollectTime(), "采集时间不能为null");
//        Assert.notNull(simulatedData.getEmissionConsumes(), "排放明细不能为null");
//        Assert.isTrue(!simulatedData.getEmissionConsumes().isEmpty(), "排放明细不能为空");
//
//        // 3. 验证数值合理性（总排放、固碳量、净排放均为非负值）
//        Assert.isTrue(simulatedData.getTotalCarbon() >= 0, "总碳排放量不能为负数");
//        Assert.isTrue(simulatedData.getSequestration() >= 0, "固碳量不能为负数");
//        Assert.isTrue(simulatedData.getNetCarbon() >= 0, "净碳排放量不能为负数");
//
//        // 4. 验证排放与固碳类型区分（固碳类型明细的排放量为负值）
//        boolean hasSequestrationType = false;
//        for (CarbonEmissionConsume consume : simulatedData.getEmissionConsumes()) {
//            // 验证明细字段完整性
//            Assert.notNull(consume.getCoefficientType(), "明细类型不能为null");
//            Assert.isTrue(consume.getConsumeAmount() > 0, "明细消耗量必须大于0");
//            Assert.notNull(consume.getCarbonEmission(), "明细碳排放量不能为null");
//
//            // 验证固碳类型（TREE/SHRUB/LAWN）的排放量为负值
//            if (consume.getCoefficientType().contains("TREE")
//                    || consume.getCoefficientType().contains("SHRUB")
//                    || consume.getCoefficientType().contains("LAWN")) {
//                Assert.isTrue(consume.getCarbonEmission() < 0, "固碳类型明细排放量必须为负值");
//                hasSequestrationType = true;
//            } else {
//                // 排放类型（电力/燃气等）的排放量为正值
//                Assert.isTrue(consume.getCarbonEmission() > 0, "排放类型明细排放量必须为正值");
//            }
//        }
//
//        // 5. 验证存在固碳类型明细（符合工具类配置的类型占比）
//        Assert.isTrue(hasSequestrationType, "模拟数据必须包含固碳类型明细");
//
//        // 6. 验证净排放计算逻辑（净排放 = 总排放 - 固碳量）
//        double expectedNetCarbon = simulatedData.getTotalCarbon() - simulatedData.getSequestration();
//        Assert.isTrue(Math.abs(simulatedData.getNetCarbon() - expectedNetCarbon) < 0.001, "净排放计算逻辑错误");
//    }
//
//    /**
//     * 测试边界场景：传入不存在的区域ID，是否能生成默认数据
//     */
//    @Test
//    void testGenerateSimulatedDataWithInvalidAreaId() {
//        // 传入不存在的区域ID（999）
//        Long invalidAreaId = 999L;
//        CarbonData simulatedData = carbonDataSimulator.generateSimulatedData(invalidAreaId);
//
//        // 验证数据仍能正常生成（使用工具类默认基准值）
//        Assert.notNull(simulatedData, "不存在的区域ID仍应生成模拟数据");
//        Assert.isTrue(simulatedData.getTotalCarbon() > 0, "默认基准值生成的总排放必须大于0");
//        Assert.isTrue(!simulatedData.getEmissionConsumes().isEmpty(), "默认数据仍需包含排放明细");
//    }
//}