public class test {
    public static void main(String[] args) {
        String sql="ALTER TABLE `link`.`t_link_%d` MODIFY COLUMN total_uv INT(11) DEFAULT 0;\n" +
                   "ALTER TABLE `link`.`t_link_%d` MODIFY COLUMN total_pv INT(11) DEFAULT 0;\n"+
                   "ALTER TABLE `link`.`t_link_%d` MODIFY COLUMN total_uip INT(11) DEFAULT 0;";
        for (int i = 0; i < 16; i++) {
            System.out.printf((sql) + "%n",i,i,i);
        }
    }
}
