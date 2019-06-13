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
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Tools.consoleMode;

/**
 * 設定ファイルを読み込む
 *
 * @author sugichan
 */
public class Config {

    public static String programCode = "LC";

    private final Plugin plugin;
    private FileConfiguration config = null;

    public static String host;
    public static String port;
    public static String database;
    public static String username;
    public static String password;

    public static boolean JumpStats;
    public static boolean OpJumpStats;
    public static boolean CheckIPAddress;
    public static boolean playerPingB;
    public static int fx;
    public static int fy;
    public static int fz;
    public static int fpitch;
    public static int fyaw;
    public static String fworld;

    public static List<String> present;
    public static List<String> IgnoreReportName;
    public static List<String> IgnoreReportIP;

    public static int AlarmCount;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        Tools.entryDebugFlag( programCode, consoleMode.none );
        Tools.Prt( "Config Loading now...", programCode );
        load();
    }

    /*
     * 設定をロードします
     */
    public void load() {
        // 設定ファイルを保存
        plugin.saveDefaultConfig();
        if (config != null) { // configが非null == リロードで呼び出された
            Tools.Prt( "Config Reloading now...", programCode );
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
        AlarmCount = config.getInt( "AlarmCount" );
        playerPingB = config.getBoolean( "PlayerPingBroadcast" );

        consoleMode DebugFlag;
        try {
            DebugFlag = consoleMode.valueOf( config.getString( "Debug" ) );
        } catch( IllegalArgumentException e ) {
            Tools.Prt( ChatColor.RED + "Config Debugモードの指定値が不正なので、normal設定にしました", programCode );
            DebugFlag = consoleMode.normal;
        }
        Tools.entryDebugFlag( programCode, DebugFlag );
    }

    public void Status( Player p ) {
        consoleMode consolePrintFlag = ( ( p == null ) ? consoleMode.none:consoleMode.stop );
        Tools.Prt( p, ChatColor.GREEN + "=== LoginContrl Status ===", consolePrintFlag, programCode );
        Tools.Prt( p, ChatColor.WHITE + "Degub Mode : " + ChatColor.YELLOW + Tools.consoleFlag.get( programCode ).toString(), consolePrintFlag, programCode );
        Tools.Prt( p, ChatColor.WHITE + "Mysql : " + ChatColor.YELLOW + host + ":" + port, consolePrintFlag, programCode );
        Tools.Prt( p, ChatColor.WHITE + "DB Name : " + ChatColor.YELLOW + database, consolePrintFlag, programCode );
        Tools.Prt( p, ChatColor.WHITE + "FirstJump : " + ChatColor.YELLOW + ( ( JumpStats ) ? "True":"None" ), consolePrintFlag, programCode );
        if ( JumpStats ) {
            Tools.Prt( p, ChatColor.WHITE + "  world:" + ChatColor.YELLOW + fworld, consolePrintFlag, programCode );
            Tools.Prt( p, ChatColor.WHITE + "  x:" + ChatColor.YELLOW + String.valueOf( fx ), consolePrintFlag, programCode );
            Tools.Prt( p, ChatColor.WHITE + "  y:" + ChatColor.YELLOW + String.valueOf( fy ), consolePrintFlag, programCode );
            Tools.Prt( p, ChatColor.WHITE + "  z:" + ChatColor.YELLOW + String.valueOf( fz ), consolePrintFlag, programCode );
            Tools.Prt( p, ChatColor.WHITE + "  p:" + ChatColor.YELLOW + String.valueOf( fpitch ), consolePrintFlag, programCode );
            Tools.Prt( p, ChatColor.WHITE + "  y:" + ChatColor.YELLOW + String.valueOf( fyaw ), consolePrintFlag, programCode );
        }
        Tools.Prt( p, ChatColor.WHITE + "Present Items", consolePrintFlag, programCode );
        present.stream().forEach( pr -> {
            String[] itemdata = pr.split( ",", 0 );
            Tools.Prt( p, ChatColor.YELLOW + " - " + itemdata[0] + "(" + itemdata[1] + ")", consolePrintFlag, programCode );
        } );

        Tools.Prt( p, ChatColor.WHITE + "Ignore Names", consolePrintFlag, programCode );
        IgnoreReportName.stream().forEach( IRN -> { Tools.Prt( p, ChatColor.YELLOW + " - " + IRN, consolePrintFlag, programCode ); } );

        Tools.Prt( p, ChatColor.WHITE + "Ignore IPs", consolePrintFlag, programCode );
        IgnoreReportIP.stream().forEach( IRI -> { Tools.Prt( p, ChatColor.YELLOW + " - " + IRI, consolePrintFlag, programCode ); } );

        Tools.Prt( p, ChatColor.WHITE + "Unknown IP Check : " + ChatColor.YELLOW + ( CheckIPAddress ? "True":"False" ), consolePrintFlag, programCode );
        Tools.Prt( p, ChatColor.WHITE + "Ping Broadcast   : " + ChatColor.YELLOW + ( playerPingB ? "True":"False" ), consolePrintFlag, programCode);
        Tools.Prt( p, ChatColor.GREEN + "==========================", consolePrintFlag, programCode );
    }

    public String KnownServers( String IP ) {
        return config.getString( IP, null );
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

    public boolean getKumaisu() {
        return config.getBoolean( "Kumaisu" );
    }
}
