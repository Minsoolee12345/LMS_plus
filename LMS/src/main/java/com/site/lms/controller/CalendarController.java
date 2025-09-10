package com.site.lms.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.site.lms.service.CalendarService;
import com.site.lms.service.UserService;

@Controller
public class CalendarController {

    private final CalendarService calendarService;
    private final UserService userService;

    public CalendarController(CalendarService calendarService,
                              UserService userService) {
        this.calendarService = calendarService;
        this.userService = userService;
    }

    @PostMapping("/calendar/add")
    public String addLecture(@RequestParam("lectureNo") Long lectureNo,
                             Principal principal) {
        var user = userService.findByUsername(principal.getName()).orElseThrow();
        calendarService.addLectureSchedule(user.getId(), lectureNo);
        return "redirect:/";
    }

    @PostMapping("/calendar/shift")
    public String shift(@RequestParam("eventId") Long eventId) {
        calendarService.shiftSchedule(eventId, LocalDate.now().plusDays(1));
        return "redirect:/";
    }

    /** AJAX 로 개별 이벤트 삭제 */
    @PostMapping("/calendar/remove")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void removeEvent(@RequestParam("eventId") Long eventId) {
        calendarService.removeEvent(eventId);
    }

    /** 강의 단위로 캘린더 전체 제거 */
    @PostMapping("/calendar/removeLecture")
    public String removeLecture(@RequestParam("lectureNo") Long lectureNo,
                                Principal principal) {
        var user = userService.findByUsername(principal.getName()).orElseThrow();
        calendarService.removeLectureSchedule(user.getId(), lectureNo);
        return "redirect:/";
    }
}