package org.mtf.shortlink.admin.remote.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 分页查询短链接响应参数实体
 */
@Data
public class ShortlinkPageRespDTO {
    private Long id;
    private String domain;
    private String shortUri;
    private String fullShortUrl;
    private String originUrl;
    private String gid;
    private String favicon;
    private Integer validDateType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") //指定日期类型的序列化格式
    private Date validDate;
    private String describe;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Integer totalUv;
    private Integer totalPv;
    private Integer totalUip;

    private Integer todayUv;
    private Integer todayPv;
    private Integer todayUip;
}
