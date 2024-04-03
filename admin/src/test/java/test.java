public class test {
    public static void main(String[] args) {
        String sql="CREATE TABLE `t_user_%d` (\n" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "  `username` varchar(256) DEFAULT NULL COMMENT '用户名',\n" +
                "  `password` varchar(512) DEFAULT NULL COMMENT '密码',\n" +
                "  `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',\n" +
                "  `phone` varchar(128) DEFAULT NULL COMMENT '手机号',\n" +
                "  `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',\n" +
                "  `deletion_time` bigint DEFAULT NULL COMMENT '注销时间戳',\n" +
                "  `create_time` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `update_time` datetime DEFAULT NULL COMMENT '修改时间',\n" +
                "  `del_flag` tinyint(1) DEFAULT NULL COMMENT '是否删除 0：未删除 1：已删除',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `idx_unique_username` (`username`) USING BTREE\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表'";
        for (int i = 0; i < 15; i++) {
            System.out.printf((sql) + "%n",i);
        }
    }
}
