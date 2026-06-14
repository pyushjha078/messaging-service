package com.messaging.service.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class H2ConsoleRedirectController {

    @GetMapping("/h2-console")
    public String redirectToConsole() {
        return "redirect:/h2-console/";
    }
}
