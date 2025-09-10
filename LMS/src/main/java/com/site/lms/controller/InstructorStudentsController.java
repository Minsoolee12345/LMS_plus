// src/main/java/com/site/lms/controller/InstructorStudentsController.java
package com.site.lms.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.site.lms.dto.InstructorStudentDto;
import com.site.lms.entity.Lecture;
import com.site.lms.service.*;

@Controller
@RequestMapping("/instructor")
public class InstructorStudentsController {

    private final SubscriptionService       subscriptionService;
    private final LectureApplicationService applicationService;
    private final UserService               userService;
    private final LectureService            lectureService;
    private final LectureVideoService       videoService;
    private final ProgressService           progressService;

    public InstructorStudentsController(
            SubscriptionService       subscriptionService,
            LectureApplicationService applicationService,
            UserService               userService,
            LectureService            lectureService,
            LectureVideoService       videoService,
            ProgressService           progressService
    ) {
        this.subscriptionService = subscriptionService;
        this.applicationService  = applicationService;
        this.userService         = userService;
        this.lectureService      = lectureService;
        this.videoService        = videoService;
        this.progressService     = progressService;
    }

    /** 내 수강생 페이지 */
    @GetMapping("/students")
    public String listAllStudents(Model model, Principal principal) {
        Long instructorId = userService
            .findByUsername(principal.getName())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
            .getId();

        // LinkedHashMap 으로 컬렉션 순서 보장
        Map<Lecture, List<InstructorStudentDto>> pendingMap  = new LinkedHashMap<>();
        Map<Lecture, List<InstructorStudentDto>> approvedMap = new LinkedHashMap<>();

        // 이 강사가 만든 강의들
        List<Lecture> lectures = lectureService.findByInstructor(instructorId);

        for (Lecture lec : lectures) {
            var subs = subscriptionService.getByLecture(lec.getLectureNo());

            // 대기 중
            List<InstructorStudentDto> pendings = subs.stream()
                .filter(s -> "PENDING".equals(s.getStatus()))
                .map(s -> toDto(lec, s))
                .collect(Collectors.toList());
            // 승인됨
            List<InstructorStudentDto> approveds = subs.stream()
                .filter(s -> "APPROVED".equals(s.getStatus()))
                .map(s -> toDto(lec, s))
                .collect(Collectors.toList());

            pendingMap.put(lec, pendings);
            approvedMap.put(lec, approveds);
        }

        model.addAttribute("pendingMap",  pendingMap);
        model.addAttribute("approvedMap", approvedMap);
        return "instructor_students";
    }

    /** 수락 */
    @PostMapping("/students/{subId}/accept")
    public String acceptSubscription(
    		@PathVariable("subId") Long subId, Principal principal
    ) {
        applicationService.acceptApplication(principal.getName(), subId);
        return "redirect:/instructor/students";
    }

    /** 거절 */
    @PostMapping("/students/{subId}/reject")
    public String rejectSubscription(
    		@PathVariable("subId") Long subId, Principal principal
    ) {
        applicationService.rejectApplication(principal.getName(), subId);
        return "redirect:/instructor/students";
    }

    /** 제외 (승인 후 제거) */
    @PostMapping("/students/{subId}/exclude")
    public String excludeSubscription(
    		@PathVariable("subId") Long subId, Principal principal
    ) {
        // 승인을 취소하는 로직으로 구현
        applicationService.rejectApplication(principal.getName(), subId);
        return "redirect:/instructor/students";
    }

    private InstructorStudentDto toDto(Lecture lec, com.site.lms.entity.Subscription sub) {
        var student  = userService.findById(sub.getMemberNo()).orElseThrow();
        var videos   = videoService.findByLectureNo(lec.getLectureNo());
        var progress = progressService.getLectureProgress(
            student.getUsername(), lec.getLectureNo(), videos
        );
        return new InstructorStudentDto(
            sub.getSubId(),
            student.getUsername(),
            progress,
            sub.getStatus()
        );
    }
}