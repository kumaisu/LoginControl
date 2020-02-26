/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.LoginControl;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.logincontrol.config.ConfigManager;
import com.mycompany.logincontrol.database.DatabaseControl;
import com.mycompany.logincontrol.database.FileRead;
import com.mycompany.logincontrol.database.HostData;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class AdminCommand implements CommandExecutor {
    private final LoginControl instance;

    public AdminCommand( LoginControl instance ) {
        this.instance = instance;
    }

    public int getNum( Player player, String NumStr ) {
        try {
            return Integer.parseInt( NumStr );
        } catch ( NumberFormatException e ) {
            Tools.Prt( player, ChatColor.RED + "数値を入力してください", Config.programCode );
            return 0;
        }
    }

    /**
     * コマンド入力があった場合に発生するイベント
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args
     * @return
     */
    @Override
    public boolean onCommand( CommandSender sender,Command cmd, String commandLabel, String[] args ) {
        Player p = ( sender instanceof Player ) ? ( Player )sender:( Player )null;

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "loginctl" ) ) {
            String msg;
            String IP = "127.0.0.0";
            String HostName = "";
            String CtlCmd = "None";

            boolean hasConsolePerm = ( p == null ? true : p.hasPermission( "LoginCtl.console" ) );
            boolean hasAdminPerm = ( p == null ? true : p.hasPermission( "LoginCtl.admin" ) );

            if ( args.length > 0 ) CtlCmd = args[0];
            if ( args.length > 1 ) IP = args[1];
            if ( args.length > 2 ) HostName = args[2];

            if ( hasConsolePerm ) {
                switch ( CtlCmd ) {
                    case "Reload":
                        LoginControl.config.load();
                        Tools.Prt( p, Utility.ReplaceString( Config.Reload ), programCode );
                        return true;
                    case "Dupcheck":
                        HostData.DuplicateCheck( p );
                        return true;
                    case "CheckIP":
                        Config.CheckIPAddress = !Config.CheckIPAddress;
                        Tools.Prt( p,
                            ChatColor.GREEN + "Unknown IP Address Check Change to " +
                            ChatColor.YELLOW + ( Config.CheckIPAddress ? "True" : "False" ),
                            programCode
                        );
                        return true;
                    case "Console":
                        if ( !Tools.setDebug( IP, programCode ) ) {
                            Tools.entryDebugFlag( programCode, Tools.consoleMode.normal );
                            Tools.Prt( ChatColor.RED + "Config Debugモードの指定値が不正なので、normal設定にしました", programCode );
                        }
                        Tools.Prt( p,
                            ChatColor.GREEN + "System Debug Mode is [ " +
                            ChatColor.RED + Tools.consoleFlag.get( programCode ) +
                            ChatColor.GREEN + " ]",
                            programCode
                        );
                        return true;
                    case "Getlog":
                        //  Getlog YYYY
                        FileRead.GetLogFile( IP );
                        return true;
                    default:
                }
            } else {
                Tools.Prt( p, "You do not have permission.", programCode );
            }

            if ( hasAdminPerm ) {
                switch ( CtlCmd.toLowerCase() ) {
                    case "reward":
                        LoginControl.config.RewardStatus( p );
                        return true;
                    case "status":
                        LoginControl.config.Status( p );
                        return true;
                    case "motd":
                        LoginControl.MotData.getStatus( p );
                        return true;
                    case "joinmsg":
                        ConfigManager.NewJoinStatus( p );
                        return true;
                    case "retmsg":
                        ConfigManager.RetJoinStatus( p );
                        return true;
                    case "sql":
                        String SQL_Cmd = "";
                        for ( int i = 1; args.length > i; i++ ) { SQL_Cmd = SQL_Cmd + " " + args[i]; }
                        DatabaseControl.SQLCommand( p, SQL_Cmd );
                        return true;
                    case "chg":
                        if ( HostName.length() < 61 ) {
                            if ( HostData.ChgHostname( IP, HostName ) ) {
                                HostData.infoHostname( p, IP );
                            }
                        } else {
                            Tools.Prt( p, ChatColor.RED + "Hostname is limited to 60 characters", programCode );
                        }
                        return true;
                    case "info":
                        if ( !IP.equals( "" ) ) {
                            Tools.Prt( p, "Check Unknown IP Information [" + IP + "]", programCode );
                            HostData.infoHostname( p, IP );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: info IPAddress", programCode );
                        }
                        return true;
                    case "add":
                        if ( !IP.equals( "" ) ) {
                            if ( HostData.GetSQL( IP ) ) {
                                Tools.Prt( p, ChatColor.RED + IP + " is already exists", programCode );
                            } else {
                                if ( !HostName.equals( "" ) ) {
                                    HostData.AddSQL( IP, HostName );
                                } else {
                                    Tools.Prt( p, ChatColor.RED + " Host name is required", programCode );
                                }
                            }
                            HostData.infoHostname( p, IP );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: add IPAddress [HostName]", programCode );
                        }
                        return true;
                    case "del":
                        if ( !IP.equals( "" ) ) {
                            if ( HostData.DelSQL( IP ) ) {
                                msg = ChatColor.GREEN + "Data Deleted [";
                            } else {
                                msg = ChatColor.RED + "Failed to Delete Data [";
                            }
                            Tools.Prt( p, msg + IP + "]", programCode );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: del IPAddress", programCode );
                        }
                        return true;
                    case "count":
                        if ( HostName.equals( "Reset" ) ) HostName = "-1";
                        try {
                            HostData.AddCountHost( IP, Integer.parseInt( HostName ) );
                            HostData.infoHostname( p, IP );
                            return true;
                        } catch ( NumberFormatException e ) {
                            Tools.Prt( p, ChatColor.RED + "値を入力してください", Config.programCode );
                            break;
                        }
                    case "search":
                        if ( !IP.equals( "" ) ) {
                            HostData.SearchHostname( p, IP );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: search word", programCode );
                        }
                        return true;
                    case "pingtop":
                        int PTLines;
                        try {
                            PTLines = Integer.parseInt( IP );
                        } catch ( NumberFormatException e ) {
                            Tools.Prt( p, ChatColor.RED + "Please specify an integer", programCode );
                            PTLines = 10;
                        }
                        if ( PTLines < 1 ) { PTLines = 10; }
                        HostData.PingTop( p, PTLines, ( HostName.equals( "full" ) ) );
                        return true;
                    case "alart":
                        if ( HostName.equals( "true" ) || HostName.equals( "false" ) ) {
                            HostData.ChangeWarning( IP, ( HostName.equals( "true" ) ) );
                            return true;
                        } else {
                            Tools.Prt( p, ChatColor.RED + "true か false を指定してください", programCode );
                        }
                    default:
                }
            } else {
                Tools.Prt( p, "You do not have permission.", programCode );
            }

            if ( ( p == null ) || p.hasPermission( "LoginCtl.console" ) ) {
                //  LoginCtl.console
                Tools.Prt( p, "loginctl Reload", programCode );
                Tools.Prt( p, "loginctl Console [max,full,normal,none]", programCode );
                Tools.Prt( p, "loginctl CheckIP", programCode );
                Tools.Prt( p, "loginctl Dupcheck", programCode );
                Tools.Prt( p, "loginctl GetLog YYYY", programCode );
            }
            if ( ( p == null ) || p.hasPermission( "LoginCtl.admin" ) ) {
                //  LoginCtl.admin
                Tools.Prt( p, "loginctl status", programCode );
                Tools.Prt( p, "loginctl motd", programCode );
                Tools.Prt( p, "loginctl joinmsg", programCode );
                Tools.Prt( p, "loginctl retmsg", programCode );
                Tools.Prt( p, "loginctl sql SQL_Command", programCode );
                Tools.Prt( p, "loginctl info IPAddress", programCode );
                Tools.Prt( p, "loginctl chg IPAddress HostName", programCode );
                Tools.Prt( p, "loginctl add IPAddress [HostName]", programCode );
                Tools.Prt( p, "loginctl del IPAddress", programCode );
                Tools.Prt( p, "loginctl count IPAddress ( num or Reset )", programCode );
                Tools.Prt( p, "loginctl alart IPAddress [true/false]", programCode );
                Tools.Prt( p, "loginctl search word", programCode );
                Tools.Prt( p, "loginctl pingtop [LineCount]", programCode );
            }
        }
        return false;
    }
}
