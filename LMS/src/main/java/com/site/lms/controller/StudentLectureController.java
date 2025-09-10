package com.site.lms.controller;

import java.security.Principal;
import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.site.lms.entity.Lecture;
import com.site.lms.entity.LectureVideo;
import com.site.lms.repository.UserRepository;
import com.site.lms.service.FavoriteService;
import com.site.lms.service.LectureService;
import com.site.lms.service.LectureVideoService;
import com.site.lms.service.ProgressService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/lectures")
public class StudentLectureController {

    private final UserRepository      userRepository;
    private final LectureService      lectureService;
    private final FavoriteService     favoriteService;
    private final LectureVideoService videoService;
    private final ProgressService     progressService;

    public StudentLectureController(
        LectureService lectureService,
        FavoriteService favoriteService,
        LectureVideoService videoService,
        UserRepository userRepository,
        ProgressService progressService
    ) {
        this.lectureService   = lectureService;
        this.favoriteService  = favoriteService;
        this.videoService     = videoService;
        this.userRepository   = userRepository;
        this.progressService  = progressService;
    }

    // 강의 리스트 보기
    @GetMapping("/list")
    public String listLectures(Model model, Principal principal) {
        // 1) 전체 공개 강의 목록 조회
        List<Lecture> lectures = lectureService.findPublicLectures(null);

        // 2) 이 수강생의 “즐겨찾기(강의, 챕터)” ID 목록 조회
        List<Long> lectureFavs = favoriteService.findFavoriteLectureNos(principal.getName());
        List<Long> chapterFavs = favoriteService.findFavoriteChapterNos(principal.getName());

        // 3) 과목별로 해당 강의의 모든 영상(챕터) 목록 조회하여 Map으로 묶기
        Map<Long, List<LectureVideo>> chaptersByLecture = new HashMap<>();
        for (Lecture lec : lectures) {
            chaptersByLecture.put(
                lec.getLectureNo(),
                videoService.findByLectureNo(lec.getLectureNo())
            );
        }

        // 4) “강의 진도율”과 “영상 진도율”을 저장할 Map 준비
        Map<Long, Integer> lectureProgressMap = new HashMap<>();
        Map<Long, Integer> videoProgressMap   = new HashMap<>();

        String username = principal.getName();

        // 5) 각 강의별로 평균 진도율(lectureProgress) 계산
        for (Lecture lec : lectures) {
            List<LectureVideo> videoList = chaptersByLecture.get(lec.getLectureNo());
            int lecPct = progressService.getLectureProgress(username, lec.getLectureNo(), videoList);
            lectureProgressMap.put(lec.getLectureNo(), lecPct);

            // 6) 해당 강의(코스)에 속한 모든 영상별 진도율 계산
            for (LectureVideo vid : videoList) {
                int vidPct = progressService.getVideoProgress(username, vid.getVideoNo());
                videoProgressMap.put(vid.getVideoNo(), vidPct);
            }
        }

        // 7) 뷰에 필요한 모든 속성 세팅
        model.addAttribute("lectures", lectures);
        model.addAttribute("lectureFavorites", lectureFavs);
        model.addAttribute("chapterFavorites", chapterFavs);
        model.addAttribute("chaptersByLecture", chaptersByLecture);

        model.addAttribute("lectureProgressMap", lectureProgressMap);
        model.addAttribute("videoProgressMap",   videoProgressMap);

        if (principal != null) {
            String name = principal.getName();
            model.addAttribute("username", name);
            userRepository.findByUsername(name)
                          .ifPresent(u -> model.addAttribute("authority", u.getAuthority()));
        }

        return "lecture_list";
    }

    // 강의 즐겨찾기 토글
    @PostMapping("/favorite/toggle")
    public String toggleLectureFavorite(
        @RequestParam(name = "lectureNo") Long lectureNo,
        Principal principal,
        RedirectAttributes ra
    ) {
        boolean nowFav = favoriteService.toggleLectureFavorite(principal.getName(), lectureNo);
        ra.addFlashAttribute("message",
            nowFav ? "강의 즐겨찾기에 등록되었습니다." : "강의 즐겨찾기에서 해제되었습니다.");
        return "redirect:/lectures/list";
    }

    // 챕터 즐겨찾기 토글
    @PostMapping("/chapter/favorite/toggle")
    public String toggleChapterFavorite(
        @RequestParam(name = "videoNo") Long videoNo,
        Principal principal,
        RedirectAttributes ra
    ) {
        boolean nowFav = favoriteService.toggleChapterFavorite(principal.getName(), videoNo);
        ra.addFlashAttribute("message",
            nowFav ? "챕터 즐겨찾기에 등록되었습니다." : "챕터 즐겨찾기에서 해제되었습니다.");
        return "redirect:/lectures/list";
    }

    // 즐겨찾기 목록 보기
    @GetMapping("/favorites")
    public String viewFavorites(Model model, Principal principal) {
        var lecFavIds = favoriteService.findFavoriteLectureNos(principal.getName());
        var lectureFavs = lectureService.findByIds(lecFavIds);

        var chapFavIds = favoriteService.findFavoriteChapterNos(principal.getName());
        var chapterFavs = videoService.findByIds(chapFavIds);

        model.addAttribute("lectureFavorites", lectureFavs);
        model.addAttribute("chapterFavorites", chapterFavs);

        if (principal != null) {
            String name = principal.getName();
            model.addAttribute("username", name);
            userRepository.findByUsername(name)
                          .ifPresent(u -> model.addAttribute("authority", u.getAuthority()));
        }

        return "favorites";
    }

    // 최근 본 영상 보기
    @GetMapping("/recent")
    public String viewRecent(
        Model model,
        HttpSession session,
        Principal principal
    ) {
    	System.out.println("hello");
        @SuppressWarnings("unchecked")
        List<Long> recentIds = (List<Long>) session.getAttribute("recentLectures");

        List<Map<String,Object>> recentViews = new ArrayList<>();
        if (recentIds != null) {
            for (Long vidNo : recentIds) {
                LectureVideo vid = videoService.findById(vidNo);
                if (vid != null) {
                    Lecture lec = lectureService.findById(vid.getLectureNo());
                    if (lec != null) {
                        Map<String,Object> m = new HashMap<>();
                        m.put("video", vid);
                        m.put("lecture", lec);
                        recentViews.add(m);
                    }
                }
            }
        }
        model.addAttribute("recentViews", recentViews);

        if (principal != null) {
            String name = principal.getName();
            model.addAttribute("username", name);
            userRepository.findByUsername(name)
                          .ifPresent(u -> model.addAttribute("authority", u.getAuthority()));
        }

        return "lecture_recent";
    }
    
}