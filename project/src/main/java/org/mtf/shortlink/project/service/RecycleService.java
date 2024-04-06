package org.mtf.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dto.req.RecycleBinCreateReqDTO;

/**
 * 回收站接口层
 */
public interface RecycleService extends IService<ShortlinkDO> {
    /**
     * 短链接移至回收站
     * @param requestParam 创建回收站记录请求参数实体
     */
    void createRecycleBin(RecycleBinCreateReqDTO requestParam);
}
