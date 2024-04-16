package org.mtf.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.mtf.shortlink.project.common.database.BaseDO;

import java.util.Date;

/**
 * 短连接访问网络统计实体
 */
@Data
@TableName("t_link_network_stats")
public class LinkNetworkStatsDO extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 访问网络
     */
    private String network;
}
