package cn.com.dhc.controller;

import cn.com.dhc.Entity.*;
import cn.com.dhc.dto.EnterpriseUpdateDTO;
import cn.com.dhc.dto.StudentUpdateDTO;
import cn.com.dhc.service.EnterpriseService;
import cn.com.dhc.service.NoticeService;
import cn.com.dhc.service.StudentService;
import cn.com.dhc.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final StudentService studentService;
    private final TaskService taskService;
    private final NoticeService noticeService;
    private final EnterpriseService enterpriseService;

    // 获取学生列表
    @GetMapping("/students")
    public ResponseEntity<Page<Student>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(studentService.findAll(PageRequest.of(page, size)));
    }

    // 获取企业列表
    @GetMapping("/enterprises")
    public ResponseEntity<Page<Enterprise>> getEnterprises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(enterpriseService.findAll(PageRequest.of(page, size)));
    }

    // 获取任务列表
    @GetMapping("/tasks")
    public ResponseEntity<Page<Task>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(taskService.findAll(PageRequest.of(page, size)));
    }


    // 修改学生信息
    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody StudentUpdateDTO updateData
    ) {
        try {
            Student updatedStudent = studentService.updateStudent(id, updateData);
            return ResponseEntity.ok(updatedStudent);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 修改企业信息
    @PutMapping("/enterprises/{id}")
    public ResponseEntity<?> updateEnterprise(
            @PathVariable Long id,
            @RequestBody EnterpriseUpdateDTO updateData
    ) {
        try {
            Enterprise updatedEnterprise = enterpriseService.updateEnterprise(id, updateData);
            return ResponseEntity.ok(updatedEnterprise);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取学生关注的企业数量
    @GetMapping("/students/{studentId}/followed-enterprises-count")
    public ResponseEntity<Integer> getStudentFollowedEnterprisesCount(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getFollowedEnterprisesCount(studentId));
    }
}
