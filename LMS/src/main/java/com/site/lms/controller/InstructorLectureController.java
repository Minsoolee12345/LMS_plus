//// src/main/java/com/site/lms/controller/InstructorLectureController.java
//package com.site.lms.controller;
//
//import java.security.Principal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import com.site.lms.entity.Lecture;
//import com.site.lms.entity.LectureVideo;
//import com.site.lms.service.LectureService;
//import com.site.lms.service.LectureVideoService;
//import com.site.lms.service.UserService;
//
//@Controller
//@RequestMapping("/instructor/lectures/manage")
//public class InstructorLectureController {
//
//    private final LectureService lectureService;
//    private final LectureVideoService videoService;
//    private final UserService userService;
//
//    public InstructorLectureController(
//        LectureService lectureService,
//        LectureVideoService videoService,
//        UserService userService
//    ) {
//        this.lectureService = lectureService;
//        this.videoService   = videoService;
//        this.userService    = userService;
//    }
//
//    @GetMapping
//    public String listMyLectures(Model model, Principal principal) {
//        // 현재 로그인한 강사의 MEMBER_NO 조회
//        Long instructorNo = userService
//            .findByUsername(principal.getName())
//            .orElseThrow().getId();
//
//        // 이 강사가 올린 영상이 속한 과목 번호 목록 조회
//        List<Long> lectureNos = videoService.findLectureNosByUploader(instructorNo);
//
//        // 과목 엔티티 조회
//        List<Lecture> lectures = lectureService.findByIds(lectureNos);
//
//        // 과목별로 이 강사가 올린 영상만 묶기
//        Map<Long, List<LectureVideo>> videosByLecture = new HashMap<>();
//        for (Long lecNo : lectureNos) {
//            List<LectureVideo> vids =
//                videoService.findByLectureNoAndUploader(lecNo, instructorNo);
//            videosByLecture.put(lecNo, vids);
//        }
//
//        model.addAttribute("lectures", lectures);
//        model.addAttribute("videosByLecture", videosByLecture);
//        return "instructor_lectures_manage";
//    }
//}
package com.site.lms.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.site.lms.entity.Lecture;
import com.site.lms.entity.LectureVideo;
import com.site.lms.service.LectureService;
import com.site.lms.service.LectureVideoService;
import com.site.lms.service.UserService;

@Controller
@RequestMapping("/instructor/lectures/manage")
public class InstructorLectureController {

    private final LectureService lectureService;
    private final LectureVideoService videoService;
    private final UserService userService;

    public InstructorLectureController(
        LectureService lectureService,
        LectureVideoService videoService,
        UserService userService
    ) {
        this.lectureService = lectureService;
        this.videoService   = videoService;
        this.userService    = userService;
    }

    /** (기존) 내 강의·영상 관리 페이지 조회 **/
    @GetMapping
    public String listMyLectures(Model model, Principal principal) {
        // 현재 로그인한 강사의 MEMBER_NO 조회
        Long instructorNo = userService
            .findByUsername(principal.getName())
            .orElseThrow().getId();

        // 이 강사가 올린 영상이 속한 과목 번호 목록 조회
        List<Long> lectureNos = videoService.findLectureNosByUploader(instructorNo);

        // 과목 엔티티 조회
        List<Lecture> lectures = lectureService.findByIds(lectureNos);

        // 과목별로 이 강사가 올린 영상만 묶기
        Map<Long, List<LectureVideo>> videosByLecture = new HashMap<>();
        for (Long lecNo : lectureNos) {
            List<LectureVideo> vids =
                videoService.findByLectureNoAndUploader(lecNo, instructorNo);
            videosByLecture.put(lecNo, vids);
        }

        model.addAttribute("lectures", lectures);
        model.addAttribute("videosByLecture", videosByLecture);
        return "instructor_lectures_manage";
    }

    /** 영상 공개 상태 변경 처리 **/
    @PostMapping("/video/update")
    public String updateVideoVisibility(
        @RequestParam("videoNo") Long videoNo,
        @RequestParam("visibility") Integer visibility,
        Principal principal,
        Model model
    ) {
        // 1) 로그인한 강사의 ID 확인
        Long instructorNo = userService.findByUsername(principal.getName())
                                      .orElseThrow(() -> new IllegalArgumentException("User not found"))
                                      .getId();

        // 2) 해당 영상이 현재 강사가 올린 영상인지 검증
        LectureVideo existing = videoService.findById(videoNo);
        if (existing == null || !existing.getUploaderNo().equals(instructorNo)) {
            model.addAttribute("message", "❌ 권한이 없거나 존재하지 않는 영상입니다.");
            return "error";
        }

        // 3) 공개 상태만 변경해서 저장
        existing.setVisibility(visibility);
        videoService.saveVideo(existing);

        // 4) 변경 후 다시 목록으로 리다이렉트
        return "redirect:/instructor/lectures/manage";
    }

    /** 영상 삭제 처리 **/
    @PostMapping("/video/delete")
    public String deleteVideo(
        @RequestParam("videoNo") Long videoNo,
        Principal principal,
        Model model
    ) {
        // 1) 로그인한 강사의 ID 확인
        Long instructorNo = userService.findByUsername(principal.getName())
                                      .orElseThrow(() -> new IllegalArgumentException("User not found"))
                                      .getId();

        // 2) 해당 영상이 현재 강사가 올린 영상인지 검증
        LectureVideo existing = videoService.findById(videoNo);
        if (existing == null || !existing.getUploaderNo().equals(instructorNo)) {
            model.addAttribute("message", "❌ 권한이 없거나 존재하지 않는 영상입니다.");
            return "error";
        }

        // 3) 영상 삭제
        videoService.deleteVideo(videoNo);

        // 4) 삭제 후 다시 목록으로 리다이렉트
        return "redirect:/instructor/lectures/manage";
    }
}