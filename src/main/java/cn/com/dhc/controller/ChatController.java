package cn.com.dhc.controller;

import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.dto.ChatMessage;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.service.ChatService;
import cn.com.dhc.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final TaskService taskService;
    private final StudentRepository studentRepository;

    // 处理WebSocket发送消息
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // 保存消息
        ChatMessage savedMessage = chatService.saveMessage(chatMessage);

        // 发送给接收者
        messagingTemplate.convertAndSendToUser(
                String.valueOf(chatMessage.getReceiverId()),
                "/queue/messages",
                savedMessage
        );
    }

    // 处理HTTP发送消息
    @PostMapping("/send")
    public ResponseEntity<?> sendMessageHttp(@RequestBody ChatMessage chatMessage) {
        try {
            System.out.println("收到HTTP发送消息请求: " + chatMessage);

            // 验证消息字段
            if (chatMessage.getSenderId() == null || chatMessage.getReceiverId() == null || chatMessage.getContent() == null) {
                System.err.println("消息字段不完整: " + chatMessage);
                Map<String, String> error = new HashMap<>();
                error.put("error", "消息字段不完整");
                return ResponseEntity.badRequest().body(error);
            }

            // 处理前端发送的临时ID
//            if (chatMessage.getId() != null ) {
//                System.out.println("收到前端临时ID: " + chatMessage.getId() + ", 将在保存后替换为数据库ID");
//            }

            // 保存消息
            ChatMessage savedMessage = chatService.saveMessage(chatMessage);
            System.out.println("消息保存成功，返回消息: " + savedMessage);

            // 尝试通过WebSocket发送给接收者
            try {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(chatMessage.getReceiverId()),
                        "/queue/messages",
                        savedMessage
                );
                System.out.println("消息已通过WebSocket发送给接收者: " + chatMessage.getReceiverId());
            } catch (Exception e) {
                // 忽略WebSocket发送失败的异常
                System.out.println("WebSocket发送失败: " + e.getMessage());
            }

            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            System.err.println("发送消息失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "发送消息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取两个用户之间的历史消息
    @GetMapping("/history/{userId1}/{userId2}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String userId1,
            @PathVariable String userId2) {
        return ResponseEntity.ok(chatService.getMessagesBetweenUsers(userId1, userId2));
    }

    // 获取用户的未读消息数量
    @GetMapping("/unread/{userId}")
    public ResponseEntity<Map<String, Object>> getUnreadMessageCount(
            @PathVariable String userId) {
        int count = chatService.getUnreadMessageCount(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    // 将消息标记为已读 - 同时支持GET和POST方法
    @RequestMapping(value = "/mark-read/{userId}/{senderId}", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable String userId,
            @PathVariable String senderId) {
        System.out.println("收到标记消息为已读的请求: userId=" + userId + ", senderId=" + senderId);
        chatService.markMessagesAsRead(userId, senderId);
        return ResponseEntity.ok().build();
    }

    // 获取学生的任务情况
    @GetMapping("/student-tasks/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentTaskInfo(@PathVariable Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();

        // 获取学生的提交记录
        List<TaskSubmission> submissions = taskService.getStudentSubmissions(studentId);

        // 获取待审核的任务
        List<TaskSubmission> pendingSubmissions = submissions.stream()
                .filter(s -> s.getStatus() == TaskSubmission.SubmissionStatus.SUBMITTED)
                .collect(Collectors.toList());

        // 获取已完成的任务
        List<TaskSubmission> completedSubmissions = submissions.stream()
                .filter(s -> s.getStatus() == TaskSubmission.SubmissionStatus.COMPLETED)
                .collect(Collectors.toList());

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("student", student);
        response.put("pendingSubmissions", pendingSubmissions);
        response.put("completedSubmissions", completedSubmissions);

        return ResponseEntity.ok(response);
    }
}
