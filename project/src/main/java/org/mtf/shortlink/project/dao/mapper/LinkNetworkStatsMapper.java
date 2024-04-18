package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mtf.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.mtf.shortlink.project.dto.req.ShortlinkGroupStatsReqDTO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;

import java.util.List;

/**
 * 短链接访问网络统计持久层
 */
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {
    
    
    @Insert("""
            INSERT INTO t_link_network_stats (
            full_short_url, gid, date, cnt, network, create_time, update_time, del_flag
            )
            VALUES(
            #{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.gid}, #{linkNetworkStats.date}, 
            #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0
            )
            ON DUPLICATE KEY UPDATE 
            cnt = cnt +  #{linkNetworkStats.cnt};
            """)
    void shortlinkNetworkStats(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStatsDO);
    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    @Select("""
            SELECT
            network, SUM(cnt) AS cnt
            FROM t_link_network_stats
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid}
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid, network;
            """)
    List<LinkNetworkStatsDO> listNetworkStatsByShortlink(@Param("param") ShortlinkStatsReqDTO requestParam);
    /**
     * 根据分组获取指定日期内访问网络监控数据
     */
    @Select("""
            SELECT
            network, SUM(cnt) AS cnt
            FROM t_link_network_stats
            WHERE gid = #{param.gid}
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY gid, network;
            """)
    List<LinkNetworkStatsDO> listNetworkStatsByGroup(@Param("param") ShortlinkGroupStatsReqDTO requestParam);
}
