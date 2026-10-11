package cn.com.dhc.controller;

import cn.com.dhc.Entity.*;
import cn.com.dhc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EnterpriseEventController {

    private final EnterpriseEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final StudentRepository studentRepository;

    // 获取所有企业活动（分页）
    @GetMapping
    public ResponseEntity<Page<EnterpriseEvent>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        return ResponseEntity.ok(eventRepository.findAll(pageable));
    }

    // 获取特定活动详情
    @GetMapping("/{eventId}")
    public ResponseEntity<EnterpriseEvent> getEvent(@PathVariable Long eventId) {
        return eventRepository.findById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 企业创建活动
    @PostMapping
    public ResponseEntity<EnterpriseEvent> createEvent(@RequestBody EnterpriseEvent event, @RequestParam Long enterpriseId) {
        return enterpriseRepository.findById(enterpriseId)
                .map(enterprise -> {
                    event.setEnterprise(enterprise);
                    event.setCreateTime(LocalDateTime.now());
                    event.setUpdateTime(LocalDateTime.now());

                    // 根据时间自动设置状态
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isBefore(event.getStartTime())) {
                        event.setStatus("UPCOMING");
                    } else if (now.isAfter(event.getEndTime())) {
                        event.setStatus("COMPLETED");
                    } else {
                        event.setStatus("ONGOING");
                    }

                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    // 更新活动信息
    @PutMapping("/{eventId}")
    public ResponseEntity<EnterpriseEvent> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EnterpriseEvent eventDetails) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    event.setTitle(eventDetails.getTitle());
                    event.setDescription(eventDetails.getDescription());
                    event.setEventType(eventDetails.getEventType());
                    event.setLocation(eventDetails.getLocation());
                    event.setStartTime(eventDetails.getStartTime());
                    event.setEndTime(eventDetails.getEndTime());
                    event.setRegistrationDeadline(eventDetails.getRegistrationDeadline());
                    event.setMaxParticipants(eventDetails.getMaxParticipants());
                    event.setBenefits(eventDetails.getBenefits());
                    event.setUpdateTime(LocalDateTime.now());

                    // 根据时间自动更新状态
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isBefore(event.getStartTime())) {
                        event.setStatus("UPCOMING");
                    } else if (now.isAfter(event.getEndTime())) {
                        event.setStatus("COMPLETED");
                    } else {
                        event.setStatus("ONGOING");
                    }

                    return ResponseEntity.ok(eventRepository.save(event));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 删除活动
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    eventRepository.delete(event);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 学生报名活动
    @PostMapping("/{eventId}/register")
    public ResponseEntity<?> registerForEvent(
            @PathVariable Long eventId,
            @RequestParam Long studentId) {
        Optional<EnterpriseEvent> eventOpt = eventRepository.findById(eventId);
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (!eventOpt.isPresent() || !studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body("活动或学生不存在");
        }

        EnterpriseEvent event = eventOpt.get();
        Student student = studentOpt.get();

        // 检查活动是否开放报名
        if (!"UPCOMING".equals(event.getStatus())) {
            return ResponseEntity.badRequest().body("活动不在报名阶段");
        }

        // 检查报名截止时间
        if (LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            return ResponseEntity.badRequest().body("报名已截止");
        }

        // 检查是否已经报名
        if (registrationRepository.findByEventEventIdAndStudentStudentId(eventId, studentId).isPresent()) {
            return ResponseEntity.badRequest().body("已经报名过该活动");
        }

        // 检查报名人数是否已满
        List<EventRegistration> registrations = registrationRepository.findByEventEventId(eventId);
        if (registrations.size() >= event.getMaxParticipants()) {
            return ResponseEntity.badRequest().body("报名人数已满");
        }

        // 创建报名记录
        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setStudent(student);
        registration.setRegistrationTime(LocalDateTime.now());
        registration.setStatus("REGISTERED");

        registrationRepository.save(registration);

        return ResponseEntity.ok().body("报名成功");
    }

    // 获取活动的所有报名记录
    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<List<EventRegistration>> getEventRegistrations(@PathVariable Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registrationRepository.findByEventEventId(eventId));
    }

    // 更新学生参与状态（出席/缺席）
    @PutMapping("/registrations/{registrationId}/status")
    public ResponseEntity<?> updateRegistrationStatus(
            @PathVariable Long registrationId,
            @RequestParam String status) {
        return registrationRepository.findById(registrationId)
                .map(registration -> {
                    registration.setStatus(status);
                    registrationRepository.save(registration);
                    return ResponseEntity.ok().body("状态更新成功");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 获取学生参与的活动
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnterpriseEvent>> getStudentEvents(@PathVariable Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(eventRepository.findEventsByStudentId(studentId));
    }

    // 获取企业发布的活动
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<List<EnterpriseEvent>> getEnterpriseEvents(@PathVariable Long enterpriseId) {
        if (!enterpriseRepository.existsById(enterpriseId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(eventRepository.findByEnterpriseEnterpriseId(enterpriseId));
    }

    // 按活动类型获取活动
    @GetMapping("/type/{eventType}")
    public ResponseEntity<List<EnterpriseEvent>> getEventsByType(@PathVariable String eventType) {
        return ResponseEntity.ok(eventRepository.findByEventType(eventType));
    }
}
