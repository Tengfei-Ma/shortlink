package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mtf.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;

import java.util.List;

/**
 * 短链接基础访问统计持久层
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {
    /**
     * 记录访问次数
     *
     * @param linkAccessStatsDO 基础访问统计实体
     */
    @Insert("""
            INSERT INTO t_link_access_stats (
            full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag
            ) 
            VALUES( 
            #{linkAccessStats.fullShortUrl}, #{linkAccessStats.gid}, #{linkAccessStats.date}, 
            #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, 
            #{linkAccessStats.hour}, #{linkAccessStats.weekday}, NOW(), NOW(), 0
            ) 
            ON DUPLICATE KEY UPDATE 
            pv = pv + #{linkAccessStats.pv}, 
            uv = uv + #{linkAccessStats.uv}, 
            uip = uip + #{linkAccessStats.uip};
            """)
    void shortlinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("""
            SELECT
            date, SUM(pv) AS pv, SUM(uv) AS uv, SUM(uip) AS uip  
            FROM t_link_access_stats 
            WHERE full_short_url = #{param.fullShortUrl} 
            AND gid = #{param.gid} 
            AND date BETWEEN #{param.startDate} and #{param.endDate} 
            GROUP BY full_short_url, gid, date;
            """)
    List<LinkAccessStatsDO> listStatsByShortlink(@Param("param") ShortlinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("""
            SELECT
            hour, SUM(pv) AS pv
            FROM t_link_access_stats
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid} 
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid, hour;
            """)
    List<LinkAccessStatsDO> listHourStatsByShortlink(@Param("param") ShortlinkStatsReqDTO requestParam);
    /**
     * 根据短链接获取指定日期内星期基础监控数据
     */
    @Select("""
            SELECT
            weekday, SUM(pv) AS pv
            FROM t_link_access_stats
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid} 
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid, weekday;
            """)
    List<LinkAccessStatsDO> listWeekdayStatsByShortlink(@Param("param") ShortlinkStatsReqDTO requestParam);
}
