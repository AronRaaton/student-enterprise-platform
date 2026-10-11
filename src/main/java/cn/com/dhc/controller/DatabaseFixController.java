package cn.com.dhc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.context.annotation.Profile("legacy-maintenance")
@RestController
@RequestMapping("/api/db-fix")
public class DatabaseFixController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/fix-chat-message-table")
    public ResponseEntity<Map<String, Object>> fixChatMessageTable() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 删除现有表
            jdbcTemplate.execute("DROP TABLE IF EXISTS chat_message");
            
            // 创建新表，确保ID字段为AUTO_INCREMENT
            jdbcTemplate.execute(
                "CREATE TABLE chat_message (" +
                "    id BIGINT NOT NULL AUTO_INCREMENT," +
                "    sender_id VARCHAR(255) NOT NULL," +
                "    sender_name VARCHAR(255) NOT NULL," +
                "    sender_type VARCHAR(50) NOT NULL," +
                "    receiver_id VARCHAR(255) NOT NULL," +
                "    content TEXT," +
                "    timestamp DATETIME," +
                "    is_read TINYINT(1) DEFAULT 0," +
                "    PRIMARY KEY (id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            
            // 添加索引
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_sender_id ON chat_message (sender_id)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_receiver_id ON chat_message (receiver_id)");
            jdbcTemplate.execute("CREATE INDEX idx_chat_message_is_read ON chat_message (is_read)");
            
            // 获取表结构
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SHOW COLUMNS FROM chat_message"
            );
            
            // 测试插入
            int result = jdbcTemplate.update(
                    "INSERT INTO chat_message (sender_id, sender_name, sender_type, receiver_id, content, timestamp, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, NOW(), ?)",
                    "test-sender", "Test Sender", "ENTERPRISE", "test-receiver", "Test message", false
            );
            
            // 获取最后插入的ID
            Long lastId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            
            // 返回结果
            response.put("success", true);
            response.put("message", "表已成功修复");
            response.put("columns", columns);
            response.put("testInsert", result > 0);
            response.put("lastInsertedId", lastId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/check-chat-message-table")
    public ResponseEntity<Map<String, Object>> checkChatMessageTable() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取表结构
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "SHOW COLUMNS FROM chat_message"
            );
            
            // 获取表状态
            List<Map<String, Object>> tableStatus = jdbcTemplate.queryForList(
                    "SHOW TABLE STATUS LIKE 'chat_message'"
            );
            
            // 获取索引
            List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                    "SHOW INDEX FROM chat_message"
            );
            
            // 返回所有信息
            response.put("success", true);
            response.put("columns", columns);
            response.put("tableStatus", tableStatus);
            response.put("indexes", indexes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(response);
        }
    }
}
