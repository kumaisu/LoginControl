/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author sugichan
 */
public class Config {

    private final Plugin plugin;
    private FileConfiguration config = null;
    
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    private String fworld;
    private boolean JumpStats;
    private boolean OpJumpStats;
    private boolean CheckIPAddress;
    private List<String> present;
    private int fx;
    private int fy;
    private int fz;
    private int fpitch;
    private int fyaw;
    private String MotD1stLine;
    private String MotD2ndLineUnknown;
    private String MotD2ndLinePlayer;
    private List<String> IgnoreReportName;
    private List<String> IgnoreReportIP;
    
    public Config(Plugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info( "Config Loading now..." );
        load();
    }
    
    /*
     * 設定をロードします
     */
    public void load() {
        // 設定ファイルを保存
        plugin.saveDefaultConfig();
        if (config != null) { // configが非null == リロードで呼び出された
            plugin.getLogger().info( "Config Reloading now..." );
            plugin.reloadConfig();
        }
        config = plugin.getConfig();

        present = new ArrayList<>();
        IgnoreReportName = new ArrayList<>();
        IgnoreReportIP = new ArrayList<>();

        host = config.getString( "mysql.host" );
        port = config.getString( "mysql.port" );
        database = config.getString( "mysql.database" );
        username = config.getString( "mysql.username" );
        password = config.getString( "mysql.password" );
        JumpStats = config.getBoolean( "FirstPoint" );
        OpJumpStats = config.getBoolean( "OpJump" );
        fworld = config.getString( "world" );
        fx = config.getInt( "x" );
        fy = config.getInt( "y" );
        fz = config.getInt( "z" );
        fpitch = config.getInt( "pitch" );
        fyaw = config.getInt( "yaw" );
        present = config.getStringList( "Present" );
        MotD1stLine = config.getString( "MotD1st" );
        MotD2ndLineUnknown = config.getString( "MotD2nd-Unknown" );
        MotD2ndLinePlayer = config.getString( "MotD2nd-Player" );
        IgnoreReportName = config.getStringList( "Ignore-Names" );
        IgnoreReportIP = config.getStringList( "Ignore-IP" );
        CheckIPAddress = config.getBoolean( "CheckIP" );
    }
    
    public void Prt( Player p, String s ) {
        if ( p == null ) {
            Bukkit.getServer().getConsoleSender().sendMessage( s );
        } else {
            p.sendMessage( s );
        }
    }
    
    public void Status( Player p ) {
        Prt( p, "=== LoginContrl Status ===" );
        Prt( p, "Mysql : " + host + ":" + port );
        Prt( p, "DB Name : " + database );
        Prt( p, "FirstJump : " + ( ( JumpStats ) ? "True":"None" ) );
        if ( JumpStats ) {
            Prt( p, "  world:" + fworld );
            Prt( p, "  x:" + String.valueOf( fx ) );
            Prt( p, "  y:" + String.valueOf( fy ) );
            Prt( p, "  z:" + String.valueOf( fz ) );
            Prt( p, "  p:" + String.valueOf( fpitch ) );
            Prt( p, "  y:" + String.valueOf( fyaw ) );
        }
        Prt( p, "Present Items" );
        present.stream().forEach( pr -> {
            String[] itemdata = pr.split( ",", 0 );
            Prt( p, " - " + itemdata[0] + "(" + itemdata[1] + ")" );
        } );

        Prt( p, "Ignore Names" );
        IgnoreReportName.stream().forEach( IRN -> { Prt( p, " - " + IRN ); } );

        Prt( p, "Ignore IPs" );
        IgnoreReportIP.stream().forEach( IRI -> { Prt( p, " - " + IRI ); } );
        
        Prt( p, "Unknown IP Check : " + ( CheckIPAddress ? "True":"False" ) );
        Prt( p, "MotD 1 Line : " + MotD1stLine );
        Prt( p, "MotD 2 Line(Unknown) : " + MotD2ndLineUnknown );
        Prt( p, "MotD 2 Line(Player ) : " + MotD2ndLinePlayer );
        Prt( p, "==========================" );
        
    }
    
    public String getHost() {
        return host;
    }
    
