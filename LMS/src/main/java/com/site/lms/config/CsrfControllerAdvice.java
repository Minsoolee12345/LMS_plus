// src/main/java/com/site/lms/config/CsrfControllerAdvice.java
package com.site.lms.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 @Controller 의 뷰 모델에 CsrfToken(_csrf) 을 자동으로 추가해 줍니다.
 */
@ControllerAdvice
public class CsrfControllerAdvice {

    // 리턴 값이 모델에 올라가며, 이름은 "_csrf" 로 고정됩니다.
    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(CsrfToken token) {
        // Spring Security 의 CsrfFilter 가 request attribute 로 심어 놓은 token
        // (만약 CSRF 기능을 disable 해 두셨다면 token 이 null 이 됩니다)
        return token;
    }
}
