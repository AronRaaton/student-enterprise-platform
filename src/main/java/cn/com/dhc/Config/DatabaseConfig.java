package cn.com.dhc.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;

@org.springframework.context.annotation.Profile("legacy-maintenance")
@Configuration
public class DatabaseConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void setMaxAllowedPacket() {
        try {
            // 检查当前的maxAllowedPacket值
            Integer currentValue = jdbcTemplate.queryForObject(
                    "SELECT @@max_allowed_packet", Integer.class);
            
            System.out.println("当前max_allowed_packet值: " + currentValue);
            
            // 如果当前值小于64MB (67108864 bytes)，尝试设置为64MB
            if (currentValue != null && currentValue < 67108864) {
                System.out.println("尝试设置max_allowed_packet为64MB (67108864 bytes)");
                jdbcTemplate.execute("SET GLOBAL max_allowed_packet = 67108864");
                
                // 验证设置是否成功
                Integer newValue = jdbcTemplate.queryForObject(
                        "SELECT @@max_allowed_packet", Integer.class);
                System.out.println("设置后max_allowed_packet值: " + newValue);
            }
        } catch (Exception e) {
            System.err.println("设置max_allowed_packet失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
