package com.site.lms.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.site.lms.entity.Lecture;
import com.site.lms.entity.LectureVideo;
import com.site.lms.service.LectureSearchService;
import com.site.lms.service.LectureService;
import com.site.lms.service.LectureVideoService;

@Controller
@RequestMapping("/lectures")
public class LectureSearchController {

    private final LectureSearchService searchService;
    private final LectureService lectureService;
    private final LectureVideoService videoService;

    public LectureSearchController(
            LectureSearchService searchService,
            LectureService lectureService,
            LectureVideoService videoService) {
        this.searchService   = searchService;
        this.lectureService  = lectureService;
        this.videoService    = videoService;
    }

    @GetMapping("/search")
    public String searchLecture(
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        if (keyword != null && !keyword.isBlank()) {
            var lectures = searchService.searchByTopKeyword(keyword.trim());
            model.addAttribute("lectures", lectures);
            model.addAttribute("keyword", keyword);
        }
        return "searchLecture";
    }

    /** 기존 template 이름(lecture_detail.html)에 맞춰 return 값과 모델 속성 조정 **/
    @GetMapping("/{lectureNo}")
    public String lectureDetail(
            @PathVariable("lectureNo") Long lectureNo,
            Model model) {

        // (1) 강의 정보
        Lecture lecture = lectureService.findById(lectureNo);

        // (2) 해당 강의의 챕터(영상) 리스트
        List<LectureVideo> chapters = videoService.findByLectureNo(lectureNo);

        model.addAttribute("lecture", lecture);
        model.addAttribute("chapters", chapters);

        // lecture_detail.html 렌더링
        return "lecture_detail";
    }
}