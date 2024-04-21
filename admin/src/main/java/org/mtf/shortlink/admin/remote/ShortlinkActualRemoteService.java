package org.mtf.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import org.mtf.shortlink.admin.dto.req.RecycleBinRemoveReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.*;
import org.mtf.shortlink.admin.remote.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 使用Feign向中台发起远程调用
 */
@FeignClient("shortlink-project")
public interface ShortlinkActualRemoteService {
    /**
     * 新增短链接
     * @param requestParam 创建短链接请求实体
     * @return 创建短链接响应实体
     */
    @PostMapping("/api/shortlink/v1/link")
    Result<ShortlinkCreateRespDTO> createShortlink(@RequestBody ShortlinkCreateReqDTO requestParam);

    /**
     * 批量创建短链接
     * @param requestParam 批量创建短链接请求实体
     * @return 批量创建短链接响应实体
     */
    @PostMapping("/api/shortlink/v1/link/batch")
    Result<ShortlinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortlinkBatchCreateReqDTO requestParam);
    /**
     * 分页查询短链接
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
     * @param requestParam 分组标识列表
     * @return 各分组内短链接数量
     */@GetMapping("/api/shortlink/v1/link/count")
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
     * @param url 原始链接
     * @return 网站标题
     */
    default Result<String> getTitleByUrl(String url){
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/title?url="+url);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }

    /**
     * 短链接移至回收站
     * @param requestParam 请求参数实体
     */
    default void createRecycleBin(RecycleBinCreateReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin", JSON.toJSONString(requestParam));
    }

    /**
     * 分页查询回收站短链接
     * @param requestParam 分页请求参数实体
     * @return 分页响应参数实体
     */
    default Result<IPage<ShortlinkPageRespDTO>> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("gidList",requestParam.getGidList());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/page",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }

    /**
     * 短链接从回收站恢复
     * @param requestParam 请求参数实体
     */
    default void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
    }
    /**
     * 短链接从回收站彻底删除
     * @param requestParam 请求参数实体
     */
    default void removeRecycleBin(RecycleBinRemoveReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
    }

    /**
     * 查询单个短链接监控数据
     * @param requestParam 单个短链接访问监控请求参数
     * @return 短链接监控响应参数
     */
    default Result<ShortlinkStatsRespDTO> oneShortlinkStats(ShortlinkStatsReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("fullShortUrl",requestParam.getFullShortUrl());
        map.put("gid", requestParam.getGid());
        map.put("startDate", requestParam.getStartDate());
        map.put("endDate", requestParam.getEndDate());
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/stats",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }
    /**
     * 查询单个短链接访问记录
     * @param requestParam 单个短链接访问记录监控请求参数
     * @return 分页短链接访问记录监控响应参数
     */

    default Result<IPage<ShortlinkStatsAccessRecordRespDTO>> shortlinkAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("fullShortUrl",requestParam.getFullShortUrl());
        map.put("gid", requestParam.getGid());
        map.put("startDate", requestParam.getStartDate());
        map.put("endDate", requestParam.getEndDate());
        map.put("current",requestParam.getCurrent());
        map.put("size",requestParam.getSize());
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/stats/access-record",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }

    /**
     * 查询分组短链接监控数据
     * @param requestParam 分组短链接访问监控请求参数
     * @return 短链接监控响应参数
     */
    default Result<ShortlinkStatsRespDTO> groupShortlinkStats(ShortlinkGroupStatsReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("startDate", requestParam.getStartDate());
        map.put("endDate", requestParam.getEndDate());
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/stats/group",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }

    /**
     * 查询分组短链接访问记录
     * @param requestParam 分组短链接访问记录监控请求参数
     * @return 分页短链接访问记录监控响应参数
     */
    default Result<IPage<ShortlinkStatsAccessRecordRespDTO>> groupShortlinkStatsAccessRecord(ShortlinkGroupStatsAccessRecordReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("startDate", requestParam.getStartDate());
        map.put("endDate", requestParam.getEndDate());
        map.put("current",requestParam.getCurrent());
        map.put("size",requestParam.getSize());
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/stats/access-record/group",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }


}
