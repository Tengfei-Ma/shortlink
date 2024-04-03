package org.mtf.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.admin.dao.entity.GroupDO;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分组
     * @param requestParam 分组名称
     */
    void saveGroup(ShortLinkGroupSaveReqDTO requestParam);
}
