package org.mtf.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.common.convention.result.Result;
import org.mtf.shortlink.project.common.convention.result.Results;
import org.mtf.shortlink.project.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.project.service.RecycleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {
    private final RecycleService recycleService;
    @PostMapping("/api/shortlink/v1/recycle-bin")
    public Result<Void> createRecycleBin(@RequestBody RecycleBinCreateReqDTO requestParam){
        recycleService.createRecycleBin(requestParam);
        return Results.success();
    }
}
