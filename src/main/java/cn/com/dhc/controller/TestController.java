package cn.com.dhc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.context.annotation.Profile("legacy-maintenance")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Server is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();

        // 创建一些测试任务
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> task = new HashMap<>();
            task.put("taskId", i);
            task.put("summary", "测试任务 " + i);
            task.put("content", "这是测试任务 " + i + " 的内容");
            task.put("techStack", "Java, Spring Boot");
            task.put("requirements", "完成基本功能");
            task.put("difficulty", 3);
            tasks.add(task);
        }

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/completed-tasks")
    public ResponseEntity<List<Map<String, Object>>> getCompletedTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();

        // 创建一些测试已完成任务
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> task = new HashMap<>();
            task.put("submissionId", i);
            task.put("status", "COMPLETED");
            task.put("submissionContent", "已完成任务 " + i + " 的提交内容");
            task.put("submissionTime", "2023-04-28T10:00:00");
            Map<String, Object> taskDetails = new HashMap<>();
            taskDetails.put("taskId", i);
            taskDetails.put("summary", "测试任务 " + i);
            taskDetails.put("content", "这是测试任务 " + i + " 的内容");
            taskDetails.put("techStack", "Java, Spring Boot");
            taskDetails.put("requirements", "完成基本功能");
            taskDetails.put("difficulty", 3);
            task.put("task", taskDetails);
            tasks.add(task);
        }

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/notices")
    public ResponseEntity<List<Map<String, Object>>> getNotices() {
        List<Map<String, Object>> notices = new ArrayList<>();

        // 创建一些测试公告
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> notice = new HashMap<>();
            notice.put("noticeId", i);
            notice.put("title", "测试公告 " + i);
            notice.put("content", "这是测试公告 " + i + " 的内容");
            notice.put("createdTime", "2023-04-28T10:00:00");
            notice.put("status", "APPROVED");
            notices.add(notice);
        }

        return ResponseEntity.ok(notices);
    }
}
