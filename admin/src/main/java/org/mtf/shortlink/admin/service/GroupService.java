package org.mtf.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.admin.dao.entity.GroupDO;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.mtf.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.mtf.shortlink.admin.dto.resp.ShortlinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分组
     * @param requestParam 分组名称
     */
    void saveGroup(ShortLinkGroupSaveReqDTO requestParam);

    /**
     * 查询短链接分组
     * @return 短链接响应实体列表
     */
    List<ShortlinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     * @param requestParam 分组标识和名称
     */
    void update(ShortLinkGroupUpdateReqDTO requestParam);
}
