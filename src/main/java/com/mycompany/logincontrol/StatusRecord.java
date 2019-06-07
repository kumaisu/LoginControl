/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
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
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.kumaisulibraries.InetCalc;
import com.mycompany.logincontrol.tool.Tools;

/**
 * 主にmySQLとの通信を司るライブラリ
 *
 * @author sugichan
 */
public class StatusRecord {

    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private Connection connection;
    private final String host, database, port, username, password;
    private final boolean Kumaisu;

    /**
     * ライブラリー読込時の初期設定
     *
     * @param CFhost
     * @param CFdb
     * @param CFport
     * @param CFuser
     * @param CFpass
     * @param KumaFlag
     */
    public StatusRecord( String CFhost, String CFdb, String CFport, String CFuser, String CFpass, boolean KumaFlag ) {
        Kumaisu = KumaFlag;
        host = CFhost;
        database = CFdb;
        port = CFport;
        username = CFuser;
        password = CFpass;
    }

    /**
     * ユーザー情報を1ラインで表紙成形する関数
     * Permission保持者には追加情報も付随する
     *
     * @param player    表示したいプレイヤー
     * @param hasPermission
     * @param gs        DBから取得したデータ
     * @return          成形されたメッセージ
     */
    public String LinePrt( Player player, ResultSet gs, boolean hasPermission ) {
        String message = "";
        try {
            message = Utility.StringBuild( message,
                    ChatColor.WHITE.toString(), String.format( "%6d", gs.getInt( "id" ) ), ": ",
                    ChatColor.GREEN.toString(), sdf.format( gs.getTimestamp( "date" ) ), " "
            );

            if ( hasPermission ) {
                message = Utility.StringBuild( message,
                        ChatColor.YELLOW.toString(), "[", String.format( "%-15s", InetCalc.toInetAddress( gs.getLong( "ip" ) ) ), "] "
                );
            }

            message = Utility.StringBuild( message, gs.getInt( "status" )==0 ? ChatColor.RED.toString():ChatColor.AQUA.toString() );

            if ( player == null ) {
                message = Utility.StringBuild( message,
                    String.format( "%-20s", gs.getString( "name" ) ),
                    ( gs.getInt( "status" )==0 ? ChatColor.RED.toString():ChatColor.WHITE.toString() ), " [",
                    GetHost( InetCalc.toInetAddress( gs.getLong( "ip" ) ) ), "]"
                );
            } else {
                message = Utility.StringBuild( message, gs.getString( "name" ) );
            }

        } catch ( SQLException e ) {}

        return message;
    }

    public String LinePrt( Player player, ResultSet gs ) {
        return LinePrt( player, gs, ( ( player == null ) || player.isOp() || player.hasPermission( "LoginCtl.view" ) ) );
    }

