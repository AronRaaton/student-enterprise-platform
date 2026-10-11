package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_like")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private ForumPost post;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "user_type", nullable = false)
    private String userType;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Enum for user type
    public enum UserType {
        STUDENT("学生"),
        ENTERPRISE("企业");
        
        private final String displayName;
        
        UserType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
