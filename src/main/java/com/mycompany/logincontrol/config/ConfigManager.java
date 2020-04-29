/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.logincontrol.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class ConfigManager {

    private static Plugin plugin;
    private static FileConfiguration config = null;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        Tools.entryDebugFlag( programCode, Tools.consoleMode.print );
        Tools.Prt( "Config Loading now...", programCode );
        load();
    }

    /*
     * 設定をロードします
     */
    public static void load() {
        // 設定ファイルを保存
        plugin.saveDefaultConfig();
        if (config != null) { // configが非null == リロードで呼び出された
            Tools.Prt( "Config Reloading now...", programCode );
            plugin.reloadConfig();
        }
        config = plugin.getConfig();

        Config.present = new ArrayList<>();
        Config.IgnoreReportName = new ArrayList<>();
        Config.IgnoreReportIP = new ArrayList<>();

        if ( !Tools.setDebug( config.getString( "Debug" ), programCode ) ) {
            Tools.entryDebugFlag( programCode, Tools.consoleMode.normal );
            Tools.Prt( ChatColor.RED + "Config Debugモードの指定値が不正なので、normal設定にしました", programCode );
        }

        //  特殊個人用フラグ
        Config.kumaisu = config.getBoolean( "Kumaisu" );

        Config.host = config.getString( "mysql.host" );
        Config.port = config.getString( "mysql.port" );
        Config.database = config.getString( "mysql.database" );
        Config.username = config.getString( "mysql.username" );
        Config.password = config.getString( "mysql.password" );
        Config.MaximumPoolSize = config.getInt( "mysql.MaximumPoolSize", 3 );
        Config.MinimumIdle     = config.getInt( "mysql.MinimumIdle", 3 );
        Config.JumpStats = config.getBoolean( "FirstPoint" );
        Config.OpJumpStats = config.getBoolean( "OpJump" );
        Config.fworld  = config.getString( "world" );
        Config.fx      = Float.valueOf( config.getString( "x" ) );
        Config.fy      = Float.valueOf( config.getString( "y" ) );
        Config.fz      = Float.valueOf( config.getString( "z" ) );
        Config.fyaw    = Float.valueOf( config.getString( "yaw" ) );
        Config.fpitch  = Float.valueOf( config.getString( "pitch" ) );
        Config.present = config.getStringList( "Present" );
        Config.IgnoreReportName = config.getStringList( "Ignore-Names" );
        Config.IgnoreReportIP = config.getStringList( "Ignore-IP" );
        Config.CheckIPAddress = config.getBoolean( "CheckIP" );
        Config.AlarmCount = config.getInt( "AlarmCount" );
        Config.playerPingB = config.getBoolean( "PlayerPingBroadcast" );

        Config.LogFull                  = config.getString( "Message.LogFull" );
        Config.Reload                   = config.getString( "Message.Reload" );
        Config.ArgsErr                  = config.getString( "Message.ArgsErr" );
        Config.OptError                 = config.getString( "Message.OptError" );
        Config.Announce                 = config.getBoolean( "ANNOUNCE.Enabled" );
        Config.AnnounceMessage          = config.getString( "ANNOUNCE.Message" );
        Config.NewJoin                  = config.getBoolean( "New_Join_Message.Enabled" );
        Config.ReturnJoin               = config.getBoolean( "Returning_Join_Message.Enabled" );
        Config.PlayerQuit               = config.getBoolean( "Quit_Message.Enabled" );
        Config.PlayerQuitMessage        = config.getString( "Quit_Message.Message" );
        Config.New_Join_Message         = config.getString( "New_Join_Message.Message" );
        Config.Returning_Join_Message   = config.getString( "Returning_Join_Message.Message" );
        Config.Incomplete_Message       = config.getString( "Incomplete_Message" );

        List< String > getName = ( List< String > ) config.getList( "KnownServer" );
        Config.KnownServers = new TreeMap<>();
        Tools.Prt( "KnownServer List.", Tools.consoleMode.full, programCode );
        if ( getName != null ) {
            for( int i = 0; i<getName.size(); i++ ) {
                String[] param = getName.get( i ).split(",");
                if ( param[1] == null ) { param[1] = "Unknown"; }
                Tools.Prt( "Param0:" + param[0] + " Param1:" + param[1], Tools.consoleMode.full, programCode );
                Config.KnownServers.put( param[0], param[1] );
            }
        }

        List< String > getWelcome = ( List< String > ) config.getList( "New_Join_Message.Lang" );
        Config.NewJoinMessage = new TreeMap<>();
        Tools.Prt( "NewJoingMessage List.", Tools.consoleMode.full, programCode );
        if ( getWelcome != null ) {
            for( int i = 0; i<getWelcome.size(); i++ ) {
                String[] param = getWelcome.get( i ).split(",");
                if ( param[1] == null ) { param[1] = Config.New_Join_Message; }
                Tools.Prt( "Param0:" + param[0] + " Param1:" + param[1], Tools.consoleMode.full, programCode );
                Config.NewJoinMessage.put( param[0], param[1] );
            }
        }

        List< String > getRetMsg = ( List< String > ) config.getList( "Returning_Join_Message.Lang" );
        Config.ReturnJoinMessage = new TreeMap<>();
        Tools.Prt( "ReturnJoinMessage List.", Tools.consoleMode.full, programCode );
        if ( getRetMsg != null ) {
            for( int i = 0; i<getRetMsg.size(); i++ ) {
                String[] param = getRetMsg.get( i ).split(",");
                if ( param[1] == null ) { param[1] = Config.Returning_Join_Message; }
                Tools.Prt( "Param0:" + param[0] + " Param1:" + param[1], Tools.consoleMode.full, programCode );
                Config.ReturnJoinMessage.put( param[0], param[1] );
            }
        }
    }

    public static void Status( Player p ) {
        Tools.Prt( p, ChatColor.GREEN + "=== LoginContrl Status ===", programCode );
        Tools.Prt( p, ChatColor.WHITE + "Degub Mode : " + ChatColor.YELLOW + Tools.consoleFlag.get( programCode ).toString(), programCode );
        Tools.Prt( p, ChatColor.WHITE + "Mysql : " + ChatColor.YELLOW + Config.host + ":" + Config.port + "/" + Config.database, programCode );
        if ( p == null ) {
            Tools.Prt( p, ChatColor.WHITE + "DB UserName  : " + ChatColor.YELLOW + Config.username + "[" + Config.password + "]", programCode );
        }
        if ( Config.JumpStats ) {
            Tools.Prt( p, ChatColor.WHITE + "First Jump Position : " +
                ChatColor.YELLOW + "[" + Config.fworld + "] " +
                ChatColor.WHITE + "x:" + ChatColor.YELLOW + String.valueOf( Config.fx ) + "," +
                ChatColor.WHITE + "y:" + ChatColor.YELLOW + String.valueOf( Config.fy ) + "," +
                ChatColor.WHITE + "z:" + ChatColor.YELLOW + String.valueOf( Config.fz ) + "," +
                ChatColor.WHITE + "pit:" + ChatColor.YELLOW + String.valueOf( Config.fpitch ) + "," +
                ChatColor.WHITE + "yaw:" + ChatColor.YELLOW + String.valueOf( Config.fyaw ),
                programCode
            );
        } else {
            Tools.Prt( p, ChatColor.WHITE + "FirstJump : " + ChatColor.YELLOW + "None", programCode );
        }
        Tools.Prt( p, ChatColor.WHITE + "Present Items", programCode );
        Config.present.stream().forEach( CP -> { Tools.Prt( p, ChatColor.WHITE + " - " + ChatColor.YELLOW + CP, programCode ); } );

        Tools.Prt( p, ChatColor.WHITE + "Ignore Names", programCode );
        Config.IgnoreReportName.stream().forEach( IRN -> { Tools.Prt( p, ChatColor.YELLOW + " - " + IRN, programCode ); } );

        Tools.Prt( p, ChatColor.WHITE + "Ignore IPs", programCode );
        Config.IgnoreReportIP.stream().forEach( IRI -> { Tools.Prt( p, ChatColor.YELLOW + " - " + IRI, programCode ); } );

        Tools.Prt( p, ChatColor.WHITE + "Unknown IP Check : " + ChatColor.YELLOW + ( Config.CheckIPAddress ? "True":"False" ), programCode );
        Tools.Prt( p, ChatColor.WHITE + "Ping Broadcast   : " + ChatColor.YELLOW + ( Config.playerPingB ? "True":"False" ), programCode );
        Tools.Prt( p, ChatColor.GREEN + "==========================", programCode );
    }

    public String KnownServers( String IP ) {
        return config.getString( IP, null );
    }

    public static void NewJoinStatus( Player player ) {
        Tools.Prt( player, ChatColor.WHITE + "New Join Message's", Config.programCode );
        for ( Map.Entry< String, String > entry : Config.NewJoinMessage.entrySet() ) {
            Tools.Prt( player, ChatColor.WHITE + entry.getKey() + " - " + entry.getValue(), Config.programCode );
        }
    }

    public static void RetJoinStatus( Player player ) {
        Tools.Prt( player, ChatColor.WHITE + "Return Join Message's", Config.programCode );
        for ( Map.Entry< String, String > entry : Config.ReturnJoinMessage.entrySet() ) {
            Tools.Prt( player, ChatColor.WHITE + entry.getKey() + " - " + entry.getValue(), Config.programCode );
        }
    }
}
