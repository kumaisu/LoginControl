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

/**
 *
 * @author sugichan
 */
public class StatusRecord {

    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private Connection connection;
    private final String host, database, port, username, password;
    private final boolean Kumaisu;
    
    public StatusRecord( String CFhost, String CFdb, String CFport, String CFuser, String CFpass, boolean KumaFlag ) {
        Kumaisu = KumaFlag;
        host = CFhost;
        database = CFdb;
        port = CFport;
        username = CFuser;
        password = CFpass;
    }
    
    public static long ipToInt( Inet4Address ipAddr ) {
        long compacted = 0;
        byte[] bytes = ipAddr.getAddress();
        for ( int i=0 ; i<bytes.length ; i++ ) {
            compacted += ( bytes[i] * Math.pow( 256, 4-i-1 ) );
        }
        return compacted;
    }
    
    private static String toInetAddress( long ipAddress ) {
        long ip = ( ipAddress < 0 ) ? (long)Math.pow(2,32)+ipAddress : ipAddress;
        Inet4Address inetAddress = null;
        String addr =  String.valueOf((ip >> 24)+"."+((ip >> 16) & 255)+"."+((ip >> 8) & 255)+"."+(ip & 255));
        return addr;
    }

    public void LineMsg( Player p, int id, Date date, String name, long ip , int status ) {
        String message = Utility.StringBuild( String.format( "%6d", id ), ": ", sdf.format( date ), " ", String.format( "%-20s", name ) );
        if ( ( p == null ) || p.hasPermission( "LoginCtl.view" ) || p.isOp() ) {
            message = Utility.StringBuild( message, ChatColor.YELLOW.toString(), "[", String.format( "%-15s", toInetAddress( ip ) ), "]", ChatColor.RED.toString(), "(", ( status==0 ? "Attempted":"Logged in" ), ")" );
        }
        Utility.Prt( p, message, ( p == null ) );
    }
            
    @SuppressWarnings("CallToPrintStackTrace")
    public void LogPrint( Player player, int lines, boolean FullFlag, List Ignore ) {
        
        Utility.Prt( player, "== Login List ==", ( player == null ) );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            boolean isOP = ( ( player == null ) ? true:player.isOp() );
            
            int i = 0;
            String chk_name = "";
            
            while( rs.next() && ( i<lines ) ) {
                String GetName = rs.getString( "name" );
                
                if ( rs.getInt( "status" ) != 0 || player == null || player.hasPermission( "LoginCtl.view" ) || isOP ) {
                    if ( ( isOP || !Ignore.contains( GetName ) ) && ( ( !chk_name.equals( GetName ) ) || ( FullFlag ) ) ) {
                        i++;
                        LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getLong( "ip" ), rs.getInt( "status" ) );
                        chk_name = GetName;
                    }
                }
            }

