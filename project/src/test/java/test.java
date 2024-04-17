public class test {
    public static void main(String[] args) {
        String sql="CREATE TABLE `t_link_stats_today_%d` (\n" +
                "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "  `gid` varchar(32) DEFAULT 'default' COMMENT '分组标识',\n" +
                "  `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接',\n" +
                "  `date` date DEFAULT NULL COMMENT '日期',\n" +
                "  `today_pv` int(11) DEFAULT '0' COMMENT '今日PV',\n" +
                "  `today_uv` int(11) DEFAULT '0' COMMENT '今日UV',\n" +
                "  `today_uip` int(11) DEFAULT '0' COMMENT '今日IP数',\n" +
                "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
                "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`) USING BTREE\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT = '短链接今日访问量统计表';";
        for (int i = 0; i < 16; i++) {
            System.out.printf((sql) + "%n",i);
        }

    }
}
