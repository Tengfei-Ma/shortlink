package org.mtf.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;

/**
 * 短链接接口层
 */
public interface ShortlinkService extends IService<ShortlinkDO> {
    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数实体
     * @return 创建短链接返回参数实体
     */
    ShortlinkCreateRespDTO createShortLink(ShortlinkCreateReqDTO requestParam);
}
