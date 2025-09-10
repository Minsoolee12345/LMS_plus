// src/main/java/com/site/lms/service/LectureVideoService.java
package com.site.lms.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.site.lms.entity.LectureVideo;
import com.site.lms.mapper.LectureVideoMapper;

@Service
public class LectureVideoService 
{

    private final LectureVideoMapper videoMapper;

    @Autowired
    public LectureVideoService(LectureVideoMapper videoMapper) {
        this.videoMapper = videoMapper;
    }

    public List<LectureVideo> findByLectureNo(Long lectureNo) {
        return videoMapper.findByLectureNo(lectureNo);
    }

    public LectureVideo findById(Long videoNo) {
        return videoMapper.findById(videoNo);
    }

    public void saveVideo(LectureVideo video) {
        if (video.getVideoNo() == null) {
            videoMapper.save(video);
        } else {
            videoMapper.update(video);
        }
    }

    public void deleteVideo(Long videoNo) {
        videoMapper.deleteById(videoNo);
    }

    public List<LectureVideo> findByIds(List<Long> videoNos) {
        if (videoNos == null || videoNos.isEmpty()) {
            return List.of();
        }
        return videoNos.stream()
                        .map(videoMapper::findById)
                        .filter(v -> v != null)
                        .collect(Collectors.toList());
    }

    /**
     * 이 강사가 올린 영상이 속한 과목 번호 목록 조회
     */
    public List<Long> findLectureNosByUploader(Long uploaderNo) {
        return videoMapper.findLectureNosByUploader(uploaderNo);
    }

    /**
     * 특정 과목에서 이 강사가 올린 영상 목록만 조회
     */
    public List<LectureVideo> findByLectureNoAndUploader(Long lectureNo, Long uploaderNo) {
        return videoMapper.findByLectureNoAndUploader(lectureNo, uploaderNo);
    }

    /**
     * 회원·영상별 시청 진도율 조회
     */
    public int findProgressPct(Long memberNo, Long videoNo) {
        Integer pct = videoMapper.findProgressPct(memberNo, videoNo);
        return (pct != null) ? pct : 0;
    }
    
    /**
     * ★ 추가: 특정 과목(lectureNo)에 속한 모든 영상 삭제
     */
    public void deleteByLecture(Long lectureNo) {
        videoMapper.deleteByLecture(lectureNo);
    }
}
