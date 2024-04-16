package org.mtf.shortlink.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.dao.mapper.*;
import org.mtf.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.mtf.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import org.mtf.shortlink.project.service.ShortlinkStatsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShortlinkStatsServiceImpl implements ShortlinkStatsService {
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocalStatsMapper linkLocalStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;


    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return null;
    }
}
