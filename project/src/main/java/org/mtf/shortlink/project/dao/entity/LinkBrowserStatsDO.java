package org.mtf.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.mtf.shortlink.project.common.database.BaseDO;

import java.util.Date;

/**
 * 短链接访问浏览器统计实体
 */
@Data
@TableName("t_link_browser_stats")
public class LinkBrowserStatsDO extends BaseDO {
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
     * 浏览器
     */
    private String browser;
}
