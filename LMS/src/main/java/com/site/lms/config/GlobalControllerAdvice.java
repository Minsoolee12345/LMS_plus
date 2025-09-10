package com.site.lms.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.site.lms.entity.User;
import com.site.lms.service.UserService;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserService userService;

    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    /**
     * 모든 컨트롤러의 뷰에서 ${authorization} 을 쓸 수 있도록 세션 기반으로 권한을 채워 준다.
     * 만약 비로그인 상태라면 null이나 1(기본 권한) 정도로 처리하면 됩니다.
     */
    @ModelAttribute("authorization")
    public Integer addAuthorizationToModel(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null; // 또는 1(ROLE_USER) 같은 기본값 
        }
        // 로그인된 사용자의 User 엔티티 조회 후, authority 반환
        User u = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return u.getAuthority();
    }

    /**
     * 모든 컨트롤러의 뷰에서 ${username} 을 쓸 수 있도록 채워 준다.
     */
    @ModelAttribute("username")
    public String addUsernameToModel(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userDetails.getUsername();
    }
}
