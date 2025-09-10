// src/main/java/com/site/lms/service/SubscriptionService.java
package com.site.lms.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.site.lms.entity.Subscription;
import com.site.lms.mapper.SubscriptionMapper;

@Service
public class SubscriptionService {
    private final SubscriptionMapper mapper;

    public SubscriptionService(SubscriptionMapper mapper) {
        this.mapper = mapper;
    }

    /** 학생이 신청(PENDING)하기 - 거절된 경우에도 상태를 PENDING 으로 되살린다 */
    public void requestSubscription(Long memberNo, Long lectureNo) {
        Subscription existing = mapper.findByMemberAndLecture(memberNo, lectureNo);

        // 1) 처음 신청 → INSERT
        if (existing == null) {
            Subscription sub = new Subscription();
            sub.setMemberNo(memberNo);
            sub.setLectureNo(lectureNo);
            sub.setStatus("PENDING");
            mapper.insert(sub);
            return;
        }

        // 2) 이미 REJECTED/EXCLUDED → 상태만 PENDING 으로 갱신
        if (!"PENDING".equals(existing.getStatus())) {
            mapper.updateStatus(existing.getSubId(), "PENDING");
        }
        // 3) PENDING, APPROVED 상태면 추가 동작 없음
    }

    /** 학생 화면: 회원별 신청(구독) 목록 조회 */
    public List<Subscription> getSubscriptions(Long memberNo) {
        return mapper.findByMember(memberNo);
    }

    /** 강사 화면: 강의별 신청 목록 조회 */
    public List<Subscription> getByLecture(Long lectureNo) {
        return mapper.findByLecture(lectureNo);
    }

    /** subId 로 단건 조회 */
    public Optional<Subscription> getById(Long subId) {
        return Optional.ofNullable(mapper.findById(subId));
    }

    /** subId 의 상태를 변경 (APPROVED 또는 REJECTED) */
    public void updateStatus(Long subId, String status) {
        mapper.updateStatus(subId, status);
    }
    
    /** 강의 삭제 전에 해당 강의의 모든 구독 내역을 제거 */
    public void deleteByLecture(Long lectureNo) {
        mapper.deleteByLecture(lectureNo);
    }
}