            Utility.Prt( player, "================", ( player == null ) );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }

    public void DateLogPrint( Player player, String ChkDate, boolean FullFlag, List Ignore ) {
        Utility.Prt( player, Utility.StringBuild( "== [", ChkDate, "] Login List ==" ), ( player == null ) );

        try {        
            String chk_name = "";

            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE date BETWEEN '" + ChkDate + " 00:00:00' AND '" + ChkDate + " 23:59:59' ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            boolean isOP = ( ( player == null ) ? true:player.isOp() );

            while( rs.next() ) {
                String GetName = rs.getString( "name" );
                
                if ( ( isOP || !Ignore.contains( GetName )  ) && ( !chk_name.equals( GetName ) || ( FullFlag ) ) ) {
                    LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getLong( "ip" ), rs.getInt( "status" ) );
                    chk_name = GetName;
                }
            }
                    
            Utility.Prt( player, "================", ( player == null ) );

        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error DateLogPrint" );
        }
    }

    public void NameLogPrint( Player player, String ChkName, boolean FullFlag, int Flag ) {
        Utility.Prt( player, Utility.StringBuild( "== [", ChkName, "] Login List ==" ), ( player == null ) );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql;
            if ( Flag == 2 ) {
                sql = "SELECT * FROM list WHERE name = '" + ChkName + "' ORDER BY date DESC;";
            } else {
                sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + ChkName + "' ORDER BY date DESC;";
                FullFlag = true;
            }
            ResultSet rs = stmt.executeQuery( sql );
            
            SimpleDateFormat cdf = new SimpleDateFormat( "yyyyMMdd" );
            String ChkDate = "";
            int i = 0;
            
            while( rs.next() && ( i<30 ) ) {
                if ( !ChkDate.equals( cdf.format( rs.getTimestamp( "date" ) ) ) || FullFlag ) {
                    i++;
                    LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getLong( "ip" ), rs.getInt( "status" ) );
                    ChkDate = cdf.format( rs.getTimestamp( "date" ) );
                }
            }
                    
            Utility.Prt( player, "================", ( player == null ) );

        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error NameLogPrint" );
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String GetPlayerName( String ip ) {

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

    @SuppressWarnings( "CallToPrintStackTrace" )
    public void CheckIP( Player player, boolean Debug ) throws UnknownHostException {
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
                        PrtData.add( Utility.StringBuild( ChatColor.WHITE.toString(), String.format( "%6d", rs.getInt( "id" ) ), ": ", ChatColor.GREEN.toString(), sdf.format( rs.getTimestamp( "date" ) ), " ", String.format( "%-20s", GetName ) ) );
                    }
                }
            }
                    
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        
        PrtData.add( Utility.StringBuild( ChatColor.RED.toString(), "=== end ===" ) );

        PrtData.stream().forEach( PD -> {
            String msg = PD;
            if ( NameData.size() > 1 ) Utility.Prt( player, msg, Debug );
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( ( player != p ) && ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
        } );
    }

    public void ChangeStatus( Date date, int status ) {
        try {
            openConnection();

            String sql = "UPDATE list SET status = " + String.valueOf( status ) + " WHERE date = '" + sdf.format( date ) + "';";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error ChangeStatus" );
        }
    }
    
    public void PreSavePlayer( Date date, String name, String UUID, String IP, int Status ) {

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
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error PreSavePlayer" );
        }
    }
    
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

    public String GetLocale( String IP, boolean Debug ) {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) {
                String HostName = rs.getString( "host" );
                if ( Debug ) Bukkit.getServer().getConsoleSender().sendMessage( Utility.StringBuild( "GetHostName = ", HostName ) );
                String[] item = HostName.split( "\\.", 0 );
                for( int i=0; i<item.length; i++ ){
                    if ( Debug ) Bukkit.getServer().getConsoleSender().sendMessage( i + " : " + item[i] );
                }
                return item[ item.length - 1 ].toUpperCase();
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error GetLocale" );
        }
        return "JP";
    }
    
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
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error AddHostToSQL" );
        }
    }

    public boolean DelHostFromSQL( String IP ) {
        try {
            openConnection();
            String sql = "DELETE FROM hosts WHERE INET_NTOA(ip) = '" + IP + "'";
            PreparedStatement preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();
            return true;
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error DelHostFromSQL" );
            return false;
        }
    }

    public String GetHost( String IP ) {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) return rs.getString( "host" );
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( "[LoginControl] Error GetUnknownHost" );
        }
        return "Unknown";
    }

    public void SearchHost( Player p, String word ) {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE host LIKE '%" + word + "%' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );

            Utility.Prt( p, Utility.StringBuild( ChatColor.YELLOW.toString(), "Search host [", word, "]..." ), ( p == null ) );
            
            int DataNum = 0;
            while( rs.next() ) {
                DataNum++;
                Utility.Prt( p,
                    Utility.StringBuild(
                        ChatColor.WHITE.toString(), String.valueOf( DataNum ), ":",
                        ChatColor.GRAY.toString(), rs.getString( "host" ),
                        ChatColor.GREEN.toString(), "(", String.valueOf( rs.getInt( "count" ) ), ")",
                        ChatColor.YELLOW.toString(), "[", toInetAddress( rs.getLong( "ip" ) ), "]",
                        ChatColor.WHITE.toString(), sdf.format( rs.getTimestamp( "lastdate" ) )
                    ), ( p == null )
                );
            }

            if ( DataNum == 0 ) Utility.Prt( p, Utility.StringBuild( ChatColor.RED.toString(), "No data for [", word, "]" ), ( p == null ) );

        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "[LoginControl] Search Error" );
        }
    }
    
    public String getUnknownHost( String IP, boolean CheckFlag, boolean Debug ) throws UnknownHostException {
        Bukkit.getServer().getConsoleSender().sendMessage( Utility.StringBuild( ChatColor.RED.toString(), "[LC] Unknown New Record : ", ChatColor.AQUA.toString(), IP ) );
        String HostName = "Unknown(IP)";
        if ( CheckFlag ) {
            Inet4Address inet = ( Inet4Address ) Inet4Address.getByName( IP );
            if ( !inet.getHostName().equals( IP ) ) { HostName = inet.getHostName(); }
        }

        //  クマイス鯖特有の特別処理
        if ( Kumaisu ) {
            if ( Debug ) Bukkit.getServer().getConsoleSender().sendMessage( Utility.StringBuild( ChatColor.GREEN.toString(), "[LC] Original Hostname = ", ChatColor.AQUA.toString(), HostName ) );
            if ( HostName.contains( "ec2" ) ) {
                String[] NameItem = HostName.split( "\\.", 0 );
                StringBuilder buf = new StringBuilder();
                for( int i = 1; i < NameItem.length; i++ ){
                    if ( Debug ) Bukkit.getServer().getConsoleSender().sendMessage( i + " : " + NameItem[i] );
                    if( i != 1 ) buf.append( "." );
                    buf.append( NameItem[i] );
                }
                HostName = buf.toString();
            }
        }

        if ( HostName.length()>60 ) { HostName = String.format( "%-60s", HostName ); }
        
        Bukkit.getServer().getConsoleSender().sendMessage( Utility.StringBuild( ChatColor.GREEN.toString(), "[LC] Change Hostname = ", ChatColor.AQUA.toString(), HostName ) );
        AddHostToSQL( IP, HostName );
        return HostName;
    }

    public String ping( String IP ) throws UnknownHostException {
        Inet4Address inet = ( Inet4Address ) Inet4Address.getByName( IP );
        return inet.getHostName();
    }

    public boolean WriteFileUnknown( String IP, String DataFolder ) {
        File UKfile = new File( DataFolder, "UnknownIP.yml" );
        FileConfiguration UKData = YamlConfiguration.loadConfiguration( UKfile );

        SimpleDateFormat cdf = new SimpleDateFormat("yyyyMMddHHmmss");
        
        UKData.set( cdf.format( new Date() ),Utility.StringBuild( IP, "[", GetHost( IP ), "]" ) );
        try {
            UKData.save( UKfile );
        }
        catch ( IOException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( Utility.StringBuild( ChatColor.RED.toString(), "Could not save UnknownIP File." ) );
            return false;
        }

        return true;
    }
    
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
            Bukkit.getServer().getConsoleSender().sendMessage( "SQLException:" + e.getMessage() );
        }
        return "";
    }

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

    public boolean chgUnknownHost( String IP, String Hostname ) {

        if ( Hostname.length()>60 ) { Hostname = String.format( "%-60s", Hostname ); }

        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "';";
            ResultSet rs = stmt.executeQuery( sql );
            
            if ( rs.next() ) {
                String chg_sql = "UPDATE hosts SET host = '" + Hostname + "' WHERE INET_NTOA( ip ) = '" + IP + "';";
                PreparedStatement preparedStatement = connection.prepareStatement(chg_sql);
                preparedStatement.executeUpdate();
                return true;
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "could not get " + IP );
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "[LoginControl] Change Database Error [" + IP + "][" + Hostname + "]" );
            //  エラー詳細ログの表示
            Bukkit.getServer().getConsoleSender().sendMessage( e.getMessage() );
        }
        return false;
    }

    public void infoUnknownHost( Player p, String IP ) {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            if ( rs.next() ) {
                Utility.Prt( p, ChatColor.YELLOW + "Check Unknown IP Information.......", ( p == null ) );
                Utility.Prt( p, ChatColor.GREEN + "IP Address  : " + ChatColor.WHITE + toInetAddress( rs.getLong( "ip" ) ), ( p == null ) );
                Utility.Prt( p, ChatColor.GREEN + "Host Name   : " + ChatColor.WHITE + rs.getString( "host" ), ( p == null ) );
                Utility.Prt( p, ChatColor.GREEN + "AccessCount : " + ChatColor.WHITE + String.valueOf( rs.getInt( "count" ) ), ( p == null ) );
                Utility.Prt( p, ChatColor.GREEN + "First Date  : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "newdate" ) ), ( p == null ) );
                Utility.Prt( p, ChatColor.GREEN + "Last Date   : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "lastdate" ) ), ( p == null ) );
            } else {
                Utility.Prt( p, ChatColor.RED + "No data for [" + IP + "]", ( p == null ) );
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "[LoginControl] Error Information" );
            //  エラー詳細ログの表示
            Bukkit.getServer().getConsoleSender().sendMessage( e.getMessage() );
        }
    }

    public void PingTop( Player p, int Lines ) {
        Utility.Prt( p, ChatColor.GREEN + "== Ping Count Top " + Lines + " ==", ( p == null ) );

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
                    Utility.Prt( p, 
                        Utility.StringBuild(
                            ChatColor.AQUA.toString(), String.format( "%5d", rs.getInt("count" ) ), ": ",
                            ChatColor.YELLOW.toString(), String.format( "%-15s", toInetAddress( rs.getLong( "ip" ) ) ),
                            ChatColor.WHITE.toString(), String.format( "%-40s", rs.getString( "host" ) ),
                            ChatColor.WHITE.toString(), sdf.format( rs.getTimestamp( "lastdate" ) )
                        ), ( p == null )
                    );
                    chk_name = GetName;
                }
            }

            Utility.Prt( p, ChatColor.GREEN + "================", ( p == null ) );
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "Error Pingtop Listing ..." );
            //  エラー詳細ログの表示
            Bukkit.getServer().getConsoleSender().sendMessage( e.getMessage() );
        }
    }

    /*
    @SuppressWarnings("CallToPrintStackTrace")
    public void DataConv( CommandSender sender ) {
        sender.sendMessage( "== Data Convert List ==" );
        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM players ORDER BY date ASC;";
            ResultSet rs = stmt.executeQuery(sql);
            while( rs.next() ) {
                //  String sql = "CREATE TABLE IF NOT EXISTS players(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip varchar(22), status varchar(10), index(id))";
                int GetID = rs.getInt( "id" );
                Date GetData = rs.getTimestamp( "date" );
                String GetName = rs.getString( "name" );
                String GetUUID = rs.getString( "uuid" );
                String GetIP = rs.getString( "ip" );
                String GetSts = rs.getString( "status" );
                String message = String.format("%6d", GetID) + ": " + sdf.format(GetData) + " " + String.format("%-10s", GetName) + ChatColor.RED + "[" + String.format("%-15s", GetIP ) + "]";
                //  sql = "CREATE TABLE IF NOT EXISTS list(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip INTEGER UNSIGNED, status byte, index(id))";
                String add_sql = "INSERT INTO list ( date, name, uuid, ip, status ) VALUES ( ?, ?, ?, INET_ATON( ? ), ? );";
                PreparedStatement preparedStatement = connection.prepareStatement( add_sql );
                preparedStatement.setString( 1, sdf.format( GetData ) );
                preparedStatement.setString( 2, GetName );
                preparedStatement.setString( 3, GetUUID );
                preparedStatement.setString( 4, GetIP );
                if ( GetSts.equals( "Logged in" ) ) {
                    preparedStatement.setInt( 5, 1 );
                } else {
                    preparedStatement.setInt( 5, 0 );
                }
                preparedStatement.executeUpdate();
                sender.sendMessage( message );
            }
                    
            sender.sendMessage( "================" );
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }
    */
}
