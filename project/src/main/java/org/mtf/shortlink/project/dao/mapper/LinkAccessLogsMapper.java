package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mtf.shortlink.project.dao.entity.LinkAccessLogsDO;
import org.mtf.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.mtf.shortlink.project.dto.req.ShortLinkStatsReqDTO;

import java.util.HashMap;
import java.util.List;

/**
 * 短链接访问日志监控持久层
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {
    /**
     * 根据短链接获取指定日期内PV、UV、UIP数据
     */
    @Select("""
            SELECT
            COUNT(user) AS pv,
            COUNT(DISTINCT user) AS uv,
            COUNT(DISTINCT ip) AS uip
            FROM t_link_access_logs
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid}
            AND create_time BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid;
            """)
    LinkAccessStatsDO findPvUvUidStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    @Select("""
            SELECT
            ip, COUNT(ip) AS count
            FROM t_link_access_logs
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid}
            AND create_time BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid, ip
            ORDER BY count DESC
            LIMIT 5;
            """)
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}
