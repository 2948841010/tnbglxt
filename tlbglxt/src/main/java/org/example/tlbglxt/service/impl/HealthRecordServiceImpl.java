package org.example.tlbglxt.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.dto.request.health.AddBloodGlucoseRequest;
import org.example.tlbglxt.dto.request.health.AddBloodPressureRequest;
import org.example.tlbglxt.dto.request.health.AddWeightRequest;
import org.example.tlbglxt.dto.request.health.QueryHealthRecordRequest;
import org.example.tlbglxt.dto.response.health.BloodGlucoseRecordResponse;
import org.example.tlbglxt.dto.response.health.BloodPressureRecordResponse;
import org.example.tlbglxt.entity.health.*;
import org.example.tlbglxt.exception.BusinessException;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.repository.mongo.*;
import org.example.tlbglxt.service.HealthRecordService;
import org.example.tlbglxt.util.HealthRecordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 健康记录服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Service
@Slf4j
public class HealthRecordServiceImpl implements HealthRecordService {

    @Autowired
    private BloodGlucoseRecordRepository bloodGlucoseRecordRepository;

    @Autowired
    private BloodPressureRecordRepository bloodPressureRecordRepository;

    @Autowired
    private UserHealthProfileRepository userHealthProfileRepository;

    @Autowired
    private WeightRecordRepository weightRecordRepository;

