package com.site.lms.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.site.lms.entity.LectureVideo;
import com.site.lms.mapper.ProgressMapper;

/**
 * ProgressMapper를 래핑해서 "영상 진도"와 "강의(코스) 진도"를 계산해 주는 서비스.
 */
@Service
public class ProgressService {

    private final ProgressMapper  progressMapper;
    private final UserService     userService;

    public ProgressService(
            ProgressMapper progressMapper,
            UserService    userService
    ) {
        this.progressMapper = progressMapper;
        this.userService    = userService;
    }

    /**
     * 특정 회원(username)이 특정 영상(videoNo)에서 진행한 퍼센트를 조회.
     * 데이터가 없으면 0을 리턴.
     */
    public int getVideoProgress(String username, Long videoNo) {
        Long memberNo = userService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username))
            .getId();

        Integer pct = progressMapper.findByMemberAndVideo(memberNo, videoNo);
        return (pct != null ? pct : 0);
    }

    /**
     * 특정 회원(username)이 특정 강의(lectureNo)에 속한 모든 영상 목록(videos)을
     * 평균하여 “강의(코스) 진도율”을 계산해 리턴.
     *
     * 영상이 하나도 없으면 0을 리턴.
     * 각 영상별 진행 퍼센트가 존재하지 않으면 0으로 간주 후 평균 계산.
     */
    public int getLectureProgress(
            String username,
            Long lectureNo,
            List<LectureVideo> videos
    ) {
        if (videos == null || videos.isEmpty()) {
            return 0;
        }

        // 현재 로그인한 회원 ID 조회
        Long memberNo = userService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username))
            .getId();

        int sum = 0;
        int count = videos.size();

        // 영상별로 진행 퍼센트를 모두 합산
        for (LectureVideo vid : videos) {
            Integer onePct = progressMapper.findByMemberAndVideo(memberNo, vid.getVideoNo());
            sum += (onePct != null ? onePct : 0);
        }

        // 평균값(정수) 리턴
        return sum / count;
    }

    /**
     * 특정 회원(username)이 특정 영상(videoNo)에서 진도(pct)를 새롭게 기록하거나 업데이트
     */
    public void saveOrUpdateVideoProgress(
            String username,
            Long videoNo,
            Integer pct
    ) {
        Long memberNo = userService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username))
            .getId();

        progressMapper.insertOrUpdate(memberNo, videoNo, pct);
    }

    public void deleteByLecture(Long lectureNo) 
    {
        progressMapper.deleteByLecture(lectureNo);
    }

}