package com.example.temporal.workflow;

import com.example.temporal.activity.GreetingActivity;
import io.temporal.testing.TestWorkflowRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit Test cho GreetingWorkflow
 * 
 * Test này sử dụng Temporal Testing framework để test workflow
 * mà không cần Temporal Server thật.
 * 
 * Temporal Testing cung cấp:
 * - Mock time control
 * - Deterministic execution
 * - Fast test execution
 * - Activity mocking
 */
@ExtendWith(MockitoExtension.class)
class GreetingWorkflowTest {

    @Mock
    private GreetingActivity mockGreetingActivity;

    @RegisterExtension
    public static final TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setWorkflowTypes(GreetingWorkflowImpl.class)
                    .build();

    @Test
    void testGreetingWorkflow_Success() {
        // Arrange
        String testName = "John Doe";
        String expectedGreeting = "Hello, John Doe!";
        String expectedProcessed = "HELLO, JOHN DOE! - Processed";
        String expectedNotification = "Notification sent to John Doe";

        // Mock activity responses
        when(mockGreetingActivity.sayHello(testName))
                .thenReturn(expectedGreeting);
        when(mockGreetingActivity.processGreeting(expectedGreeting))
                .thenReturn(expectedProcessed);
        when(mockGreetingActivity.sendNotification(any(), any()))
                .thenReturn(expectedNotification);

        // Register mocked activity
        testWorkflowRule.getWorker().registerActivitiesImplementations(mockGreetingActivity);

        // Act
        GreetingWorkflow workflow = testWorkflowRule.getWorkflowClient()
                .newWorkflowStub(GreetingWorkflow.class);
        
        String result = workflow.greetUser(testName);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("WORKFLOW COMPLETED SUCCESSFULLY"));
        assertTrue(result.contains(testName));
        assertTrue(result.contains(expectedGreeting));
    }

    @Test
    void testGreetingWorkflow_WithEmptyName() {
        // Arrange
        String emptyName = "";
        
        when(mockGreetingActivity.sayHello(emptyName))
                .thenReturn("Hello, !");
        when(mockGreetingActivity.processGreeting(any()))
                .thenReturn("HELLO, ! - Processed");
        when(mockGreetingActivity.sendNotification(any(), any()))
                .thenReturn("Notification sent");

        testWorkflowRule.getWorker().registerActivitiesImplementations(mockGreetingActivity);

        // Act
        GreetingWorkflow workflow = testWorkflowRule.getWorkflowClient()
                .newWorkflowStub(GreetingWorkflow.class);
        
        String result = workflow.greetUser(emptyName);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("WORKFLOW COMPLETED SUCCESSFULLY"));
    }

    @Test
    void testGreetingWorkflow_ActivityFailure() {
        // Arrange
        String testName = "John Doe";
        
        // Mock activity to throw exception
        when(mockGreetingActivity.sayHello(testName))
                .thenThrow(new RuntimeException("Activity failed"));

        testWorkflowRule.getWorker().registerActivitiesImplementations(mockGreetingActivity);

        // Act & Assert
        GreetingWorkflow workflow = testWorkflowRule.getWorkflowClient()
                .newWorkflowStub(GreetingWorkflow.class);
        
        // Workflow should propagate the exception
        assertThrows(RuntimeException.class, () -> {
            workflow.greetUser(testName);
        });
    }
}
