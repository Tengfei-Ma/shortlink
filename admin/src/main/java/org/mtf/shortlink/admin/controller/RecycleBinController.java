package org.mtf.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.common.convention.result.Results;
import org.mtf.shortlink.admin.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.admin.remote.ShortlinkRemoteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {
    ShortlinkRemoteService shortlinkRemoteService=new ShortlinkRemoteService() {
    };
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/save")
    public Result<Void> createRecycleBin(@RequestBody RecycleBinCreateReqDTO requestParam){
        shortlinkRemoteService.createRecycleBin(requestParam);
        return Results.success();
    }
}
