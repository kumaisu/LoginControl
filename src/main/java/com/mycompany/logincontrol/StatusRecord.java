/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author sugichan
 */
public class StatusRecord {

    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    private Connection connection;
    private final String host, database, port, username, password;
    
    public StatusRecord( String CFhost, String CFdb, String CFport, String CFuser, String CFpass ) {
        host = CFhost;
        database = CFdb;
        port = CFport;
        username = CFuser;
        password = CFpass;
    }
    
    public void MsgPrt( Player player, String msg ) {
        if ( player == null ) {
            Bukkit.getServer().getConsoleSender().sendMessage( msg );
        } else {
            player.sendMessage( msg );
        }
    }

    public static long ipToInt( Inet4Address ipAddr )
    {
        long compacted = 0;
        byte[] bytes = ipAddr.getAddress();
        for ( int i=0 ; i<bytes.length ; i++ ) {
            compacted += ( bytes[i] * Math.pow( 256, 4-i-1 ) );
        }
        return compacted;
    }
    
    private static String toInetAddress(long ipAddress)
    {
        long ip = ( ipAddress < 0 ) ? (long)Math.pow(2,32)+ipAddress : ipAddress;
        Inet4Address inetAddress = null;
        String addr =  String.valueOf((ip >> 24)+"."+((ip >> 16) & 255)+"."+((ip >> 8) & 255)+"."+(ip & 255));
        return addr;
    }

    public void LineMsg( Player p, int id, Date date, String name, long ip , int status ) {

        String message = String.format( "%6d", id ) + ": " + sdf.format( date ) + " " + String.format( "%-20s", name );
        if ( ( p == null ) || p.hasPermission( "LoginCtl.view" ) || p.isOp() ) {
            message += ChatColor.YELLOW + "[" + String.format( "%-15s", toInetAddress( ip ) ) + "]" + ChatColor.RED + "(" + ( status==0 ? "Attempted":"Logged in" ) + ")";
        }
        MsgPrt( p, message );
    }
            
    @SuppressWarnings("CallToPrintStackTrace")
    public void LogPrint( Player player, int lines, boolean FullFlag ) {
        
        MsgPrt( player, "== Login List ==" );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            int i = 0;
            String chk_name = "";
            
            while( rs.next() && ( i<lines ) ) {
                String GetName = rs.getString( "name" );
                
                if ( rs.getInt( "status" ) != 0 || player == null || player.hasPermission( "LoginCtl.view" ) || player.isOp() ) {
                    if ( ( !chk_name.equals( GetName ) ) || ( FullFlag ) ) {
                        i++;
                        LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getLong( "ip" ), rs.getInt( "status" ) );
                        chk_name = GetName;
                    }
                }
            }

            MsgPrt( player, "================" );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void DateLogPrint( Player player, String ChkDate, boolean FullFlag ) {
        MsgPrt( player, "== [" + ChkDate + "] Login List ==" );

        try {        
            String chk_name = "";

            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE date BETWEEN '" + ChkDate + " 00:00:00' AND '" + ChkDate + " 23:59:59' ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );

            while( rs.next() ) {
                if ( !chk_name.equals( rs.getString( "name" ) ) || ( FullFlag ) ) {
                    LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getLong( "ip" ), rs.getInt( "status" ) );
                    chk_name = rs.getString( "name" );
                }
            }
                    
