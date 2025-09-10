// src/main/java/com/site/lms/controller/ErrorController.java
package com.site.lms.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // error.html 템플릿을 렌더링
        return "error";
    }
}