package com.tan.gen.util;

import javax.swing.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * JDBC 工具类
 * @autor qwop
 * @date 2020-05-11
 */
public class DBUtil {
    /*
        jdbc.driverClass=com.mysql.jdbc.Driver
        #jdbc.url=jdbc:mysql://119.23.13.107:3306/xxx?useUnicode=true&characterEncoding=utf8&autoReconnect=true&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull
        #jdbc.url=jdbc:mysql://localhost:3306/zzz?useUnicode=true&characterEncoding=utf8&autoReconnect=true&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull

        #jdbc.username=root
        #jdbc.password=222
        #####################\uFFFD\uFFFD\uFFFD\uFFFD#############################
        jdbc.url=jdbc:mysql://59.110.225.26:3306/zzx?useUnicode=true&characterEncoding=utf8&autoReconnect=true&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull
        jdbc.username=root
        jdbc.password=333
    */
    public static final String JUTILS_PROPERTIES = "jutils.properties";
    private static DBUtil util = new DBUtil();
    private static Properties props;
    private static long LAST_MODIFIED;
    public static final File FILE = new File(System.getProperty("java.io.tmpdir"), JUTILS_PROPERTIES);
    public static final File DDL_FILE = new File(System.getProperty("java.io.tmpdir"), "ddl.sql");

