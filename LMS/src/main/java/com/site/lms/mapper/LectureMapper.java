package com.site.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.site.lms.entity.Lecture;

@Mapper
public interface LectureMapper {
    int existsById(@Param("lectureNo") Long lectureNo);
    Lecture findById(@Param("lectureNo") Long lectureNo);
    List<Lecture> findByInstructor(@Param("instructorNo") Long instructorNo);
    int deleteById(@Param("lectureNo") Long lectureNo);
    int update(Lecture lecture);

    List<Lecture> findByVisibility(@Param("visibility") Integer visibility);
    List<Lecture> findByIds(@Param("ids") List<Long> ids);
    /** 관리자가 새 과목을 등록 */
    void insertLecture(Lecture lecture);
}
