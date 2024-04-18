package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mtf.shortlink.project.dao.entity.LinkLocalStatsDO;
import org.mtf.shortlink.project.dto.req.ShortlinkGroupStatsReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;

import java.util.List;

/**
 * 地区访问统计持久层
 */
public interface LinkLocalStatsMapper extends BaseMapper<LinkLocalStatsDO> {
    /**
     * 记录地区访问监控数据
     */
    @Insert("""
            INSERT INTO t_link_local_stats (
            full_short_url, gid, date, cnt, country, province, city, adcode, create_time, update_time, del_flag
            )
            VALUES(
            #{linkLocalStats.fullShortUrl}, #{linkLocalStats.gid}, #{linkLocalStats.date}, 
            #{linkLocalStats.cnt}, #{linkLocalStats.country}, #{linkLocalStats.province}, 
            #{linkLocalStats.city}, #{linkLocalStats.adcode}, NOW(), NOW(), 0
            )
            ON DUPLICATE KEY UPDATE 
            cnt = cnt +  #{linkLocalStats.cnt};
            """)
    void shortlinkLocalStats(@Param("linkLocalStats") LinkLocalStatsDO linkLocalStatsDO);

    /**
     * 根据短链接获取指定日期内地区监控数据
     */
    @Select("""
            SELECT
            province, SUM(cnt) AS cnt
            FROM t_link_local_stats
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid}
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid, province;
            """)
    List<LinkLocalStatsDO> listLocalByShortlink(@Param("param") ShortlinkStatsReqDTO requestParam);
    /**
     * 根据分组获取指定日期内地区监控数据
     */
    @Select("""
            SELECT
            province, SUM(cnt) AS cnt
            FROM t_link_local_stats
            WHERE gid = #{param.gid}
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY gid, province;
            """)
    List<LinkLocalStatsDO> listLocaleByGroup(@Param("param") ShortlinkGroupStatsReqDTO requestParam);
}
