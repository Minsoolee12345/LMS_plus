// src/main/java/com/site/lms/mapper/CalendarEventMapper.java
package com.site.lms.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.site.lms.entity.CalendarEvent;

@Mapper
public interface CalendarEventMapper {

    /* 삽입 ------------------------------------------------------- */
    void insert(
        @Param("memberNo")  Long memberNo,
        @Param("videoNo")   Long videoNo,
        @Param("eventDate") LocalDate eventDate
    );

    /* 조회 ------------------------------------------------------- */
    List<CalendarEvent> findByMember(@Param("memberNo") Long memberNo);

    /* 단순 상태만 바꾸던 기존 메서드(선택) ----------------------- */
    void updateStatus(
        @Param("eventId") Long eventId,
        @Param("status")  String status
    );

    /* ★ 진도율과 상태를 동시에 업데이트 -------------------------- */
    void updateProgressAndStatus(
        @Param("eventId") Long    eventId,
        @Param("pct")     Integer pct,
        @Param("status")  String  status
    );

    /* 날짜 이동 -------------------------------------------------- */
    void shiftEvent(
        @Param("eventId") Long      eventId,
        @Param("newDate") LocalDate newDate
    );

    /* 삭제(개별·강의 단위) -------------------------------------- */
    void deleteEvent(@Param("eventId") Long eventId);

    void deleteByLecture(
        @Param("memberNo")  Long memberNo,
        @Param("lectureNo") Long lectureNo
    );
}