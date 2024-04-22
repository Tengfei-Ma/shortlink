package org.mtf.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
import org.mtf.shortlink.project.dto.req.ShortlinkBatchCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkCreateReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkPageReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkUpdateReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkBatchCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkCreateRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkGroupCountRespDTO;
import org.mtf.shortlink.project.dto.resp.ShortlinkPageRespDTO;

import java.util.List;

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
     * 根据分布式锁创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortlinkCreateRespDTO createShortlinkByLock(ShortlinkCreateReqDTO requestParam);
    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 分页查询短链接相应响应参数
     */
    IPage<ShortlinkPageRespDTO> pageShortlink(ShortlinkPageReqDTO requestParam);

    /**
     * 查询分组内短链接数量
     * @param requestParam 短链接分组标识列表
     * @return 各分组内短连接数量响应参数
     */
    List<ShortlinkGroupCountRespDTO> listGroupShortlinkCount(List<String> requestParam);

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求实体
     */
    void updateShortlink(ShortlinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转
     * @param shortUri 6位短链接
     * @param request 请求
     * @param response 响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);
    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 批量创建短链接返回参数
     */
    ShortlinkBatchCreateRespDTO batchCreateShortlink(ShortlinkBatchCreateReqDTO requestParam);
}
