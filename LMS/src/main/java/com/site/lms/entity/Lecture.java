package com.site.lms.entity;

import java.util.Date;

import lombok.Data;

@Data
public class Lecture {
    private Long lectureNo;
    private Long instructorNo;
    private String title;        // ← 추가
    private String lectureDesc;
    private Date createdDate;
    private Integer visibility;
}