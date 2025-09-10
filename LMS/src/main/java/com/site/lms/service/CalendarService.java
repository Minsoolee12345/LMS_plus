// src/main/java/com/site/lms/service/CalendarService.java
package com.site.lms.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.site.lms.entity.CalendarEvent;
import com.site.lms.entity.LectureVideo;
import com.site.lms.mapper.CalendarEventMapper;

/** 캘린더 + 진도율 관리 */
@Service
public class CalendarService {

    private final CalendarEventMapper mapper;
    private final LectureVideoService lectureVideoService;
    private final ProgressService     progressService;   // ★ 추가
    private final UserService         userService;       // ★ 추가

    public CalendarService(CalendarEventMapper mapper,
                           LectureVideoService lectureVideoService,
                           ProgressService     progressService,   // ★
                           UserService         userService) {     // ★
        this.mapper               = mapper;
        this.lectureVideoService  = lectureVideoService;
        this.progressService      = progressService;   // ★
        this.userService          = userService;       // ★
    }

    /*───────────────────────────────────────────────────────────────*/
    /** 회원의 모든 캘린더 이벤트 (진도율 + 상태) 갱신 */
    @Transactional
    public void updateStatuses(Long memberNo) {

        String username = userService.findById(memberNo)            // ★
                             .orElseThrow().getUsername();          // ★

        LocalDate today = LocalDate.now();
        List<CalendarEvent> events = mapper.findByMember(memberNo);

        for (CalendarEvent ev : events) {

            /* 1) 최신 진도율 조회 ----------------------------------- */
            int pct = progressService.getVideoProgress(username,     // ★
                                                       ev.getVideoNo());
            /* 2) 상태 계산 ------------------------------------------ */
            String newStatus;
            if (pct >= 100)                     newStatus = "COMPLETED";
            else if (ev.getEventDate()
                       .isBefore(today))        newStatus = "MISSED";
            else                                newStatus = "PENDING";

            /* 3) 변경 사항이 있으면 DB 반영 ------------------------- */
            if (pct != ev.getProgressPct() || !newStatus.equals(ev.getStatus())) {
                mapper.updateProgressAndStatus(                       // ★ 새 mapper 메서드
                    ev.getEventId(), pct, newStatus);
                ev.setProgressPct(pct);
                ev.setStatus(newStatus);
            }
        }
    }
    /*───────────────────────────────────────────────────────────────*/

    /** 공개 강의 선택 → 챕터별로 하루씩 일정 생성 */
    public void addLectureSchedule(Long memberNo, Long lectureNo) {
        List<LectureVideo> videos = lectureVideoService.findByLectureNo(lectureNo);
        LocalDate date = LocalDate.now();
        for (LectureVideo vid : videos) {
            mapper.insert(memberNo, vid.getVideoNo(), date);
            date = date.plusDays(1);
        }
    }

    /** 개별 이벤트를 다른 날짜로 이동 */
    public void shiftSchedule(Long eventId, LocalDate newDate) {
        mapper.shiftEvent(eventId, newDate);
    }

    /** 회원의 모든 이벤트 조회 */
    public List<CalendarEvent> getEventsForMember(Long memberNo) {
        return mapper.findByMember(memberNo);
    }

    /** 개별 이벤트 삭제 */
    public void removeEvent(Long eventId) {
        mapper.deleteEvent(eventId);
    }

    /** 강의 단위로 이벤트 일괄 삭제 */
    public void removeLectureSchedule(Long memberNo, Long lectureNo) {
        mapper.deleteByLecture(memberNo, lectureNo);
    }
}