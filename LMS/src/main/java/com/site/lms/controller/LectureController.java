package com.site.lms.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.site.lms.entity.Lecture;
import com.site.lms.entity.LectureVideo;
import com.site.lms.entity.User;
import com.site.lms.service.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/lecture")
public class LectureController {

    private final SubscriptionService subscriptionService;
    private final LectureService      lectureService;
    private final LectureVideoService videoService;
    private final ProgressService     progressService;
    private final UserService         userService;
    private final ObjectMapper        objectMapper = new ObjectMapper();

    public LectureController(
        LectureService lectureService,
        LectureVideoService videoService,
        ProgressService progressService,
        UserService userService,
        SubscriptionService subscriptionService
    ) {
        this.lectureService      = lectureService;
        this.videoService        = videoService;
        this.progressService     = progressService;
        this.userService         = userService;
        this.subscriptionService = subscriptionService;
    }

    /** 
     * 1) 강사 본인의 과목 목록 → GET /lecture
     */
    @GetMapping("")
    public String listLectures(
            Model model,
            @AuthenticationPrincipal UserDetails ud
    ) {
        User currentUser = userService.findByUsername(ud.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Lecture> lectures = lectureService.findByInstructor(currentUser.getId());
        model.addAttribute("lectures", lectures);
        model.addAttribute("currentUser", currentUser);
        return "lectures";
    }

    /**
     * 2) 새 과목 등록 폼 → GET /lecture/new
     */
    @GetMapping("/new")
    public String showCreateForm(
            Model model,
            @AuthenticationPrincipal UserDetails ud
    ) {
        User currentUser = userService.findByUsername(ud.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("lecture", new Lecture());
        return "lectureForm";
    }

    /**
     * 3) 새 과목 저장 처리 → POST /lecture/new
     */
    @PostMapping("/new")
    public String createLecture(
            @AuthenticationPrincipal UserDetails ud,
            @ModelAttribute Lecture lecture,
            RedirectAttributes ra
    ) {
        User currentUser = userService.findByUsername(ud.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        lectureService.createLecture(
            currentUser.getId(),
            lecture.getTitle(),
            lecture.getLectureDesc(),
            lecture.getVisibility()
        );
        ra.addFlashAttribute("message", "과목이 생성되었습니다.");
        return "redirect:/lecture";
    }

    /**
     * 4) 영상 시청 페이지 → GET /lecture/watch/{videoNo}
     */
    @GetMapping("/watch/{videoNo}")
    public String watchLecture(
            @PathVariable("videoNo") Long videoNo,
            Model model,
            HttpSession session,
            Principal principal
    ) throws IOException {
        LectureVideo video = videoService.findById(videoNo);
        if (video == null) {
            model.addAttribute("message", "유효하지 않은 영상 번호입니다: " + videoNo);
            return "error";
        }

        int savedPct = 0;
        if (principal != null) {
            savedPct = progressService.getVideoProgress(principal.getName(), videoNo);
        }

        @SuppressWarnings("unchecked")
        List<Long> recent = (List<Long>) session.getAttribute("recentLectures");
        if (recent == null) recent = new java.util.ArrayList<>();
        recent.remove(videoNo);
        recent.add(0, videoNo);
        if (recent.size() > 10) recent = recent.subList(0, 10);
        session.setAttribute("recentLectures", recent);

        List<List<Object>> topKeywords = objectMapper.readValue(
            video.getWordset(), new TypeReference<>() {});
        List<Map<String,Object>> segments = objectMapper.readValue(
            video.getSegmentsJson(), new TypeReference<>() {});

        // 추가: 요약 정보 모델에 추가
        String summary = video.getSummary();

        model.addAttribute("videoPct", savedPct);
        model.addAttribute("video", video);
        model.addAttribute("topKeywords", topKeywords);
        model.addAttribute("segments", segments);
        model.addAttribute("summary", summary);

        return "lecture_watch";
    }
    
    /**
     * ★ 추가: 과목 삭제 처리 → 영상 먼저, 강의 나중에 삭제
     */
    @PostMapping("/delete/{lectureNo}")
    public String deleteLecture(
            @PathVariable("lectureNo") Long lectureNo,
            Principal principal,
            RedirectAttributes ra) {
        // 1) (선택) 강사 인증 체크 생략...

        // 2) 구독(Subscription) 기록 삭제
        subscriptionService.deleteByLecture(lectureNo);

        // 3) 시청 진도(Progress) 기록 삭제 ← **여기**를 꼭 먼저!
        progressService.deleteByLecture(lectureNo);

        // 4) 영상(LectureVideo) 기록 삭제
        videoService.deleteByLecture(lectureNo);

        // 5) 강의 자체 삭제
        lectureService.deleteLecture(lectureNo);

        ra.addFlashAttribute("message", "강의가 완전히 삭제되었습니다.");
        return "redirect:/lecture";
    }
}