    public String getPort() {
        return port;
    }
    
    public String getDB() {
        return database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }

    public Boolean getJump() {
        return JumpStats;
    }

    public String getWorld() {
        return fworld;
    }
    
    public int getX() {
        return fx;
    }
    
    public int getY() {
        return fy;
    }
    
    public int getZ() {
        return fz;
    }
    
    public int getPitch() {
        return fpitch;
    }
    
    public int getYaw() {
        return fyaw;
    }

    public String KnownServers( String IP ) {
        return config.getString( IP );
    }
    
    public boolean OpJump( boolean isOP ) {
        return OpJumpStats && isOP;
    }
    
    public List<String> getPresent() {
        return present;
    }

    public String get1stLine() {
        return MotD1stLine;
    }
    
    public String get2ndLine( boolean flag ) {
        return ( flag ? MotD2ndLinePlayer:MotD2ndLineUnknown );
    }

    public List<String> getIgnoreName() {
        return IgnoreReportName;
    }

    public List<String> getIgnoreIP() {
        return IgnoreReportIP;
    }

    public String LogFull() {
        return config.getString( "Message.LogFull" );
    }

    public String Reload() {
        return config.getString( "Message.Reload" );
    }

    public String ArgsErr() {
        return config.getString( "Message.ArgsErr" );
    }

    public String OptError() {
        return config.getString( "Message.OptError" );
    }

    public String WriteUnknown( String IPS ) throws UnknownHostException {
        File UKfile = new File( plugin.getDataFolder(), "UnknownIP.yml" );
        FileConfiguration UKData = YamlConfiguration.loadConfiguration( UKfile );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        StatusRecord statusRecord = new StatusRecord( host, database, port, username, password );

        String HostName = statusRecord.getUnknownHost( IPS );
        ChatColor NameColor;
        
        if ( HostName.equals( "Unknown" ) ) {
            HostName = statusRecord.setUnknownHost( IPS, CheckIPAddress );
            NameColor = ChatColor.RED;
            UKData.set( sdf.format( new Date() ),IPS + "[" + HostName + "]" );

            try {
                UKData.save( UKfile );
            }
            catch (IOException e) {
                plugin.getServer().getLogger().log( Level.SEVERE, "{0}Could not save UnknownIP File.", ChatColor.RED );
                return "Unknown";
            }

        } else {
            int count = statusRecord.countUnknownHost( IPS, false );
            HostName += "(" + String.valueOf( count ) + ")";
            NameColor = ChatColor.LIGHT_PURPLE;
        }

        return NameColor + HostName;
    }
    
    public boolean Announce() {
        return config.getBoolean( "ANNOUNCE.Enabled" );
    }
    
    public String AnnounceMessage() {
        return config.getString( "ANNOUNCE.Message" );
    }

    public boolean NewJoin() {
        return config.getBoolean( "New_Join_Message.Enabled" );
    }
    
    public String NewJoinMessage() {
        return config.getString( "New_Join_Message.Message" );
    }
    
    public boolean ReturnJoin() {
        return config.getBoolean( "Returning_Join_Message.Enabled" );
    }

    public String ReturnJoinMessage() {
        return config.getString( "Returning_Join_Message.Message" );
    }
    
    public boolean PlayerQuti() {
        return config.getBoolean( "Quit_Message.Enabled" );
    }

    public String PlayerQuitMessage() {
        return config.getString( "Quit_Message.Message" );
    }
    
    public boolean PlayerKick() {
        return config.getBoolean( "Kick_Message.Enabled" );
    }
    
    public String KickMessage() {
        return config.getString( "Kick_Message.Message" );
    }

    public boolean DeathMessageFlag() {
        return config.getBoolean( "Death_Message.Enabled" );
    }
    
    public String DeathMessage( String mob ) {
        String msg = config.getString( "Death_Message.Messages." + mob );
        if ( msg == null ) msg = config.getString( "Death_Message.Messages.DEFAULT" );
        return msg;
    }
    
    public boolean getCheckIP() {
        return CheckIPAddress;
    }
    
    public void setCheckIP( boolean flag ) {
        CheckIPAddress = flag;
    }
}
