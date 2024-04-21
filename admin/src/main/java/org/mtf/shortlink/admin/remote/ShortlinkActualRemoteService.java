package org.mtf.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import org.mtf.shortlink.admin.dto.req.RecycleBinRemoveReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkBatchCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 使用Feign向中台发起远程调用
 */
@FeignClient("shortlink-project")
public interface ShortlinkActualRemoteService {
    /**
     * 新增短链接
     *
     * @param requestParam 创建短链接请求实体
     * @return 创建短链接响应实体
     */
    @PostMapping("/api/shortlink/v1/link")
    Result<ShortlinkCreateRespDTO> createShortlink(@RequestBody ShortlinkCreateReqDTO requestParam);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求实体
     * @return 批量创建短链接响应实体
     */
    @PostMapping("/api/shortlink/v1/link/batch")
    Result<ShortlinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortlinkBatchCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     *
     * @param gid      分组标识
     * @param orderTag 排序类型
     * @param current  当前页
     * @param size     页大小
     * @return 分页响应参数实体
     */
    @GetMapping("/api/shortlink/v1/link/page")
    Result<Page<ShortlinkPageRespDTO>> pageShortlink(@RequestParam("gid") String gid,
                                                     @RequestParam("orderTag") String orderTag,
                                                     @RequestParam("current") Long current,
                                                     @RequestParam("size") Long size);

    /**
     * 查询分组内短链接数量
     *
     * @param requestParam 分组标识列表
     * @return 各分组内短链接数量
     */
    @GetMapping("/api/shortlink/v1/link/count")
    Result<List<ShortlinkGroupCountRespDTO>> listGroupShortlinkCount(@RequestParam("requestParam") List<String> requestParam);

    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求实体
     */
    @PutMapping("/api/shortlink/v1/link")
    void updateShortlink(@RequestBody ShortlinkUpdateReqDTO requestParam);

    /**
     * 根据原始链接获取网站标题
     *
     * @param url 原始链接
     * @return 网站标题
     */
    @GetMapping("/api/shortlink/v1/title")
    Result<String> getTitleByUrl(@RequestParam("url") String url);

    /**
     * 短链接移至回收站
     *
     * @param requestParam 请求参数实体
     */
    @PostMapping("/api/shortlink/v1/recycle-bin")
    void createRecycleBin(@RequestBody RecycleBinCreateReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     *
     * @param gidList 分组标识集合
     * @param current 当前页
     * @param size    页大小
     * @return 分页响应参数实体
     */
    @GetMapping("/api/shortlink/v1/recycle-bin/page")
    Result<Page<ShortlinkPageRespDTO>> pageRecycleBin(@RequestParam("gidList") List<String> gidList,
                                                      @RequestParam("current") Long current,
                                                      @RequestParam("size") Long size);


    /**
     * 短链接从回收站恢复
     *
     * @param requestParam 请求参数实体
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam);

    /**
     * 短链接从回收站彻底删除
     *
     * @param requestParam 请求参数实体
     */
    @PostMapping("/api/shortlink/v1/recycle-bin/remove")
    void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam);

    /**
     * 查询单个短链接监控数据
     *
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param startDate    开始时间
     * @param endDate      结束时间
     * @return 短链接监控响应参数
     */
    @GetMapping("/api/shortlink/v1/stats")
    Result<ShortlinkStatsRespDTO> oneShortlinkStats(@RequestParam("fullShortUrl") String fullShortUrl,
                                                    @RequestParam("gid") String gid,
                                                    @RequestParam("startDate") String startDate,
                                                    @RequestParam("endDate") String endDate);

    /**
     * 查询单个短链接访问记录
     *
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param startDate    开始时间
     * @param endDate      结束时间
     * @param current      当前页
     * @param size         一页数据量
     * @return 分页短链接访问记录监控响应参数
     */
    @GetMapping("/api/shortlink/v1/stats/access-record")
    Result<Page<ShortlinkStatsAccessRecordRespDTO>> shortlinkAccessRecord(@RequestParam("fullShortUrl") String fullShortUrl,
                                                                          @RequestParam("gid") String gid,
                                                                          @RequestParam("startDate") String startDate,
                                                                          @RequestParam("endDate") String endDate,
                                                                          @RequestParam("current") Long current,
                                                                          @RequestParam("size") Long size);

    /**
     * 查询分组短链接监控数据
     *
     * @param gid       分组标识
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 短链接监控响应参数
     */
    @GetMapping("/api/shortlink/v1/stats/group")
    Result<ShortlinkStatsRespDTO> groupShortlinkStats(@RequestParam("gid") String gid,
                                                      @RequestParam("startDate") String startDate,
                                                      @RequestParam("endDate") String endDate);

    /**
     * 查询分组短链接访问记录
     * @param gid       分组标识
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @param current   当前页
     * @param size      页大小
     * @return 分页短链接访问记录监控响应参数
     */
    @GetMapping("/api/shortlink/v1/stats/access-record/group")
    Result<Page<ShortlinkStatsAccessRecordRespDTO>> groupShortlinkStatsAccessRecord(@RequestParam("gid") String gid,
                                                                                    @RequestParam("startDate") String startDate,
                                                                                    @RequestParam("endDate") String endDate,
                                                                                    @RequestParam("current") Long current,
                                                                                    @RequestParam("size") Long size);


}
