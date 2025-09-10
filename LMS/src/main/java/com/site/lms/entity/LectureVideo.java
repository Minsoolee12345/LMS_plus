package com.site.lms.entity;

import lombok.Data;

@Data
public class LectureVideo {
    private Long videoNo;
    private Long lectureNo;
    private String videoPath;
    private String videoDesc;
    private String wordset;
    private String rawText;
    private String segmentsJson;
    private Integer visibility;
    private Long uploaderNo;
    private String summary;
}