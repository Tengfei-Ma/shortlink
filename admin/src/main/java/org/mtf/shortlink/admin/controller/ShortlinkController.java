package org.mtf.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.common.convention.result.Results;
import org.mtf.shortlink.admin.remote.ShortlinkRemoteService;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ShortlinkController {
    ShortlinkRemoteService shortlinkRemoteService=new ShortlinkRemoteService() {
    };
    /**
     * 创建短链接
     */
    @PostMapping("/api/shortlink/admin/v1/create")
    public Result<ShortlinkCreateRespDTO> createShortLink(@RequestBody ShortlinkCreateReqDTO requestParam){
        return shortlinkRemoteService.createShortlink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortlink/admin/v1/page")
    public Result<IPage<ShortlinkPageRespDTO>> pageShortlink(@RequestBody ShortlinkPageReqDTO requestParam){
        return shortlinkRemoteService.pageShortlink(requestParam);
    }
    /**
     * 查询分组内短连接数量
     */
    @GetMapping("/api/shortlink/admin/v1/count")
    public Result<List<ShortlinkGroupCountRespDTO>> listGroupShortlinkCount(@RequestParam("requestParam") List<String> requestParam){
        return shortlinkRemoteService.listGroupShortlinkCount(requestParam);
    }
    /**
     * 修改短链接
     */
    @PutMapping("/api/shortlink/admin/v1/update")
    public Result<Void> updateShortlink(@RequestBody ShortlinkUpdateReqDTO requestParam){
        shortlinkRemoteService.updateShortlink(requestParam);
        return Results.success();
    }
}
