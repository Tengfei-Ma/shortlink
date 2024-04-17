package org.mtf.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.mtf.shortlink.project.dao.entity.ShortlinkDO;
/**
 * 短链接持久层
 */
public interface ShortlinkMapper extends BaseMapper<ShortlinkDO> {
    /**
     * 短链接访问统计自增
     */
    @Update("""
            UPDATE t_link 
            SET total_pv = total_pv + #{totalPv},
            total_uv = total_uv + #{totalUv}, 
            total_uip = total_uip + #{totalUip} 
            WHERE gid = #{gid} 
            AND full_short_url = #{fullShortUrl}
            """)

    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip
    );
}
