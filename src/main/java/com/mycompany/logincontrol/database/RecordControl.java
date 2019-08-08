/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.database;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.kumaisulibraries.InetCalc;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Tools.consoleMode;
import com.mycompany.logincontrol.config.Config;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 * 主にmySQLとの通信を司るライブラリ
 *
 * @author sugichan
 */
public class RecordControl {

    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private Connection connection;
    private final HikariDataSource dataSource;

    /**
     * ライブラリー読込時の初期設定
     *
     */
    public RecordControl() {
        // HikariCPの初期化
        HikariConfig config = new HikariConfig();

        // MySQL用ドライバを設定
        config.setDriverClassName( "com.mysql.jdbc.Driver" );

        // 「jdbc:mysql://ホスト:ポート/DB名」の様なURLで指定
        config.setJdbcUrl( "jdbc:mysql://" + Config.host + ":" + Config.port + "/" + Config.database );

        // ユーザ名、パスワード指定
        config.addDataSourceProperty( "user", Config.username );
        config.addDataSourceProperty( "password", Config.password );

        // キャッシュ系の設定(任意)
        config.addDataSourceProperty( "cachePrepStmts", "true" );
        config.addDataSourceProperty( "prepStmtCacheSize", "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit", "2048" );
        // サーバサイドプリペアードステートメントを使用する(任意)
        config.addDataSourceProperty( "useServerPrepStmts", "true" );
        // エンコーディング
        config.addDataSourceProperty( "characterEncoding", "utf8" );

        // 最小接続数まで接続を確保できない時に例外を投げる
        config.setInitializationFailFast( true );
        // 接続をテストするためのクエリ
        config.setConnectionInitSql( "SELECT 1" );

        // 接続
        dataSource = new HikariDataSource( config );

        try {
            connection = dataSource.getConnection();
        } catch( SQLException e ) {
            Tools.Prt( "Connection Error : " + e.getMessage(), programCode);
        }

        updateTables();
    }

    public void close() {
        if ( dataSource != null ) {
            dataSource.close();
        }
    }

    /**
     * MySQLへのコネクション処理
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void updateTables() {
        try {
            //  mysql> create table list(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip INTEGER UNSIGNED, status byte, index(id));
            //  テーブルの作成
            //  存在すれば、無視される
            String sql = "CREATE TABLE IF NOT EXISTS list(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip INTEGER UNSIGNED, status int, index(id))";
            Tools.Prt( sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();

            //  mysql> create table IF NOT EXISTS unknowns (ip varchar(22), host varchar(60), count int, newdate DATETIME, lastdate DATETIME );
            //  Unknowns テーブルの作成
            //  存在すれば、無視される
            sql = "CREATE TABLE IF NOT EXISTS hosts (ip INTEGER UNSIGNED, host varchar(60), count int, newdate DATETIME, lastdate DATETIME )";
            Tools.Prt( sql, Tools.consoleMode.max, programCode );
            preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();
        } catch( SQLException ex ) {
            Tools.Prt( "Error SQL update tables " + ex.getMessage(), programCode );
        }
    }

    /**
     * ユーザー情報を1ラインで表紙成形する関数
     * Permission保持者には追加情報も付随する
     *
     * @param player    表示したいプレイヤー
     * @param gs        DBから取得したデータ
     * @return          成形されたメッセージ
     */
    public String LinePrt( Player player, ResultSet gs ) {
        String message = "";
        try {
            message = Utility.StringBuild( message,
                    ChatColor.WHITE.toString(), String.format( "%6d", gs.getInt( "id" ) ), ": ",
                    ChatColor.GREEN.toString(), sdf.format( gs.getTimestamp( "date" ) ), " "
            );

            if ( ( player == null ) || player.isOp() || player.hasPermission( "LoginCtl.view" ) ) {
                message = Utility.StringBuild( message,
                        ChatColor.YELLOW.toString(),
                        "[",
                        String.format( "%-15s", InetCalc.toInetAddress( gs.getLong( "ip" ) ) ),
                        "] "
                );
            }

            message = Utility.StringBuild( message,
                    gs.getInt( "status" )==0 ? ChatColor.RED.toString() : ChatColor.AQUA.toString(),
                    gs.getString( "name" )
            );

            if ( player == null ) {
                message = Utility.StringBuild( message, " : ",
                    ( gs.getInt( "status" )==0 ? ChatColor.RED.toString() : ChatColor.AQUA.toString() ),
                    GetHost( InetCalc.toInetAddress( gs.getLong( "ip" ) ) )
                );
            }

        } catch ( SQLException e ) {
            Tools.Prt( "Error LinePrt : " + e.getMessage(), programCode );
        }

        return message;
    }

