package com.site.lms.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.site.lms.dto.VideoProcessResponse;
import com.site.lms.entity.LectureVideo;
import com.site.lms.service.LectureVideoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/videos")
public class LectureVideoController {

    private final LectureVideoService lectureVideoService;
    private static final Logger logger = LoggerFactory.getLogger(LectureVideoController.class);
    private final String FLASK_API_URL = "http://localhost:5000/api/process";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public LectureVideoController(LectureVideoService lectureVideoService) {
        this.lectureVideoService = lectureVideoService;
    }

    @GetMapping("/analyze")
    public ResponseEntity<?> analyzeVideo(
        @RequestParam("videoPath") String videoPath,
        @RequestParam("videoNo") Long videoNo) {
        try {
            // Flask API 호출 준비
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> request = Map.of("video_path", videoPath);
            var entity = new org.springframework.http.HttpEntity<>(request, headers);

            logger.info("DEBUG (Spring): Calling Flask API at: {}", FLASK_API_URL); // 추가
            ResponseEntity<VideoProcessResponse> response = restTemplate.postForEntity(
                FLASK_API_URL, entity, VideoProcessResponse.class
            );
            logger.info("DEBUG (Spring): Received response status from Flask: {}", response.getStatusCode()); // 추가

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                VideoProcessResponse data = response.getBody();

                // !!!!! 이 라인을 추가합니다 !!!!!
                logger.info("DEBUG (Spring): VideoProcessResponse summary received from Flask: '{}' (length: {})",
                            data.getSummary() != null ? data.getSummary().substring(0, Math.min(data.getSummary().length(), 200)) + "..." : "null",
                            data.getSummary() != null ? data.getSummary().length() : 0);


                // 기존 영상 레코드 조회
                LectureVideo video = lectureVideoService.findById(videoNo);
                if (video == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                         .body("해당 비디오를 찾을 수 없습니다: " + videoNo);
                }

                // 필드 업데이트
                video.setWordset(objectMapper.writeValueAsString(data.getTop_keywords()));
                video.setRawText(data.getRaw_text());
                video.setSegmentsJson(objectMapper.writeValueAsString(data.getSegments()));
                video.setSummary(data.getSummary()); // <--- 이 라인에 브레이크포인트 설정 가능

                // !!!!! 이 라인을 추가합니다 !!!!!
                logger.info("DEBUG (Spring): LectureVideo entity summary before save: '{}' (length: {})",
                            video.getSummary() != null ? video.getSummary().substring(0, Math.min(video.getSummary().length(), 200)) + "..." : "null",
                            video.getSummary() != null ? video.getSummary().length() : 0);


                // 레코드 갱신
                lectureVideoService.saveVideo(video);

                logger.info("✅ Flask 분석 완료: [{}], 키워드 {}개, 요약 길이 {}자",
                            data.getFilename(), data.getTop_keywords().size(),
                            data.getSummary() != null ? data.getSummary().length() : 0);

                return ResponseEntity.ok(data);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("전처리 실패");
        } catch (Exception e) {
            logger.error("❌ Flask 연동 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("에러: " + e.getMessage());
        }
    }
}