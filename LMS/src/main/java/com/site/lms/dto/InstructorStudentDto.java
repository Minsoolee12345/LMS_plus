// 변경 후
package com.site.lms.dto;

public record InstructorStudentDto(
  Long subId,
  String username,
  int progress,
  String status
) {}
