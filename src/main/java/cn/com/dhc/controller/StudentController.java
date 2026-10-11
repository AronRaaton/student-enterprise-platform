package cn.com.dhc.controller;

import cn.com.dhc.Entity.Student;
import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.service.StudentService;
import cn.com.dhc.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final TaskService taskService;

    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.findById(studentId));
    }

    @GetMapping("/{studentId}/completed-tasks")
    public ResponseEntity<List<TaskSubmission>> getStudentCompletedTasks(@PathVariable Long studentId) {
        List<TaskSubmission> allSubmissions = taskService.getStudentSubmissions(studentId);
        List<TaskSubmission> completedSubmissions = allSubmissions.stream()
                .filter(submission -> submission.getStatus() == TaskSubmission.SubmissionStatus.COMPLETED)
                .collect(Collectors.toList());
        return ResponseEntity.ok(completedSubmissions);
    }
}
