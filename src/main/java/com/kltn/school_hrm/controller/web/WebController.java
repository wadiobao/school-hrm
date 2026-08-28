package com.kltn.school_hrm.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebController {

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/employees")
    public String employees() {
        return "employees/list";
    }
}
