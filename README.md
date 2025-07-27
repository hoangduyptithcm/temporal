# 🚀 Temporal Learning Project - Java Spring Boot

## 📋 Mục lục
1. [Giới thiệu về Temporal](#giới-thiệu-về-temporal)
2. [Cài đặt và thiết lập](#cài-đặt-và-thiết-lập)
3. [Khái niệm cơ bản](#khái-niệm-cơ-bản)
4. [Cấu trúc project](#cấu-trúc-project)
5. [Bước 1: Tạo Activity đầu tiên](#bước-1-tạo-activity-đầu-tiên)
6. [Bước 2: Tạo Workflow đầu tiên](#bước-2-tạo-workflow-đầu-tiên)
7. [Bước 3: Tạo Worker](#bước-3-tạo-worker)
8. [Bước 4: Tạo Client](#bước-4-tạo-client)
9. [Bước 5: Tạo REST API](#bước-5-tạo-rest-api)
10. [Chạy ứng dụng](#chạy-ứng-dụng)
11. [Ví dụ nâng cao](#ví-dụ-nâng-cao)

## 🎯 Giới thiệu về Temporal

**Temporal** là một platform để xây dựng các ứng dụng phân tán đáng tin cậy. Nó giúp bạn:

- ✅ Xử lý các workflow phức tạp, dài hạn
- ✅ Tự động retry khi có lỗi
- ✅ Theo dõi trạng thái workflow
- ✅ Xử lý timeout và failure
- ✅ Versioning cho workflow

### 🔑 Khái niệm cơ bản

**Workflow**: Là logic nghiệp vụ chính, định nghĩa các bước cần thực hiện
**Activity**: Là các tác vụ cụ thể (gọi API, xử lý database, gửi email...)
**Worker**: Là service chạy và thực thi workflow/activity
**Client**: Là thành phần khởi tạo và tương tác với workflow

## 🛠️ Cài đặt và thiết lập

### Yêu cầu hệ thống:
- Java 17+
- Maven 3.6+
- Docker (để chạy Temporal Server)

### 1. Cài đặt Temporal Server bằng Docker:

```bash
# Tải và chạy Temporal Server
git clone https://github.com/temporalio/docker-compose.git temporal-docker
cd temporal-docker
docker-compose up -d

# Kiểm tra Temporal Web UI tại: http://localhost:8080
```

### 2. Clone project này:

```bash
git clone <repository-url>
cd temporal-learning
```

### 3. Build project:

```bash
mvn clean install
```

## 📁 Cấu trúc project

```
src/
├── main/
│   ├── java/com/example/temporal/
│   │   ├── TemporalLearningApplication.java    # Main class
│   │   ├── config/
│   │   │   └── TemporalConfig.java             # Cấu hình Temporal
│   │   ├── workflow/
│   │   │   ├── GreetingWorkflow.java           # Interface workflow
│   │   │   └── GreetingWorkflowImpl.java       # Implementation workflow
│   │   ├── activity/
│   │   │   ├── GreetingActivity.java           # Interface activity
│   │   │   └── GreetingActivityImpl.java       # Implementation activity
│   │   ├── worker/
│   │   │   └── TemporalWorker.java             # Worker configuration
│   │   ├── client/
│   │   │   └── TemporalClient.java             # Client service
│   │   └── controller/
│   │       └── WorkflowController.java         # REST API
│   └── resources/
│       └── application.yml                     # Spring configuration
└── test/                                       # Unit tests
```

## 🏁 Bước 1: Tạo Activity đầu tiên

**Activity** là nơi thực hiện các tác vụ cụ thể như gọi API, truy vấn database, gửi email...

### 1.1 Tạo Activity Interface:

```java
// src/main/java/com/example/temporal/activity/GreetingActivity.java
package com.example.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GreetingActivity {

    @ActivityMethod
    String sayHello(String name);

    @ActivityMethod
    String processGreeting(String greeting);
}
```

### 1.2 Tạo Activity Implementation:

```java
// src/main/java/com/example/temporal/activity/GreetingActivityImpl.java
package com.example.temporal.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GreetingActivityImpl implements GreetingActivity {

    private static final Logger logger = LoggerFactory.getLogger(GreetingActivityImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("Executing sayHello activity for: {}", name);

        // Simulate some processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "Hello, " + name + "!";
    }

    @Override
    public String processGreeting(String greeting) {
        logger.info("Processing greeting: {}", greeting);

        // Simulate processing
        String processed = greeting.toUpperCase() + " - Processed at " +
                          java.time.LocalDateTime.now();

        logger.info("Processed result: {}", processed);
        return processed;
    }
}
```

**💡 Giải thích:**
- `@ActivityInterface`: Đánh dấu interface là Temporal Activity
- `@ActivityMethod`: Đánh dấu method có thể được gọi từ Workflow
- Activity có thể thực hiện side effects (gọi API, database...)
- Activity có thể retry tự động khi có lỗi

## 🔄 Bước 2: Tạo Workflow đầu tiên

**Workflow** chứa logic nghiệp vụ và điều phối các Activity.

### 2.1 Tạo Workflow Interface:

```java
// src/main/java/com/example/temporal/workflow/GreetingWorkflow.java
package com.example.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface GreetingWorkflow {

    @WorkflowMethod
    String greetUser(String name);
}
```

### 2.2 Tạo Workflow Implementation:

```java
// src/main/java/com/example/temporal/workflow/GreetingWorkflowImpl.java
package com.example.temporal.workflow;

import com.example.temporal.activity.GreetingActivity;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import java.time.Duration;

public class GreetingWorkflowImpl implements GreetingWorkflow {

    private static final Logger logger = Workflow.getLogger(GreetingWorkflowImpl.class);

    // Cấu hình Activity options
    private final ActivityOptions activityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(2))
            .setRetryOptions(
                io.temporal.common.RetryOptions.newBuilder()
                    .setMaximumAttempts(3)
                    .build()
            )
            .build();

    // Tạo Activity stub
    private final GreetingActivity greetingActivity =
        Workflow.newActivityStub(GreetingActivity.class, activityOptions);

    @Override
    public String greetUser(String name) {
        logger.info("Starting greeting workflow for: {}", name);

        // Bước 1: Tạo lời chào
        String greeting = greetingActivity.sayHello(name);
        logger.info("Received greeting: {}", greeting);

        // Bước 2: Xử lý lời chào
        String processedGreeting = greetingActivity.processGreeting(greeting);
        logger.info("Processed greeting: {}", processedGreeting);

        // Bước 3: Trả về kết quả
        String result = "Workflow completed! Final result: " + processedGreeting;
        logger.info("Workflow finished: {}", result);

        return result;
    }
}
```

**💡 Giải thích:**
- `@WorkflowInterface`: Đánh dấu interface là Temporal Workflow
- `@WorkflowMethod`: Method chính của workflow
- `ActivityOptions`: Cấu hình timeout và retry cho Activity
- `Workflow.newActivityStub()`: Tạo proxy để gọi Activity
- Workflow KHÔNG được thực hiện side effects trực tiếp

## ⚙️ Bước 3: Tạo Worker

**Worker** là service chạy và thực thi các Workflow và Activity.

```java
// src/main/java/com/example/temporal/worker/TemporalWorker.java
package com.example.temporal.worker;

import com.example.temporal.activity.GreetingActivityImpl;
import com.example.temporal.workflow.GreetingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class TemporalWorker {

    private static final Logger logger = LoggerFactory.getLogger(TemporalWorker.class);
    private static final String TASK_QUEUE = "greeting-task-queue";

    @Autowired
    private WorkflowClient workflowClient;

    @Autowired
    private GreetingActivityImpl greetingActivity;

    private WorkerFactory workerFactory;

    @PostConstruct
    public void startWorker() {
        logger.info("Starting Temporal Worker...");

        // Tạo WorkerFactory
        workerFactory = WorkerFactory.newInstance(workflowClient);

        // Tạo Worker cho task queue
        Worker worker = workerFactory.newWorker(TASK_QUEUE);

        // Đăng ký Workflow implementations
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);

        // Đăng ký Activity implementations
        worker.registerActivitiesImplementations(greetingActivity);

        // Bắt đầu worker
        workerFactory.start();

        logger.info("Temporal Worker started successfully!");
    }

    @PreDestroy
    public void stopWorker() {
        if (workerFactory != null) {
            logger.info("Stopping Temporal Worker...");
            workerFactory.shutdown();
        }
    }

    public static String getTaskQueue() {
        return TASK_QUEUE;
    }
}
```

**💡 Giải thích:**
- `WorkerFactory`: Tạo và quản lý các Worker
- `worker.registerWorkflowImplementationTypes()`: Đăng ký Workflow implementations
- `worker.registerActivitiesImplementations()`: Đăng ký Activity implementations
- `TASK_QUEUE`: Tên queue để Worker lắng nghe các task

## 📞 Bước 4: Tạo Client

**Client** dùng để khởi tạo và tương tác với Workflow.

```java
// src/main/java/com/example/temporal/client/TemporalClient.java
package com.example.temporal.client;

import com.example.temporal.workflow.GreetingWorkflow;
import com.example.temporal.worker.TemporalWorker;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class TemporalClient {

    private static final Logger logger = LoggerFactory.getLogger(TemporalClient.class);

    @Autowired
    private WorkflowClient workflowClient;

    public String startGreetingWorkflow(String name) {
        logger.info("Starting greeting workflow for: {}", name);

        // Tạo workflow ID duy nhất
        String workflowId = "greeting-workflow-" + UUID.randomUUID().toString();

        // Cấu hình workflow options
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(TemporalWorker.getTaskQueue())
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .build();

        // Tạo workflow stub
        GreetingWorkflow workflow = workflowClient.newWorkflowStub(
                GreetingWorkflow.class, options);

        // Bắt đầu workflow (async)
        WorkflowClient.start(workflow::greetUser, name);

        logger.info("Workflow started with ID: {}", workflowId);
        return workflowId;
    }

    public String startGreetingWorkflowSync(String name) {
        logger.info("Starting greeting workflow synchronously for: {}", name);

        // Tạo workflow ID duy nhất
        String workflowId = "greeting-workflow-sync-" + UUID.randomUUID().toString();

        // Cấu hình workflow options
        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(TemporalWorker.getTaskQueue())
                .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                .build();

        // Tạo workflow stub
        GreetingWorkflow workflow = workflowClient.newWorkflowStub(
                GreetingWorkflow.class, options);

        // Chạy workflow và đợi kết quả (sync)
        String result = workflow.greetUser(name);

        logger.info("Workflow completed with result: {}", result);
        return result;
    }

    public String getWorkflowResult(String workflowId) {
        logger.info("Getting result for workflow: {}", workflowId);

        // Tạo workflow stub từ workflow ID
        WorkflowStub workflowStub = workflowClient.newUntypedWorkflowStub(workflowId);

        // Lấy kết quả
        String result = workflowStub.getResult(String.class);

        logger.info("Workflow result: {}", result);
        return result;
    }
}
```

**💡 Giải thích:**
- `WorkflowOptions`: Cấu hình cho workflow (ID, task queue, timeout...)
- `WorkflowClient.start()`: Chạy workflow bất đồng bộ
- `workflow.greetUser()`: Chạy workflow đồng bộ và đợi kết quả
- `workflowStub.getResult()`: Lấy kết quả từ workflow đã chạy

## 🌐 Bước 5: Tạo REST API

Tạo REST API để trigger workflows từ HTTP requests.

```java
// src/main/java/com/example/temporal/controller/WorkflowController.java
package com.example.temporal.controller;

import com.example.temporal.client.TemporalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);

    @Autowired
    private TemporalClient temporalClient;

    @PostMapping("/greeting/async")
    public ResponseEntity<Map<String, String>> startGreetingWorkflowAsync(
            @RequestBody Map<String, String> request) {

        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        logger.info("Received request to start async greeting workflow for: {}", name);

        try {
            String workflowId = temporalClient.startGreetingWorkflow(name);

            Map<String, String> response = new HashMap<>();
            response.put("workflowId", workflowId);
            response.put("status", "started");
            response.put("message", "Workflow started successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error starting workflow", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to start workflow: " + e.getMessage()));
        }
    }

    @PostMapping("/greeting/sync")
    public ResponseEntity<Map<String, String>> startGreetingWorkflowSync(
            @RequestBody Map<String, String> request) {

        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Name is required"));
        }

        logger.info("Received request to start sync greeting workflow for: {}", name);

        try {
            String result = temporalClient.startGreetingWorkflowSync(name);

            Map<String, String> response = new HashMap<>();
            response.put("result", result);
            response.put("status", "completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error executing workflow", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to execute workflow: " + e.getMessage()));
        }
    }

    @GetMapping("/result/{workflowId}")
    public ResponseEntity<Map<String, String>> getWorkflowResult(
            @PathVariable String workflowId) {

        logger.info("Received request to get result for workflow: {}", workflowId);

        try {
            String result = temporalClient.getWorkflowResult(workflowId);

            Map<String, String> response = new HashMap<>();
            response.put("workflowId", workflowId);
            response.put("result", result);
            response.put("status", "completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting workflow result", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get workflow result: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Temporal Learning API",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
```

**💡 Giải thích:**
- `@RestController`: Đánh dấu class là REST controller
- `@RequestMapping`: Định nghĩa base path cho tất cả endpoints
- `@PostMapping/@GetMapping`: Định nghĩa HTTP methods
- Error handling với try-catch và proper HTTP status codes

## 🚀 Chạy ứng dụng

### Bước 1: Khởi động Temporal Server

```bash
# Option 1: Sử dụng Docker Compose (Recommended)
git clone https://github.com/temporalio/docker-compose.git temporal-docker
cd temporal-docker
docker-compose up -d

# Option 2: Sử dụng Temporal CLI (nếu đã cài đặt)
temporal server start-dev
```

**Kiểm tra Temporal Server:**
- Temporal Web UI: http://localhost:8080
- Temporal Server: localhost:7233

### Bước 2: Build và chạy ứng dụng

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run

# Hoặc chạy từ JAR file
java -jar target/temporal-learning-1.0.0.jar
```

**Kiểm tra ứng dụng:**
- API Health Check: http://localhost:8081/api/workflows/health
- Application logs sẽ hiển thị Worker status

### Bước 3: Test API endpoints

#### 3.1 Test Async Workflow:

```bash
# Khởi tạo workflow bất đồng bộ
curl -X POST http://localhost:8081/api/workflows/greeting/async \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe"}'

# Response:
{
  "success": true,
  "workflowId": "greeting-workflow-12345-67890",
  "status": "STARTED",
  "message": "Workflow started successfully",
  "name": "John Doe",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 3.2 Lấy kết quả workflow:

```bash
# Sử dụng workflowId từ response trên
curl http://localhost:8081/api/workflows/result/greeting-workflow-12345-67890

# Response:
{
  "success": true,
  "workflowId": "greeting-workflow-12345-67890",
  "result": "🎉 WORKFLOW COMPLETED SUCCESSFULLY! 🎉\n\n👤 User: John Doe\n...",
  "status": "COMPLETED",
  "timestamp": "2024-01-15T10:32:00"
}
```

#### 3.3 Test Sync Workflow:

```bash
# Khởi tạo workflow đồng bộ (đợi kết quả)
curl -X POST http://localhost:8081/api/workflows/greeting/sync \
  -H "Content-Type: application/json" \
  -d '{"name": "Jane Smith"}'

# Response sẽ chứa kết quả ngay lập tức
{
  "success": true,
  "result": "🎉 WORKFLOW COMPLETED SUCCESSFULLY! 🎉\n\n👤 User: Jane Smith\n...",
  "status": "COMPLETED",
  "name": "Jane Smith",
  "timestamp": "2024-01-15T10:35:00"
}
```

### Bước 4: Theo dõi workflows trên Temporal Web UI

1. Mở http://localhost:8080
2. Xem danh sách workflows đang chạy
3. Click vào workflow để xem chi tiết:
   - Workflow execution history
   - Activity executions
   - Retry attempts
   - Timeline
   - Input/Output data

## 🎓 Ví dụ nâng cao

### 1. Workflow với Signal và Query

Temporal hỗ trợ Signal (gửi data đến workflow đang chạy) và Query (lấy state từ workflow).

### 2. Workflow với Timer và Sleep

```java
// Trong workflow implementation
@Override
public String processWithDelay(String input) {
    // Sleep 30 giây
    Workflow.sleep(Duration.ofSeconds(30));

    // Hoặc sử dụng timer
    Workflow.newTimer(Duration.ofMinutes(5)).get();

    return "Processed after delay: " + input;
}
```

### 3. Workflow với Conditional Logic

```java
@Override
public String conditionalWorkflow(String input, boolean shouldProcess) {
    String greeting = greetingActivity.sayHello(input);

    if (shouldProcess) {
        // Chỉ xử lý nếu condition = true
        return greetingActivity.processGreeting(greeting);
    }

    return greeting;
}
```

### 4. Workflow với Parallel Activities

```java
@Override
public String parallelProcessing(String name) {
    // Chạy 2 activities song song
    Promise<String> greeting = Async.function(greetingActivity::sayHello, name);
    Promise<String> notification = Async.function(greetingActivity::sendNotification,
                                                  "Welcome!", name);

    // Đợi cả 2 hoàn thành
    String greetingResult = greeting.get();
    String notificationResult = notification.get();

    return greetingResult + "\n" + notificationResult;
}
```

### 5. Error Handling và Retry

```java
// Trong Activity implementation
@Override
public String unreliableActivity(String input) {
    // Mô phỏng lỗi ngẫu nhiên
    if (Math.random() < 0.3) {
        throw new RuntimeException("Random failure occurred");
    }

    return "Success: " + input;
}
```

## 🔧 Troubleshooting

### Lỗi thường gặp:

#### 1. "Connection refused" khi kết nối Temporal Server
```
Caused by: io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
```
**Giải pháp:**
- Kiểm tra Temporal Server đã chạy: `docker ps`
- Kiểm tra port 7233 có available không
- Restart Temporal Server: `docker-compose restart`

#### 2. "Task Queue not found"
```
No workers are polling task queue: greeting-task-queue
```
**Giải pháp:**
- Kiểm tra Worker đã start thành công
- Kiểm tra task queue name khớp giữa Client và Worker
- Xem logs để đảm bảo Worker đã register

#### 3. "Workflow execution timeout"
```
WorkflowExecutionTimeout
```
**Giải pháp:**
- Tăng `WorkflowExecutionTimeout` trong WorkflowOptions
- Kiểm tra Activity có bị hang không
- Optimize Activity performance

#### 4. Activity retry hết attempts
```
Activity task failed after maximum retry attempts
```
**Giải pháp:**
- Tăng `MaximumAttempts` trong RetryOptions
- Fix root cause của lỗi trong Activity
- Implement proper error handling

### Debug Tips:

#### 1. Enable debug logging:
```yaml
# application.yml
logging:
  level:
    io.temporal: DEBUG
    com.example.temporal: DEBUG
```

#### 2. Sử dụng Temporal Web UI:
- Xem workflow execution history
- Check activity failures
- Monitor retry attempts
- View input/output data

#### 3. Add more logging:
```java
// Trong Activity
logger.info("Processing input: {}", input);
logger.debug("Intermediate result: {}", intermediateResult);
```

## 📚 Tài liệu tham khảo

### Official Documentation:
- [Temporal Documentation](https://docs.temporal.io/)
- [Java SDK Guide](https://docs.temporal.io/dev-guide/java)
- [Spring Boot Integration](https://docs.temporal.io/dev-guide/java/foundations#spring-boot)

### Best Practices:
- [Workflow Design Patterns](https://docs.temporal.io/dev-guide/java/foundations#workflow-design-patterns)
- [Activity Best Practices](https://docs.temporal.io/dev-guide/java/foundations#activity-best-practices)
- [Error Handling](https://docs.temporal.io/dev-guide/java/foundations#error-handling)

### Community:
- [Temporal Community Forum](https://community.temporal.io/)
- [GitHub Repository](https://github.com/temporalio/temporal)
- [Discord Server](https://temporal.io/discord)

## 🎯 Bài tập thực hành

### Bài tập 1: Tạo Order Processing Workflow
Tạo workflow xử lý đơn hàng với các bước:
1. Validate order
2. Check inventory
3. Process payment
4. Ship order
5. Send confirmation email

### Bài tập 2: Implement Saga Pattern
Tạo workflow với compensation logic khi có lỗi xảy ra.

### Bài tập 3: Add Signal và Query
Thêm khả năng:
- Signal để cancel workflow
- Query để lấy progress status

### Bài tập 4: Monitoring và Metrics
Thêm metrics và monitoring cho workflows.

---

🎉 **Chúc mừng!** Bạn đã hoàn thành việc học Temporal cơ bản với Java Spring Boot!

Hãy thực hành với các ví dụ và bài tập để nắm vững kiến thức. Temporal là một công cụ mạnh mẽ cho việc xây dựng các ứng dụng phân tán đáng tin cậy.
```
temporal
