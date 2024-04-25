package org.mtf.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mtf.shortlink.admin.dao.entity.GroupDO;
import org.mtf.shortlink.admin.dto.req.ShortlinkGroupUpdateReqDTO;
import org.mtf.shortlink.admin.dto.req.ShortlinkGroupSortReqDTO;
import org.mtf.shortlink.admin.dto.resp.ShortlinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 已登录用户新增短链接分组
     * @param name 短链接名
     */
    void saveGroup(String name);
    /**
     * 注册时新增默认短链接分组
     * @param username 注册用户名
     * @param name 短链接名
     */
    void saveGroup(String username,String name);

    /**
     * 查询短链接分组
     * @return 短链接响应实体列表
     */
    List<ShortlinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     * @param requestParam 分组标识和名称
     */
    void updateGroup(ShortlinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     * @param gid 分组标识
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam 分组标识和排序字段
     */
    void sortGroup(List<ShortlinkGroupSortReqDTO> requestParam);


}
