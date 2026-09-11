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

    @GetMapping("/departments")
    public String departments() {
        return "departments/index";
    }

    @GetMapping("/positions")
    public String positions() {
        return "positions/index";
    }

    @GetMapping("/attendance")
    public String attendance() {
        return "attendance/index";
    }

    @GetMapping("/leaves")
    public String leaves() {
        return "leaves/index";
    }
}
