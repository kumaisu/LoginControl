/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
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
            plugin.reloadConfig();
        }
        config = plugin.getConfig();

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
    
}
