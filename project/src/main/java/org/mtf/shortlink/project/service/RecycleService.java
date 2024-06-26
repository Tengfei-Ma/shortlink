package org.mtf.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dto.req.RecycleBinCreateReqDTO;
import org.mtf.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import org.mtf.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkRecycleBinPageReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;

/**
 * 回收站接口层
 */
public interface RecycleService extends IService<ShortlinkDO> {
    /**
     * 短链接移至回收站
     * @param requestParam 创建回收站记录请求参数实体
     */
    void createRecycleBin(RecycleBinCreateReqDTO requestParam);
    /**
     * 分页查询回收站
     * @param requestParam 分页查询短链接请求参数
     * @return 分页查询短链接相应响应参数
     */
    IPage<ShortlinkPageRespDTO> pageRecycleBin(ShortlinkRecycleBinPageReqDTO requestParam);

    /**
     * 短链接从回收站恢复
     * @param requestParam 回收站恢复请求参数实体
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    /**
     * 短链接从回收站彻底删除
     * @param requestParam 回收站删除请求参数实体
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
