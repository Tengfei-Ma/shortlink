package org.mtf.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.service.UrlTitleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 标题控制层
 */
@Controller
@RequiredArgsConstructor
public class UrlTitleController {
    private final UrlTitleService urlTitleService;

    /**
     * 根据原始链接获取网站标题
     */
    @RequestMapping("/api/shortlink/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url){
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
