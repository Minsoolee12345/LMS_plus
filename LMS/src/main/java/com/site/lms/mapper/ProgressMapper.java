package com.site.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 수강생-영상별 진도 정보를 가져오거나 저장하기 위한 MyBatis 매퍼
 */
@Mapper
public interface ProgressMapper 
{

    /**
     * 특정 회원(memberNo)이 특정 영상(videoNo)에서 진행한 퍼센트를 조회
     * @param memberNo 회원 번호
     * @param videoNo  영상 번호
     * @return 진행 퍼센트 (없으면 null 반환)
     */
    Integer findByMemberAndVideo(
        @Param("memberNo") Long memberNo,
        @Param("videoNo")  Long videoNo
    );

    /**
     * 특정 회원+강의(lectureNo)에 속한 모든 영상의 진행 퍼센트 리스트를 가져옴
     * (영상이 없는 경우엔 결과에 포함되지 않으므로, 후처리 시 “0”으로 간주해야 함)
     * @param memberNo  회원 번호
     * @param lectureNo 강의(코스) 번호
     * @return {0~100} 퍼센트 리스트
     */
    List<Integer> findProgressByMemberAndLecture(
        @Param("memberNo")  Long memberNo,
        @Param("lectureNo") Long lectureNo
    );

    /**
     * 회원+영상 조합이 이미 존재하면 업데이트, 없으면 INSERT
     * (Oracle MERGE 문 활용)
     * @param memberNo 회원 번호
     * @param videoNo  영상 번호
     * @param progress 퍼센트 (0~100)
     */
    void insertOrUpdate(
        @Param("memberNo") Long memberNo,
        @Param("videoNo")  Long videoNo,
        @Param("progress") Integer progress
    );
    
    void deleteByLecture(@Param("lectureNo") Long lectureNo);
}