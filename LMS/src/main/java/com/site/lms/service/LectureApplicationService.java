package com.site.lms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.site.lms.entity.Lecture;
import com.site.lms.entity.Subscription;
import com.site.lms.entity.User;

@Service
public class LectureApplicationService {

    private final SubscriptionService subscriptionService;
    private final LectureService      lectureService;
    private final UserService         userService;

    public LectureApplicationService(
            SubscriptionService subscriptionService,
            LectureService      lectureService,
            UserService         userService
    ) {
        this.subscriptionService = subscriptionService;
        this.lectureService      = lectureService;
        this.userService         = userService;
    }

    /**
     * 학생(studentId)이 lectureNo 번 비공개 강의를 신청하고,
     * 해당 강의를 올린 강사에게 메시지를 보낸다.
     */
    @Transactional
    public void applyToLecture(Long studentId, Long lectureNo) {
        // 1) 신청 저장 (PENDING 상태로)
        subscriptionService.requestSubscription(studentId, lectureNo);

        // 2) 강의 조회 및 존재 확인
        Lecture lecture = lectureService.findById(lectureNo);
        if (lecture == null) {
            throw new IllegalArgumentException("강의를 찾을 수 없습니다: " + lectureNo);
        }

        // 3) 강사·학생 조회
        User instructor = userService.findById(lecture.getInstructorNo())
            .orElseThrow(() -> new IllegalArgumentException("강사를 찾을 수 없습니다: " + lecture.getInstructorNo()));
        User student = userService.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + studentId));

        // 4) 강사에게 알림 메시지
        String content = "<수강신청 요청> 강의: ["
                       + lecture.getTitle()
                       + "] 신청자: "
                       + student.getUsername();
        userService.messageSave(
            instructor.getUsername(),   // 수신자: 강사
            student.getUsername(),      // 발신자: 학생
            content
        );
    }
    

    
    /**
     * 강사가 subId 번 수강신청을 수락한다.
     */
    @Transactional
    public void acceptApplication(String instructorUsername, Long subId) {
        // 1) 신청 정보 조회
        Subscription sub = subscriptionService.getById(subId)
            .orElseThrow(() -> new IllegalArgumentException("수강신청을 찾을 수 없습니다: " + subId));

        // 2) 강의 조회 및 존재 확인
        Lecture lecture = lectureService.findById(sub.getLectureNo());
        if (lecture == null) {
            throw new IllegalArgumentException("강의를 찾을 수 없습니다: " + sub.getLectureNo());
        }

        // 3) 강사 조회
        User instructor = userService.findByUsername(instructorUsername)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + instructorUsername));

        // 4) 권한 확인: 안전한 equals 비교
        if (!instructor.getId().equals(lecture.getInstructorNo())) {
            throw new SecurityException("본인의 강의가 아닌 수강신청을 처리할 수 없습니다.");
        }

        // 5) 상태 변경
        subscriptionService.updateStatus(subId, "APPROVED");

        // 6) 학생에게 알림
        User student = userService.findById(sub.getMemberNo())
            .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sub.getMemberNo()));
        String content = "<수강신청 수락> 강의: ["
                       + lecture.getTitle()
                       + "] 신청이 승인되었습니다.";
        userService.messageSave(
            student.getUsername(),      // 수신자: 학생
            instructorUsername,         // 발신자: 강사
            content
        );
    }

    /**
     * 강사가 subId 번 수강신청을 거절한다.
     */
    @Transactional
    public void rejectApplication(String instructorUsername, Long subId) {
        // 1) 신청 정보 조회
        Subscription sub = subscriptionService.getById(subId)
            .orElseThrow(() -> new IllegalArgumentException("수강신청을 찾을 수 없습니다: " + subId));

        // 2) 강의 조회 및 존재 확인
        Lecture lecture = lectureService.findById(sub.getLectureNo());
        if (lecture == null) {
            throw new IllegalArgumentException("강의를 찾을 수 없습니다: " + sub.getLectureNo());
        }

        // 3) 강사 조회
        User instructor = userService.findByUsername(instructorUsername)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + instructorUsername));

        // 4) 권한 확인
        if (!instructor.getId().equals(lecture.getInstructorNo())) {
            throw new SecurityException("본인의 강의가 아닌 수강신청을 처리할 수 없습니다.");
        }

        // 5) 상태 변경
        subscriptionService.updateStatus(subId, "REJECTED");

        // 6) 학생에게 알림
        User student = userService.findById(sub.getMemberNo())
            .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: " + sub.getMemberNo()));
        String content = "<수강신청 거절> 강의: ["
                       + lecture.getTitle()
                       + "] 신청이 거절되었습니다.";
        userService.messageSave(
            student.getUsername(),      // 수신자: 학생
            instructorUsername,         // 발신자: 강사
            content
        );
    }
}
