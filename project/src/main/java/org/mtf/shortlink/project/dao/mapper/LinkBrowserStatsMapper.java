package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mtf.shortlink.project.dao.entity.LinkBrowserStatsDO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;

import java.util.HashMap;
import java.util.List;

/**
 * 短链接访问浏览器统计持久层
 */
public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {
    /**
     * 记录浏览器访问监控数据
     */
    @Insert("""
            INSERT INTO t_link_browser_stats (
            full_short_url, gid, date, cnt, browser, create_time, update_time, del_flag
            )
            VALUES(
            #{linkBrowserStats.fullShortUrl}, #{linkBrowserStats.gid}, #{linkBrowserStats.date}, 
            #{linkBrowserStats.cnt}, #{linkBrowserStats.browser}, NOW(), NOW(), 0
            )
            ON DUPLICATE KEY UPDATE 
            cnt = cnt +  #{linkBrowserStats.cnt};
            """)
    void shortlinkBrowserStats(@Param("linkBrowserStats") LinkBrowserStatsDO linkBrowserStatsDO);

    /**
     * 根据短链接获取指定日期内浏览器监控数据
     */
    @Select("""
            SELECT
            browser, SUM(cnt) as count
            FROM t_link_browser_stats
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid}
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid, browser
            """)
    List<HashMap<String, Object>> listBrowserStatsByShortlink(@Param("param") ShortlinkStatsReqDTO requestParam);
}
