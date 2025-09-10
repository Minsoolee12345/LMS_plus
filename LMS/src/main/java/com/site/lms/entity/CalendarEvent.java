package com.site.lms.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CalendarEvent
{
    private Long eventId;
    private Long memberNo;
    private Long videoNo;
    private LocalDate eventDate;
    private String status;         // SCHEDULED, COMPLETED, MISSED
    private LocalDateTime createdDate;
    private Integer progressPct;   // 진도(%)
}
