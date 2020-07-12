/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.database;

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
import org.bukkit.entity.Player;
import com.mycompany.kumaisulibraries.InetCalc;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.config.Config;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class ListData {

    /**
     * リストステータスを新規に追加する
     *
     * @param date
     * @param name
     * @param UUID
     * @param IP
     * @param Status
     */
    public static void AddSQL( Date date, String name, String UUID, String IP, int Status ) {
        /*
        getLogger().log( Level.INFO, "Date   : {0}", sdf.format( date ) );
        getLogger().log( Level.INFO, "name   : {0}", name );
        getLogger().log( Level.INFO, "UUID   : {0}", UUID );
        getLogger().log( Level.INFO, "IP     : {0}", IP );
        getLogger().log( Level.INFO, "Status : {0}", Status );
        */
        try ( Connection con = Database.dataSource.getConnection() ) {
            String sql = "INSERT INTO list (date, name, uuid, ip, status) VALUES (?, ?, ?, INET_ATON( ? ), ?);";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = con.prepareStatement( sql );
            preparedStatement.setString(1, Database.sdf.format( date ) );
            preparedStatement.setString(2, name );
            preparedStatement.setString(3, UUID );
            preparedStatement.setString(4, IP );
            preparedStatement.setInt(5, Status );

            preparedStatement.executeUpdate();
            con.close();
            Tools.Prt( "Add List to SQL Success", Tools.consoleMode.max, programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error PreSavePlayer : " + e.getMessage(), programCode );
        }
    }

    /**
     * IPアドレスから、最後にログインしたプレイヤー名を取得
     *
     * @param ip
     * @return     取得成功時はプレイヤー名、記録が無い時はUnknownを戻す
     *              SQLエラーが発生した場合は、IPアドレスを戻す
     */
    public static String GetPlayerName( String ip ) {
        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + ip + "' ORDER BY date DESC;";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );
            String retStr = ( rs.next() ? rs.getString( "name" ):"Unknown" );
            con.close();
            Tools.Prt( "listGetPlayerName Success", Tools.consoleMode.max, programCode );
            return retStr;
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error listGetPlayerName : " + e.getMessage(), programCode );
        }
        return ip;
    }

    /**
     * リストステータスを変更する
     *
     * @param date
     * @param status
     */
    public static void ChangeStatus( Date date, int status ) {
        try ( Connection con = Database.dataSource.getConnection() ) {
            String sql = "UPDATE list SET status = " + String.valueOf( status ) + " WHERE date = '" + Database.sdf.format( date ) + "';";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.executeUpdate();
            con.close();
            Tools.Prt( "listChangeStatus Success", Tools.consoleMode.max, programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error ChangeStatus : " + e.getMessage(), programCode );
        }
    }

    /**
     * 同一IPアドレスで別名のログインがあるかのチェックを行う
     *
     * @param player    結果を表示するプレイヤー
     */
    public static void CheckIP( Player player ) {
        List<String> PrtData;
        PrtData = new ArrayList<>();
        List<String> NameData;
        NameData = new ArrayList<>();

        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== Check IP Address ===", ChatColor.YELLOW.toString(), "[", player.getAddress().getHostString(), "]" ) );

        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + player.getAddress().getHostString() + "' ORDER BY date DESC;";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
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
            con.close();
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error listCheckIP : " + e.getMessage(), programCode );
        }

        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== end ===" ) );

        if ( NameData.size() < 2 ) { return; }

        PrtData.stream().forEach( PD -> {
            String msg = PD;
            Tools.Prt( msg, Tools.consoleMode.normal, Config.programCode );
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> (
                ( p.hasPermission( "LoginCtl.view" ) ) ) ).forEachOrdered( ( p ) -> {
                    Tools.Prt( p, msg, Config.programCode );
                }
            );
        } );
    }

    /**
     * 同一UUIDで別IPアドレスのアクセスがあるかチェックする
     *
     * @param player 
     */
    public static void CheckUUID( Player player ) {
        List<String> PrtData;
        PrtData = new ArrayList<>();
        List<String> IPData;
        IPData = new ArrayList<>();

        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== Check UUID ===", ChatColor.YELLOW.toString(), "[", player.getAddress().getHostString(), "]" ) );

        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM list WHERE uuid = '" + player.getUniqueId().toString() + "' ORDER BY date DESC;";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            Tools.Prt( "listCheckUUID : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            int i = 0;
            while( rs.next() ) {
                String GetIP = InetCalc.toInetAddress( rs.getLong( "ip" ) );
                
                if ( i>0 ) {
                    if ( !IPData.contains( GetIP ) ) {
                        i++;
                        IPData.add( GetIP );
                        PrtData.add( LinePrt( player,rs ) );
                    }
                } else {
                    i++;
                }
            }
            con.close();
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error listCheckUUID : " + e.getMessage(), programCode );
        }

        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== end ===" ) );

        if ( IPData.size() < 2 ) { return; }

        PrtData.stream().forEach( PD -> {
            String msg = PD;
            Tools.Prt( msg, Tools.consoleMode.normal, Config.programCode );
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> (
                ( p.hasPermission( "LoginCtl.view" ) ) ) ).forEachOrdered( ( p ) -> {
                    Tools.Prt( p, msg, Config.programCode );
                }
            );
        } );
        
    }

    /**
     * 直近のログインプレイヤーリストを表示する関数
     *
     * @param player    表示するプレイヤー
     * @param lines     リストに表示する人数（過去lines人分)
     * @param FullFlag  重複ログインを省略しないか？
     */
    public static void LogPrint( Player player, int lines, boolean FullFlag ) {
        boolean hasPermission = ( ( player == null ) || player.hasPermission( "LoginCtl.view" ) );

        Tools.Prt( player, "== Login List ==", programCode );

        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM list ORDER BY date DESC;";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
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
            con.close();
            Tools.Prt( player, "================", programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error LogPrint : " + e.getMessage(), programCode );
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
    public static boolean exLogPrint( Player player, String checkString, boolean FullFlag, int PrtMode, int lines )  {
        String sqlCmd;
        String checkName = "";
        boolean isOP = ( ( player == null ) ? true:player.isOp() );

        String titleMessage = ChatColor.WHITE + "== [" + checkString + "] Login List ==";
        if ( PrtMode == 3 && isOP ) {
            HostData.GetSQL( checkString );
            titleMessage += ChatColor.YELLOW + " [" + Database.Host + "]";
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

        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
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
            con.close();
            Tools.Prt( player, "================", programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error exLogPrint : " + e.getMessage(), programCode );
            return false;
        }
        return true;
    }

    /**
     * ユーザー情報を1ラインで表紙成形する関数
     * Permission保持者には追加情報も付随する
     *
     * @param player    表示したいプレイヤー
     * @param gs        DBから取得したデータ
     * @return          成形されたメッセージ
     */
    public static String LinePrt( Player player, ResultSet gs ) {
        String message = "";
        try {
            message = Utility.StringBuild( message,
                    ChatColor.WHITE.toString(), String.format( "%6d", gs.getInt( "id" ) ), ": ",
                    ChatColor.GREEN.toString(), Database.sdf.format( gs.getTimestamp( "date" ) ), " "
            );

            if ( ( player == null ) || player.isOp() || player.hasPermission( "LoginCtl.view" ) ) {
                message = Utility.StringBuild( message,
                        ChatColor.YELLOW.toString(),
                        "[",
                        String.format( "%-15s", InetCalc.toInetAddress( gs.getLong( "ip" ) ) ),
                        "] "
                );
            }

            message = Utility.StringBuild( message, gs.getInt( "status" )==0 ? ChatColor.RED.toString() : ChatColor.AQUA.toString() );

            if ( player == null ) {
                HostData.GetSQL( InetCalc.toInetAddress( gs.getLong( "ip" ) ) );
                message = Utility.StringBuild(
                        message,
                        String.format( "%-15s %s", gs.getString( "name" ), Database.Host )
                );
            } else {
                message = Utility.StringBuild( message, gs.getString( "name" ) );
            }

        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error LinePrt : " + e.getMessage(), programCode );
        }

        return message;
    }

}
