package com.site.lms.controller;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.site.lms.entity.*;
import com.site.lms.service.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainHome {

    private final UserService         userService;
    private final CalendarService     calendarService;
    private final LectureService      lectureService;
    private final LectureVideoService lectureVideoService;
    private final SubscriptionService subscriptionService;
    private final ProgressService     progressService;   // ★
    private final ObjectMapper        objectMapper;

    public MainHome(UserService userService,
                    CalendarService calendarService,
                    LectureService lectureService,
                    LectureVideoService lectureVideoService,
                    SubscriptionService subscriptionService,
                    ProgressService progressService,     // ★
                    ObjectMapper objectMapper) {
        this.userService         = userService;
        this.calendarService     = calendarService;
        this.lectureService      = lectureService;
        this.lectureVideoService = lectureVideoService;
        this.subscriptionService = subscriptionService;
        this.progressService     = progressService;      // ★
        this.objectMapper        = objectMapper;
    }

    @GetMapping("/")
    public String mainHome(Model model, Principal principal) throws Exception {

        if (principal == null) return "mainHome";

        /* ───── 기본 정보 ───── */
        User user     = userService.findByUsername(principal.getName()).orElseThrow();
        Long memberNo = user.getId();
        model.addAttribute("username",  user.getUsername());
        model.addAttribute("authority", user.getAuthority());

        /* ───── 캘린더 이벤트 ───── */
        calendarService.updateStatuses(memberNo);
        List<CalendarEvent> events =
            calendarService.getEventsForMember(memberNo);
        model.addAttribute("eventsJson",
            objectMapper.writeValueAsString(events));

        /* ───── 강의/영상 목록 ───── */
        List<Lecture> publicLectures  = lectureService.findByVisibility(1);
        List<Lecture> privateLectures = lectureService.findByVisibility(0);

        Map<Long,List<LectureVideo>> videosByLecture = new LinkedHashMap<>();
        for (Lecture lec : publicLectures) {
            videosByLecture.put(
                lec.getLectureNo(),
                lectureVideoService.findByLectureNo(lec.getLectureNo()));
        }
        for (Lecture lec : privateLectures) {                             // 비공개도 포함
            videosByLecture.put(
                lec.getLectureNo(),
                lectureVideoService.findByLectureNo(lec.getLectureNo()));
        }

        model.addAttribute("publicLectures",  publicLectures);
        model.addAttribute("privateLectures", privateLectures);
        model.addAttribute("videosByLecture", videosByLecture);

        /* ───── ★ 진도 맵 계산 ───── */
        Map<Long,Integer> lectureProgressMap = new HashMap<>();
        Map<Long,Integer> videoProgressMap   = new HashMap<>();

        String username = user.getUsername();
        for (Map.Entry<Long,List<LectureVideo>> e : videosByLecture.entrySet()) {
            Long lecNo              = e.getKey();
            List<LectureVideo> vids = e.getValue();

            // 강의 평균
            int lecPct = progressService.getLectureProgress(
                            username, lecNo, vids);
            lectureProgressMap.put(lecNo, lecPct);

            // 각 영상 퍼센트
            for (LectureVideo vid : vids) {
                int vidPct = progressService.getVideoProgress(
                                username, vid.getVideoNo());
                videoProgressMap.put(vid.getVideoNo(), vidPct);
            }
        }
        model.addAttribute("lectureProgressMap", lectureProgressMap);  // ★
        model.addAttribute("videoProgressMap",   videoProgressMap);    // ★

        /* ───── 일정에 등록된 강의 ───── */
        Set<Long> scheduledLectureNos = events.stream()
            .map(CalendarEvent::getVideoNo)
            .collect(Collectors.toSet())
            .stream()
            .flatMap(vn -> videosByLecture.entrySet().stream()
                           .filter(e -> e.getValue().stream()
                               .anyMatch(v -> v.getVideoNo().equals(vn)))
                           .map(Map.Entry::getKey))
            .collect(Collectors.toSet());
        model.addAttribute("scheduledLectures", scheduledLectureNos);

        /* ───── 비공개 강의 신청 상태 ───── */
        List<Subscription> subs = subscriptionService.getSubscriptions(memberNo);

        model.addAttribute("pendingNos",
            subs.stream().filter(s -> "PENDING".equals(s.getStatus()))
                .map(Subscription::getLectureNo).collect(Collectors.toSet()));

        model.addAttribute("approvedNos",
            subs.stream().filter(s -> "APPROVED".equals(s.getStatus()))
                .map(Subscription::getLectureNo).collect(Collectors.toSet()));

        return "mainHome";
    }
}