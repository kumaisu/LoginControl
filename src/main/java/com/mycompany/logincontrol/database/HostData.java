/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.database;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.kumaisulibraries.InetCalc;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class HostData {

    /**
     * ホスト名を新規追加する
     *
     * @param IP
     * @param Host
     */
    public static void AddSQL( String IP, String Host ) {
        try ( Connection con = Database.dataSource.getConnection() ) {
            String sql = "INSERT INTO hosts ( ip, host, count, newdate, lastdate, warning ) VALUES ( INET_ATON( ? ), ?, ?, ?, ?, ? );";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = con.prepareStatement( sql );
            preparedStatement.setString( 1, IP );
            preparedStatement.setString( 2, Host );
            preparedStatement.setInt( 3, 0 );
            preparedStatement.setString( 4, Database.sdf.format( new Date() ) );
            preparedStatement.setString( 5, Database.sdf.format( new Date() ) );
            preparedStatement.setInt( 6, 0 );
                
            preparedStatement.executeUpdate();
            Tools.Prt( "Add Host to SQL Success.", Tools.consoleMode.max, programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error AddHostToSQL : " + e.getMessage(), programCode );
        }
    }

    /**
     * 登録IPアドレスを削除する
     *
     * @param IP
     * @return
     */
    public static boolean DelSQL( String IP ) {
        try ( Connection con = Database.dataSource.getConnection() ) {
            String sql = "DELETE FROM hosts WHERE INET_NTOA(ip) = '" + IP + "'";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = con.prepareStatement( sql );
            preparedStatement.executeUpdate();
            con.close();
            Tools.Prt( "Del Host from SQL Success.", Tools.consoleMode.max, programCode );
            return true;
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error DelHostFromSQL : " + e.getMessage(), programCode );
            return false;
        }
    }

    /**
     * IPアドレスからホスト名を取得する
     *
     * @param IP
     * @return
     */
    public static boolean GetSQL( String IP ) {
        boolean retFlag = false;
        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            Tools.Prt( "GetHost : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) {
                Database.IP         = InetCalc.toInetAddress( rs.getLong( "ip" ) );
                Database.Host       = rs.getString( "host" );
                Database.Count      = rs.getInt( "count" );
                Database.NewDate    = rs.getTimestamp( "newdate" );
                Database.LastDate   = rs.getTimestamp( "lastdate" );
                Database.Warning    = ( rs.getInt( "warning" ) == 0 );
                retFlag = true;
            }
            con.close();
            Tools.Prt( "Get Host from SQL Success.", Tools.consoleMode.max, programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error GetHost : " + e.getMessage(), programCode );
        }
        return retFlag;
    }

    /**
     * 新規IPをhostsへ登録する処理
     * クマイス鯖特有のホスト名変更処理
     *
     * @param IP
     * @param CheckFlag
     * @return
     */
    public static String AddHostname( String IP, boolean CheckFlag ) {
        Tools.Prt( Utility.StringBuild( ChatColor.RED.toString(), "Unknown New Record : ", ChatColor.AQUA.toString(), IP ), programCode );
        String HostName = "Unknown(IP)";
        if ( CheckFlag ) {
            Inet4Address inet = null;
            try {
                inet = ( Inet4Address ) Inet4Address.getByName( IP );
            } catch ( UnknownHostException ex ) {
                Tools.Prt( ChatColor.RED + "getUnknownHost (AddressConv) : " + ex.getMessage(), programCode );
            }
            if ( !inet.getHostName().equals( IP ) ) { HostName = inet.getHostName(); }
        }

        //  クマイス鯖特有の特別処理
        if ( Config.kumaisu ) {
            Tools.Prt( Utility.StringBuild( ChatColor.GREEN.toString(), "Original Hostname = ", ChatColor.AQUA.toString(), HostName ), Tools.consoleMode.full, programCode );
            HostName = changeHostName( HostName );
        }

        if ( HostName.length()>60 ) { HostName = String.format( "%-60s", HostName ); }

        Tools.Prt( Utility.StringBuild( ChatColor.GREEN.toString(), "Change Hostname = ", ChatColor.AQUA.toString(), HostName ), Tools.consoleMode.normal, programCode );
        AddSQL( IP, HostName );
        return HostName;
    }

    /**
     * IPアドレスの登録ホスト名を変更する
     *
     * @param IP
     * @param Hostname
     * @return
     */
    public static boolean ChgHostname( String IP, String Hostname ) {
        if ( Hostname.length()>60 ) { Hostname = String.format( "%-60s", Hostname ); }

        try ( Connection con = Database.dataSource.getConnection() ) {
            String sql = "UPDATE hosts SET host = '" + Hostname + "' WHERE INET_NTOA( ip ) = '" + IP + "';";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = con.prepareStatement( sql );
            preparedStatement.executeUpdate();
            con.close();
            Tools.Prt( "Change HostName to SQL Success", Tools.consoleMode.max, programCode );
            return true;
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Change Database Error [" + IP + "][" + Hostname + "]", programCode );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage(), programCode );
        }

        return false;
    }

    /**
     * hosts からキーワードに該当するホストを取得する
     *
     * @param p
     * @param word
     */
    public static void SearchHostname( Player p, String word ) {
        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
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
                        ChatColor.WHITE.toString(), Database.sdf.format( rs.getTimestamp( "lastdate" ) )
                    ),
                    programCode
                );
            }

            if ( DataNum == 0 ) Tools.Prt( p, Utility.StringBuild( ChatColor.RED.toString(), "No data for [", word, "]" ), programCode );
            con.close();
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Search Error : " + e.getMessage(), programCode );
        }
    }

    /**
     * 登録されているIPの情報を表示する
     *
     * @param p
     * @param IP
     */
    public static void infoHostname( Player p, String IP ) {
        if ( GetSQL( IP ) ) {
            Tools.Prt( p, ChatColor.YELLOW + "Check Unknown IP Information.......", programCode );
            Tools.Prt( p, ChatColor.GREEN + "IP Address  : " + ChatColor.WHITE + Database.IP, programCode );
            Tools.Prt( p, ChatColor.GREEN + "Host Name   : " + ChatColor.WHITE + Database.Host, programCode );
            Tools.Prt( p, ChatColor.GREEN + "AccessCount : " + ChatColor.WHITE + Database.Count, programCode );
            Tools.Prt( p, ChatColor.GREEN + "First Date  : " + ChatColor.WHITE + Database.sdf.format( Database.NewDate ), programCode );
            Tools.Prt( p, ChatColor.GREEN + "Last Date   : " + ChatColor.WHITE + Database.sdf.format( Database.LastDate ), programCode );
        } else {
            Tools.Prt( p, ChatColor.RED + "No data for [" + IP + "]", programCode );
        }
    }

    /**
     * 照会回数を加算する
     *
     * @param IP
     * @param ZeroF
     */
    public static void AddCountHost( String IP, int ZeroF ) {
        try ( Connection con = Database.dataSource.getConnection() ) {
            String sql = "UPDATE hosts SET count = ";

            if ( ZeroF == 0 ) {
                sql += "count + 1";
                Database.Count++;
            } else {
                if ( ZeroF < 0 ) {
                    //  sql += Utility.StringBuild( "0, newdate = '", Database.sdf.format( new Date() ), "'" );
                    //  Database.NewDate = new Date();
                    sql += Utility.StringBuild( "0, newdate = '", Database.sdf.format( Database.LastDate ), "'" );
                    Database.NewDate = Database.LastDate;
                } else {
                    sql += String.valueOf( ZeroF );
                }
            }

            sql += Utility.StringBuild( ", lastdate = '", Database.sdf.format( new Date() ), "' WHERE INET_NTOA( ip ) = '", IP, "';" );
            Database.LastDate = new Date();
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = con.prepareStatement( sql );
            preparedStatement.executeUpdate();
            con.close();
            Tools.Prt( "Add Reference Count Success", Tools.consoleMode.max, programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error AddCountHosts : " + e.getMessage(), programCode );
        }
    }

    public static void ChangeWarning( String IP, boolean Flag ) {
       try ( Connection con = Database.dataSource.getConnection() ) {
            String sql = "UPDATE hosts SET waning = " + ( Flag ? 0:1 ) + " WHERE INET_NTOA( ip ) = '" + IP + "';";
            Database.Warning = Flag;
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
            PreparedStatement preparedStatement = con.prepareStatement( sql );
            preparedStatement.executeUpdate();
            con.close();
            Tools.Prt( "Change Warning Flag Success", Tools.consoleMode.max, programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error ChangeWarning : " + e.getMessage(), programCode );
        }
     }

    /**
     * サーバーへの照会回数が多い順位表示
     *
     * @param p
     * @param Lines 表示する順位の人数
     */
    public static void PingTop( Player p, int Lines ) {
        Tools.Prt( p, ChatColor.GREEN + "== Ping Count Top " + Lines + " ==", programCode );

        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            String sql = "SELECT * FROM hosts ORDER BY count DESC;";
            Tools.Prt( "SQL : " + sql, Tools.consoleMode.max, programCode );
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
                            ChatColor.WHITE.toString(), Database.sdf.format( rs.getTimestamp( "lastdate" ) ), " ",
                            ChatColor.GOLD.toString(), Database.sdf.format( rs.getTimestamp( "newdate" ) )
                        ), programCode
                    );
                    chk_name = GetName;
                }
            }
            con.close();
            Tools.Prt( "PingTop Success", Tools.consoleMode.max, programCode );
        } catch ( SQLException e ) {
            Tools.Prt( ChatColor.RED + "Error Pingtop Listing ...", programCode );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage(), programCode );
        }

        Tools.Prt( p, ChatColor.GREEN + "================", programCode );
    }

    /**
     * DB上の重複をチェックする
     *
     * @param p
     */
    public static void DuplicateCheck( Player p ) {
        //  重複チェック mysql> SELECT ip FROM hosts GROUP BY ip HAVING COUNT(ip) > 1;
        Tools.Prt( p, ChatColor.GREEN + "== Database Duplicat Check ==", programCode );

        try ( Connection con = Database.dataSource.getConnection() ) {
            Statement stmt = con.createStatement();
            String sql = "SELECT ip FROM hosts GROUP BY ip HAVING COUNT(ip) > 1;";
            Tools.Prt( "DublicateCheck : " + sql, Tools.consoleMode.max, programCode );
            ResultSet rs = stmt.executeQuery( sql );

            while( rs.next() ) {
                Tools.Prt( p, ChatColor.YELLOW + "Duplicate Check IP Information.......", programCode );
                Tools.Prt( p, ChatColor.GREEN + "IP Address  : " + ChatColor.WHITE + InetCalc.toInetAddress( rs.getLong( "ip" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "Host Name   : " + ChatColor.WHITE + rs.getString( "host" ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "AccessCount : " + ChatColor.WHITE + String.valueOf( rs.getInt( "count" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "First Date  : " + ChatColor.WHITE + Database.sdf.format( rs.getTimestamp( "newdate" ) ), programCode );
                Tools.Prt( p, ChatColor.GREEN + "Last Date   : " + ChatColor.WHITE + Database.sdf.format( rs.getTimestamp( "lastdate" ) ), programCode );
            }
            con.close();
        } catch ( SQLException e ) {
            Tools.Prt( p, ChatColor.RED + "Error Information", programCode );
            //  エラー詳細ログの表示
            Tools.Prt( e.getMessage(), programCode );
        }

        Tools.Prt( p, ChatColor.GREEN + "================", programCode );
    }

    /**
     * データベースにプレイヤー情報を付加する
     * データが無い場合は新規に追加する
     *
     * @param IP
     * @param Name
     */
    public static void AddPlayerToSQL( String IP, String Name ) {
        String DataName = Utility.StringBuild( Name, ".Player." );
        if ( GetSQL( IP ) ) {
            if ( !Database.Host.contains( "Player" ) ) {
                String GetName = Database.Host;
                int dataLength = 60 - DataName.length();
                if ( GetName.length() > dataLength ) { GetName = String.format( "%" + dataLength + "s", GetName ); }
                ChgHostname( IP, Utility.StringBuild( DataName, GetName ) );
            }
        } else AddSQL( IP, Utility.StringBuild( DataName, "none" ) );
    }

    /**
     * クマイス鯖専用関数、特定のホスト名を決め打ちのホスト名に変換
     *
     * @param hostName  チェックするホスト名
     * @return
     */
    public static String changeHostName( String hostName ) {
        if ( hostName.contains( "gae.google" ) ) { hostName = "gae.googleusercontent.com"; }
        if ( hostName.contains( "bbtec.net" ) ) { hostName = "softbank.bbtec.net"; }
        if ( hostName.contains( "ec2" ) ) {
            String[] NameItem = hostName.split( "\\.", 0 );
            StringBuilder buf = new StringBuilder();
            for( int i = 1; i < NameItem.length; i++ ){
                Tools.Prt( String.format( "%2d : %s", i, NameItem[i] ), Tools.consoleMode.max, programCode );
                if( i != 1 ) buf.append( "." );
                buf.append( NameItem[i] );
            }
            hostName = buf.toString();
        }
        return hostName;
    }

}
