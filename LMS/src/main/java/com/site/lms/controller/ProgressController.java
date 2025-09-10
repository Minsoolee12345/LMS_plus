package com.site.lms.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.site.lms.service.ProgressService;

/**
 * 영상 시청 중 진도(퍼센트)를 저장하는 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * 클라이언트에서 JSON 형태로 {"videoNo": <Long>, "progress": <Integer>}를 POST로 전송할 때
     * 해당 회원(username) + 영상(videoNo)의 진도를 저장(INSERT 또는 UPDATE)한다.
     */
    @PostMapping("/save")
    public ResponseEntity<?> saveProgress(
            @RequestBody Map<String, Object> payload,
            Principal principal
    ) {
        // 로그인 정보가 없으면 401 Unauthorized 반환
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            String username = principal.getName();

            // payload에서 videoNo와 progress 값을 파싱
            // payload.get("videoNo")와 payload.get("progress")는 Integer로 넘어온다
            Long videoNo = Long.valueOf((Integer) payload.get("videoNo"));
            Integer progressValue = (Integer) payload.get("progress");

            // progressValue가 0~100 범위 내에 있도록 보정
            if (progressValue == null) {
                progressValue = 0;
            }
            if (progressValue < 0) {
                progressValue = 0;
            }
            if (progressValue > 100) {
                progressValue = 100;
            }

            // ProgressService를 통해 저장 또는 업데이트
            progressService.saveOrUpdateVideoProgress(username, videoNo, progressValue);

            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            // 예외 발생 시 500 에러와 메시지 반환
            return ResponseEntity.status(500)
                    .body("진도 저장 중 오류: " + e.getMessage());
        }
    }
}