            MsgPrt( player, "================" );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void NameLogPrint( Player player, String ChkName, boolean FullFlag ) {
        MsgPrt( player, "== [" + ChkName + "] Login List ==" );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE name = '" + ChkName + "' ORDER BY date DESC;";
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
                    
            MsgPrt( player, "================" );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String GetPlayerName( String ip ) {

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + ip + "' ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            return ( rs.next() ? rs.getString("name"):"Unknown" );
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        
        return ip;
    }

    @SuppressWarnings( "CallToPrintStackTrace" )
    public void CheckIP( Player player ) throws UnknownHostException {
        Boolean PrtF = false;
        List<String> PrtData;
        PrtData = new ArrayList<>();
        List<String> NameData;
        NameData = new ArrayList<>();
        
        PrtData.add( ChatColor.RED + "=== Check IP Address ===" + ChatColor.YELLOW + "[" + player.getAddress().getHostString() + "]" );
        
        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM list WHERE INET_NTOA(ip) = '" + player.getAddress().getHostString() + "' ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );

            int i = 0;
            while( rs.next() ) {
                String GetName = rs.getString( "name" );

                if ( ( i == 0 ) && ( GetName.equals( player.getName() ) ) ) {
                    // pass                    
                    i++;
                } else {
                    if ( !NameData.contains( GetName ) ) {
                        i++;
                        PrtF = true; // ( i>1 );
                        NameData.add( GetName );
                        PrtData.add( ChatColor.WHITE + String.format( "%6d", rs.getInt( "id" ) ) + ": " + ChatColor.GREEN + sdf.format( rs.getTimestamp( "date" ) ) + " " + String.format( "%-20s", GetName ) );
                    }
                }
            }
                    
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        
        PrtData.add( ChatColor.RED + "=== end ===" );
        
        if ( PrtF ) {
            PrtData.stream().forEach( PD -> {
                String msg = PD;
                Bukkit.getServer().getConsoleSender().sendMessage( msg );
                Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
            } );
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void ChangeStatus( Date date, int status ) {
        try {
            openConnection();

            String sql = "UPDATE list SET status = " + String.valueOf( status ) + " WHERE date = '" + sdf.format( date ) + "';";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
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
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, sdf.format( date ) );
            preparedStatement.setString(2, name );
            preparedStatement.setString(3, UUID );
            preparedStatement.setString(4, IP );
            preparedStatement.setInt(5, Status );

            preparedStatement.executeUpdate();
            
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }
    
    private void openConnection() throws SQLException, ClassNotFoundException {

        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
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

            //  mysql> create table IF NOT EXISTS unknowns (ip varchar(22), host varchar(60), count int, lastdate DATETIME );
            //  Unknowns テーブルの作成
            //  存在すれば、無視される
            sql = "CREATE TABLE IF NOT EXISTS hosts (ip INTEGER UNSIGNED, host varchar(60), count int, lastdate DATETIME )";
            preparedStatement = connection.prepareStatement( sql );
            preparedStatement.executeUpdate();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String getUnknownHost( String IP ) {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            if ( rs.next() ) return rs.getString( "host" );
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String setUnknownHost( String IP, boolean CheckFlag ) throws UnknownHostException {
        Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "Ping [Debug] Unknown New Record" );
        String HostName = "Unknown(IP)";
        if ( CheckFlag ) {
            Inet4Address inet = ( Inet4Address ) Inet4Address.getByName( IP );
            if ( !inet.getHostName().equals( IP ) ) HostName = inet.getHostName();
        }

        //  Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "Write Unknown IP : " + IP );
        //  Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "Get Unknown Host : " + inet.getHostName() );
        //  Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "Get Unknown Cano : " + inet.getCanonicalHostName() );
        //  Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "Get Unknown Addr : " + inet.getHostAddress() );

        //  テーブルへ追加
        //  sql = "CREATE TABLE IF NOT EXISTS unknowns (ip varchar(22), host varchar(60), count int, lastdate DATETIME )";

        if ( HostName.length()>60 ) { HostName = String.format( "%-60s", HostName ); }

        try {
            openConnection();

            String sql = "INSERT INTO hosts ( ip, host, count, lastdate ) VALUES ( INET_ATON( ? ), ?, ?, ? );";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString( 1, IP );
            preparedStatement.setString( 2, HostName );
            preparedStatement.setInt( 3, 1 );
            preparedStatement.setString( 4, sdf.format( new Date() ) );

            preparedStatement.executeUpdate();
            
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        return HostName;
    }

    public String ping( String IP ) throws UnknownHostException {
        Inet4Address inet = ( Inet4Address ) Inet4Address.getByName( IP );
        return inet.getHostName();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public int countUnknownHost( String IP ) throws UnknownHostException {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            //  sql = "CREATE TABLE IF NOT EXISTS unknowns (ip varchar(22) not null primary, host varchar(40), count int, lastdate DATETIME )";
            if ( rs.next() ) {
                int count = rs.getInt( "count" ) + 1;
                    
                String chg_sql = "UPDATE hosts SET count = " + count +
                        ", lastdate = '" + sdf.format( new Date() ) +
                        "' WHERE INET_NTOA( ip ) = '" + IP + "';";
                PreparedStatement preparedStatement = connection.prepareStatement(chg_sql);
                preparedStatement.executeUpdate();
                return count;
            }

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        return 0;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public boolean chgUnknownHost( String IP, String Hostname ) {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            //  sql = "CREATE TABLE IF NOT EXISTS unknowns (ip varchar(22) not null primary, host varchar(40), count int, lastdate DATETIME )";
            if ( rs.next() ) {
                String chg_sql = "UPDATE hosts SET host = '" + Hostname + "' WHERE INET_NTOA( ip ) = '" + IP + "';";
                PreparedStatement preparedStatement = connection.prepareStatement(chg_sql);
                preparedStatement.executeUpdate();
                return true;
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void infoUnknownHost( Player p, String IP ) {
        try {
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts WHERE INET_NTOA(ip) = '" + IP + "' ORDER BY ip DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            //  sql = "CREATE TABLE IF NOT EXISTS unknowns (ip varchar(22) not null primary, host varchar(40), count int, lastdate DATETIME )";
            if ( rs.next() ) {
                MsgPrt( p, ChatColor.YELLOW + "Check Unknown IP Information......." );
                MsgPrt( p, ChatColor.GREEN + "IP Address  : " + ChatColor.WHITE + toInetAddress( rs.getLong( "ip" ) ) );
                MsgPrt( p, ChatColor.GREEN + "Host Name   : " + ChatColor.WHITE + rs.getString( "host" ) );
                MsgPrt( p, ChatColor.GREEN + "AccessCount : " + ChatColor.WHITE + String.valueOf( rs.getInt( "count" ) ) );
                MsgPrt( p, ChatColor.GREEN + "Last Date   : " + ChatColor.WHITE + sdf.format( rs.getTimestamp( "lastdate" ) ) );
            }
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }

    public void PingTop( Player p ) {
        MsgPrt( p, ChatColor.GREEN + "== Ping Count Top10 ==" );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM hosts ORDER BY count DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            int i = 0;
            String chk_name = "";
            
            while( rs.next() && ( i<10 ) ) {
                String GetName = rs.getString( "host" );
                
                if ( !chk_name.equals( GetName ) ) {
                    i++;
                    MsgPrt( p, 
                            ChatColor.AQUA + String.format( "%4d", rs.getInt("count" ) ) + ": " +
                            ChatColor.YELLOW + String.format( "%-15s", toInetAddress( rs.getLong( "ip" ) ) ) +
                            ChatColor.WHITE + String.format( "%-40s", rs.getString( "host" ) ) +
                            ChatColor.WHITE + sdf.format( rs.getTimestamp( "lastdate" ) )
                    );
                    chk_name = GetName;
                }
            }

            MsgPrt( p, ChatColor.GREEN + "================" );
        } catch ( ClassNotFoundException | SQLException e ) {
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.RED + "Error Pingtop Listing ..." );
        }
    }

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
}
