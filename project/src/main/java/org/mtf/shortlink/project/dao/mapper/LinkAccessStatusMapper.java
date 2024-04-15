package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.mtf.shortlink.project.dao.entity.LinkAccessStatsDO;

/**
 * 短链接基础访问统计持久层
 */
public interface LinkAccessStatusMapper extends BaseMapper<LinkAccessStatsDO> {
    /**
     * 记录访问次数
     * @param linkAccessStatsDO
     */
    @Insert("INSERT INTO t_link_access_stats (full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag) " +
            "VALUES( #{linkAccessStats.fullShortUrl}, #{linkAccessStats.gid}, #{linkAccessStats.date}, #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, #{linkAccessStats.hour}, #{linkAccessStats.weekday}, NOW(), NOW(), 0)" +
            " ON DUPLICATE KEY UPDATE pv = pv +  #{linkAccessStats.pv}, " + "uv = uv + #{linkAccessStats.uv}, " + " uip = uip + #{linkAccessStats.uip};")
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);
}
