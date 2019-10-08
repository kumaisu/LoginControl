/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.config.Config;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class DatabaseControl {
    
    /**
     * Database Connection(接続) 処理
     */
    public static void connect() {
        if ( Database.dataSource != null ) {
            if ( Database.dataSource.isClosed() ) {
                Tools.Prt( ChatColor.RED + "database closed.", programCode );
                disconnect();
            } else {
                Tools.Prt( ChatColor.AQUA + "dataSource is not null", programCode );
                return;
            }
        }

        // HikariCPの初期化
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl( "jdbc:mysql://" + Config.host + ":" + Config.port + "/" + Config.database );
        config.setPoolName( Config.database );
        config.setAutoCommit( true );
        config.setConnectionInitSql( "SET SESSION query_cache_type=0" );
        config.setMaximumPoolSize( Config.MaximumPoolSize );
        config.setMinimumIdle( Config.MinimumIdle );
        config.setMaxLifetime( TimeUnit.MINUTES.toMillis( 15 ) );
        //  config.setConnectionTimeout(0);
        //  config.setIdleTimeout(0);
        config.setUsername( Config.username );
        config.setPassword( Config.password );

        Properties properties = new Properties();
        properties.put( "useSSL", "false" );
        properties.put( "autoReconnect", "true" );
        properties.put( "maintainTimeStats", "false" );
        properties.put( "elideSetAutoCommits", "true" );
        properties.put( "useLocalSessionState", "true" );
        properties.put( "alwaysSendSetIsolation", "false" );
        properties.put( "cacheServerConfiguration", "true" );
        properties.put( "cachePrepStmts", "true" );
        properties.put( "prepStmtCacheSize", "250" );
        properties.put( "prepStmtCacheSqlLimit", "2048" );
        properties.put( "useUnicode", "true" );
        properties.put( "characterEncoding", "UTF-8" );
        properties.put( "characterSetResults", "UTF-8" );
        properties.put( "useServerPrepStmts", "true" );

        config.setDataSourceProperties( properties );

        Database.dataSource = new HikariDataSource( config );
    }

    /**
     * Database disConnect(切断) 処理
     */
    public static void disconnect() {
        if ( Database.dataSource != null ) {
            Database.dataSource.close();
        }
    }

    /**
     * Database Table Initialize
     */
    public static void TableUpdate() {
        try ( Connection con = Database.dataSource.getConnection() ) {
            //  sql = "CREATE TABLE IF NOT EXISTS list(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip INTEGER UNSIGNED, status int, index(id))";
            //  id int auto_increment   DB_ID 
            //  date DATETIME           Login Date
            //  name varchar(20)        Login Player
            //  uuid varchar(36),       Loign UUID
            //  ip INTEGER UNSIGNED     IP Address
            //  status int              Success Flag
            //  Login List テーブルの作成
            //  存在すれば、無視される
            String sql = "CREATE TABLE IF NOT EXISTS list(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip INTEGER UNSIGNED, status int, index(id))";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max , programCode );
            PreparedStatement preparedStatement = con.prepareStatement( sql );
            preparedStatement.executeUpdate();

            //  sql = "CREATE TABLE IF NOT EXISTS hosts (ip INTEGER UNSIGNED, host varchar(60), count int, newdate DATETIME, lastdate DATETIME )";
            //  ip INTEGER UNSIGNED IP Address
            //  host varchar(60)    Host name
            //  count int           Reference Count   
            //  newdate DATETIME    First Log Date
            //  lastdate DATETIME   Last Log Date
            //  Host テーブルの作成
            //  存在すれば、無視される
            sql = "CREATE TABLE IF NOT EXISTS hosts (ip INTEGER UNSIGNED, host varchar(60), count int, newdate DATETIME, lastdate DATETIME )";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max , programCode );
            preparedStatement = con.prepareStatement( sql );
            preparedStatement.executeUpdate();

            Tools.Prt( ChatColor.AQUA + "dataSource Open Success.", programCode );
            con.close();
        } catch( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Table Initialize Error : " + e.getMessage(), programCode);
        }
    }

    /**
     * 新規の照会があった場合に、テキストファイルへ日時と共に記録する
     *
     * @param IP
     * @param DataFolder
     * @return
     */
    public static boolean WriteFileUnknown( String IP, String DataFolder ) {
        File UKfile = new File( DataFolder, "UnknownIP.yml" );
        FileConfiguration UKData = YamlConfiguration.loadConfiguration( UKfile );

        SimpleDateFormat cdf = new SimpleDateFormat("yyyyMMddHHmmss");

        HostData.GetSQL( IP );
        UKData.set( cdf.format( new Date() ),Utility.StringBuild( IP, "[", Database.Host, "]" ) );
        try {
            UKData.save( UKfile );
        }
        catch ( IOException e ) {
            Tools.Prt( Utility.StringBuild( ChatColor.RED.toString(), "Could not save UnknownIP File." ), programCode );
            return false;
        }

        return true;
    }

    /**
     * MySQLコマンドを直接送信する
     *
     * @param player
     * @param cmd 
     */
    public static void SQLCommand( Player player, String cmd ) {
        Tools.Prt( player, "== Original SQL Command ==", programCode );

        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            Tools.Prt( "SQL Command : " + cmd, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( cmd );

            while( rs.next() ) {
                Tools.Prt( player, ListData.LinePrt( player, rs ),  programCode );
            }

            Tools.Prt( player, "================", programCode );
            con.close();
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error SQL Command : " + e.getMessage(), programCode );
        }
    }
}
