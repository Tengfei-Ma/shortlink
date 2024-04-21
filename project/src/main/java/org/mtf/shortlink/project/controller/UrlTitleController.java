package org.mtf.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.service.UrlTitleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标题控制层
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    private final UrlTitleService urlTitleService;

    /**
     * 根据原始链接获取网站标题
     */
    @GetMapping("/api/shortlink/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url){
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
