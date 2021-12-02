package com.gsitm.intern.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController {

    @GetMapping(value= "/admin/noticeStats")
    public String stats() {

        return "/notice/noticeStats";
    }
}
