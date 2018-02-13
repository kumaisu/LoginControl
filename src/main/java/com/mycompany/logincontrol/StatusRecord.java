/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author sugichan
 */
public class StatusRecord {

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

    public void LineMsg( Player p, int id, Date date, String name, String ip , String status ) {
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

        String message = String.format( "%6d", id ) + ": " + sdf.format( date ) + " " + String.format( "%-20s", name );
        if ( ( p == null ) || p.hasPermission( "KumaisuPlugin.view" ) || p.isOp() ) {
            message += ChatColor.YELLOW + "[" + String.format( "%-15s", ip ) + "]" + ChatColor.RED + "(" + status + ")";
        }
        MsgPrt( p, message );
    }
            
    @SuppressWarnings("CallToPrintStackTrace")
    public void LogPrint( Player player, int lines, boolean FullFlag ) {
        
        MsgPrt( player, "== Login List ==" );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM players ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            int i = 0;
            String chk_name = "";
            
            while( rs.next() && ( i<lines ) ) {
                String GetName = rs.getString( "name" );
                
                if ( ( !chk_name.equals( GetName ) ) || ( FullFlag ) ) {
                    i++;
                    LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getString( "ip" ), rs.getString( "status" ) );
                    chk_name = GetName;
                }
            }

            MsgPrt( player, "================" );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void DateLogPrint( Player player, String ChkDate, boolean FullFlag ) {
        MsgPrt( player, "== Date Login List ==" );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM players ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            SimpleDateFormat cdf = new SimpleDateFormat( "yyyyMMdd" );
            String chk_name = "";
            
            while( rs.next() ) {
                if ( ChkDate.equals( cdf.format( rs.getTimestamp( "date" ) ) ) && ( !chk_name.equals( rs.getString( "name" ) ) || ( FullFlag ) ) ) {
                    LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getString( "ip" ), rs.getString( "status" ) );
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
            String sql = "SELECT * FROM players ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            SimpleDateFormat cdf = new SimpleDateFormat( "yyyyMMdd" );
            String ChkDate = cdf.format( new Date() );
            int i = 0;
            
            while( rs.next() && ( i<30 ) ) {
                if ( ChkName.equals( rs.getString( "name" ) ) && ( !ChkDate.equals( cdf.format( rs.getTimestamp( "date" ) ) ) || FullFlag ) ) {
                    i++;
                    LineMsg( player, rs.getInt( "id" ), rs.getTimestamp( "date" ), rs.getString( "name" ), rs.getString( "ip" ), rs.getString( "status" ) );
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
            String sql = "SELECT * FROM players ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            while( rs.next() ) {
                if ( rs.getString("ip").equals( ip ) ) {
                    return rs.getString("name");
                }
            }
            return "Unknown";

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        
        return ip;
    }

    @SuppressWarnings( "CallToPrintStackTrace" )
    public void CheckIP( Player player ) {
        Boolean PrtF = false;
        List<String> PrtData;
        PrtData = new ArrayList<>();
        List<String> NameData;
        NameData = new ArrayList<>();
        
        PrtData.add( ChatColor.AQUA + "=== Check IP Address ===" + ChatColor.YELLOW + "[" + player.getAddress().getHostString() + "]" );
        
        try {        
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM players WHERE ip  = '" + player.getAddress().getHostString() + "' ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery( sql );
            
            while( rs.next() ) {
                String GetName = rs.getString( "name" );

                if ( !NameData.contains( GetName ) ) {
                    PrtF = true;
                    NameData.add( GetName );
                    PrtData.add( ChatColor.WHITE + String.format( "%6d", rs.getInt( "id" ) ) + ": " + ChatColor.GREEN + sdf.format( rs.getTimestamp( "date" ) ) + " " + String.format( "%-20s", GetName ) );
                }
            }
                    
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
        
        PrtData.add( ChatColor.AQUA + "=== end ===" );
        
        if ( PrtF ) {
            for(Iterator it = PrtData.iterator(); it.hasNext();) {
                String msg = (String)it.next();
                Bukkit.getServer().getConsoleSender().sendMessage( msg );
                Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
            }
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void ChangeStatus( Date date, String status ) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            openConnection();

            String sql = "UPDATE players SET status = '" + status + "' WHERE date = '" + sdf.format( date ) + "';";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            
        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    public void PreSavePlayer( Date date, String name, String UUID, String IP, String Status ) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //  mysql> create table players(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip varchar(22), status varchar(10), index(id));
        // Played
        // Banned
        // NoRep
        // Kick

        /*
        getLogger().log( Level.INFO, "Date   : {0}", sdf.format( date ) );
        getLogger().log( Level.INFO, "name   : {0}", name );
        getLogger().log( Level.INFO, "UUID   : {0}", UUID );
        getLogger().log( Level.INFO, "IP     : {0}", IP );
        getLogger().log( Level.INFO, "Status : {0}", Status );
        */
        
        try {
            openConnection();

            String sql = "INSERT INTO players (date, name, uuid, ip, status) VALUES (?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, sdf.format( date ) );
            preparedStatement.setString(2, name );
            preparedStatement.setString(3, UUID );
            preparedStatement.setString(4, IP );
            preparedStatement.setString(5, Status );

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
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

            //  mysql> create table players(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip varchar(22), index(id));
            //  テーブルの作成
            //  存在すれば、無視される
            String sql = "CREATE TABLE IF NOT EXISTS players(id int auto_increment, date DATETIME,name varchar(20), uuid varchar(36), ip varchar(22), status varchar(10), index(id))";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
        }
    }

    /*
    @SuppressWarnings("CallToPrintStackTrace")
    public void DataConv( CommandSender sender ) {
        sender.sendMessage( "== Data Convert List ==" );

        try {        
            openConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM players ORDER BY date DESC;";
            ResultSet rs = stmt.executeQuery(sql);
            
            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
            SimpleDateFormat cdf = new SimpleDateFormat( "yyyyMMdd" );
            
            String chk_name = "";
            
            while( rs.next() ) {
                int GetID = rs.getInt("id");
                Date GetData = rs.getTimestamp("date");
                String GetName = rs.getString("name");
                String GetIP = rs.getString("ip");

                String message = String.format("%6d", GetID) + ": " + sdf.format(GetData) + " " + String.format("%-10s", GetName) + ChatColor.RED + "[" + String.format("%-15s", GetIP ) + "]";

                if ( rs.getString( "status" ) == null ) {
                    String chg_sql = "UPDATE players SET status = 'Logeed In' WHERE date = '" + sdf.format( GetData ) + "';";
                    PreparedStatement preparedStatement = connection.prepareStatement(chg_sql);
                    preparedStatement.executeUpdate();
                } else {
                    String GetStatus = rs.getString( "status" );
                }
                
                if ( GetIP.indexOf( ":" )>0 ) {
                    String SetIP = GetIP.substring( 1,GetIP.indexOf( ":" ) );
                    String chg_sql = "UPDATE players SET ip = '" + SetIP + "' WHERE date = '" + sdf.format( GetData ) + "';";
                    PreparedStatement preparedStatement = connection.prepareStatement(chg_sql);
                    preparedStatement.executeUpdate();
                    message += ChatColor.AQUA + " => [" + String.format("%-15s", SetIP ) + "]";
                }

                sender.sendMessage( message );
            }
                    
            sender.sendMessage( "================" );

        } catch ( ClassNotFoundException | SQLException e ) {
            e.printStackTrace();
        }
    }
    */
}
