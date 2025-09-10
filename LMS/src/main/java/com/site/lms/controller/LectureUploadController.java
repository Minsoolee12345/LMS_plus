package com.site.lms.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.site.lms.entity.Lecture;
import com.site.lms.entity.LectureVideo;
import com.site.lms.entity.User;
import com.site.lms.repository.UserRepository;
import com.site.lms.service.LectureService;
import com.site.lms.service.LectureVideoService;
import com.site.lms.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/lecture")
public class LectureUploadController {

    private static final Logger logger = LoggerFactory.getLogger(LectureUploadController.class);

    private final UserRepository    userRepository;
    private final LectureVideoService videoService;
    private final LectureService    lectureService;
    private final UserService       userService;
    private final RestTemplate      restTemplate   = new RestTemplate();
    private final ObjectMapper      objectMapper   = new ObjectMapper();

    @Value("${lecture.upload.path}")
    private String uploadDir;

    public LectureUploadController(
            LectureVideoService videoService,
            LectureService lectureService,
            UserService userService,
            UserRepository userRepository) {
        this.userRepository  = userRepository;
        this.videoService    = videoService;
        this.lectureService  = lectureService;
        this.userService     = userService;
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                               .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Lecture> lectures = lectureService.findByInstructor(user.getId());

        model.addAttribute("lectures", lectures);
        model.addAttribute("username", username);
        userRepository.findByUsername(username)
                      .ifPresent(u -> model.addAttribute("authority", u.getAuthority()));
        return "lecture_upload";
    }

    @PostMapping("/upload")
    public String handleUpload(
            @RequestParam("lectureNo") Long lectureNo,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "visibility", defaultValue = "1") Integer visibility,
            Principal principal,
            Model model) {

        logger.info("강의 업로드 요청: lectureNo={}, filename={}, visibility={}",
                    lectureNo, file.getOriginalFilename(), visibility);

        if (!lectureService.existsLecture(lectureNo)) {
            model.addAttribute("message", "❌ 업로드 실패: 유효한 강의를 선택해주세요.");
            return "lecture_upload";
        }
        if (file.isEmpty()) {
            model.addAttribute("message", "❌ 업로드 실패: 영상 파일을 선택해주세요.");
            return "lecture_upload";
        }

        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File target = new File(uploadDir, filename);
            file.transferTo(target);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String,String>> req = new HttpEntity<>(
                Map.of("video_path", target.getAbsolutePath()), headers);

            // Flask API 호출 시 ResponseEntity<String> 대신 ResponseEntity<JsonNode>를 직접 사용하면 더 깔끔합니다.
            // 하지만 현재 String으로 받아 JsonNode로 파싱하는 방식도 작동하므로 일단 유지합니다.
            ResponseEntity<String> resp = restTemplate
                .postForEntity("http://localhost:5000/api/process", req, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                model.addAttribute("message", "❌ 분석 실패: Flask 응답이 없습니다.");
                return "lecture_upload";
            }

            JsonNode json = objectMapper.readTree(resp.getBody());

            // 🎯 업로더 정보 세팅
            Long uploaderNo = userService.findByUsername(principal.getName())
                                         .orElseThrow(() -> new IllegalArgumentException("User not found"))
                                         .getId();

            LectureVideo video = new LectureVideo();
            video.setLectureNo(lectureNo);
            video.setVideoPath(filename);
            video.setVideoDesc("자동 분석된 영상");
            video.setWordset(objectMapper.writeValueAsString(json.get("top_keywords")));
            video.setRawText(json.get("raw_text").asText());
            video.setSegmentsJson(objectMapper.writeValueAsString(json.get("segments")));
            video.setVisibility(visibility);
            video.setUploaderNo(uploaderNo);

            // !!!!! 이 라인을 추가합니다: Flask 응답에서 summary를 추출하여 LectureVideo에 설정 !!!!!
            // json.get("summary")가 null이 아닐 경우에만 asText() 호출
            if (json.has("summary") && json.get("summary") != null) {
                video.setSummary(json.get("summary").asText());
            } else {
                logger.warn("Flask 응답에 'summary' 필드가 없거나 null입니다. videoNo: {}", video.getVideoNo());
            }


            videoService.saveVideo(video);
            return "redirect:/lecture/watch/" + video.getVideoNo();

        } catch (IOException e) {
            logger.error("파일 처리 중 오류: {}", e.getMessage(), e);
            model.addAttribute("message", "파일 처리 중 오류: " + e.getMessage());
            return "lecture_upload";
        } catch (Exception e) {
            logger.error("서버 오류: {}", e.getMessage(), e);
            model.addAttribute("message", "서버 오류: " + e.getMessage());
            return "lecture_upload";
        }
    }
}