package com.gsitm.intern.controller;

import com.gsitm.intern.entity.EmailNotice;
import com.gsitm.intern.repository.EmailNoticeRepository;
import com.gsitm.intern.repository.SmsNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class StatsController {

    private final EmailNoticeRepository emailNoticeRepository;
    private final SmsNoticeRepository smsNoticeRepository;

    @GetMapping(value= "/admin/noticeStats")
    public String stats(Model model, EmailNotice emailNotice) {

        long emailCount = emailNoticeRepository.count();
        long smsCount = smsNoticeRepository.count();

        model.addAttribute("emailCount", emailCount);
        model.addAttribute("smsCount", smsCount);

        return "/notice/noticeStats";
    }
}
