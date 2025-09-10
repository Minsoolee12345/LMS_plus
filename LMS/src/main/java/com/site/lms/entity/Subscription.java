package com.site.lms.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Subscription 
{
    private Long subId;
    private Long memberNo;
    private Long lectureNo;
    private String status;       // PENDING, APPROVED
    private LocalDateTime requestDate;
}