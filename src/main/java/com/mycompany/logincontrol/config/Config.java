/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.config;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.tool.Tools;

/**
 * 設定ファイルを読み込む
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

    private boolean JumpStats;
    private boolean OpJumpStats;
    private boolean CheckIPAddress;
    private List<String> present;
    private int fx;
    private int fy;
    private int fz;
    private int fpitch;
    private int fyaw;
    private String fworld;

    private List<String> IgnoreReportName;
    private List<String> IgnoreReportIP;

    public static Utility.consoleMode DebugFlag = Utility.consoleMode.none;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        Tools.Prt( "Config Loading now..." );
        load();
    }

    /*
     * 設定をロードします
     */
    public void load() {
        // 設定ファイルを保存
        plugin.saveDefaultConfig();
        if (config != null) { // configが非null == リロードで呼び出された
            Tools.Prt( "Config Reloading now..." );
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
        IgnoreReportName = config.getStringList( "Ignore-Names" );
        IgnoreReportIP = config.getStringList( "Ignore-IP" );
        CheckIPAddress = config.getBoolean( "CheckIP" );

        try {
            DebugFlag = Utility.consoleMode.valueOf( config.getString( "Debug" ) );
        } catch( IllegalArgumentException e ) {
            Tools.Prt( ChatColor.RED + "Config Debugモードの指定値が不正なので、normal設定にしました" );
            DebugFlag = Utility.consoleMode.normal;
        }
    }

    public void Status( Player p ) {
        Utility.consoleMode consolePrintFlag = ( ( p == null ) ? Utility.consoleMode.none:Utility.consoleMode.stop );
        Tools.Prt( p, ChatColor.GREEN + "=== LoginContrl Status ===", consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Degub Mode : " + ChatColor.YELLOW + DebugFlag.toString(), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Mysql : " + ChatColor.YELLOW + host + ":" + port, consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "DB Name : " + ChatColor.YELLOW + database, consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "FirstJump : " + ChatColor.YELLOW + ( ( JumpStats ) ? "True":"None" ), consolePrintFlag );
        if ( JumpStats ) {
            Tools.Prt( p, ChatColor.WHITE + "  world:" + ChatColor.YELLOW + fworld, consolePrintFlag );
            Tools.Prt( p, ChatColor.WHITE + "  x:" + ChatColor.YELLOW + String.valueOf( fx ), consolePrintFlag );
            Tools.Prt( p, ChatColor.WHITE + "  y:" + ChatColor.YELLOW + String.valueOf( fy ), consolePrintFlag );
            Tools.Prt( p, ChatColor.WHITE + "  z:" + ChatColor.YELLOW + String.valueOf( fz ), consolePrintFlag );
            Tools.Prt( p, ChatColor.WHITE + "  p:" + ChatColor.YELLOW + String.valueOf( fpitch ), consolePrintFlag );
            Tools.Prt( p, ChatColor.WHITE + "  y:" + ChatColor.YELLOW + String.valueOf( fyaw ), consolePrintFlag );
        }
        Tools.Prt( p, ChatColor.WHITE + "Present Items", consolePrintFlag );
        present.stream().forEach( pr -> {
            String[] itemdata = pr.split( ",", 0 );
            Tools.Prt( p, ChatColor.YELLOW + " - " + itemdata[0] + "(" + itemdata[1] + ")", consolePrintFlag );
        } );

        Tools.Prt( p, ChatColor.WHITE + "Ignore Names", consolePrintFlag );
        IgnoreReportName.stream().forEach( IRN -> { Tools.Prt( p, ChatColor.YELLOW + " - " + IRN, consolePrintFlag ); } );

        Tools.Prt( p, ChatColor.WHITE + "Ignore IPs", consolePrintFlag );
        IgnoreReportIP.stream().forEach( IRI -> { Tools.Prt( p, ChatColor.YELLOW + " - " + IRI, consolePrintFlag ); } );

        Tools.Prt( p, ChatColor.WHITE + "Unknown IP Check : " + ChatColor.YELLOW + ( CheckIPAddress ? "True":"False" ), consolePrintFlag );
        Tools.Prt( p, ChatColor.GREEN + "==========================", consolePrintFlag );
    }

    /**
     * 一時的にDebugModeを設定しなおす
     * ただし、Config.ymlには反映しない
     *
     * @param key 
     */
    public void setDebug( String key ) {
        try {
            DebugFlag = Utility.consoleMode.valueOf( key );
        } catch( IllegalArgumentException e ) {
            DebugFlag = Utility.consoleMode.none;
        }
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDatabase() {
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
        return config.getString( IP, null );
    }

    public boolean OpJump( boolean isOP ) {
        return OpJumpStats && isOP;
    }

    public List<String> getPresent() {
        return present;
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

    public boolean Announce() {
        return config.getBoolean( "ANNOUNCE.Enabled" );
    }

    public String AnnounceMessage() {
        return config.getString( "ANNOUNCE.Message" );
    }

    public boolean NewJoin() {
        return config.getBoolean( "New_Join_Message.Enabled" );
    }

    public String NewJoinMessage( String Lang ) {
        return config.getString( "New_Join_Message." + Lang, config.getString( "New_Join_Message.Message" ) );
    }

    public boolean ReturnJoin() {
        return config.getBoolean( "Returning_Join_Message.Enabled" );
    }

    public String ReturnJoinMessage( String Lang ) {
        return config.getString( "Returning_Join_Message." + Lang, config.getString( "Returning_Join_Message.Message" ) );
    }

    public boolean PlayerQuit() {
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

    public boolean getKumaisu() {
        return config.getBoolean( "Kumaisu" );
    }

}
