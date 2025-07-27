package com.example.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Greeting Activity Interface
 * 
 * Activity là nơi thực hiện các tác vụ cụ thể như:
 * - Gọi API bên ngoài
 * - Truy vấn database
 * - Gửi email
 * - Xử lý file
 * - Các side effects khác
 * 
 * Activity có thể retry tự động khi có lỗi và có thể
 * được timeout nếu chạy quá lâu.
 */
@ActivityInterface
public interface GreetingActivity {
    
    /**
     * Tạo lời chào cho người dùng
     * 
     * @param name Tên người dùng
     * @return Lời chào
     */
    @ActivityMethod
    String sayHello(String name);
    
    /**
     * Xử lý và format lời chào
     * 
     * @param greeting Lời chào gốc
     * @return Lời chào đã được xử lý
     */
    @ActivityMethod
    String processGreeting(String greeting);
    
    /**
     * Gửi thông báo (mô phỏng gửi email/SMS)
     * 
     * @param message Nội dung thông báo
     * @param recipient Người nhận
     * @return Trạng thái gửi
     */
    @ActivityMethod
    String sendNotification(String message, String recipient);
}
