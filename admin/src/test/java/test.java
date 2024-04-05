public class test {
    public static void main(String[] args) {
        String sql="CREATE TABLE `link`.`t_group_%d` (\n" +
                "  `id` BIGINT(20) NOT NULL COMMENT 'ID',\n" +
                "  `gid` VARCHAR(32) NULL COMMENT '分组标识',\n" +
                "  `name` VARCHAR(64) NULL COMMENT '分组名称',\n" +
                "  `username` VARCHAR(256) NULL COMMENT '创建分组用户名',\n" +
                "  `sort_order` INT(3) NULL COMMENT '排序标识',\n" +
                "  `create_time` DATETIME NULL COMMENT '创建时间',\n" +
                "  `update_time` DATETIME NULL COMMENT '修改时间',\n" +
                "  `del_flag` TINYINT(1) NULL COMMENT '是否删除 0：未删除 1：已删除',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE INDEX `idx_unique_username_gid` USING BTREE (`gid`, `username`) VISIBLE\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短链接分组表';";
        for (int i = 0; i < 16; i++) {
            System.out.printf((sql) + "%n",i);
        }
    }
}
