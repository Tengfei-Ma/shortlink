public class test {
    public static void main(String[] args) {
        String sql="CREATE TABLE `link`.`t_link_%d` (\n" +
                "  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "  `domain` VARCHAR(128) NULL COMMENT '域名',\n" +
                "  `short_uri` VARCHAR(8) NULL COMMENT '短链接',\n" +
                "  `full_short_url` VARCHAR(128) NULL COMMENT '带域名，端口和短链接的完整短链接',\n" +
                "  `origin_url` VARCHAR(1024) NULL COMMENT '目标长链接',\n" +
                "  `click_num` INT(11) NULL DEFAULT 0 COMMENT '点击量',\n" +
                "  `gid` VARCHAR(32) NULL DEFAULT 'default' COMMENT '短链接分组标识',\n" +
                "  `favicon` varchar(256) DEFAULT NULL COMMENT '网站图标',\n" +
                "  `enable_status` TINYINT(1) NULL COMMENT '启用标识 0：启用 1：未启用',\n" +
                "  `create_type` TINYINT(1) NULL COMMENT '创建类型 0：接口创建 1：控制台创建',\n" +
                "  `valid_date_type` TINYINT(1) NULL COMMENT '有效期类型 0：永久有效 1：自定义',\n" +
                "  `valid_date` DATETIME NULL COMMENT '有效期',\n" +
                "  `describe` VARCHAR(1024) NULL COMMENT '描述',\n" +
                "  `create_time` DATETIME NULL COMMENT '创建时间',\n" +
                "  `update_time` DATETIME NULL COMMENT '修改时间',\n" +
                "  `del_flag` TINYINT(1) NULL COMMENT '删除标识 0：未删除 1：已删除',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE INDEX `idx_unique_full_short_url` USING BTREE (`full_short_url`) VISIBLE\n" +
                ")ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COMMENT = '短链接表';";
        for (int i = 0; i < 16; i++) {
            System.out.printf((sql) + "%n",i);
        }

    }

}
