public class test {
    public static void main(String[] args) {
        String sql="CREATE TABLE `link`.`t_link_goto_%d` (\n" +
                "  `id` BIGINT(20) NOT NULL COMMENT 'ID',\n" +
                "  `full_short_url` VARCHAR(128) NULL COMMENT '完整短链接',\n" +
                "  `gid` VARCHAR(32) NULL COMMENT '分组标识',\n" +
                "  PRIMARY KEY (`id`))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4\n" +
                "COMMENT = '路由表（完整短链接路由分组标识）';";
        for (int i = 0; i < 16; i++) {
            System.out.printf((sql) + "%n",i);
        }

    }

}
