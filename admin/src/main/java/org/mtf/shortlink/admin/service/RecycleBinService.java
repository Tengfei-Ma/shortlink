package org.mtf.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mtf.shortlink.admin.common.convention.result.Result;
import org.mtf.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.mtf.shortlink.admin.remote.dto.resp.ShortlinkPageRespDTO;

/**
 * 回收站接口层，只针对分页查询
 */
public interface RecycleBinService {
    /**
     * 分页查询
     * @param requestParam 请求参数实体
     * @return 响应参数实体
     */
    Result<IPage<ShortlinkPageRespDTO>> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam);
}
