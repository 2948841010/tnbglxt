package org.example.tlbglxt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * MongoDB配置类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Configuration
@EnableMongoRepositories(basePackages = "org.example.tlbglxt.repository.mongo")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "tlbglxt_health";
    }

    @Override
    protected boolean autoIndexCreation() {
        // 启用自动索引创建
        return true;
    }

    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        // 设置时区
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        return new MongoCustomConversions(java.util.Collections.emptyList());
    }
} 