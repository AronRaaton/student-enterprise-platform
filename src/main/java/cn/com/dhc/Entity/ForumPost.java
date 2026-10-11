package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    @Column(name = "author_type", nullable = false)
    private String authorType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "category")
    private String category;

    @Column(name = "subcategory")
    private String subcategory;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumLike> likes = new ArrayList<>();

    // Enum for post status
    public enum PostStatus {
        ACTIVE("活跃"),
        DELETED("已删除");

        private final String displayName;

        PostStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum for author type
    public enum AuthorType {
        STUDENT("学生"),
        ENTERPRISE("企业");

        private final String displayName;

        AuthorType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum for post category
    public enum Category {
        EMPLOYMENT("就业相关"),
        EVENTS("最新事件"),
        COMMUNITY("互动社区"),
        RESOURCES("资源分享");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum for employment subcategory
    public enum EmploymentSubcategory {
        INTERNSHIP("实习经验分享"),
        JOB("最新岗位招聘"),
        COOPERATION("校企合作意向征集");

        private final String displayName;

        EmploymentSubcategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum for events subcategory
    public enum EventsSubcategory {
        TECHNOLOGY("前沿技术"),
        NEWS("最新新闻"),
        ACHIEVEMENTS("产学研成果展示");

        private final String displayName;

        EventsSubcategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum for community subcategory
    public enum CommunitySubcategory {
        QA("校企问答专区"),
        DEBATE("行业趋势辩论");

        private final String displayName;

        CommunitySubcategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
