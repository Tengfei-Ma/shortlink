package org.mtf.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
/**
 * 短链接路由实体
 */
@Data
@TableName("t_link_goto")
public class ShortlinkGotoDO {
    private Long id;
    private String fullShortUrl;
    private String gid;
}
