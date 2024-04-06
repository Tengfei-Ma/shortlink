package org.mtf.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface ShortlinkRemoteService {
    /**
     * 新增短链接
     * @param requestParam 创建短链接请求实体
     * @return 创建短链接响应实体
     */
    default Result<ShortlinkCreateRespDTO> createShortlink(ShortlinkCreateReqDTO requestParam) {
        String resp = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/link", JSON.toJSONString(requestParam));
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页请求参数实体
     * @return 分页响应参数实体
     */
    default Result<IPage<ShortlinkPageRespDTO>> pageShortlink(ShortlinkPageReqDTO requestParam) {
        Map<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/link/page",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }

    /**
     * 查询分组内短链接数量
     * @param requestParam 分组标识列表
     * @return 各分组内短链接数量
     */
    default Result<List<ShortlinkGroupCountRespDTO>> listGroupShortlinkCount(List<String> requestParam) {
        Map<String, Object> map = new HashMap<>();
        map.put("requestParam", requestParam);
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/link/count",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }
    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求实体
     */
    default void updateShortlink(ShortlinkUpdateReqDTO requestParam) {
        //TODO HttpUtil无法发送put请求？
        String resp = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/link/page",JSON.toJSONString(requestParam));
    }

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
}