    /**
     * 直近のログインプレイヤーリストを表示する関数
     *
     * @param player    表示するプレイヤー
     * @param lines     リストに表示する人数（過去lines人分)
     * @param FullFlag  重複ログインを省略しないか？
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void LogPrint( Player player, int lines, boolean FullFlag ) {
        boolean hasPermission = ( ( player == null ) || player.isOp() || player.hasPermission( "LoginCtl.view" ) );

        Tools.Prt( player, "== Login List ==", programCode );

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list ORDER BY date DESC;";
            Tools.Prt( "LogPrint : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            int i = 0;
            String chk_name = "";

            while( rs.next() && ( i<lines ) ) {
                String GetName = rs.getString( "name" );

                if ( rs.getInt( "status" ) != 0 || hasPermission ) {
                    if ( ( !Config.IgnoreReportName.contains( GetName ) || hasPermission ) && ( !chk_name.equals( GetName ) || FullFlag ) ) {
                        i++;
                        Tools.Prt( player, LinePrt( player, rs ),  programCode );
                        chk_name = GetName;
                    }
                }
            }

            Tools.Prt( player, "================", programCode );

        } catch ( SQLException e ) {
            Tools.Prt( "Error LogPrint : " + e.getMessage(), programCode );
        }
    }

    /**
     * 色々な形式でのプレイヤー一覧を表示する関数
     *
     * @param player        結果を表示するプレイヤー、nullならばコンソール表示
     * @param checkString   検索する目的の文字列（プレイヤー名や日付など)
     * @param FullFlag      重複プレイヤーの表示可否（true:全部,false:省略)
     * @param PrtMode       一覧の形式指定（1:指定日の一覧,2:プレイヤーの履歴,3:IPアドレスの履歴）
     * @param lines         表示する行数指定
     * @return
     */
    public boolean exLogPrint( Player player, String checkString, boolean FullFlag, int PrtMode, int lines )  {
        String sqlCmd;
        String checkName = "";
        boolean isOP = ( ( player == null ) ? true:player.isOp() );

        String titleMessage = ChatColor.WHITE + "== [" + checkString + "] Login List ==";
        if ( PrtMode == 3 && isOP ) {
            titleMessage += ChatColor.YELLOW + " [" + GetHost( checkString ) + "]";
        }
        Tools.Prt( player, titleMessage, programCode );

        switch( PrtMode ) {
            case 1:
                sqlCmd = "SELECT * FROM list WHERE date BETWEEN '" + checkString + " 00:00:00' AND '" + checkString + " 23:59:59' ORDER BY date DESC;";
                break;
            case 2:
                sqlCmd = "SELECT * FROM list WHERE name = '" + checkString + "' ORDER BY date DESC;";
                break;
            case 3:
                sqlCmd = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + checkString + "' ORDER BY date DESC;";
                break;
            default:
                return false;
        }

        try {
            Statement stmt = connection.createStatement();
            Tools.Prt( "exLogPrint : " + sqlCmd, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sqlCmd );

            int loopCount = 0;
            SimpleDateFormat cdf = new SimpleDateFormat( "yyyyMMdd" );

            while( rs.next() && ( loopCount<lines ) ) {
                String getName = rs.getString( "name" );
                String getDate = cdf.format( rs.getTimestamp( "date" ) );
                if ( isOP || ( !Config.IgnoreReportName.contains( getName ) && !Config.IgnoreReportIP.contains( InetCalc.toInetAddress( rs.getLong( "ip" ) ) ) ) ) {
                    boolean checkPrint;

                    switch( PrtMode ) {
                        case 1:
                            checkPrint = !checkName.equals( getName );
                            break;
                        case 2:
                        case 3:
                            checkPrint = !checkName.equals( getDate );
                            break;
                        default:
                            checkPrint = false;
                    }

                    if ( checkPrint || FullFlag ) {
                        loopCount++;
                        Tools.Prt( player, LinePrt( player, rs ), programCode );

                        switch( PrtMode ) {
                            case 1:
                                checkName = getName;
                                break;
                            case 2:
                            case 3:
                                checkName = getDate;
                                break;
                            default:
                                checkName = "";
                        }

                    }
                }
            }

            Tools.Prt( player, "================", programCode );

        } catch ( SQLException e ) {
            Tools.Prt( "Error exLogPrint : " + e.getMessage(), programCode );
            return false;
        }
        return true;
    }

