package com.example.temporal.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Temporal Client và các thành phần liên quan
 * 
 * Class này thiết lập kết nối đến Temporal Server và
 * cung cấp các bean cần thiết cho Spring Context.
 */
@Configuration
public class TemporalConfig {

    private static final Logger logger = LoggerFactory.getLogger(TemporalConfig.class);

    @Value("${temporal.connection.target:localhost:7233}")
    private String temporalTarget;

    @Value("${temporal.namespace:default}")
    private String temporalNamespace;

    /**
     * Tạo WorkflowServiceStubs - kết nối đến Temporal Server
     * 
     * @return WorkflowServiceStubs instance
     */
    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        logger.info("🔗 Connecting to Temporal Server at: {}", temporalTarget);
        
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalTarget)
                .build();
        
        WorkflowServiceStubs serviceStubs = WorkflowServiceStubs.newServiceStubs(options);
        
        logger.info("✅ Connected to Temporal Server successfully!");
        return serviceStubs;
    }

    /**
     * Tạo WorkflowClient - client chính để tương tác với workflows
     * 
     * @param serviceStubs WorkflowServiceStubs instance
     * @return WorkflowClient instance
     */
    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        logger.info("🏗️ Creating Temporal WorkflowClient for namespace: {}", temporalNamespace);
        
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
                .setNamespace(temporalNamespace)
                .build();
        
        WorkflowClient client = WorkflowClient.newInstance(serviceStubs, options);
        
        logger.info("✅ WorkflowClient created successfully!");
        return client;
    }
}
