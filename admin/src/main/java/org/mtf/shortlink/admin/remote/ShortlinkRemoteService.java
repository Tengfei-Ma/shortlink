package org.mtf.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.admin.remote.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkPageRespDTO;

import java.util.HashMap;
import java.util.Map;


public interface ShortlinkRemoteService {
    default Result<ShortlinkCreateRespDTO> createShortLink(ShortlinkCreateReqDTO requestParam) {
        String resp = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }


    default Result<IPage<ShortlinkPageRespDTO>> pageShortlink(ShortlinkPageReqDTO requestParam) {
        Map<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String resp = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/page",map);
        return JSON.parseObject(resp, new TypeReference<>() {
        });
    }
}