    /**
     * IPアドレスから、最後にログインしたプレイヤー名を取得
     *
     * @param ip
     * @return     取得成功時はプレイヤー名、記録が無い時はUnknownを戻す
     *              SQLエラーが発生した場合は、IPアドレスを戻す
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public String listGetPlayerName( String ip ) {

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + ip + "' ORDER BY date DESC;";
            Tools.Prt( "listGetPlayerName : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );
            return ( rs.next() ? rs.getString( "name" ):"Unknown" );
        } catch ( SQLException e ) {
            Tools.Prt( "Error listGetPlayerName : " + e.getMessage(), programCode );
        }

        return ip;
    }

    /**
     * 同一IPアドレスで別名のログインがあるかのチェックを行う
     *
     * @param player    結果を表示するプレイヤー
     * @throws UnknownHostException
     */
    @SuppressWarnings( "CallToPrintStackTrace" )
    public void listCheckIP( Player player ) throws UnknownHostException {
        List<String> PrtData;
        PrtData = new ArrayList<>();
        List<String> NameData;
        NameData = new ArrayList<>();

        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== Check IP Address ===", ChatColor.YELLOW.toString(), "[", player.getAddress().getHostString(), "]" ) );

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + player.getAddress().getHostString() + "' ORDER BY date DESC;";
            Tools.Prt( "listCheckIP : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            int i = 0;
            while( rs.next() ) {
                String GetName = rs.getString( "name" );

                if ( ( i == 0 ) && ( GetName.equals( player.getName() ) ) ) {
                    i++;
                } else {
                    if ( !NameData.contains( GetName ) ) {
                        i++;
                        NameData.add( GetName );
                        PrtData.add( LinePrt( player,rs ) );
                    }
                }
            }

        } catch ( SQLException e ) {
            Tools.Prt( "Error listCheckIP : " + e.getMessage(), programCode );
        }

        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== end ===" ) );

        PrtData.stream().forEach( PD -> {
            String msg = PD;
            Tools.Prt( ( ( NameData.size() > 1 ) ? player:null ), msg, consoleMode.normal, programCode );
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> (
                ( player != p ) && ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ) ).forEachOrdered( ( p ) -> {
                    Tools.Prt( p, msg, consoleMode.max, programCode );
                }
            );
        } );
    }

    /**
     * リストステータスを変更する
     *
     * @param date
     * @param status
     */
    public void listChangeStatus( Date date, int status ) {
        try {

            String sql = "UPDATE list SET status = " + String.valueOf( status ) + " WHERE date = '" + sdf.format( date ) + "';";
            Tools.Prt( "listChangeStatus : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();

        } catch ( SQLException e ) {
            Tools.Prt( "Error ChangeStatus : " + e.getMessage(), programCode );
        }
    }

    /**
     * リストステータスを新規に追加する
     *
     * @param date
     * @param name
     * @param UUID
     * @param IP
     * @param Status
     */
    public void listPreSave( Date date, String name, String UUID, String IP, int Status ) {

        /*
        getLogger().log( Level.INFO, "Date   : {0}", sdf.format( date ) );
        getLogger().log( Level.INFO, "name   : {0}", name );
        getLogger().log( Level.INFO, "UUID   : {0}", UUID );
        getLogger().log( Level.INFO, "IP     : {0}", IP );
        getLogger().log( Level.INFO, "Status : {0}", Status );
        */

        try {

            String sql = "INSERT INTO list (date, name, uuid, ip, status) VALUES (?, ?, ?, INET_ATON( ? ), ?);";
            Tools.Prt( "listPreSave : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.setString(1, sdf.format( date ) );
            preparedStatement.setString(2, name );
            preparedStatement.setString(3, UUID );
            preparedStatement.setString(4, IP );
            preparedStatement.setInt(5, Status );

            preparedStatement.executeUpdate();

        } catch ( SQLException e ) {
            Tools.Prt( "Error PreSavePlayer : " + e.getMessage(), programCode );
        }
    }

    /**
     * データベースにプレイヤー情報を付加する
     * データが無い場合は新規に追加する
     *
     * @param IP
     * @param Name
     */
    public void AddPlayerToSQL( String IP, String Name ) {
        String DataName = Utility.StringBuild( Name, ".Player." );
        String GetName = GetHost( IP );

        if ( GetName.equals( "Unknown" ) ) {
            AddHostToSQL( IP, Utility.StringBuild( DataName, "none" ) );
        } else {
            if ( !GetName.contains( "Player" ) ) {
                int dataLength = 60 - DataName.length();
                if ( GetName.length() > dataLength ) { GetName = String.format( "%" + dataLength + "s", GetName ); }
                chgUnknownHost( IP, Utility.StringBuild( DataName, GetName ) );
            }
        }
    }

    /**
     * IPアドレスからホスト名を取得、接続国を特定する
     *
     * @param IP
     * @return
     */
    public String GetLocale( String IP ) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            Tools.Prt( "GetLocale : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) {
                String HostName = rs.getString( "host" );
                Tools.Prt( Utility.StringBuild( "GetHostName = ", HostName ), consoleMode.normal, programCode );
                String[] item = HostName.split( "\\.", 0 );
                for( int i=0; i<item.length; i++ ){
                    Tools.Prt( i + " : " + item[i], consoleMode.full, programCode );
                }
                return item[ item.length - 1 ].toUpperCase();
            }
        } catch ( SQLException e ) {
            Tools.Prt( "Error GetLocale : " + e.getMessage(), programCode );
        }
        return "JP";
    }

    /**
     * ホスト名を新規追加する
     *
     * @param IP
     * @param Host
     */
    public void AddHostToSQL( String IP, String Host ) {
        try {

            String sql = "INSERT INTO hosts ( ip, host, count, newdate, lastdate ) VALUES ( INET_ATON( ? ), ?, ?, ?, ? );";
            Tools.Prt( "AddHostToSQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.setString( 1, IP );
            preparedStatement.setString( 2, Host );
            preparedStatement.setInt( 3, 0 );
            preparedStatement.setString( 4, sdf.format( new Date() ) );
            preparedStatement.setString( 5, sdf.format( new Date() ) );

            preparedStatement.executeUpdate();

        } catch ( SQLException e ) {
            Tools.Prt( "Error AddHostToSQL : " + e.getMessage(), programCode );
        }
    }

    /**
     * 登録IPアドレスを削除する
     *
     * @param IP
     * @return
     */
    public boolean DelHostFromSQL( String IP ) {
        try {
            String sql = "DELETE FROM hosts WHERE INET_NTOA(ip) = '" + IP + "'";
            Tools.Prt( "DelHostFromSQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();
            return true;
        } catch ( SQLException e ) {
            Tools.Prt( "Error DelHostFromSQL : " + e.getMessage(), programCode );
            return false;
        }
    }

    /**
     * IPアドレスからホスト名を取得する
     *
     * @param IP
     * @return
     */
    public String GetHost( String IP ) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            Tools.Prt( "GetHost : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) return rs.getString( "host" );
        } catch ( SQLException e ) {
            Tools.Prt( "Error GetUnknownHost : " + e.getMessage(), programCode );
        }
        return "Unknown";
    }

    /**
     * hosts からキーワードに該当するホストを取得する
     *
     * @param p
     * @param word
     */
    public void SearchHost( Player p, String word ) {

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE host LIKE '%" + word + "%' ORDER BY ip DESC;";
            Tools.Prt( "SearchHost : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            Tools.Prt( p, Utility.StringBuild( ChatColor.YELLOW.toString(), "Search host [", word, "]..." ), programCode );

            int DataNum = 0;
            while( rs.next() ) {
                DataNum++;
                Tools.Prt( p,
                    Utility.StringBuild(
                        ChatColor.WHITE.toString(), String.valueOf( DataNum ), ":",
                        ChatColor.GRAY.toString(), rs.getString( "host" ),
                        ChatColor.GREEN.toString(), "(", String.valueOf( rs.getInt( "count" ) ), ")",
                        ChatColor.YELLOW.toString(), "[", InetCalc.toInetAddress( rs.getLong( "ip" ) ), "]",
                        ChatColor.WHITE.toString(), sdf.format( rs.getTimestamp( "lastdate" ) )
                    ),
                    programCode
                );
            }

            if ( DataNum == 0 ) Tools.Prt( p, Utility.StringBuild( ChatColor.RED.toString(), "No data for [", word, "]" ), programCode );

        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Search Error : " + e.getMessage(), programCode );
        }
    }

    /**
     * クマイス鯖専用関数、特定のホスト名を決め打ちのホスト名に変換
     *
     * @param hostName  チェックするホスト名
     * @return
     */
    public String changeHostName( String hostName ) {
        if ( hostName.contains( "gae.google" ) ) { hostName = "gae.googleusercontent.com"; }
        if ( hostName.contains( "bbtec.net" ) ) { hostName = "softbank.bbtec.net"; }
        if ( hostName.contains( "ec2" ) ) {
            String[] NameItem = hostName.split( "\\.", 0 );
            StringBuilder buf = new StringBuilder();
            for( int i = 1; i < NameItem.length; i++ ){
                Tools.Prt( String.format( "%2d : %s", i, NameItem[i] ), consoleMode.max, programCode );
                if( i != 1 ) buf.append( "." );
                buf.append( NameItem[i] );
            }
            hostName = buf.toString();
        }
        return hostName;
    }

    /**
     * データベース内で未変換のホスト名を一括返還する
     *
     * @param p
     */
    public void convertHostName( Player p ) {
        Tools.Prt( p, ChatColor.YELLOW + "Kumaisu Data Converter Execute", programCode );
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts;";
            Tools.Prt( "convertHostName : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            while( rs.next() ) {
                String orgHostName = rs.getString( "host" );
                String getHostName = changeHostName( orgHostName );
                if ( !orgHostName.equals( getHostName ) ) {
                    Tools.Prt( "Change " + orgHostName + " to " + getHostName, programCode );
                    String chg_sql = "UPDATE hosts SET host = '" + getHostName + "' WHERE ip = " + rs.getLong( "ip" ) + ";";
                    Tools.Prt( "convertHostName : " + chg_sql, Tools.consoleMode.max, programCode );
                    PreparedStatement preparedStatement = connection.prepareStatement( chg_sql );
                    preparedStatement.executeUpdate();
                }
            }
            Tools.Prt( p, ChatColor.YELLOW + "Convert Finished", programCode );
        } catch ( SQLException e ) {
            Tools.Prt( "HostName Convert Error : " + e.getMessage(), programCode );
        }
    }

    /**
     * 新規IPをhostsへ登録する処理
     * クマイス鯖特有のホスト名変更処理
     *
     * @param IP
     * @param CheckFlag
     * @return
     * @throws UnknownHostException
     */
    public String getUnknownHost( String IP, boolean CheckFlag ) throws UnknownHostException {
        Tools.Prt( Utility.StringBuild( ChatColor.RED.toString(), "Unknown New Record : ", ChatColor.AQUA.toString(), IP ), programCode );
        String HostName = "Unknown(IP)";
        if ( CheckFlag ) {
            Inet4Address inet = ( Inet4Address ) Inet4Address.getByName( IP );
            if ( !inet.getHostName().equals( IP ) ) { HostName = inet.getHostName(); }
        }

        //  クマイス鯖特有の特別処理
        if ( Config.kumaisu ) {
            Tools.Prt( Utility.StringBuild( ChatColor.GREEN.toString(), "Original Hostname = ", ChatColor.AQUA.toString(), HostName ), consoleMode.full, programCode );
            HostName = changeHostName( HostName );
        }

        if ( HostName.length()>60 ) { HostName = String.format( "%-60s", HostName ); }

        Tools.Prt( Utility.StringBuild( ChatColor.GREEN.toString(), "Change Hostname = ", ChatColor.AQUA.toString(), HostName ), consoleMode.normal, programCode );
        AddHostToSQL( IP, HostName );
        return HostName;
    }

    /**
     * Internet DNS に対してホスト名を照会するためにIPを使ってPingを打つ
     *
     * @param IP
     * @return
     * @throws UnknownHostException
     */
    public String ping( String IP ) throws UnknownHostException {
        Inet4Address inet = ( Inet4Address ) Inet4Address.getByName( IP );
        return inet.getHostName();
    }

    /**
     * 新規の照会があった場合に、テキストファイルへ日時と共に記録する
     *
     * @param IP
     * @param DataFolder
     * @return
     */
    public boolean WriteFileUnknown( String IP, String DataFolder ) {
        File UKfile = new File( DataFolder, "UnknownIP.yml" );
        FileConfiguration UKData = YamlConfiguration.loadConfiguration( UKfile );

        SimpleDateFormat cdf = new SimpleDateFormat("yyyyMMddHHmmss");

        UKData.set( cdf.format( new Date() ),Utility.StringBuild( IP, "[", GetHost( IP ), "]" ) );
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
     * IPアドレスから初アクセスまたは、最終アクセス日を取得する
     *
     * @param IP
     * @param newf  True:初アクセス　False:最終アクセス
     * @return
     * @throws ClassNotFoundException
     */
    public String getDateHost( String IP, boolean newf ) throws ClassNotFoundException {
        //  True : カウントを開始した日を指定
        //  False : 最後にカウントされた日を指定
        try {
            Statement stmt;
            stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            Tools.Prt( "getDateHost : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) {
                if ( newf ) {
                    return sdf.format( rs.getTimestamp( "newdate" ) );
                } else {
                    return sdf.format( rs.getTimestamp( "lastdate" ) );
                }
            }
        } catch ( SQLException e ) {
            Tools.Prt( "SQLException : " + e.getMessage(), programCode );
        }
        return "";
    }

    /**
     * IPアドレスの参照回数を取得する
     *
     * @param IP
     * @return
     * @throws UnknownHostException
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public int GetcountHosts( String IP ) throws UnknownHostException {
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            Tools.Prt( "GetcountHosts : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) return rs.getInt( "count" );

        } catch ( SQLException e ) {
            Tools.Prt( "Error GetCountHosts : " + e.getMessage(), programCode );
        }
        return 0;
    }

    /**
     * 照会回数を加算する
     *
     * @param IP
     * @param ZeroF
     * @throws UnknownHostException
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void AddCountHost( String IP, int ZeroF ) throws UnknownHostException {
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            Tools.Prt( "AddCountHost : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) {
                String ResetDate = "";

                int count = ZeroF;
                if ( ZeroF < 0 ) {
                    count = 0;
                    ResetDate = Utility.StringBuild( ", newdate = '", sdf.format( new Date() ), "'" );
                }
                if ( ZeroF == 0 ) count = rs.getInt( "count" ) + 1;

                String chg_sql = "UPDATE hosts SET count = " + count + ResetDate + ", lastdate = '" + sdf.format( new Date() ) + "' WHERE INET_NTOA( ip ) = '" + IP + "';";
                Tools.Prt( "AddCountHost : " + chg_sql, Tools.consoleMode.max, programCode );
                PreparedStatement preparedStatement = connection.prepareStatement( chg_sql );
                preparedStatement.executeUpdate();
            }

        } catch ( SQLException e ) {
            Tools.Prt( "Error AddCountHosts : " + e.getMessage(), programCode );
        }
    }

    /**
     * IPアドレスの登録ホスト名を変更する
     *
     * @param IP
     * @param Hostname
     * @return
     */
    public boolean chgUnknownHost( String IP, String Hostname ) {

        if ( Hostname.length()>60 ) { Hostname = String.format( "%-60s", Hostname ); }

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            Tools.Prt( "chgUnknownHost : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) {
                String chg_sql = "UPDATE hosts SET host = '" + Hostname + "' WHERE INET_NTOA( ip ) = '" + IP + "';";
                Tools.Prt( "chgUnknownHost : " + chg_sql, Tools.consoleMode.max, programCode );
                PreparedStatement preparedStatement = connection.prepareStatement( chg_sql );
                preparedStatement.executeUpdate();
                return true;
            } else {
                Tools.Prt( ChatColor.RED + "could not get " + IP, programCode );
            }
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Change Database Error [" + IP + "][" + Hostname + "]", programCode );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage(), programCode );
        }
        return false;
    }

    /**
     * 登録されているIPの情報を表示する
     *
     * @param p
     * @param IP
     */
    public void infoUnknownHost( Player p, String IP ) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            Tools.Prt( "infoUnknownHost : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) {
                Tools.Prt( p, ChatColor.YELLOW + "Check Unknown IP Information.......", programCode );
                Tools.Prt( p, ChatColor.GREEN + "IP Address  : " + ChatColor.WHITE + InetCalc.toInetAddress( rs.getLong( "ip" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "Host Name   : " + ChatColor.WHITE + rs.getString( "host" ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "AccessCount : " + ChatColor.WHITE + String.valueOf( rs.getInt( "count" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "First Date  : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "newdate" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "Last Date   : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "lastdate" ) ), programCode );
            } else {
                Tools.Prt( p, ChatColor.RED + "No data for [" + IP + "]", programCode );
            }
        } catch ( SQLException e ) {
            Tools.Prt( p, ChatColor.RED + "Error Information : ", programCode );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage(), programCode );
        }
    }

    /**
     * DB上の重複をチェックする
     *
     * @param p
     */
    public void DuplicateCheck( Player p ) {
        //  重複チェック mysql> SELECT ip FROM hosts GROUP BY ip HAVING COUNT(ip) > 1;
        Tools.Prt( p, ChatColor.GREEN + "== Database Duplicat Check ==", programCode );

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT ip FROM hosts GROUP BY ip HAVING COUNT(ip) > 1;";
            Tools.Prt( "DublicateCheck : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            while( rs.next() ) {
                Tools.Prt( p, ChatColor.YELLOW + "Duplicate Check IP Information.......", programCode );
                Tools.Prt( p, ChatColor.GREEN + "IP Address  : " + ChatColor.WHITE + InetCalc.toInetAddress( rs.getLong( "ip" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "Host Name   : " + ChatColor.WHITE + rs.getString( "host" ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "AccessCount : " + ChatColor.WHITE + String.valueOf( rs.getInt( "count" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "First Date  : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "newdate" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "Last Date   : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "lastdate" ) ), programCode );
            }
        } catch ( SQLException e ) {
            Tools.Prt( p, ChatColor.RED + "Error Information", programCode );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage(), programCode );
        }

        Tools.Prt( p, ChatColor.GREEN + "================", programCode );
    }

    /**
     * サーバーへの照会回数が多い順位表示
     *
     * @param p
     * @param Lines 表示する順位の人数
     */
    public void PingTop( Player p, int Lines ) {
        Tools.Prt( p, ChatColor.GREEN + "== Ping Count Top " + Lines + " ==", programCode );

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts ORDER BY count DESC;";
            Tools.Prt( "PingTop : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            int i = 0;
            String chk_name = "";

            while( rs.next() && ( i<Lines ) ) {
                String GetName = rs.getString( "host" );

                if ( !chk_name.equals( GetName ) ) {
                    i++;
                    Tools.Prt( p,
                        Utility.StringBuild(
                            ChatColor.AQUA.toString(), String.format( "%5d", rs.getInt("count" ) ), ": ",
                            ChatColor.YELLOW.toString(), String.format( "%-15s", InetCalc.toInetAddress( rs.getLong( "ip" ) ) ), " ",
                            ChatColor.LIGHT_PURPLE.toString(), String.format( "%-40s", Utility.CutMiddleString( rs.getString( "host" ), 40 ) ), " ",
                            ChatColor.WHITE.toString(), sdf.format( rs.getTimestamp( "lastdate" ) ), " ",
                            ChatColor.GOLD.toString(), sdf.format( rs.getTimestamp( "newdate" ) )
                        ), programCode
                    );
                    chk_name = GetName;
                }
            }
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error Pingtop Listing ...", programCode );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage(), programCode );
        }

        Tools.Prt( p, ChatColor.GREEN + "================", programCode );
    }
}