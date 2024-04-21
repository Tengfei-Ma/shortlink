package org.mtf.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.remote.ShortlinkActualRemoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标题控制层
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    private final ShortlinkActualRemoteService shortlinkActualRemoteService;

    /**
     * 根据原始链接获取网站标题
     */
    @GetMapping("/api/shortlink/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url){
        return shortlinkActualRemoteService.getTitleByUrl(url);
    }
}
