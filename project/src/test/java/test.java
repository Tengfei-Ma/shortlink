public class test {
    public static void main(String[] args) {
        String sql="ALTER TABLE `link`.`t_link_stats_today_%d` \n" +
                "DROP INDEX `idx_unique_full-short-url` ,\n" +
                "ADD UNIQUE INDEX `idx_unique_full-short-url` USING BTREE (`full_short_url`, `gid`, `date`) VISIBLE;\n" +
                ";";
        for (int i = 0; i < 16; i++) {
            System.out.printf((sql) + "%n",i);
        }

    }
}
