public class test {
    public static void main(String[] args) {
        String sql="ALTER TABLE `link`.`t_link_%d` \n" +
                "ADD COLUMN `del_time` DATETIME NULL COMMENT '删除时间' AFTER `del_flag`,\n" +
                "DROP INDEX `idx_unique_full-short-url` ,\n" +
                "ADD UNIQUE INDEX `idx_unique_full-short-url` USING BTREE (`full_short_url`, `del_time`) VISIBLE;";
        for (int i = 0; i < 16; i++) {
            System.out.printf((sql) + "%n",i);
        }
    }
}
