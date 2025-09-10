// src/main/java/com/site/lms/mapper/SubscriptionMapper.java
package com.site.lms.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.site.lms.entity.Subscription;

@Mapper
public interface SubscriptionMapper {
    void insert(Subscription sub);

    List<Subscription> findByMember(@Param("memberNo") Long memberNo);

    Subscription findByMemberAndLecture(
        @Param("memberNo")  Long memberNo,
        @Param("lectureNo") Long lectureNo
    );

    Subscription findById(@Param("subId") Long subId);

    void updateStatus(
        @Param("subId") Long subId,
        @Param("status") String status
    );

    List<Subscription> findByLecture(@Param("lectureNo") Long lectureNo);
    
    /** 특정 강의의 모든 subscription 삭제 */
    void deleteByLecture(@Param("lectureNo") Long lectureNo);
}
