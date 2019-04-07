/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
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

    private int MotD_Count;
    private int MotD_MaxCount;
    private String MotD1stLine;
    private List<String> MotD2ndLine;

    private List<String> IgnoreReportName;
    private List<String> IgnoreReportIP;

    private int DebugFlag;

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
        MotD_Count = config.getInt( "MotD2nd-Ping-Count" );
        MotD_MaxCount = config.getInt( "MotD2nd-Ping-Max-Count", 0 );
        IgnoreReportName = config.getStringList( "Ignore-Names" );
        IgnoreReportIP = config.getStringList( "Ignore-IP" );
        CheckIPAddress = config.getBoolean( "CheckIP" );

        MotD2ndLine = new ArrayList<>();
        MotD2ndLine.add( config.getString( "MotD2nd-Unknown" ) );
        MotD2ndLine.add( config.getString( "MotD2nd-Ping" ) );
        MotD2ndLine.add( config.getString( "MotD2nd-Player" ) );
        MotD2ndLine.add( config.getString( "MotD2nd-Ping-Player" ) );
        MotD2ndLine.add( config.getString( "MotD2nd-Alive" ) );

        switch ( config.getString( "Debug" ) ) {
        case "full":
            DebugFlag = 2;
            break;
        case "normal":
            DebugFlag = 1;
            break;
        case "none":
            DebugFlag = 0;
            break;
        default:
            DebugFlag = 0;
        }

    }

    public void Status( Player p ) {
        boolean consolePrintFlag = ( p == null );
        Utility.Prt( p, "=== LoginContrl Status ===", consolePrintFlag );
        Utility.Prt( p, "Degub Mode : " + DBString( DebugFlag ), consolePrintFlag );
        Utility.Prt( p, "Mysql : " + host + ":" + port, consolePrintFlag );
        Utility.Prt( p, "DB Name : " + database, consolePrintFlag );
        Utility.Prt( p, "FirstJump : " + ( ( JumpStats ) ? "True":"None" ), consolePrintFlag );
        if ( JumpStats ) {
            Utility.Prt( p, "  world:" + fworld, consolePrintFlag );
            Utility.Prt( p, "  x:" + String.valueOf( fx ), consolePrintFlag );
            Utility.Prt( p, "  y:" + String.valueOf( fy ), consolePrintFlag );
            Utility.Prt( p, "  z:" + String.valueOf( fz ), consolePrintFlag );
            Utility.Prt( p, "  p:" + String.valueOf( fpitch ), consolePrintFlag );
            Utility.Prt( p, "  y:" + String.valueOf( fyaw ), consolePrintFlag );
        }
        Utility.Prt( p, "Present Items", consolePrintFlag );
        present.stream().forEach( pr -> {
            String[] itemdata = pr.split( ",", 0 );
            Utility.Prt( p, " - " + itemdata[0] + "(" + itemdata[1] + ")", consolePrintFlag );
        } );

        Utility.Prt( p, "Ignore Names", consolePrintFlag );
        IgnoreReportName.stream().forEach( IRN -> { Utility.Prt( p, " - " + IRN, consolePrintFlag ); } );

        Utility.Prt( p, "Ignore IPs", consolePrintFlag );
        IgnoreReportIP.stream().forEach( IRI -> { Utility.Prt( p, " - " + IRI, consolePrintFlag ); } );

        Utility.Prt( p, "Unknown IP Check : " + ( CheckIPAddress ? "True":"False" ), consolePrintFlag );
        Utility.Prt( p, "MotD 1 Line : " + MotD1stLine, consolePrintFlag );
        Utility.Prt( p, "MotD 2 Line(Unknown) : " + MotD2ndLine.get( 0 ), consolePrintFlag );
        Utility.Prt( p, "MotD 2 Line(Player ) : " + MotD2ndLine.get( 2 ), consolePrintFlag );
        Utility.Prt( p, "==========================", consolePrintFlag );
    }

    public int getDebug() {
        return DebugFlag;
    }

    public void setDebug( int num ) {
        DebugFlag = num;
    }

    public boolean DBFlag( int lvl ) {
    // 0:none 1:normal 2:full
        Boolean prtf;
        switch ( DebugFlag ) {
            case 0:
                prtf = ( lvl == 0 );
                break;
            case 1:
                prtf = ( lvl == 1 );
                break;
            case 2:
                prtf = true;
                break;
            default:
                prtf = false;
        }
        return prtf;
    }

    public String DBString( int lvl ) {
        switch ( lvl ) {
            case 0:
                return "none";
            case 1:
                return "normal";
            case 2:
                return "full";
            default:
                return "Error";
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

    public String get1stLine() {
        return MotD1stLine;
    }

    public String get2ndLine( int num ) {
        return MotD2ndLine.get( num );
    }

    public int getmotDCount() {
        return MotD_Count;
    }

    public int getmotDMaxCount() {
        return MotD_MaxCount;
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
