package org.mtf.shortlink.project.dao.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.mtf.shortlink.project.common.database.BaseDO;

import java.util.Date;

/**
 * 短链接实体
 */
@Data
@TableName("t_link")
public class ShortlinkDO extends BaseDO {
    private Long id;
    private String domain;
    private String shortUri;
    private String fullShortUrl;
    private String originUrl;
    private Integer clickNum;
    private String gid;
    private String favicon;
    private Integer enableStatus;
    private Integer createType;
    private Integer validDateType;
    private Date validDate;
    @TableField("`describe`") //describe是MySQL的关键字，用``防止冲突
    private String describe;
    private Integer totalUv;
    private Integer totalPv;
    private Integer totalUip;
    /**
     * 今日PV
     */
    @TableField(exist = false)
    private Integer todayPv;

    /**
     * 今日UV
     */
    @TableField(exist = false)
    private Integer todayUv;

    /**
     * 今日UIP
     */
    @TableField(exist = false)
    private Integer todayUip;
}