    @Override
    public Boolean addBloodGlucoseRecord(Long userId, AddBloodGlucoseRequest request) {
        try {
            // 获取或创建用户的血糖记录文档
            BloodGlucoseRecord record = bloodGlucoseRecordRepository.findByUserId(userId)
                    .orElse(createNewBloodGlucoseRecord(userId));

            // 创建新的血糖记录条目
            BloodGlucoseRecord.GlucoseEntry entry = new BloodGlucoseRecord.GlucoseEntry();
            entry.setValue(request.getValue());
            entry.setMeasureType(request.getMeasureType());
            entry.setMeasureTime(request.getMeasureTime());
            entry.setMealType(request.getMealType());
            entry.setNote(request.getNote());

            // 添加到记录列表
            if (record.getRecords() == null) {
                record.setRecords(new ArrayList<>());
            }
            record.getRecords().add(entry);

            // 更新统计信息
            updateBloodGlucoseStatistics(record);

            // 设置更新时间
            record.setUpdateTime(LocalDateTime.now());

            // 保存到数据库
            bloodGlucoseRecordRepository.save(record);

            return true;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "添加血糖记录失败：" + e.getMessage());
        }
    }

    @Override
    public Boolean addBloodPressureRecord(Long userId, AddBloodPressureRequest request) {
        try {
            // 获取或创建用户的血压记录文档
            BloodPressureRecord record = bloodPressureRecordRepository.findByUserId(userId)
                    .orElse(createNewBloodPressureRecord(userId));

            // 创建新的血压记录条目
            BloodPressureRecord.PressureEntry entry = new BloodPressureRecord.PressureEntry();
            entry.setSystolic(request.getSystolic());
            entry.setDiastolic(request.getDiastolic());
            entry.setHeartRate(request.getHeartRate());
            entry.setMeasureTime(request.getMeasureTime());
            entry.setMeasureState(request.getMeasureState());
            entry.setNote(request.getNote());

            // 添加到记录列表
            if (record.getRecords() == null) {
                record.setRecords(new ArrayList<>());
            }
            record.getRecords().add(entry);

            // 更新统计信息
            updateBloodPressureStatistics(record);

            // 设置更新时间
            record.setUpdateTime(LocalDateTime.now());

            // 保存到数据库
            bloodPressureRecordRepository.save(record);

            return true;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "添加血压记录失败：" + e.getMessage());
        }
    }

    @Override
    public BloodGlucoseRecordResponse getBloodGlucoseRecords(Long userId, QueryHealthRecordRequest request) {
        Optional<BloodGlucoseRecord> recordOpt = bloodGlucoseRecordRepository.findByUserId(userId);
        
        if (!recordOpt.isPresent()) {
            return createEmptyBloodGlucoseResponse(userId);
        }

        BloodGlucoseRecord record = recordOpt.get();
        BloodGlucoseRecordResponse response = new BloodGlucoseRecordResponse();
        response.setUserId(userId);

        // 转换记录列表
        List<BloodGlucoseRecord.GlucoseEntry> entries = record.getRecords();
        if (entries != null) {
            // 时间范围过滤
            if (request.getStartTime() != null && request.getEndTime() != null) {
                entries = entries.stream()
                        .filter(entry -> entry.getMeasureTime().isAfter(request.getStartTime()) 
                                      && entry.getMeasureTime().isBefore(request.getEndTime()))
                        .collect(Collectors.toList());
            }

            // 转换为VO
            List<BloodGlucoseRecordResponse.GlucoseEntryVO> entryVOs = entries.stream()
                    .map(this::convertToGlucoseEntryVO)
                    .collect(Collectors.toList());

            response.setRecords(entryVOs);
        }

        // 转换统计信息
        if (record.getStatistics() != null) {
            response.setStatistics(convertToGlucoseStatisticsVO(record.getStatistics()));
        }

        return response;
    }

    @Override
    public Boolean deleteBloodGlucoseRecord(Long userId, String recordId) {
        Optional<BloodGlucoseRecord> recordOpt = bloodGlucoseRecordRepository.findByUserId(userId);
        
        if (!recordOpt.isPresent()) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "血糖记录不存在");
        }

        BloodGlucoseRecord record = recordOpt.get();
        List<BloodGlucoseRecord.GlucoseEntry> entries = record.getRecords();
        
        if (entries != null) {
            boolean removed = entries.removeIf(entry -> recordId.equals(entry.getId()));
            
            if (removed) {
                // 重新计算统计信息
                updateBloodGlucoseStatistics(record);
                record.setUpdateTime(LocalDateTime.now());
                bloodGlucoseRecordRepository.save(record);
                return true;
            }
        }

        throw new BusinessException(ResultCode.DATA_NOT_EXIST, "指定ID的血糖记录不存在");
    }

    @Override
    public Boolean deleteBloodPressureRecord(Long userId, String recordId) {
        Optional<BloodPressureRecord> recordOpt = bloodPressureRecordRepository.findByUserId(userId);
        
        if (!recordOpt.isPresent()) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "血压记录不存在");
        }

        BloodPressureRecord record = recordOpt.get();
        List<BloodPressureRecord.PressureEntry> entries = record.getRecords();
        
        if (entries != null) {
            boolean removed = entries.removeIf(entry -> recordId.equals(entry.getId()));
            
            if (removed) {
                // 重新计算统计信息
                updateBloodPressureStatistics(record);
                record.setUpdateTime(LocalDateTime.now());
                bloodPressureRecordRepository.save(record);
                return true;
            }
        }

        throw new BusinessException(ResultCode.DATA_NOT_EXIST, "指定ID的血压记录不存在");
    }

    @Override
    public Boolean updateBloodGlucoseRecord(Long userId, String recordId, AddBloodGlucoseRequest request) {
        try {
            Optional<BloodGlucoseRecord> recordOpt = bloodGlucoseRecordRepository.findByUserId(userId);
            
            if (!recordOpt.isPresent()) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "血糖记录不存在");
            }

            BloodGlucoseRecord record = recordOpt.get();
            List<BloodGlucoseRecord.GlucoseEntry> entries = record.getRecords();
            
            if (entries != null) {
                // 查找要更新的记录
                BloodGlucoseRecord.GlucoseEntry entryToUpdate = entries.stream()
                        .filter(entry -> recordId.equals(entry.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (entryToUpdate != null) {
                    // 更新记录数据
                    entryToUpdate.setValue(request.getValue());
                    entryToUpdate.setMeasureType(request.getMeasureType());
                    entryToUpdate.setMeasureTime(request.getMeasureTime());
                    entryToUpdate.setMealType(request.getMealType());
                    entryToUpdate.setNote(request.getNote());
                    
                    // 重新计算统计信息
                    updateBloodGlucoseStatistics(record);
                    record.setUpdateTime(LocalDateTime.now());
                    
                    bloodGlucoseRecordRepository.save(record);
                    return true;
                }
            }
            
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "指定ID的血糖记录不存在");
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新血糖记录失败：" + e.getMessage());
        }
    }

    @Override
    public Boolean updateBloodPressureRecord(Long userId, String recordId, AddBloodPressureRequest request) {
        try {
            Optional<BloodPressureRecord> recordOpt = bloodPressureRecordRepository.findByUserId(userId);
            
            if (!recordOpt.isPresent()) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "血压记录不存在");
            }

            BloodPressureRecord record = recordOpt.get();
            List<BloodPressureRecord.PressureEntry> entries = record.getRecords();
            
            if (entries != null) {
                // 查找要更新的记录
                BloodPressureRecord.PressureEntry entryToUpdate = entries.stream()
                        .filter(entry -> recordId.equals(entry.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (entryToUpdate != null) {
                    // 更新记录数据
                    entryToUpdate.setSystolic(request.getSystolic());
                    entryToUpdate.setDiastolic(request.getDiastolic());
                    entryToUpdate.setHeartRate(request.getHeartRate());
                    entryToUpdate.setMeasureTime(request.getMeasureTime());
                    entryToUpdate.setMeasureState(request.getMeasureState());
                    entryToUpdate.setNote(request.getNote());
                    
                    // 重新计算统计信息
                    updateBloodPressureStatistics(record);
                    record.setUpdateTime(LocalDateTime.now());
                    
                    bloodPressureRecordRepository.save(record);
                    return true;
                }
            }
            
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "指定ID的血压记录不存在");
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新血压记录失败：" + e.getMessage());
        }
    }

    @Override
    public BloodPressureRecordResponse getBloodPressureRecords(Long userId, QueryHealthRecordRequest request) {
        Optional<BloodPressureRecord> recordOpt = bloodPressureRecordRepository.findByUserId(userId);
        
        if (!recordOpt.isPresent()) {
            return createEmptyBloodPressureResponse(userId);
        }

        BloodPressureRecord record = recordOpt.get();
        BloodPressureRecordResponse response = new BloodPressureRecordResponse();
        response.setUserId(userId);

        // 转换记录列表
        List<BloodPressureRecord.PressureEntry> entries = record.getRecords();
        if (entries != null) {
            // 时间范围过滤
            if (request.getStartTime() != null && request.getEndTime() != null) {
                entries = entries.stream()
                        .filter(entry -> entry.getMeasureTime().isAfter(request.getStartTime()) 
                                      && entry.getMeasureTime().isBefore(request.getEndTime()))
                        .collect(Collectors.toList());
            }

            // 转换为VO
            List<BloodPressureRecordResponse.PressureEntryVO> entryVOs = entries.stream()
                    .map(this::convertToPressureEntryVO)
                    .collect(Collectors.toList());

            response.setRecords(entryVOs);
        }

        // 转换统计信息
        if (record.getStatistics() != null) {
            response.setStatistics(convertToPressureStatisticsVO(record.getStatistics()));
        }

        return response;
    }

    @Override
    public Boolean batchDeleteBloodGlucoseRecords(Long userId, List<String> recordIds) {
        try {
            Optional<BloodGlucoseRecord> recordOpt = bloodGlucoseRecordRepository.findByUserId(userId);
            
            if (!recordOpt.isPresent()) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "血糖记录不存在");
            }

            BloodGlucoseRecord record = recordOpt.get();
            List<BloodGlucoseRecord.GlucoseEntry> entries = record.getRecords();
            
            if (entries != null) {
                // 批量删除指定ID的记录
                boolean removed = entries.removeIf(entry -> 
                    recordIds.contains(entry.getId()));
                
                if (removed) {
                    // 重新计算统计信息
                    updateBloodGlucoseStatistics(record);
                    record.setUpdateTime(LocalDateTime.now());
                    bloodGlucoseRecordRepository.save(record);
                    return true;
                }
            }
            
            return false; // 没有记录被删除
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "批量删除血糖记录失败：" + e.getMessage());
        }
    }

    @Override
    public Boolean batchDeleteBloodPressureRecords(Long userId, List<String> recordIds) {
        try {
            Optional<BloodPressureRecord> recordOpt = bloodPressureRecordRepository.findByUserId(userId);
            
            if (!recordOpt.isPresent()) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "血压记录不存在");
            }

            BloodPressureRecord record = recordOpt.get();
            List<BloodPressureRecord.PressureEntry> entries = record.getRecords();
            
            if (entries != null) {
                // 批量删除指定ID的记录
                boolean removed = entries.removeIf(entry -> 
                    recordIds.contains(entry.getId()));
                
                if (removed) {
                    // 重新计算统计信息
                    updateBloodPressureStatistics(record);
                    record.setUpdateTime(LocalDateTime.now());
                    bloodPressureRecordRepository.save(record);
                    return true;
                }
            }
            
            return false; // 没有记录被删除
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "批量删除血压记录失败：" + e.getMessage());
        }
    }

    @Override
    public Object getHealthDataTrend(Long userId, String dataType, Integer days) {
        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            log.info("查询健康数据趋势 - 用户ID: {}, 数据类型: {}, 天数: {}, 开始时间: {}, 结束时间: {}", 
                    userId, dataType, days, startTime, endTime);
            
            Map<String, Object> trendData = new HashMap<>();
            trendData.put("dataType", dataType);
            trendData.put("days", days);
            trendData.put("startTime", startTime);
            trendData.put("endTime", endTime);
            
            List<Map<String, Object>> dataPoints = new ArrayList<>();
            
            switch (dataType.toLowerCase()) {
                case "glucose":
                    Optional<BloodGlucoseRecord> glucoseRecord = bloodGlucoseRecordRepository.findByUserId(userId);
                    log.info("血糖记录查询结果 - 用户ID: {}, 记录存在: {}", userId, glucoseRecord.isPresent());
                    
                    if (glucoseRecord.isPresent() && glucoseRecord.get().getRecords() != null) {
                        List<BloodGlucoseRecord.GlucoseEntry> allEntries = glucoseRecord.get().getRecords();
                        log.info("血糖记录总数: {}", allEntries.size());
                        
                        // 打印所有记录的时间，用于调试
                        for (int i = 0; i < Math.min(allEntries.size(), 5); i++) {
                            BloodGlucoseRecord.GlucoseEntry entry = allEntries.get(i);
                            log.info("血糖记录样本 {}: 时间={}, 值={}, 类型={}", 
                                   i+1, entry.getMeasureTime(), entry.getValue(), entry.getMeasureType());
                        }
                        
                        log.info("查询时间范围: {} 到 {}", startTime, endTime);
                        
                        // 先不过滤时间，获取所有数据
                        List<Map<String, Object>> allDataPoints = allEntries.stream()
                                .map(entry -> {
                                    Map<String, Object> point = new HashMap<>();
                                    point.put("time", entry.getMeasureTime());
                                    point.put("value", entry.getValue());
                                    point.put("type", entry.getMeasureType());
                                    return point;
                                })
                                .collect(Collectors.toList());
                        
                        log.info("不过滤时间的血糖数据点数量: {}", allDataPoints.size());
                        
                        // 然后应用时间过滤，但使用更宽松的条件
                        dataPoints = allEntries.stream()
                                .filter(entry -> {
                                    boolean inRange = entry.getMeasureTime().isAfter(startTime.minusDays(1)) 
                                                   && entry.getMeasureTime().isBefore(endTime.plusDays(1));
                                    if (!inRange) {
                                        log.debug("血糖记录时间过滤 - 记录时间: {}, 不在范围内 ({} ~ {})", 
                                                entry.getMeasureTime(), startTime, endTime);
                                    }
                                    return inRange;
                                })
                                .map(entry -> {
                                    Map<String, Object> point = new HashMap<>();
                                    point.put("time", entry.getMeasureTime());
                                    point.put("value", entry.getValue());
                                    point.put("type", entry.getMeasureType());
                                    return point;
                                })
                                .collect(Collectors.toList());
                                
                        log.info("血糖趋势数据点数量: {}", dataPoints.size());
                        
                        // 如果过滤后没有数据，返回所有数据用于调试
                        if (dataPoints.isEmpty() && !allDataPoints.isEmpty()) {
                            log.warn("时间过滤后没有数据，返回最近{}条记录用于调试", Math.min(10, allDataPoints.size()));
                            dataPoints = allDataPoints.stream()
                                    .sorted((a, b) -> ((LocalDateTime)b.get("time")).compareTo((LocalDateTime)a.get("time")))
                                    .limit(10)
                                    .collect(Collectors.toList());
                        }
                    } else {
                        log.warn("用户ID {} 没有血糖记录或记录为空", userId);
                    }
                    break;
                case "pressure":
                    Optional<BloodPressureRecord> pressureRecord = bloodPressureRecordRepository.findByUserId(userId);
                    log.info("血压记录查询结果 - 用户ID: {}, 记录存在: {}", userId, pressureRecord.isPresent());
                    
                    if (pressureRecord.isPresent() && pressureRecord.get().getRecords() != null) {
                        List<BloodPressureRecord.PressureEntry> allEntries = pressureRecord.get().getRecords();
                        log.info("血压记录总数: {}", allEntries.size());
                        
                        // 打印所有记录的时间，用于调试
                        for (int i = 0; i < Math.min(allEntries.size(), 5); i++) {
                            BloodPressureRecord.PressureEntry entry = allEntries.get(i);
                            log.info("血压记录样本 {}: 时间={}, 收缩压={}, 舒张压={}", 
                                   i+1, entry.getMeasureTime(), entry.getSystolic(), entry.getDiastolic());
                        }
                        
                        log.info("查询时间范围: {} 到 {}", startTime, endTime);
                        
                        // 先不过滤时间，获取所有数据
                        List<Map<String, Object>> allDataPoints = allEntries.stream()
                                .map(entry -> {
                                    Map<String, Object> point = new HashMap<>();
                                    point.put("time", entry.getMeasureTime());
                                    point.put("systolic", entry.getSystolic());
                                    point.put("diastolic", entry.getDiastolic());
                                    if (entry.getHeartRate() != null) {
                                        point.put("heartRate", entry.getHeartRate());
                                    }
                                    return point;
                                })
                                .collect(Collectors.toList());
                        
                        log.info("不过滤时间的血压数据点数量: {}", allDataPoints.size());
                        
                        // 然后应用时间过滤，但使用更宽松的条件
                        dataPoints = allEntries.stream()
                                .filter(entry -> {
                                    boolean inRange = entry.getMeasureTime().isAfter(startTime.minusDays(1)) 
                                                   && entry.getMeasureTime().isBefore(endTime.plusDays(1));
                                    if (!inRange) {
                                        log.debug("血压记录时间过滤 - 记录时间: {}, 不在范围内 ({} ~ {})", 
                                                entry.getMeasureTime(), startTime, endTime);
                                    }
                                    return inRange;
                                })
                                .map(entry -> {
                                    Map<String, Object> point = new HashMap<>();
                                    point.put("time", entry.getMeasureTime());
                                    point.put("systolic", entry.getSystolic());
                                    point.put("diastolic", entry.getDiastolic());
                                    if (entry.getHeartRate() != null) {
                                        point.put("heartRate", entry.getHeartRate());
                                    }
                                    return point;
                                })
                                .collect(Collectors.toList());
                                
                        log.info("血压趋势数据点数量: {}", dataPoints.size());
                        
                        // 如果过滤后没有数据，返回所有数据用于调试
                        if (dataPoints.isEmpty() && !allDataPoints.isEmpty()) {
                            log.warn("时间过滤后没有数据，返回最近{}条记录用于调试", Math.min(10, allDataPoints.size()));
                            dataPoints = allDataPoints.stream()
                                    .sorted((a, b) -> ((LocalDateTime)b.get("time")).compareTo((LocalDateTime)a.get("time")))
                                    .limit(10)
                                    .collect(Collectors.toList());
                        }
                    } else {
                        log.warn("用户ID {} 没有血压记录或记录为空", userId);
                    }
                    break;
                case "weight":
                    Optional<WeightRecord> weightRecord = weightRecordRepository.findByUserId(userId);
                    if (weightRecord.isPresent() && weightRecord.get().getRecords() != null) {
                        dataPoints = weightRecord.get().getRecords().stream()
                                .filter(entry -> entry.getMeasureTime().isAfter(startTime) 
                                              && entry.getMeasureTime().isBefore(endTime))
                                .map(entry -> {
                                    Map<String, Object> point = new HashMap<>();
                                    point.put("time", entry.getMeasureTime());
                                    point.put("weight", entry.getWeight());
                                    if (entry.getBmi() != null) {
                                        point.put("bmi", entry.getBmi());
                                    }
                                    return point;
                                })
                                .collect(Collectors.toList());
                    }
                    break;
                default:
                    throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的数据类型：" + dataType);
            }
            
            trendData.put("data", dataPoints);
            return trendData;
            
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "获取健康数据趋势失败：" + e.getMessage());
        }
    }

    @Override
    public Object getHealthStatistics(Long userId) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 血糖统计
            Optional<BloodGlucoseRecord> glucoseRecord = bloodGlucoseRecordRepository.findByUserId(userId);
            if (glucoseRecord.isPresent() && glucoseRecord.get().getStatistics() != null) {
                Map<String, Object> glucoseStats = new HashMap<>();
                BloodGlucoseRecord.GlucoseStatistics stats = glucoseRecord.get().getStatistics();
                glucoseStats.put("avgValue", stats.getAvgValue());
                glucoseStats.put("maxValue", stats.getMaxValue());
                glucoseStats.put("minValue", stats.getMinValue());
                glucoseStats.put("totalCount", stats.getTotalCount());
                glucoseStats.put("normalCount", stats.getNormalCount());
                glucoseStats.put("highCount", stats.getHighCount());
                glucoseStats.put("lowCount", stats.getLowCount());
                statistics.put("glucose", glucoseStats);
            }
            
            // 血压统计
            Optional<BloodPressureRecord> pressureRecord = bloodPressureRecordRepository.findByUserId(userId);
            if (pressureRecord.isPresent() && pressureRecord.get().getStatistics() != null) {
                Map<String, Object> pressureStats = new HashMap<>();
                BloodPressureRecord.PressureStatistics stats = pressureRecord.get().getStatistics();
                pressureStats.put("avgSystolic", stats.getAvgSystolic());
                pressureStats.put("avgDiastolic", stats.getAvgDiastolic());
                pressureStats.put("avgHeartRate", stats.getAvgHeartRate());
                pressureStats.put("totalCount", stats.getTotalCount());
                pressureStats.put("normalCount", stats.getNormalCount());
                pressureStats.put("highCount", stats.getHighCount());
                pressureStats.put("lowCount", stats.getLowCount());
                statistics.put("pressure", pressureStats);
            }
            
            // 体重统计
            Optional<WeightRecord> weightRecord = weightRecordRepository.findByUserId(userId);
            if (weightRecord.isPresent() && weightRecord.get().getStatistics() != null) {
                Map<String, Object> weightStats = new HashMap<>();
                WeightRecord.WeightStatistics stats = weightRecord.get().getStatistics();
                weightStats.put("avgWeight", stats.getAvgWeight());
                weightStats.put("currentWeight", stats.getCurrentWeight());
                weightStats.put("currentBmi", stats.getCurrentBmi());
                weightStats.put("targetWeight", stats.getTargetWeight());
                weightStats.put("weightChange7Days", stats.getWeightChange7Days());
                weightStats.put("weightChange30Days", stats.getWeightChange30Days());
                weightStats.put("totalCount", stats.getTotalCount());
                statistics.put("weight", weightStats);
            }
            
            // 计算总记录数
            int totalRecords = 0;
            if (glucoseRecord.isPresent() && glucoseRecord.get().getRecords() != null) {
                totalRecords += glucoseRecord.get().getRecords().size();
            }
            if (pressureRecord.isPresent() && pressureRecord.get().getRecords() != null) {
                totalRecords += pressureRecord.get().getRecords().size();
            }
            if (weightRecord.isPresent() && weightRecord.get().getRecords() != null) {
                totalRecords += weightRecord.get().getRecords().size();
            }
            statistics.put("totalRecords", totalRecords);
            
            return statistics;
            
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "获取健康统计信息失败：" + e.getMessage());
        }
    }

    @Override
    public Boolean initUserHealthProfile(Long userId) {
        if (userHealthProfileRepository.existsByUserId(userId)) {
            return true; // 已存在，无需初始化
        }

        UserHealthProfile profile = new UserHealthProfile();
        profile.setUserId(userId);
        profile.setCreateTime(LocalDateTime.now());
        profile.setUpdateTime(LocalDateTime.now());

        userHealthProfileRepository.save(profile);
        return true;
    }
    
    @Override
    public Boolean recalculateBloodGlucoseStatistics(Long userId) {
        try {
            log.info("🔧 开始重新计算用户 {} 的血糖统计数据", userId);
            
            Optional<BloodGlucoseRecord> recordOpt = bloodGlucoseRecordRepository.findByUserId(userId);
            
            if (!recordOpt.isPresent()) {
                log.warn("用户 {} 没有血糖记录", userId);
                return false;
            }
            
            BloodGlucoseRecord record = recordOpt.get();
            
            // 使用已修复的统计方法重新计算
            updateBloodGlucoseStatistics(record);
            
            // 保存更新后的记录
            record.setUpdateTime(LocalDateTime.now());
            bloodGlucoseRecordRepository.save(record);
            
            log.info("✅ 用户 {} 的血糖统计数据重新计算完成", userId);
            log.info("   - 总记录数: {}", record.getStatistics().getTotalCount());
            log.info("   - 正常记录: {}", record.getStatistics().getNormalCount());
            log.info("   - 偏高记录: {}", record.getStatistics().getHighCount());
            log.info("   - 偏低记录: {}", record.getStatistics().getLowCount());
            
            return true;
        } catch (Exception e) {
            log.error("重新计算血糖统计数据失败", e);
            return false;
        }
    }

    /**
     * 创建新的血糖记录文档
     */
    private BloodGlucoseRecord createNewBloodGlucoseRecord(Long userId) {
        BloodGlucoseRecord record = new BloodGlucoseRecord();
        record.setUserId(userId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setRecords(new ArrayList<>());
        return record;
    }

    /**
     * 创建新的血压记录文档
     */
    private BloodPressureRecord createNewBloodPressureRecord(Long userId) {
        BloodPressureRecord record = new BloodPressureRecord();
        record.setUserId(userId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setRecords(new ArrayList<>());
        return record;
    }

    /**
     * 更新血糖统计信息
     */
    private void updateBloodGlucoseStatistics(BloodGlucoseRecord record) {
        List<BloodGlucoseRecord.GlucoseEntry> entries = record.getRecords();
        
        // 如果记录为空，清空统计数据
        if (entries == null || entries.isEmpty()) {
            BloodGlucoseRecord.GlucoseStatistics emptyStats = new BloodGlucoseRecord.GlucoseStatistics();
            emptyStats.setAvgValue(null);
            emptyStats.setMaxValue(null);
            emptyStats.setMinValue(null);
            emptyStats.setTotalCount(0);
            emptyStats.setNormalCount(0);
            emptyStats.setHighCount(0);
            emptyStats.setLowCount(0);
            emptyStats.setLastUpdateTime(LocalDateTime.now());
            record.setStatistics(emptyStats);
            return;
        }

        BloodGlucoseRecord.GlucoseStatistics stats = new BloodGlucoseRecord.GlucoseStatistics();
        
        // 计算统计数据
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = entries.get(0).getValue();
        BigDecimal min = entries.get(0).getValue();
        int normalCount = 0;
        int highCount = 0;
        int lowCount = 0;

        for (BloodGlucoseRecord.GlucoseEntry entry : entries) {
            BigDecimal value = entry.getValue();
            sum = sum.add(value);
            
            if (value.compareTo(max) > 0) {
                max = value;
            }
            if (value.compareTo(min) < 0) {
                min = value;
            }
            
            // 🔥 修复：使用每条记录实际的measureType进行判断，而不是固定使用"random"
            String measureType = entry.getMeasureType();
            if (measureType == null || measureType.isEmpty()) {
                measureType = "random"; // 如果没有指定，默认使用随机标准
            }
            String level = HealthRecordUtil.evaluateBloodGlucoseLevel(value, measureType);
            switch (level) {
                case "low":
                    lowCount++;
                    break;
                case "high":
                    highCount++;
                    break;
                case "normal":
                default:
                    normalCount++;
                    break;
            }
        }

        stats.setAvgValue(sum.divide(new BigDecimal(entries.size()), 2, RoundingMode.HALF_UP));
        stats.setMaxValue(max);
        stats.setMinValue(min);
        stats.setTotalCount(entries.size());
        stats.setNormalCount(normalCount);
        stats.setHighCount(highCount);
        stats.setLowCount(lowCount);
        stats.setLastUpdateTime(LocalDateTime.now());

        record.setStatistics(stats);
    }

    /**
     * 更新血压统计信息
     */
    private void updateBloodPressureStatistics(BloodPressureRecord record) {
        List<BloodPressureRecord.PressureEntry> entries = record.getRecords();
        
        // 如果记录为空，清空统计数据
        if (entries == null || entries.isEmpty()) {
            BloodPressureRecord.PressureStatistics emptyStats = new BloodPressureRecord.PressureStatistics();
            emptyStats.setAvgSystolic(null);
            emptyStats.setAvgDiastolic(null);
            emptyStats.setAvgHeartRate(null);
            emptyStats.setMaxSystolic(null);
            emptyStats.setMinSystolic(null);
            emptyStats.setTotalCount(0);
            emptyStats.setNormalCount(0);
            emptyStats.setHighCount(0);
            emptyStats.setLowCount(0);
            emptyStats.setLastUpdateTime(LocalDateTime.now());
            record.setStatistics(emptyStats);
            return;
        }

        BloodPressureRecord.PressureStatistics stats = new BloodPressureRecord.PressureStatistics();
        
        // 计算统计数据
        BigDecimal systolicSum = BigDecimal.ZERO;
        BigDecimal diastolicSum = BigDecimal.ZERO;
        BigDecimal heartRateSum = BigDecimal.ZERO;
        int systolicMax = entries.get(0).getSystolic();
        int systolicMin = entries.get(0).getSystolic();
        int diastolicMax = entries.get(0).getDiastolic();
        int diastolicMin = entries.get(0).getDiastolic();
        int normalCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int heartRateCount = 0;

        for (BloodPressureRecord.PressureEntry entry : entries) {
            systolicSum = systolicSum.add(new BigDecimal(entry.getSystolic()));
            diastolicSum = diastolicSum.add(new BigDecimal(entry.getDiastolic()));
            
            if (entry.getHeartRate() != null) {
                heartRateSum = heartRateSum.add(new BigDecimal(entry.getHeartRate()));
                heartRateCount++;
            }
            
            // 最值计算
            systolicMax = Math.max(systolicMax, entry.getSystolic());
            systolicMin = Math.min(systolicMin, entry.getSystolic());
            diastolicMax = Math.max(diastolicMax, entry.getDiastolic());
            diastolicMin = Math.min(diastolicMin, entry.getDiastolic());
            
            // 血压水平判断 - 使用工具类
            String level = HealthRecordUtil.evaluateBloodPressureLevel(entry.getSystolic(), entry.getDiastolic());
            switch (level) {
                case "low":
                    lowCount++;
                    break;
                case "high":
                    highCount++;
                    break;
                case "normal":
                default:
                    normalCount++;
                    break;
            }
        }

        stats.setAvgSystolic(systolicSum.divide(new BigDecimal(entries.size()), 2, RoundingMode.HALF_UP));
        stats.setAvgDiastolic(diastolicSum.divide(new BigDecimal(entries.size()), 2, RoundingMode.HALF_UP));
        if (heartRateCount > 0) {
            stats.setAvgHeartRate(heartRateSum.divide(new BigDecimal(heartRateCount), 2, RoundingMode.HALF_UP));
        }
        stats.setMaxSystolic(systolicMax);
        stats.setMinSystolic(systolicMin);
        stats.setMaxDiastolic(diastolicMax);
        stats.setMinDiastolic(diastolicMin);
        stats.setTotalCount(entries.size());
        stats.setNormalCount(normalCount);
        stats.setHighCount(highCount);
        stats.setLowCount(lowCount);
        stats.setLastUpdateTime(LocalDateTime.now());

        record.setStatistics(stats);
    }

    /**
     * 转换为血糖记录VO
     */
    private BloodGlucoseRecordResponse.GlucoseEntryVO convertToGlucoseEntryVO(BloodGlucoseRecord.GlucoseEntry entry) {
        BloodGlucoseRecordResponse.GlucoseEntryVO vo = new BloodGlucoseRecordResponse.GlucoseEntryVO();
        vo.setId(entry.getId());
        vo.setValue(entry.getValue());
        vo.setMeasureType(entry.getMeasureType());
        vo.setMeasureTime(entry.getMeasureTime());
        vo.setMealType(entry.getMealType());
        vo.setNote(entry.getNote());
        
        // 血糖水平评估 - 使用工具类
        vo.setLevel(HealthRecordUtil.evaluateBloodGlucoseLevel(entry.getValue(), entry.getMeasureType()));
        
        return vo;
    }

    /**
     * 转换为血糖统计VO
     */
    private BloodGlucoseRecordResponse.GlucoseStatisticsVO convertToGlucoseStatisticsVO(BloodGlucoseRecord.GlucoseStatistics stats) {
        BloodGlucoseRecordResponse.GlucoseStatisticsVO vo = new BloodGlucoseRecordResponse.GlucoseStatisticsVO();
        vo.setAvgValue(stats.getAvgValue());
        vo.setMaxValue(stats.getMaxValue());
        vo.setMinValue(stats.getMinValue());
        vo.setTotalCount(stats.getTotalCount());
        vo.setNormalCount(stats.getNormalCount());
        vo.setHighCount(stats.getHighCount());
        vo.setLowCount(stats.getLowCount());
        vo.setLastUpdateTime(stats.getLastUpdateTime());
        return vo;
    }

    /**
     * 转换为血压记录VO
     */
    private BloodPressureRecordResponse.PressureEntryVO convertToPressureEntryVO(BloodPressureRecord.PressureEntry entry) {
        BloodPressureRecordResponse.PressureEntryVO vo = new BloodPressureRecordResponse.PressureEntryVO();
        vo.setId(entry.getId());
        vo.setSystolic(entry.getSystolic());
        vo.setDiastolic(entry.getDiastolic());
        vo.setHeartRate(entry.getHeartRate());
        vo.setMeasureTime(entry.getMeasureTime());
        vo.setMeasureState(entry.getMeasureState());
        vo.setNote(entry.getNote());
        
        // 血压水平评估 - 使用工具类
        vo.setLevel(HealthRecordUtil.evaluateBloodPressureLevel(entry.getSystolic(), entry.getDiastolic()));
        
        return vo;
    }

    /**
     * 转换为血压统计VO
     */
    private BloodPressureRecordResponse.PressureStatisticsVO convertToPressureStatisticsVO(BloodPressureRecord.PressureStatistics stats) {
        BloodPressureRecordResponse.PressureStatisticsVO vo = new BloodPressureRecordResponse.PressureStatisticsVO();
        vo.setAvgSystolic(stats.getAvgSystolic());
        vo.setAvgDiastolic(stats.getAvgDiastolic());
        vo.setAvgHeartRate(stats.getAvgHeartRate());
        vo.setMaxSystolic(stats.getMaxSystolic());
        vo.setMinSystolic(stats.getMinSystolic());
        vo.setMaxDiastolic(stats.getMaxDiastolic());
        vo.setMinDiastolic(stats.getMinDiastolic());
        vo.setTotalCount(stats.getTotalCount());
        vo.setNormalCount(stats.getNormalCount());
        vo.setHighCount(stats.getHighCount());
        vo.setLowCount(stats.getLowCount());
        vo.setLastUpdateTime(stats.getLastUpdateTime());
        return vo;
    }

    /**
     * 创建空的血糖记录响应
     */
    private BloodGlucoseRecordResponse createEmptyBloodGlucoseResponse(Long userId) {
        BloodGlucoseRecordResponse response = new BloodGlucoseRecordResponse();
        response.setUserId(userId);
        response.setRecords(new ArrayList<>());
        return response;
    }

    /**
     * 创建空的血压记录响应
     */
    private BloodPressureRecordResponse createEmptyBloodPressureResponse(Long userId) {
        BloodPressureRecordResponse response = new BloodPressureRecordResponse();
        response.setUserId(userId);
        response.setRecords(new ArrayList<>());
        return response;
    }

    // ===== 体重记录相关方法实现 =====

    @Override
    public Boolean addWeightRecord(Long userId, AddWeightRequest request) {
        try {
            // 获取或创建用户的体重记录文档
            WeightRecord record = weightRecordRepository.findByUserId(userId)
                    .orElse(createNewWeightRecord(userId));

            // 创建新的体重记录条目
            WeightRecord.WeightEntry entry = new WeightRecord.WeightEntry();
            entry.setWeight(request.getWeight());
            entry.setHeight(request.getHeight());
            entry.setBodyFatRate(request.getBodyFatRate());
            entry.setMuscleMass(request.getMuscleMass());
            entry.setBmr(request.getBmr());
            entry.setMeasureTime(request.getMeasureTime());
            entry.setMeasureState(request.getMeasureState());
            entry.setNote(request.getNote());
            entry.setIsManualInput(request.getIsManualInput());
            entry.setDeviceType(request.getDeviceType());

            // 计算BMI
            if (request.getHeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal heightInMeters = request.getHeight().divide(new BigDecimal("100"));
                BigDecimal bmi = request.getWeight().divide(
                    heightInMeters.multiply(heightInMeters), 2, RoundingMode.HALF_UP);
                entry.setBmi(bmi);
            }

            // 添加到记录列表
            if (record.getRecords() == null) {
                record.setRecords(new ArrayList<>());
            }
            record.getRecords().add(entry);

            // 更新统计信息
            updateWeightStatistics(record);

            // 设置更新时间
            record.setUpdateTime(LocalDateTime.now());

            // 保存到数据库
            weightRecordRepository.save(record);

            return true;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "添加体重记录失败：" + e.getMessage());
        }
    }

    @Override
    public Boolean updateWeightRecord(Long userId, String recordId, AddWeightRequest request) {
        try {
            Optional<WeightRecord> recordOpt = weightRecordRepository.findByUserId(userId);
            
            if (!recordOpt.isPresent()) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "体重记录不存在");
            }

            WeightRecord record = recordOpt.get();
            List<WeightRecord.WeightEntry> entries = record.getRecords();
            
            if (entries != null) {
                // 查找要更新的记录
                WeightRecord.WeightEntry entryToUpdate = entries.stream()
                        .filter(entry -> recordId.equals(entry.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (entryToUpdate != null) {
                    // 更新记录数据
                    entryToUpdate.setWeight(request.getWeight());
                    entryToUpdate.setHeight(request.getHeight());
                    entryToUpdate.setBodyFatRate(request.getBodyFatRate());
                    entryToUpdate.setMuscleMass(request.getMuscleMass());
                    entryToUpdate.setBmr(request.getBmr());
                    entryToUpdate.setMeasureTime(request.getMeasureTime());
                    entryToUpdate.setMeasureState(request.getMeasureState());
                    entryToUpdate.setNote(request.getNote());
                    entryToUpdate.setIsManualInput(request.getIsManualInput());
                    entryToUpdate.setDeviceType(request.getDeviceType());

                    // 重新计算BMI
                    if (request.getHeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal heightInMeters = request.getHeight().divide(new BigDecimal("100"));
                        BigDecimal bmi = request.getWeight().divide(
                            heightInMeters.multiply(heightInMeters), 2, RoundingMode.HALF_UP);
                        entryToUpdate.setBmi(bmi);
                    }
                    
                    // 重新计算统计信息
                    updateWeightStatistics(record);
                    record.setUpdateTime(LocalDateTime.now());
                    
                    weightRecordRepository.save(record);
                    return true;
                }
            }
            
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "指定ID的体重记录不存在");
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新体重记录失败：" + e.getMessage());
        }
    }

    @Override
    public Object getWeightRecords(Long userId, QueryHealthRecordRequest request) {
        Optional<WeightRecord> recordOpt = weightRecordRepository.findByUserId(userId);
        
        if (!recordOpt.isPresent()) {
            return createEmptyWeightResponse(userId);
        }

        WeightRecord record = recordOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);

        // 转换记录列表
        List<WeightRecord.WeightEntry> entries = record.getRecords();
        if (entries != null) {
            // 时间范围过滤
            if (request.getStartTime() != null && request.getEndTime() != null) {
                entries = entries.stream()
                        .filter(entry -> entry.getMeasureTime().isAfter(request.getStartTime()) 
                                      && entry.getMeasureTime().isBefore(request.getEndTime()))
                        .collect(Collectors.toList());
            }

            response.put("records", entries);
        }

        // 添加统计信息
        if (record.getStatistics() != null) {
            response.put("statistics", record.getStatistics());
        }

        return response;
    }

    @Override
    public Boolean deleteWeightRecord(Long userId, String recordId) {
        Optional<WeightRecord> recordOpt = weightRecordRepository.findByUserId(userId);
        
        if (!recordOpt.isPresent()) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "体重记录不存在");
        }

        WeightRecord record = recordOpt.get();
        List<WeightRecord.WeightEntry> entries = record.getRecords();
        
        if (entries != null) {
            boolean removed = entries.removeIf(entry -> recordId.equals(entry.getId()));
            
            if (removed) {
                // 重新计算统计信息
                updateWeightStatistics(record);
                record.setUpdateTime(LocalDateTime.now());
                weightRecordRepository.save(record);
                return true;
            }
        }

        throw new BusinessException(ResultCode.DATA_NOT_EXIST, "指定ID的体重记录不存在");
    }

    @Override
    public Boolean batchDeleteWeightRecords(Long userId, List<String> recordIds) {
        try {
            Optional<WeightRecord> recordOpt = weightRecordRepository.findByUserId(userId);
            
            if (!recordOpt.isPresent()) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST, "体重记录不存在");
            }

            WeightRecord record = recordOpt.get();
            List<WeightRecord.WeightEntry> entries = record.getRecords();
            
            if (entries != null) {
                // 批量删除指定ID的记录
                boolean removed = entries.removeIf(entry -> 
                    recordIds.contains(entry.getId()));
                
                if (removed) {
                    // 重新计算统计信息
                    updateWeightStatistics(record);
                    record.setUpdateTime(LocalDateTime.now());
                    weightRecordRepository.save(record);
                    return true;
                }
            }
            
            return false; // 没有记录被删除
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "批量删除体重记录失败：" + e.getMessage());
        }
    }

    /**
     * 创建新的体重记录文档
     */
    private WeightRecord createNewWeightRecord(Long userId) {
        WeightRecord record = new WeightRecord();
        record.setUserId(userId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setRecords(new ArrayList<>());
        return record;
    }

    /**
     * 更新体重统计信息
     */
    private void updateWeightStatistics(WeightRecord record) {
        List<WeightRecord.WeightEntry> entries = record.getRecords();
        
        // 如果记录为空，清空统计数据
        if (entries == null || entries.isEmpty()) {
            WeightRecord.WeightStatistics emptyStats = new WeightRecord.WeightStatistics();
            emptyStats.setAvgWeight(null);
            emptyStats.setMaxWeight(null);
            emptyStats.setMinWeight(null);
            emptyStats.setCurrentWeight(null);
            emptyStats.setCurrentBmi(null);
            emptyStats.setTotalCount(0);
            emptyStats.setWeightChange7Days(null);
            emptyStats.setWeightChange30Days(null);
            emptyStats.setLastUpdateTime(LocalDateTime.now());
            record.setStatistics(emptyStats);
            return;
        }

        WeightRecord.WeightStatistics stats = new WeightRecord.WeightStatistics();
        
        // 按时间排序，最新的在前面
        entries.sort((a, b) -> b.getMeasureTime().compareTo(a.getMeasureTime()));
        
        // 计算统计数据
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal maxWeight = entries.get(0).getWeight();
        BigDecimal minWeight = entries.get(0).getWeight();
        BigDecimal currentWeight = entries.get(0).getWeight(); // 最新的体重
        BigDecimal currentBmi = entries.get(0).getBmi();

        for (WeightRecord.WeightEntry entry : entries) {
            BigDecimal weight = entry.getWeight();
            sum = sum.add(weight);
            
            if (weight.compareTo(maxWeight) > 0) {
                maxWeight = weight;
            }
            if (weight.compareTo(minWeight) < 0) {
                minWeight = weight;
            }
        }

        stats.setAvgWeight(sum.divide(new BigDecimal(entries.size()), 2, RoundingMode.HALF_UP));
        stats.setMaxWeight(maxWeight);
        stats.setMinWeight(minWeight);
        stats.setCurrentWeight(currentWeight);
        stats.setCurrentBmi(currentBmi);
        stats.setTotalCount(entries.size());

        // 计算7天和30天的体重变化
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        Optional<WeightRecord.WeightEntry> sevenDaysEntry = entries.stream()
                .filter(entry -> entry.getMeasureTime().isBefore(sevenDaysAgo))
                .findFirst();
        if (sevenDaysEntry.isPresent()) {
            BigDecimal change = currentWeight.subtract(sevenDaysEntry.get().getWeight());
            stats.setWeightChange7Days(change);
        }

        Optional<WeightRecord.WeightEntry> thirtyDaysEntry = entries.stream()
                .filter(entry -> entry.getMeasureTime().isBefore(thirtyDaysAgo))
                .findFirst();
        if (thirtyDaysEntry.isPresent()) {
            BigDecimal change = currentWeight.subtract(thirtyDaysEntry.get().getWeight());
            stats.setWeightChange30Days(change);
        }

        stats.setLastUpdateTime(LocalDateTime.now());

        record.setStatistics(stats);
    }

    /**
     * 创建空的体重记录响应
     */
    private Map<String, Object> createEmptyWeightResponse(Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("records", new ArrayList<>());
        return response;
    }
} 