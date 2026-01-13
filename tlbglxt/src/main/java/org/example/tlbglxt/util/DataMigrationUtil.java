package org.example.tlbglxt.util;

import org.example.tlbglxt.entity.health.BloodGlucoseRecord;
import org.example.tlbglxt.entity.health.BloodPressureRecord;
import org.example.tlbglxt.entity.health.WeightRecord;
import org.example.tlbglxt.repository.mongo.BloodGlucoseRecordRepository;
import org.example.tlbglxt.repository.mongo.BloodPressureRecordRepository;
import org.example.tlbglxt.repository.mongo.WeightRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 数据迁移工具类
 * 用于为现有健康记录添加ID字段
 */
@Component
public class DataMigrationUtil {

    @Autowired
    private BloodGlucoseRecordRepository bloodGlucoseRecordRepository;

    @Autowired
    private BloodPressureRecordRepository bloodPressureRecordRepository;

    @Autowired
    private WeightRecordRepository weightRecordRepository;

    /**
     * 为所有现有记录添加ID字段
     */
    public void migrateAllRecords() {
        migrateBloodGlucoseRecords();
        migrateBloodPressureRecords();
        migrateWeightRecords();
    }

    /**
     * 为血糖记录添加ID
     */
    public void migrateBloodGlucoseRecords() {
        List<BloodGlucoseRecord> records = bloodGlucoseRecordRepository.findAll();
        for (BloodGlucoseRecord record : records) {
            if (record.getRecords() != null) {
                boolean needUpdate = false;
                for (BloodGlucoseRecord.GlucoseEntry entry : record.getRecords()) {
                    if (entry.getId() == null || entry.getId().isEmpty()) {
                        entry.setId(UUID.randomUUID().toString());
                        needUpdate = true;
                    }
                }
                if (needUpdate) {
                    bloodGlucoseRecordRepository.save(record);
                }
            }
        }
    }

    /**
     * 为血压记录添加ID
     */
    public void migrateBloodPressureRecords() {
        List<BloodPressureRecord> records = bloodPressureRecordRepository.findAll();
        for (BloodPressureRecord record : records) {
            if (record.getRecords() != null) {
                boolean needUpdate = false;
                for (BloodPressureRecord.PressureEntry entry : record.getRecords()) {
                    if (entry.getId() == null || entry.getId().isEmpty()) {
                        entry.setId(UUID.randomUUID().toString());
                        needUpdate = true;
                    }
                }
                if (needUpdate) {
                    bloodPressureRecordRepository.save(record);
                }
            }
        }
    }

    /**
     * 为体重记录添加ID
     */
    public void migrateWeightRecords() {
        List<WeightRecord> records = weightRecordRepository.findAll();
        for (WeightRecord record : records) {
            if (record.getRecords() != null) {
                boolean needUpdate = false;
                for (WeightRecord.WeightEntry entry : record.getRecords()) {
                    if (entry.getId() == null || entry.getId().isEmpty()) {
                        entry.setId(UUID.randomUUID().toString());
                        needUpdate = true;
                    }
                }
                if (needUpdate) {
                    weightRecordRepository.save(record);
                }
            }
        }
    }
} 