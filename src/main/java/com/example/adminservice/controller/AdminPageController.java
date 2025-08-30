package com.example.adminservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/admin")
@Tag(name = "관리자 페이지", description = "관리자용 웹 페이지")
public class AdminPageController {

    @Operation(summary = "배너 관리 페이지", description = "드래그 앤 드롭으로 배너 순서를 관리할 수 있는 페이지")
    @GetMapping("/banners")
    public String bannerManagementPage() {
        log.info("배너 관리 페이지 접근 요청");
        return "redirect:/banner-management.html";
    }
}