    /**
     * 直近のログインプレイヤーリストを表示する関数
     *
     * @param player    表示するプレイヤー
     * @param lines     リストに表示する人数（過去lines人分)
     * @param FullFlag  重複ログインを省略しないか？
     * @param Ignore    Ignoreに記録されていプレイヤーを表示するか？
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void LogPrint( Player player, int lines, boolean FullFlag, List Ignore ) {
        Utility.consoleMode consolePrint = ( ( player == null ) ? Utility.consoleMode.none : Utility.consoleMode.stop );
        boolean hasPermission = ( ( player == null ) || player.isOp() || player.hasPermission( "LoginCtl.view" ) );

        Tools.Prt( player, "== Login List ==", consolePrint );

        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );

            int i = 0;
            String chk_name = "";

            while( rs.next() && ( i<lines ) ) {
                String GetName = rs.getString( "name" );

                if ( rs.getInt( "status" ) != 0 || hasPermission ) {
                    if ( ( !Ignore.contains( GetName ) || hasPermission ) && ( !chk_name.equals( GetName ) || FullFlag ) ) {
                        i++;
                        Tools.Prt( player, LinePrt( player, rs, hasPermission ),  consolePrint );
                        chk_name = GetName;
                    }
                }
            }

            Tools.Prt( player, "================", consolePrint );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }

    /**
     * 色々な形式でのプレイヤー一覧を表示する関数
     *
     * @param player        結果を表示するプレイヤー、nullならばコンソール表示
     * @param checkString   検索する目的の文字列（プレイヤー名や日付など)
     * @param FullFlag      重複プレイヤーの表示可否（true:全部,false:省略)
     * @param ignoreName    非表示にするプレイヤー名リスト
     * @param ignoreIP      非表示にするIPアドレスリスト
     * @param PrtMode       一覧の形式指定（1:指定日の一覧,2:プレイヤーの履歴,3:IPアドレスの履歴）
     * @param lines         表示する行数指定
     * @return
     */
    public boolean exLogPrint( Player player, String checkString, boolean FullFlag, List ignoreName, List ignoreIP, int PrtMode, int lines )  {
        String sqlCmd;
        String checkName = "";
        Utility.consoleMode consolePrint = ( ( player == null ) ? Utility.consoleMode.none : Utility.consoleMode.stop );
        boolean isOP = ( ( player == null ) ? true:player.isOp() );

        String titleMessage = ChatColor.WHITE + "== [" + checkString + "] Login List ==";
        if ( PrtMode == 3 && isOP ) {
            titleMessage += ChatColor.YELLOW + " [" + GetHost( checkString ) + "]";
        }
        Tools.Prt( player, titleMessage, consolePrint );

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
            openConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery( sqlCmd );

            int loopCount = 0;
            SimpleDateFormat cdf = new SimpleDateFormat( "yyyyMMdd" );

            while( rs.next() && ( loopCount<lines ) ) {
                String getName = rs.getString( "name" );
                String getDate = cdf.format( rs.getTimestamp( "date" ) );
                if ( isOP || ( !ignoreName.contains( getName ) && !ignoreIP.contains( InetCalc.toInetAddress( rs.getLong( "ip" ) ) ) ) ) {
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
                        Tools.Prt( player, LinePrt( player, rs ), consolePrint );

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

            Tools.Prt( player, "================", consolePrint );

        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "Error exLogPrint" );
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
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + ip + "' ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            return ( rs.next() ? rs.getString( "name" ):"Unknown" );
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
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
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + player.getAddress().getHostString() + "' ORDER BY date DESC;";
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

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }

        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== end ===" ) );

        PrtData.stream().forEach( PD -> {
            String msg = PD;
            Tools.Prt( ( ( NameData.size() > 1 ) ? player:null ), msg, Utility.consoleMode.normal );
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> (
                ( player != p ) && ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ) ).forEachOrdered( ( p ) -> {
                    Tools.Prt( p, msg, Utility.consoleMode.max );
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
            openConnection();

            String sql = "UPDATE list SET status = " + String.valueOf( status ) + " WHERE date = '" + sdf.format( date ) + "';";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();

        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "Error ChangeStatus" );
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
            openConnection();

            String sql = "INSERT INTO list (date, name, uuid, ip, status) VALUES (?, ?, ?, INET_ATON( ? ), ?);";
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.setString(1, sdf.format( date ) );
            preparedStatement.setString(2, name );
            preparedStatement.setString(3, UUID );
            preparedStatement.setString(4, IP );
            preparedStatement.setInt(5, Status );

            preparedStatement.executeUpdate();

        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "Error PreSavePlayer" );
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
     * MySQLへのコネクション処理
     *
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void openConnection() throws SQLException, ClassNotFoundException {

        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized ( this ) {
            if ( connection != null && !connection.isClosed() ) {
                return;
            }
            Class.forName( "com.mysql.jdbc.Driver" );
            connection = DriverManager.getConnection( "jdbc:mysql://" + host + ":" + port + "/" + database, username, password );

            //  mysql> create table list(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip INTEGER UNSIGNED, status byte, index(id));
            //  テーブルの作成
            //  存在すれば、無視される
            String sql = "CREATE TABLE IF NOT EXISTS list(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip INTEGER UNSIGNED, status int, index(id))";
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();

            //  mysql> create table IF NOT EXISTS unknowns (ip varchar(22), host varchar(60), count int, newdate DATETIME, lastdate DATETIME );
            //  Unknowns テーブルの作成
            //  存在すれば、無視される
            sql = "CREATE TABLE IF NOT EXISTS hosts (ip INTEGER UNSIGNED, host varchar(60), count int, newdate DATETIME, lastdate DATETIME )";
            preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();
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
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) {
                String HostName = rs.getString( "host" );
                Tools.Prt( Utility.StringBuild( "GetHostName = ", HostName ), Utility.consoleMode.normal );
                String[] item = HostName.split( "\\.", 0 );
                for( int i=0; i<item.length; i++ ){
                    Tools.Prt( i + " : " + item[i], Utility.consoleMode.normal );
                }
                return item[ item.length - 1 ].toUpperCase();
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "Error GetLocale" );
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
            openConnection();

            String sql = "INSERT INTO hosts ( ip, host, count, newdate, lastdate ) VALUES ( INET_ATON( ? ), ?, ?, ?, ? );";
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.setString( 1, IP );
            preparedStatement.setString( 2, Host );
            preparedStatement.setInt( 3, 0 );
            preparedStatement.setString( 4, sdf.format( new Date() ) );
            preparedStatement.setString( 5, sdf.format( new Date() ) );

            preparedStatement.executeUpdate();

        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "Error AddHostToSQL" );
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
            openConnection();
            String sql = "DELETE FROM hosts WHERE INET_NTOA(ip) = '" + IP + "'";
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();
            return true;
        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "Error DelHostFromSQL" );
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
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) return rs.getString( "host" );
        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "Error GetUnknownHost" );
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
        Utility.consoleMode consolePrint = ( ( p == null ) ? Utility.consoleMode.none : Utility.consoleMode.stop );

        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE host LIKE '%" + word + "%' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );

            Tools.Prt( p, Utility.StringBuild( ChatColor.YELLOW.toString(), "Search host [", word, "]..." ), consolePrint );

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
                    ), consolePrint
                );
            }

            if ( DataNum == 0 ) Tools.Prt( p, Utility.StringBuild( ChatColor.RED.toString(), "No data for [", word, "]" ), consolePrint );

        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( ChatColor.RED + "Search Error" );
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
                Tools.Prt( i + " : " + NameItem[i], Utility.consoleMode.full );
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
        Utility.consoleMode consolePrint = ( ( p == null ) ? Utility.consoleMode.none : Utility.consoleMode.stop );
        Tools.Prt( p, ChatColor.YELLOW + "Kumaisu Data Converter Execute", consolePrint );
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts;";
            ResultSet rs = stmt.executeQuery( sql );

            while( rs.next() ) {
                String orgHostName = rs.getString( "host" );
                String getHostName = changeHostName( orgHostName );
                if ( !orgHostName.equals( getHostName ) ) {
                    Tools.Prt( "Change " + orgHostName + " to " + getHostName );
                    String chg_sql = "UPDATE hosts SET host = '" + getHostName + "' WHERE ip = " + rs.getLong( "ip" ) + ";";
                    PreparedStatement preparedStatement = connection.prepareStatement( chg_sql );
                    preparedStatement.executeUpdate();
                }
            }
            Tools.Prt( p, ChatColor.YELLOW + "Convert Finished", consolePrint );
        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( "HostName Convert Error" );
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
        Tools.Prt( Utility.StringBuild( ChatColor.RED.toString(), "Unknown New Record : ", ChatColor.AQUA.toString(), IP ) );
        String HostName = "Unknown(IP)";
        if ( CheckFlag ) {
            Inet4Address inet = ( Inet4Address ) Inet4Address.getByName( IP );
            if ( !inet.getHostName().equals( IP ) ) { HostName = inet.getHostName(); }
        }

        //  クマイス鯖特有の特別処理
        if ( Kumaisu ) {
            Tools.Prt( Utility.StringBuild( ChatColor.GREEN.toString(), "Original Hostname = ", ChatColor.AQUA.toString(), HostName ), Utility.consoleMode.full );
            HostName = changeHostName( HostName );
        }

        if ( HostName.length()>60 ) { HostName = String.format( "%-60s", HostName ); }

        Tools.Prt( Utility.StringBuild( ChatColor.GREEN.toString(), "Change Hostname = ", ChatColor.AQUA.toString(), HostName ), Utility.consoleMode.normal );
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
            Tools.Prt( Utility.StringBuild( ChatColor.RED.toString(), "Could not save UnknownIP File." ) );
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
            openConnection();
            Statement stmt;
            stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) {
                if ( newf ) {
                    return sdf.format( rs.getTimestamp( "newdate" ) );
                } else {
                    return sdf.format( rs.getTimestamp( "lastdate" ) );
                }
            }
        } catch ( SQLException e ) {
            Tools.Prt( "SQLException:" + e.getMessage() );
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
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) return rs.getInt( "count" );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
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
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
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
                PreparedStatement preparedStatement = connection.prepareStatement( chg_sql );
                preparedStatement.executeUpdate();
            }

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
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
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) {
                String chg_sql = "UPDATE hosts SET host = '" + Hostname + "' WHERE INET_NTOA( ip ) = '" + IP + "';";
                PreparedStatement preparedStatement = connection.prepareStatement( chg_sql );
                preparedStatement.executeUpdate();
                return true;
            } else {
                Tools.Prt( ChatColor.RED + "could not get " + IP );
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( ChatColor.RED + "Change Database Error [" + IP + "][" + Hostname + "]" );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage() );
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
        Utility.consoleMode consolePrint = ( ( p == null ) ? Utility.consoleMode.none : Utility.consoleMode.stop );
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );

            if ( rs.next() ) {
                Tools.Prt( p, ChatColor.YELLOW + "Check Unknown IP Information.......", consolePrint );
                Tools.Prt( p, ChatColor.GREEN + "IP Address  : " + ChatColor.WHITE + InetCalc.toInetAddress( rs.getLong( "ip" ) ), consolePrint );
                Tools.Prt( p, ChatColor.GREEN + "Host Name   : " + ChatColor.WHITE + rs.getString( "host" ), consolePrint );
                Tools.Prt( p, ChatColor.GREEN + "AccessCount : " + ChatColor.WHITE + String.valueOf( rs.getInt( "count" ) ), consolePrint );
                Tools.Prt( p, ChatColor.GREEN + "First Date  : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "newdate" ) ), consolePrint );
                Tools.Prt( p, ChatColor.GREEN + "Last Date   : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "lastdate" ) ), consolePrint );
            } else {
                Tools.Prt( p, ChatColor.RED + "No data for [" + IP + "]", consolePrint );
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error Information" );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage() );
        }
    }

    /**
     * サーバーへの照会回数が多い順位表示
     *
     * @param p
     * @param Lines 表示する順位の人数
     */
    public void PingTop( Player p, int Lines ) {
        Utility.consoleMode consolePrint = ( ( p == null ) ? Utility.consoleMode.none : Utility.consoleMode.stop );
        Tools.Prt( p, ChatColor.GREEN + "== Ping Count Top " + Lines + " ==", consolePrint );

        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts ORDER BY count DESC;";
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
                            ChatColor.YELLOW.toString(), String.format( "%-15s", InetCalc.toInetAddress( rs.getLong( "ip" ) ) ),
                            ChatColor.WHITE.toString(), String.format( "%-40s", rs.getString( "host" ) ),
                            ChatColor.WHITE.toString(), sdf.format( rs.getTimestamp( "lastdate" ) )
                        ), consolePrint
                    );
                    chk_name = GetName;
                }
            }

            Tools.Prt( p, ChatColor.GREEN + "================", consolePrint );
        } catch ( ClassNotFoundException | SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error Pingtop Listing ..." );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage() );
        }
    }
}
