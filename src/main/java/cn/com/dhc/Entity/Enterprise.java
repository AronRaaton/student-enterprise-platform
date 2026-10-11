package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enterpriseId;

    @Column(unique = true)
    private String email;
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String name;
    private Double rating = 0.0;

    @Column(name = "register_date")
    private LocalDateTime registerDate = LocalDateTime.now();

    @OneToMany(mappedBy = "enterprise")
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>();

    private String type = "";
    private String location = "";
    private Integer employeeCount = 0;
    private Integer foundingYear = LocalDateTime.now().getYear();
    private String description = "";
    private String website = "";
}