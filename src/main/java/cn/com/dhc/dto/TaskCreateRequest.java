package cn.com.dhc.dto;


import javax.validation.constraints.NotBlank;

public class TaskCreateRequest {
    @NotBlank
    private String summary;

    @NotBlank
    private String content;

    @NotBlank
    private String techStack;

    @NotBlank
    private String requirements;

    private Integer difficulty;

    // 必须添加getter/setter
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
}