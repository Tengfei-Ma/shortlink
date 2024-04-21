package org.mtf.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.common.convention.result.Results;
import org.mtf.shortlink.admin.remote.ShortlinkActualRemoteService;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkBatchCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.*;
import org.mtf.shortlink.admin.toolkit.EasyExcelWebUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortlinkController {
    private final ShortlinkActualRemoteService shortlinkActualRemoteService;
    /**
     * 创建短链接
     */
    @PostMapping("/api/shortlink/admin/v1/link")
    public Result<ShortlinkCreateRespDTO> createShortlink(@RequestBody ShortlinkCreateReqDTO requestParam){
        return shortlinkActualRemoteService.createShortlink(requestParam);
    }
    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/shortlink/admin/v1/link/batch")
    public void batchCreateShortlink(@RequestBody ShortlinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortlinkBatchCreateRespDTO> shortlinkBatchCreateRespDTOResult = shortlinkActualRemoteService.batchCreateShortLink(requestParam);
        if (shortlinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortlinkBaseInfoRespDTO> baseLinkInfos = shortlinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortlinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }
    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortlink/admin/v1/link/page")
    public Result<Page<ShortlinkPageRespDTO>> pageShortlink(ShortlinkPageReqDTO requestParam){
        return shortlinkActualRemoteService.pageShortlink(requestParam.getGid(),requestParam.getOrderTag(),requestParam.getCurrent(),requestParam.getSize());
    }
    /**
     * 查询分组内短连接数量
     */
    @GetMapping("/api/shortlink/admin/v1/link/count")
    public Result<List<ShortlinkGroupCountRespDTO>> listGroupShortlinkCount(@RequestParam("requestParam") List<String> requestParam){
        return shortlinkActualRemoteService.listGroupShortlinkCount(requestParam);
    }
    /**
     * 修改短链接
     */
    @PutMapping("/api/shortlink/admin/v1/link")
    public Result<Void> updateShortlink(@RequestBody ShortlinkUpdateReqDTO requestParam){
        shortlinkActualRemoteService.updateShortlink(requestParam);
        return Results.success();
    }
}
