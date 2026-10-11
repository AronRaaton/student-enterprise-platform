package cn.com.dhc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentUpdateDTO {
    private String studentNumber;
    private String name;
    private String email;
    private String school;
    private String major;
    private Integer enrollmentYear;
}