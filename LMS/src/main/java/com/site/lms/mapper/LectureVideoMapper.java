// src/main/java/com/site/lms/mapper/LectureVideoMapper.java
package com.site.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.site.lms.entity.LectureVideo;

@Mapper
public interface LectureVideoMapper {

    void save(LectureVideo video);

    LectureVideo findById(@Param("videoNo") Long videoNo);

    List<LectureVideo> findByLectureNo(@Param("lectureNo") Long lectureNo);

    int countByLectureNo(@Param("lectureNo") Long lectureNo);

    List<Long> findLectureNosByTopKeyword(@Param("keyword") String keyword);

    int update(LectureVideo video);

    int deleteById(@Param("videoNo") Long videoNo);

    List<LectureVideo> findByIds(@Param("ids") List<Long> ids);

    // ─── 업로더 기준 과목 번호만 조회 ─────────────────────────
    List<Long> findLectureNosByUploader(@Param("uploaderNo") Long uploaderNo);

    // ─── 과목 번호 + 업로더 기준으로 영상만 조회 ────────────────
    List<LectureVideo> findByLectureNoAndUploader(
        @Param("lectureNo") Long lectureNo,
        @Param("uploaderNo") Long uploaderNo
    );
    
    // ★ 추가: 특정 과목의 모든 영상을 삭제하는 매퍼 메서드
    void deleteByLecture(@Param("lectureNo") Long lectureNo);
    
    // ─── 회원·영상별 시청 진도율 조회 ──────────────────────────
    @Select("""
      SELECT pr.PROGRESS_PCT
        FROM VIDEO_PROGRESS pr
       WHERE pr.MEMBER_NO = #{memberNo}
         AND pr.VIDEO_NO  = #{videoNo}
    """)
    Integer findProgressPct(
      @Param("memberNo") Long memberNo,
      @Param("videoNo")  Long videoNo
    );
}