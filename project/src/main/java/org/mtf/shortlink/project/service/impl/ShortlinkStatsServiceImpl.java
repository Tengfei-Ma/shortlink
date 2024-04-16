package org.mtf.shortlink.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import org.mtf.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.mtf.shortlink.project.dao.entity.LinkDeviceStatsDO;
import org.mtf.shortlink.project.dao.entity.LinkLocalStatsDO;
import org.mtf.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.mtf.shortlink.project.dao.mapper.*;
import org.mtf.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.mtf.shortlink.project.dto.resp.*;
import org.mtf.shortlink.project.service.ShortlinkStatsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
    public ShortlinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        List<LinkAccessStatsDO> listStatsByShortlink=linkAccessStatsMapper.listStatsByShortLink(requestParam);
        if(CollUtil.isEmpty(listStatsByShortlink)){
            return null;
        }
        // 基础访问数据
        LinkAccessStatsDO pvUvUidStatsByShortlink = linkAccessLogsMapper.findPvUvUidStatsByShortLink(requestParam);
        // 基础访问详情
        List<ShortlinkStatsAccessDailyRespDTO> daily = new ArrayList<>();
        List<DateTime> dateTimes = DateUtil.rangeToList(DateUtil.parse(requestParam.getStartDate()), DateUtil.parse(requestParam.getEndDate()), DateField.DAY_OF_MONTH);
        List<String> rangeDates = dateTimes.stream().map(DateUtil::formatDate).toList();
        rangeDates.forEach(s -> listStatsByShortlink.stream()
                .filter(linkAccessStatsDO -> Objects.equals(DateUtil.formatDate(linkAccessStatsDO.getDate()), s))
                .findFirst().ifPresentOrElse(linkAccessStatsDO -> {
                    ShortlinkStatsAccessDailyRespDTO dailyRespDTO = new ShortlinkStatsAccessDailyRespDTO();
                    dailyRespDTO.setDate(s);
                    dailyRespDTO.setPv(linkAccessStatsDO.getPv());
                    dailyRespDTO.setUv(linkAccessStatsDO.getUv());
                    dailyRespDTO.setUip(linkAccessStatsDO.getUip());
                    daily.add(dailyRespDTO);
                }, () -> {
                    ShortlinkStatsAccessDailyRespDTO dailyRespDTO=new ShortlinkStatsAccessDailyRespDTO();
                    dailyRespDTO.setDate(s);
                    dailyRespDTO.setPv(0);
                    dailyRespDTO.setUv(0);
                    dailyRespDTO.setUip(0);
                    daily.add(dailyRespDTO);
                }));
        // 地区访问详情（仅国内）
        List<ShortlinkStatsLocalCNRespDTO> localCnStats = new ArrayList<>();
        List<LinkLocalStatsDO> listedLocalByShortlink = linkLocalStatsMapper.listLocalByShortlink(requestParam);
        int localCntSum = listedLocalByShortlink.stream().mapToInt(LinkLocalStatsDO::getCnt).sum();
        listedLocalByShortlink.forEach(linkLocalStatsDO -> {
            double ratio = (double) linkLocalStatsDO.getCnt() / localCntSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortlinkStatsLocalCNRespDTO localCNRespDTO=new ShortlinkStatsLocalCNRespDTO();
            localCNRespDTO.setCnt(linkLocalStatsDO.getCnt());
            localCNRespDTO.setLocal(linkLocalStatsDO.getProvince());
            localCNRespDTO.setRatio(actualRatio);
            localCnStats.add(localCNRespDTO);
        });
        // 小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDO> listHourStatsByShortlink = linkAccessStatsMapper.listHourStatsByShortLink(requestParam);
        for (int i = 0; i < 24; i++) {
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatsByShortlink.stream()
                    .filter(linkAccessStatsDO -> Objects.equals(linkAccessStatsDO.getHour(), hour.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 高频访问IP详情
        List<ShortlinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortlink = linkAccessLogsMapper.listTopIpByShortLink(requestParam);
        listTopIpByShortlink.forEach(map -> {
            ShortlinkStatsTopIpRespDTO statsTopIpRespDTO = new ShortlinkStatsTopIpRespDTO();
            statsTopIpRespDTO.setCnt(Integer.parseInt(map.get("count").toString()));
            statsTopIpRespDTO.setIp(map.get("ip").toString());
            topIpStats.add(statsTopIpRespDTO);
        });
        // 一周访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        List<LinkAccessStatsDO> listWeekdayStatsByShortlink = linkAccessStatsMapper.listWeekdayStatsByShortLink(requestParam);
        for (int i = 1; i < 8; i++) {
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = listWeekdayStatsByShortlink.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }
        // 浏览器访问详情
        List<ShortlinkStatsBrowserRespDTO> browserStats = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStatsByShortlink = linkBrowserStatsMapper.listBrowserStatsByShortLink(requestParam);
        int browserSum = listBrowserStatsByShortlink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStatsByShortlink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortlinkStatsBrowserRespDTO browserRespDTO = new ShortlinkStatsBrowserRespDTO();
            browserRespDTO.setCnt(Integer.parseInt(each.get("count").toString()));
            browserRespDTO.setBrowser((each.get("browser").toString()));
            browserRespDTO.setRatio(actualRatio);
            browserStats.add(browserRespDTO);
        });
        // 操作系统访问详情
        List<ShortlinkStatsOsRespDTO> osStats = new ArrayList<>();
        List<HashMap<String, Object>> listOsStatsByShortlink = linkOsStatsMapper.listOsStatsByShortLink(requestParam);
        int osSum = listOsStatsByShortlink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listOsStatsByShortlink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortlinkStatsOsRespDTO osRespDTO = new ShortlinkStatsOsRespDTO();
            osRespDTO.setCnt(Integer.parseInt(each.get("count").toString()));
            osRespDTO.setOs(each.get("os").toString());
            osRespDTO.setRatio(actualRatio);
            osStats.add(osRespDTO);
        });
        //TODO 访客类型

        // 访问设备类型详情
        List<ShortlinkStatsDeviceRespDTO> deviceStats = new ArrayList<>();
        List<LinkDeviceStatsDO> listDeviceStatsByShortlink = linkDeviceStatsMapper.listDeviceStatsByShortLink(requestParam);
        int deviceSum = listDeviceStatsByShortlink.stream()
                .mapToInt(LinkDeviceStatsDO::getCnt)
                .sum();
        listDeviceStatsByShortlink.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortlinkStatsDeviceRespDTO deviceRespDTO = new ShortlinkStatsDeviceRespDTO();
            deviceRespDTO.setCnt(each.getCnt());
            deviceRespDTO.setDevice(each.getDevice());
            deviceRespDTO.setRatio(actualRatio);
            deviceStats.add(deviceRespDTO);
        });
        // 访问网络类型详情
        List<ShortlinkStatsNetworkRespDTO> networkStats = new ArrayList<>();
        List<LinkNetworkStatsDO> listNetworkStatsByShortlink = linkNetworkStatsMapper.listNetworkStatsByShortLink(requestParam);
        int networkSum = listNetworkStatsByShortlink.stream()
                .mapToInt(LinkNetworkStatsDO::getCnt)
                .sum();
        listNetworkStatsByShortlink.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortlinkStatsNetworkRespDTO networkRespDTO = new ShortlinkStatsNetworkRespDTO();
            networkRespDTO.setCnt(each.getCnt());
            networkRespDTO.setNetwork(each.getNetwork());
            networkRespDTO.setRatio(actualRatio);
            networkStats.add(networkRespDTO);
        });
        ShortlinkStatsRespDTO shortlinkStatsRespDTO=new ShortlinkStatsRespDTO();
        shortlinkStatsRespDTO.setPv(pvUvUidStatsByShortlink.getPv());
        shortlinkStatsRespDTO.setUv(pvUvUidStatsByShortlink.getUv());
        shortlinkStatsRespDTO.setUip(pvUvUidStatsByShortlink.getUip());
        shortlinkStatsRespDTO.setDaily(daily);
        shortlinkStatsRespDTO.setLocalCnStats(localCnStats);
        shortlinkStatsRespDTO.setHourStats(hourStats);
        shortlinkStatsRespDTO.setTopIpStats(topIpStats);
        shortlinkStatsRespDTO.setWeekdayStats(weekdayStats);
        shortlinkStatsRespDTO.setBrowserStats(browserStats);
        shortlinkStatsRespDTO.setOsStats(osStats);
        shortlinkStatsRespDTO.setUvTypeStats(null);
        shortlinkStatsRespDTO.setDeviceStats(deviceStats);
        shortlinkStatsRespDTO.setNetworkStats(networkStats);
        return shortlinkStatsRespDTO;
    }
}
