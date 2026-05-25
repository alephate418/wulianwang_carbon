package ynu.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ynu.edu.entity.CarbonCoefficient;
import ynu.edu.mapper.CarbonCoefficientMapper;
import ynu.edu.service.CarbonCoefficientService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarbonCoefficientServiceImpl implements CarbonCoefficientService {

    @Autowired
    private CarbonCoefficientMapper carbonCoefficientMapper;

    /**
     * 获取所有系数配置
     *
     * @return
     */
    @Override
    public List<CarbonCoefficient> getAllCoefficients() {
        return carbonCoefficientMapper.selectAllCoefficients();
    }

    /**
     * 根据类型获取系数值
     *
     * @param type
     * @return
     */
    @Override
    public Double getCoefficientValueByType(String type) {
        CarbonCoefficient coefficient = carbonCoefficientMapper.selectByType(type);
        return coefficient != null ? coefficient.getCoefficientValue() : 0.0;
    }

    /**
     * 更新系数值
     *
     * @param type
     * @param value
     * @return
     */
    @Override
    public boolean updateCoefficientValue(String type, Double value) {
        CarbonCoefficient coefficient = carbonCoefficientMapper.selectByType(type);
        if (coefficient == null) {
            return false;
        }
        coefficient.setCoefficientValue(value);
        coefficient.setUpdateTime(LocalDateTime.now());
        return carbonCoefficientMapper.updateById(coefficient) > 0;
    }

    @Override
    public boolean add(CarbonCoefficient coefficient) {
        // 验证系数类型是否已存在
        CarbonCoefficient existing = carbonCoefficientMapper.selectByType(coefficient.getCoefficientType());
        if (existing != null) {
            return false; // 类型已存在，不允许重复添加
        }
        // 设置创建时间（如果实体类有该字段）
        coefficient.setCreateTime(LocalDateTime.now());
        coefficient.setUpdateTime(LocalDateTime.now());
        return carbonCoefficientMapper.insert(coefficient) > 0;
    }
}