    public static DBUtil getInstance() {
        long lastModified = FILE.lastModified();
        if ( lastModified > LAST_MODIFIED  || props == null) {
            props = new Properties();
            try {
                props.load(new FileInputStream(FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return util;
    }

    private DBUtil() {
    }


    /**
     * 获取数据库连接
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     * @throws InterruptedException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Connection getConnection() throws ClassNotFoundException, SQLException, IOException, InterruptedException, IllegalAccessException, InstantiationException {
        File file = new File(System.getProperty("java.io.tmpdir"), JUTILS_PROPERTIES);
        boolean flag = false;
        if ( file.isDirectory() ) { flag = true; file.delete();  }
        if ( flag || !file.exists()  || file.length() == 0 ) {
            JOptionPane.showMessageDialog( null, "请配置文件信息" + file.getAbsolutePath());
            // 生成模板
            Properties templateProp = new Properties();
            templateProp.setProperty("driverClass", "com.mysql.jdbc.Driver");
            templateProp.setProperty("url", "jdbc:mysql://192.168.1.222:3306/xxx?useUnicode=true&characterEncoding=utf8&elideSetAutoCommits=true&useSSL=false&autoReconnect=true&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull");
            templateProp.setProperty("username", "root");
            templateProp.setProperty("password", "xxx");
            templateProp.setProperty("loginTimeout", "10");
            templateProp.setProperty("schema", "yimeiduo");
            templateProp.store(new FileWriter(file), "jdbc config");
            Thread.sleep(300);
            Runtime.getRuntime().exec("notepad " + file.getAbsolutePath());
            return null;
        }
        Class clazz = Class.forName(props.getProperty("driverClass"));
        DriverManager.setLoginTimeout( Integer.parseInt( props.getProperty( "loginTimeout")  ));
        Connection connection = DriverManager.getConnection(props.getProperty("url"), props.getProperty("username"),props.getProperty("password"));
        return connection;
    }

    public String getSchema() {
        return props.getProperty("schema");
    }

    public boolean requireConfig()  {
        // 判断配置文件是否有变动
        long lastModified = FILE.lastModified();
        if ( FILE.isDirectory() ) {
            FILE.delete();
            return true;
        }
        if ( FILE.isFile() && FILE.length() == 0 ) {
            return true;
        }
        if ( props == null) {
            // 如果配置文件变动， 或者配置属性未加载
            return true;
        }
        return false;
    }

    public static void config() throws Exception {
        if ( FILE.exists() ) {
            // 生成模板
            Runtime.getRuntime().exec("notepad " + FILE.getAbsolutePath());
        } else if (  !FILE.exists()  || FILE.length() == 0 ) {
                JOptionPane.showMessageDialog( null, "请配置文件信息" + FILE.getAbsolutePath());
                // 生成模板
                Properties templateProp = new Properties();
                templateProp.setProperty("driverClass", "com.mysql.jdbc.Driver");
                templateProp.setProperty("url", "jdbc:mysql://192.168.1.146:3306/yimeiduo?useUnicode=true&characterEncoding=utf8&elideSetAutoCommits=true&useSSL=false&autoReconnect=true&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull");
                templateProp.setProperty("username", "root");
                templateProp.setProperty("password", "yhxd");
                templateProp.setProperty("loginTimeout", "10");
                templateProp.setProperty("schema", "yimeiduo");
                templateProp.store(new FileWriter(FILE), "jdbc config");
                Thread.sleep(300);
                Runtime.getRuntime().exec("notepad " + FILE.getAbsolutePath());
        }
    }

    public static void main(String[] args) throws Exception {
//        config();
/*        Connection connection = DBUtil.getInstance().getConnection();
        System.out.println(connection);
        if (null != connection) {
            connection.close();
        }*/
//        DBUtil.getInstance().getTableCommentDDL( "yimeiduo", "activity", "activity_state", "活动状态");
//        System.getProperties().list(System.out);
        System.out.println(System.getProperty("java.io.tmpdir"));
        File file = new File(System.getProperty("java.io.tmpdir"), "ddl.sql");
//        Runtime.getRuntime().exec("cmd /c echo 'shit you' >> " + file.getAbsolutePath());
        Runtime.getRuntime().exec("notepad  " + file.getAbsolutePath());
    }
    public static void openDDl() throws IOException {
        if ( DDL_FILE.exists() ) {
            // 生成模板
            Runtime.getRuntime().exec("notepad " + DDL_FILE.getAbsolutePath());
        }
    }

    public void writeDDl(String ddl) {
        File file = new File(System.getProperty("java.io.tmpdir"), "ddl.sql");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true );
            fw.write(ddl);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fw) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public  List<String> getTableNames() throws Exception {
        Connection connection = DBUtil.getInstance().getConnection();
        try {
            List<String> tables = new ArrayList<>();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs;
           /* if (DbType.valueOf(config.getDbType()) == DbType.SQL_Server) {
                String sql = "select name from sysobjects  where xtype='u' or xtype='v' order by name";
                rs = connection.createStatement().executeQuery(sql);
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            } else if (DbType.valueOf(config.getDbType()) == DbType.Oracle){
                rs = md.getTables(null, config.getUsername().toUpperCase(), null, new String[] {"TABLE", "VIEW"});
            } else if (DbType.valueOf(config.getDbType())==DbType.Sqlite){
                String sql = "Select name from sqlite_master;";
                rs = connection.createStatement().executeQuery(sql);
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            }
            else*/
            {
                // rs = md.getTables(null, config.getUsername().toUpperCase(), null, null);


                rs = md.getTables(DBUtil.getInstance().getSchema(), null, "%", new String[] {"TABLE", "VIEW"});			//针对 postgresql 的左侧数据表显示
            }
            while (rs.next()) {
                tables.add(rs.getString(3));
            }

            if (tables.size()>1) {
                Collections.sort(tables);
            }
            for (String table : tables) {
                System.out.println(table);
            }
            return tables;
        } finally {
            connection.close();
        }
    }

    static String ADD_COMMENT_SQL =
            "SELECT \n" +
                    "table_name,\n" +
                    "column_name,\n" +
                    "CONCAT('ALTER TABLE `',\n" +
                    "        table_name,\n" +
                    "        '` CHANGE `',\n" +
                    "        column_name,\n" +
                    "        '` `',\n" +
                    "        column_name,\n" +
                    "        '` ',\n" +
                    "        column_type,\n" +
                    "        ' ',\n" +
                    "        IF(is_nullable = 'YES', '' , 'NOT NULL '),\n" +
                    "        IF(column_default IS NOT NULL, concat('DEFAULT ', IF(column_default = 'CURRENT_TIMESTAMP', column_default, CONCAT('\\'',column_default,'\\'') ), ' '), ''),\n" +
                    "        IF(column_default IS NULL AND is_nullable = 'YES' AND column_key = '' AND column_type = 'timestamp','NULL ', ''),\n" +
                    "        IF(column_default IS NULL AND is_nullable = 'YES' AND column_key = '','DEFAULT NULL ', ''),\n" +
                    "        extra,\n" +
                    "        ' COMMENT \\'#column_comment#\\' ;') as script \n" +
                    "FROM\n" +
                    "    information_schema.columns\n" +
                    "WHERE\n" +
                    "    table_schema = '#table_schema#'\n" +
                    "		and table_name = '#table_name#'\n" +
                    "		and column_name = '#column_name#'\n" +
                    "ORDER BY table_name , column_name";


    public  String getTableCommentDDL( String tableSchema, String tableName, String columnName,String columnComment) throws Exception {
        Connection connection = DBUtil.getInstance().getConnection();
        try {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs;
            String sql = ADD_COMMENT_SQL
                    .replace( "#table_schema#", tableSchema)
                    .replace( "#table_name#", tableName)
                    .replace( "#column_name#", columnName)
                    .replace( "#column_comment#", columnComment)
                    ;
            rs = connection.createStatement().executeQuery(sql);
            String script = "";
            if (rs.next()) {
                script = rs.getString("script");
            }
            System.out.println(script);
            return script;
        } finally {
            connection.close();
        }
    }

}
