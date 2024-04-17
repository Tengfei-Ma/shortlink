package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mtf.shortlink.project.dao.entity.LinkDeviceStatsDO;
import org.mtf.shortlink.project.dto.req.ShortlinkStatsReqDTO;

import java.util.List;

/**
 * 短链接访问设备统计持久层
 */
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {
    /**
     * 记录设备访问监控数据
     */
    @Insert("""
            INSERT INTO t_link_device_stats (
            full_short_url, gid, date, cnt, device, create_time, update_time, del_flag
            )
            VALUES(
            #{linkDeviceStats.fullShortUrl}, #{linkDeviceStats.gid}, #{linkDeviceStats.date}, 
            #{linkDeviceStats.cnt}, #{linkDeviceStats.device}, NOW(), NOW(), 0
            )
            ON DUPLICATE KEY UPDATE 
            cnt = cnt +  #{linkDeviceStats.cnt};
            """)
    void shortlinkDeviceStats(@Param("linkDeviceStats") LinkDeviceStatsDO linkDeviceStatsDO);

    /**
     * 根据短链接获取指定日期内访问设备监控数据
     */
    @Select("""
            SELECT
            device, SUM(cnt) AS cnt
            FROM t_link_device_stats
            WHERE full_short_url = #{param.fullShortUrl}
            AND gid = #{param.gid}
            AND date BETWEEN #{param.startDate} and #{param.endDate}
            GROUP BY full_short_url, gid, device;
            """)
    List<LinkDeviceStatsDO> listDeviceStatsByShortlink(@Param("param") ShortlinkStatsReqDTO requestParam);
}
