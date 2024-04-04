package org.mtf.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;

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

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 分页查询短链接相应响应参数
     */
    IPage<ShortlinkPageRespDTO> pageShortlink(ShortlinkPageReqDTO requestParam);
}
