// src/main/java/com/site/lms/controller/SubscriptionController.java
package com.site.lms.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.site.lms.entity.Lecture;
import com.site.lms.entity.Subscription;
import com.site.lms.entity.User;
import com.site.lms.service.LectureApplicationService;
import com.site.lms.service.LectureService;
import com.site.lms.service.SubscriptionService;
import com.site.lms.service.UserService;

@Controller
@RequestMapping("/lectures")
public class SubscriptionController {

    private final LectureService lectureService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final LectureApplicationService applicationService;

    public SubscriptionController(
            LectureService lectureService,
            UserService userService,
            SubscriptionService subscriptionService,
            LectureApplicationService applicationService
    ) {
        this.lectureService      = lectureService;
        this.userService         = userService;
        this.subscriptionService = subscriptionService;
        this.applicationService  = applicationService;
    }

    @GetMapping("/apply")
    public String showApplyPage(Model model, Principal principal) {
        // 1) 로그인된 학생 ID 조회
        Long memberId = userService.findByUsername(principal.getName())
                                   .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                                   .getId();

        // 2) 비공개 강의 목록
        List<Lecture> privateLectures = lectureService.findByVisibility(0);
        model.addAttribute("privateLectures", privateLectures);

        // 3) 학생별 신청 내역 조회
        List<Subscription> subs = subscriptionService.getSubscriptions(memberId);

        // 4) 상태별로 분류
        Set<Long> pendingNos  = subs.stream()
                                    .filter(s -> "PENDING".equals(s.getStatus()))
                                    .map(Subscription::getLectureNo)
                                    .collect(Collectors.toSet());
        Set<Long> approvedNos = subs.stream()
                                    .filter(s -> "APPROVED".equals(s.getStatus()))
                                    .map(Subscription::getLectureNo)
                                    .collect(Collectors.toSet());

        model.addAttribute("pendingNos",  pendingNos);
        model.addAttribute("approvedNos", approvedNos);

        return "lectures_apply";
    }


    /**
     * POST /lectures/subscribe
     * 비공개 강의 신청 처리 (DB 저장 + 강사에게 메시지 발송)
     */
    @PostMapping("/subscribe")
    public String subscribe(
            @RequestParam("lectureNo") Long lectureNo,
            Principal principal
    ) {
        // 1) 로그인 학생 ID 조회
        var student = userService.findByUsername(principal.getName())
                         .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 2) 신청 로직 및 메시지 발송
        applicationService.applyToLecture(student.getId(), lectureNo);
        // 3) 다시 신청 페이지로 리다이렉트 (요청 성공 표시를 쿼리파라미터로 넘길 수도 있습니다)
        return "redirect:/lectures/apply?success";
    }
}