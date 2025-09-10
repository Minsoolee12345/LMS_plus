package com.site.lms.entity;

import lombok.Data;

/**
 * PROGRESS 테이블과 매핑될 엔티티 (MyBatis 사용 시 파라미터 객체로 활용)
 */
@Data
public class Progress 
{
    private Long memberNo;   // MEMBER.MEMBER_NO
    private Long videoNo;    // LECTURE_VIDEO.VIDEO_NO
    private Integer progress; // 0~100 (퍼센트)
}