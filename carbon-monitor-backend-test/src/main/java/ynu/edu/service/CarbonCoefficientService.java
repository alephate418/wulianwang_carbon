package ynu.edu.service;

import ynu.edu.entity.CarbonCoefficient;

import java.util.List;

public interface CarbonCoefficientService {

    List<CarbonCoefficient> getAllCoefficients();

    Double getCoefficientValueByType(String type);

    boolean updateCoefficientValue(String type, Double value);

    boolean add(CarbonCoefficient coefficient);
}
