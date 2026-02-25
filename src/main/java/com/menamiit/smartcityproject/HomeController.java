package com.menamiit.smartcityproject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("src/main/resources/static/index.html")
    public String home() {
        return "index.html";
    }
}
