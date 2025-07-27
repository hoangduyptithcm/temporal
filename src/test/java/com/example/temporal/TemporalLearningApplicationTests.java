package com.example.temporal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic Spring Boot Application Test
 * 
 * Test này đảm bảo Spring Boot application có thể start thành công
 * với tất cả các configuration và dependencies.
 */
@SpringBootTest
class TemporalLearningApplicationTests {

    @Test
    void contextLoads() {
        // Test này sẽ pass nếu Spring context load thành công
        // Bao gồm tất cả các @Component, @Service, @Configuration
    }
}
