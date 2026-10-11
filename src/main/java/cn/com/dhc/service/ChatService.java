package cn.com.dhc.service;

import cn.com.dhc.Entity.ChatMessage;
import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.mapper.ChatMessageRepository;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 保存消息
    @Transactional
    public cn.com.dhc.dto.ChatMessage saveMessage(cn.com.dhc.dto.ChatMessage messageDto) {
        try {
            System.out.println("开始保存消息: " + messageDto);

            // 创建实体对象
            ChatMessage entity = new ChatMessage();

            // 设置字段
            entity.setSenderId(messageDto.getSenderId());
            entity.setSenderName(messageDto.getSenderName());
            entity.setSenderType(messageDto.getSenderType());
            entity.setReceiverId(messageDto.getReceiverId());
            entity.setContent(messageDto.getContent());

            // 处理时间戳
            try {
                if (messageDto.getTimestamp() != null) {
                    // 如果是字符串类型，尝试解析
                    if (messageDto.getTimestamp() instanceof String) {
                        String timestampStr = (String) messageDto.getTimestamp();
                        System.out.println("收到字符串时间戳: " + timestampStr);
                        try {
                            // 尝试解析字符串时间格式，如 "2023-04-29 12:34:56"
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime dateTime = LocalDateTime.parse(timestampStr, formatter);
                            entity.setTimestamp(dateTime);
                            System.out.println("解析字符串时间戳成功: " + dateTime);
                        } catch (Exception e) {
                            System.err.println("解析字符串时间戳失败: " + e.getMessage());
                            entity.setTimestamp(LocalDateTime.now());
                        }
                    } else if (messageDto.getTimestamp() instanceof LocalDateTime) {
                        // 如果是LocalDateTime类型，直接使用
                        entity.setTimestamp((LocalDateTime) messageDto.getTimestamp());
                        System.out.println("使用消息中的LocalDateTime时间戳: " + messageDto.getTimestamp());
                    } else {
                        // 如果是其他类型，使用当前时间
                        System.out.println("时间戳类型不支持: " + messageDto.getTimestamp().getClass().getName());
                        entity.setTimestamp(LocalDateTime.now());
                    }
                } else {
                    entity.setTimestamp(LocalDateTime.now());
                    System.out.println("使用当前时间作为时间戳: " + entity.getTimestamp());
                }
            } catch (Exception e) {
                System.err.println("处理时间戳失败: " + e.getMessage());
                e.printStackTrace();
                entity.setTimestamp(LocalDateTime.now());
                System.out.println("因处理失败，使用当前时间作为时间戳: " + entity.getTimestamp());
            }

            // 设置已读状态
            // 新消息始终应该是未读状态，除非是发送给自己的消息
            boolean isReadValue = false;

            // 如果发送者和接收者是同一个人，则标记为已读
            if (messageDto.getSenderId().equals(messageDto.getReceiverId())) {
                isReadValue = true;
            }

            entity.setIsRead(isReadValue);
            System.out.println("设置消息的已读状态为: " + isReadValue);

            // 使用JdbcTemplate直接插入数据，避免JPA/Hibernate的问题
            System.out.println("准备使用JdbcTemplate直接插入数据");

            // 准备时间戳
            LocalDateTime timestamp = entity.getTimestamp();
            if (timestamp == null) {
                timestamp = LocalDateTime.now();
            }

            // 执行插入
            int result = jdbcTemplate.update(
                "INSERT INTO chat_message (sender_id, sender_name, sender_type, receiver_id, content, timestamp, is_read) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                entity.getSenderId(),
                entity.getSenderName(),
                entity.getSenderType(),
                entity.getReceiverId(),
                entity.getContent(),
                timestamp,
                entity.isRead() ? 1 : 0 // 使用更新后的isRead值
            );

            System.out.println("插入数据库的is_read值: " + (entity.isRead() ? 1 : 0));

            // 获取最后插入的ID
            Long lastId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            System.out.println("消息已保存到数据库，影响行数: " + result + ", ID: " + lastId);

            // 创建一个新的实体对象，包含生成的ID
            ChatMessage savedEntity = new ChatMessage();
            savedEntity.setId(Math.toIntExact(lastId));
            savedEntity.setSenderId(entity.getSenderId());
            savedEntity.setSenderName(entity.getSenderName());
            savedEntity.setSenderType(entity.getSenderType());
            savedEntity.setReceiverId(entity.getReceiverId());
            savedEntity.setContent(entity.getContent());
            savedEntity.setTimestamp(timestamp);
            savedEntity.setIsRead(entity.isRead());

            // 转换回 DTO
            cn.com.dhc.dto.ChatMessage savedDto = new cn.com.dhc.dto.ChatMessage();
            savedDto.setId(String.valueOf(savedEntity.getId()));
            savedDto.setSenderId(savedEntity.getSenderId());
            savedDto.setSenderName(savedEntity.getSenderName());
            savedDto.setSenderType(savedEntity.getSenderType());
            savedDto.setReceiverId(savedEntity.getReceiverId());
            savedDto.setContent(savedEntity.getContent());

            // 将 LocalDateTime 转换为字符串格式，以便前端处理
            if (savedEntity.getTimestamp() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                savedDto.setTimestamp(savedEntity.getTimestamp().format(formatter));
            } else {
                savedDto.setTimestamp(null);
            }

            savedDto.setRead(savedEntity.isRead());

            return savedDto;
        } catch (Exception e) {
            System.err.println("保存消息失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("保存消息失败: " + e.getMessage(), e);
        }
    }

    // 获取两个用户之间的所有消息
    @Transactional(readOnly = true)
    public List<cn.com.dhc.dto.ChatMessage> getMessagesBetweenUsers(String userId1, String userId2) {
        System.out.println("获取用户 " + userId1 + " 和 " + userId2 + " 之间的消息");

        try {
            // 使用JdbcTemplate直接查询数据
            String sql = ""
                + "SELECT * FROM chat_message "
                + "WHERE (sender_id = ? AND receiver_id = ?) "
                + "   OR (sender_id = ? AND receiver_id = ?) "
                + "ORDER BY timestamp ASC";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId1, userId2, userId2, userId1);

            // 转换为DTO列表
            List<cn.com.dhc.dto.ChatMessage> messages = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (Map<String, Object> row : rows) {
                cn.com.dhc.dto.ChatMessage dto = new cn.com.dhc.dto.ChatMessage();
                dto.setId(String.valueOf(row.get("id")));
                dto.setSenderId((String) row.get("sender_id"));
                dto.setSenderName((String) row.get("sender_name"));
                dto.setSenderType((String) row.get("sender_type"));
                dto.setReceiverId((String) row.get("receiver_id"));
                dto.setContent((String) row.get("content"));

                // 处理时间戳
                if (row.get("timestamp") != null) {
                    if (row.get("timestamp") instanceof java.sql.Timestamp) {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) row.get("timestamp");
                        LocalDateTime dateTime = timestamp.toLocalDateTime();
                        dto.setTimestamp(dateTime.format(formatter));
                    } else {
                        dto.setTimestamp(row.get("timestamp").toString());
                    }
                } else {
                    dto.setTimestamp(null);
                }

                // 处理已读状态
                if (row.get("is_read") != null) {
                    if (row.get("is_read") instanceof Boolean) {
                        dto.setRead((Boolean) row.get("is_read"));
                    } else if (row.get("is_read") instanceof Number) {
                        dto.setRead(((Number) row.get("is_read")).intValue() > 0);
                    } else {
                        dto.setRead(Boolean.parseBoolean(row.get("is_read").toString()));
                    }
                } else {
                    dto.setRead(false);
                }

                messages.add(dto);
            }

            System.out.println("找到 " + messages.size() + " 条消息");
            return messages;
        } catch (Exception e) {
            System.err.println("获取消息失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 获取用户的未读消息数量
    @Transactional(readOnly = true)
    public int getUnreadMessageCount(String userId) {
        try {
            // 使用JdbcTemplate直接查询数据
            String sql = "SELECT COUNT(*) FROM chat_message WHERE receiver_id = ? AND is_read = 0";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);

            // 如果查询结果为null，返回0
            int unreadCount = (count != null) ? count : 0;
            System.out.println("用户 " + userId + " 的未读消息数量: " + unreadCount);
            return unreadCount;
        } catch (Exception e) {
            System.err.println("获取未读消息数量失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // 将消息标记为已读
    @Transactional
    public void markMessagesAsRead(String userId, String senderId) {
        try {
            // 使用JdbcTemplate直接更新数据
            String sql = "UPDATE chat_message SET is_read = 1 WHERE receiver_id = ? AND sender_id = ? AND is_read = 0";
            int updatedRows = jdbcTemplate.update(sql, userId, senderId);

            System.out.println("已将用户 " + userId + " 收到的来自 " + senderId + " 的消息标记为已读，影响行数: " + updatedRows);
        } catch (Exception e) {
            System.err.println("标记消息为已读失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("标记消息为已读失败: " + e.getMessage(), e);
        }
    }

    // 创建聊天ID
    private String createChatId(String userId1, String userId2) {
        // 确保ID顺序一致，无论发送方和接收方如何
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    // 获取用户名
    public String getUserName(String userId, String userType) {
        if ("STUDENT".equals(userType)) {
            Optional<Student> student = studentRepository.findById(Long.parseLong(userId));
            return student.map(Student::getName).orElse("未知学生");
        } else if ("ENTERPRISE".equals(userType)) {
            Optional<Enterprise> enterprise = enterpriseRepository.findById(Long.parseLong(userId));
            return enterprise.map(Enterprise::getName).orElse("未知企业");
        }
        return "未知用户";
    }
}
