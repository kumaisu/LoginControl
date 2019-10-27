/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.util.Date;
import java.net.UnknownHostException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.command.LoginlistCommand;
import com.mycompany.logincontrol.command.SpawnCommand;
import com.mycompany.logincontrol.command.PingCommand;
import com.mycompany.logincontrol.command.FlightCommand;
import com.mycompany.logincontrol.tools.Teleport;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.logincontrol.config.ConfigManager;
import com.mycompany.logincontrol.database.Database;
import com.mycompany.logincontrol.database.HostData;
import com.mycompany.logincontrol.database.ListData;
import com.mycompany.logincontrol.database.FileRead;
import com.mycompany.logincontrol.database.DatabaseControl;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class LoginControl extends JavaPlugin implements Listener {

    public ConfigManager config;
    private Date date;
    private MotDControl MotData;
    private String lastName = "Begin";

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents( this, this );
        config = new ConfigManager( this );
        MotData = new MotDControl( this );
        DatabaseControl.connect();
        DatabaseControl.TableUpdate();
        getCommand( "spawn" ).setExecutor( new SpawnCommand( this ) );
        getCommand( "flight" ).setExecutor( new FlightCommand( this ) );
        getCommand( "loginlist" ).setExecutor( new LoginlistCommand( this ) );
        getCommand( "ping" ).setExecutor( new PingCommand( this ) );
    }

    @Override
    public void onDisable() {
        DatabaseControl.disconnect();
        super.onDisable(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onLoad() {
        super.onLoad(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * プレイヤーがログインしようとした時に起きるイベント
     * BANなどされていてもこのイベントは発生する
     *
     * @param event
     */
    @EventHandler
    public void prePlayerLogin( AsyncPlayerPreLoginEvent event ) {
        Tools.Prt( "PrePlayerLogin process", Tools.consoleMode.max, programCode );
        date = new Date();
        ListData.AddSQL( date, event.getName(), event.getUniqueId().toString(), event.getAddress().getHostAddress(), 0 );
        HostData.AddPlayerToSQL( event.getAddress().getHostAddress(), event.getName() );
    }

    /**
     * プレイヤーがログインを成功すると発生するイベント
     * ここでプレイヤーに対して、様々な処理を実行する
     *
     * @param event
     * @throws UnknownHostException
     */
    @EventHandler( priority = EventPriority.HIGH )
    public void onPlayerLogin( PlayerJoinEvent event ) throws UnknownHostException {

        Tools.Prt( "onPlayerLogin process", Tools.consoleMode.max, programCode );
        event.setJoinMessage( null );
        Player player = event.getPlayer();
        if ( Database.dataSource == null ) {
            player.kickPlayer( Config.Incomplete_Message );
        }
        ListData.ChangeStatus( date, 1 );
        ListData.LogPrint( player, 5, false );
        HostData.AddCountHost( player.getAddress().getHostString(), -1 );
        ListData.CheckIP( player );

        if ( Config.Announce ) {
            Tools.Prt( player, Utility.ReplaceString( Config.AnnounceMessage, player.getDisplayName() ), Tools.consoleMode.max, programCode );
        }

        if ( !player.hasPlayedBefore() || ( Config.OpJumpStats && player.isOp() ) ) {
            Tools.Prt( ChatColor.AQUA + "The First Login Player", Tools.consoleMode.normal, programCode );

            if ( Config.JumpStats ) {
                if ( !Teleport.Beginner( player ) ) {
                    Tools.Prt( player, "You failed the beginner teleport", Tools.consoleMode.full, programCode);
                }
            } else Tools.Prt( "not Beginner Teleport", Tools.consoleMode.full, programCode );
    
            Config.present.stream().forEach( CP -> {
                Tools.ExecOtherCommand( player, CP, "" );
                Tools.Prt( ChatColor.AQUA + "Command Execute : " + ChatColor.WHITE + CP, Tools.consoleMode.max, programCode );
            } );
            
        } else {
            Tools.Prt( ChatColor.AQUA + "The Repeat Login Player", Tools.consoleMode.normal, programCode );
        }

        //  プレイヤーの言語設定を取得するために遅延処理の後 Welcome メッセージの表示を行う
        //  ラグが大きいが現状はこれが精一杯の状態
        getServer().getScheduler().scheduleSyncDelayedTask( this, () -> {
            String getLocale = Tools.getLanguage( player ).substring( 3, 5 );
            String locale2byte = Tools.getLanguage( player ).substring( 3, 5 ).toUpperCase();
            Tools.Prt( ChatColor.AQUA + "Player Menu is " + getLocale + " / " + locale2byte, programCode );
            
            if ( !player.hasPlayedBefore() || ( Config.OpJumpStats && player.isOp() ) ) {
                if( Config.NewJoin ) {
                    Tools.Prt( "Player host = " + player.getAddress().getHostString(), Tools.consoleMode.normal, programCode );
                    Tools.Prt( "Get Locale = " + locale2byte, Tools.consoleMode.normal, programCode );
                    String WelcomeMessage = ( Config.NewJoinMessage.get( locale2byte) == null ? Config.New_Join_Message : Config.NewJoinMessage.get( locale2byte ) );
                    Bukkit.broadcastMessage( Utility.ReplaceString( WelcomeMessage, player.getDisplayName() ) );
                }
            } else {
                if( Config.ReturnJoin && !player.hasPermission( "LoginCtl.silentjoin" ) ) {
                    String ReturnMessage = ( Config.ReturnJoinMessage.get( locale2byte) == null ? Config.Returning_Join_Message : Config.ReturnJoinMessage.get( locale2byte ) );
                    Bukkit.broadcastMessage( Utility.ReplaceString( ReturnMessage, player.getDisplayName() ) );
                }
            }
        }, 100 );
    }

    /**
     * プレイヤーがログアウトした時に発生するイベント
     *
     * @param event
     */
    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event ) {
        if ( event.getPlayer().hasPermission( "LoginCtl.silentquit" ) ) {
            event.setQuitMessage( null );
            return;
        }
        if ( Config.PlayerQuit ) {
            event.setQuitMessage( Utility.ReplaceString( Config.PlayerQuitMessage, event.getPlayer().getDisplayName() ) );
            Bukkit.broadcastMessage( Utility.ReplaceString( Config.PlayerQuitMessage, event.getPlayer().getDisplayName() ) );
        }
    }

    /**
     * サーバーへのリスト照会が来た時に起きるイベント
     * プレイヤーのサーバーリストへの文言（MotD）の内容を個別に修正して返信する
     *
     * @param event
     * @throws UnknownHostException
     * @throws ClassNotFoundException
     */
    @EventHandler
    public void onServerListPing( ServerListPingEvent event ) throws UnknownHostException, ClassNotFoundException {
        String Names = "Unknown";
        // ConsoleLog Flag 2:Full 1:Normal(Playerのみ)
	Tools.consoleMode PrtStatus = Tools.consoleMode.full;

        String MotdMsg = MotData.get1stLine();
        String MsgColor = ChatColor.GRAY.toString();
        String Host = Config.KnownServers.get( event.getAddress().getHostAddress() );

        if ( Host == null ) {
            //  簡易DNSからホスト名を取得
            int MsgNum = 0;
            if ( HostData.GetSQL( event.getAddress().getHostAddress() ) ) {
                Host = Database.Host;
                //  未知のホスト名の場合は LIGHT_PURPLE , 既知のPlayerだった場合は WHITE になる
                if ( Host.contains( "Player" ) ) {
                    MsgColor = ChatColor.WHITE.toString();
                    //  簡易DNSにプレイヤー登録されている場合は、ログイン履歴を参照して最新のプレイヤー名を取得する
                    Names = ListData.GetPlayerName( event.getAddress().getHostAddress() );
                    Tools.Prt( "Names    [" + Names + "]", Tools.consoleMode.max, programCode );
                    Tools.Prt( "Ignore   [" + ( Config.IgnoreReportName.contains( Names) ? "True" : "False" ) + "]", Tools.consoleMode.max, programCode);
                    if ( ( Config.playerPingB && !Config.IgnoreReportName.contains( Names ) ) && ( Names != null && !Names.equals( lastName ) ) ) {
                        Tools.Prt( "lastName [" + lastName + "]", Tools.consoleMode.max, programCode );
                        //  if ( Bukkit.getServer().getOnlinePlayers().size() > 0 ) {
                        for( Player p : Bukkit.getOnlinePlayers() ) {
                            p.sendMessage( ChatColor.GREEN + "Ping From Player " + ChatColor.WHITE + Names );
                        }
                        if ( Names == null ) {
                            Tools.Prt( ChatColor.GREEN + "(Console) Ping From Player " + ChatColor.WHITE + Names,Tools.consoleMode.full, programCode );
                        }
                        lastName = Names;
                    }
                    MsgNum = 2;
                    PrtStatus = Tools.consoleMode.normal;
                } else {
                    MsgColor = ChatColor.LIGHT_PURPLE.toString();
                }
            } else {
                //  DBに該当なしなので、DB登録
                //  ホスト名が取得できなかった場合は、Unknown Player を File に記録し、新規登録
                MsgColor = ChatColor.RED.toString();
                Host = HostData.AddHostname( event.getAddress().getHostAddress(), Config.CheckIPAddress );
                //  新規ホストとして、Unknown.yml ファイルへ書き出し
                DatabaseControl.WriteFileUnknown( event.getAddress().getHostAddress(), this.getDataFolder().toString() );
            }

            HostData.AddCountHost( event.getAddress().getHostAddress(), 0 );

            Host = Utility.StringBuild( Host, "(", String.valueOf( Database.Count ), ")" );

            String Motd2ndLine = MotData.getModifyMessage( Names, event.getAddress().getHostAddress() );

            if ( "".equals( Motd2ndLine ) ) {
                if ( ( MotData.getmotDMaxCount() != 0 ) && ( Database.Count>MotData.getmotDMaxCount() ) ) {
                    MsgNum = 4;
                } else {
                    if ( ( MotData.getmotDCount() != 0 ) && ( Database.Count>MotData.getmotDCount() ) ) MsgNum++;
                }

                Motd2ndLine = MotData.get2ndLine( MsgNum );
                Motd2ndLine = Motd2ndLine.replace( "%count", String.valueOf( Database.Count ) );
                Motd2ndLine = Motd2ndLine.replace( "%date", Database.sdf.format( Database.NewDate ) );
                MotdMsg = Utility.StringBuild( MotdMsg, Motd2ndLine );
                Tools.Prt( Utility.StringBuild( "MotD = ", Utility.ReplaceString( Motd2ndLine, Names ) ), Tools.consoleMode.max, programCode );
            } else {
                MotdMsg = Motd2ndLine;
                Tools.Prt( Utility.StringBuild( "Change = ", Utility.ReplaceString( Motd2ndLine.replace( "\n", " " ), Names ) ), Tools.consoleMode.max, programCode );
            }

            if ( ( Config.AlarmCount != 0 ) && ( Database.Count >= Config.AlarmCount ) ) { PrtStatus = Tools.consoleMode.print; }

        } else {
            //  Configに既知のホスト登録があった場合
            MotdMsg = Utility.StringBuild( MotdMsg, MotData.get2ndLine( 4 ) );
        }

        event.setMotd( Utility.ReplaceString( MotdMsg, Names ) );
        // event.getNumPlayers().set( 30 );

        if ( !Config.IgnoreReportIP.contains( event.getAddress().getHostAddress() ) ) {
            String msg = Utility.StringBuild( ChatColor.GREEN.toString(), "Ping from ", MsgColor, Host, ChatColor.YELLOW.toString(), " [", event.getAddress().getHostAddress(), "]" );
            Tools.Prt( msg, PrtStatus, programCode );
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
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
                        config = new ConfigManager( this );
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
                        FileRead.GetLogFile( IP );
                        return true;
                    default:
                }
            } else {
                Tools.Prt( p, "You do not have permission.", programCode );
            }

            if ( hasAdminPerm ) {
                switch ( CtlCmd ) {
                    case "status":
                        config.Status( p );
                        return true;
                    case "Motd":
                        MotData.getStatus( p );
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
                        HostData.AddCountHost( IP, Integer.parseInt( HostName ) );
                        HostData.infoHostname( p, IP );
                        return true;
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
                        HostData.PingTop( p, PTLines );
                        return true;
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
                Tools.Prt( p, "loginctl GetLog", programCode );
            }
            if ( ( p == null ) || p.hasPermission( "LoginCtl.admin" ) ) {
                //  LoginCtl.admin
                Tools.Prt( p, "loginctl status", programCode );
                Tools.Prt( p, "loginctl MotD", programCode );
                Tools.Prt( p, "loginctl sql SQL_Command", programCode );
                Tools.Prt( p, "loginctl info IPAddress", programCode );
                Tools.Prt( p, "loginctl chg IPAddress HostName", programCode );
                Tools.Prt( p, "loginctl add IPAddress [HostName]", programCode );
                Tools.Prt( p, "loginctl del IPAddress", programCode );
                Tools.Prt( p, "loginctl count IPAddress ( num or Reset )", programCode );
                Tools.Prt( p, "loginctl search word", programCode );
                Tools.Prt( p, "loginctl pingtop [LineCount]", programCode );
            }
        }
        return false;
    }

    //
    //  Extra Command
    //  オプショナリーな機能
    //

    /**
     * サーバー内でルール違反等がありキックされた時に発生するイベント
     *
     * @param event
     */
    @EventHandler
    public void onKickMessage( PlayerKickEvent event ) {
        if ( Config.PlayerKick ) {
            String msg = Utility.ReplaceString( Config.KickMessage, event.getPlayer().getDisplayName() );
            if ( !event.getReason().equals( "" ) ) {
                msg = Utility.ReplaceString( msg.replace( "%Reason%", event.getReason() ) );
            } else {
                msg = msg.replace( "%Reason%", "Unknown Reason" );
            }
            Bukkit.broadcastMessage( msg );
        }
    }

    /**
     * サーバー内で死亡した時に発生するイベント
     * 現在はイベントを拾って、単純に表示する程度の中途半端
     * 将来的には死因やキラーの表示をちゃんと出来るようにしたい（修正中）
     *
     * @param event
     */
    @EventHandler
    public void onPlayerDeath( PlayerDeathEvent event ) {
        if ( Config.DeathMessageFlag ) {
            Tools.Prt( Utility.StringBuild( "DeathMessage: ", event.getDeathMessage() ), Tools.consoleMode.full, programCode );
            Tools.Prt( Utility.StringBuild( "DisplayName : ", event.getEntity().getDisplayName() ), Tools.consoleMode.full, programCode );
            if ( event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent ) {
                EntityDamageByEntityEvent lastcause = ( EntityDamageByEntityEvent ) event.getEntity().getLastDamageCause();
                Entity entity = lastcause.getDamager();
                Tools.Prt( Utility.StringBuild( "Killer Name : ", entity.getName() ), Tools.consoleMode.full, programCode );
                String msg = config.DeathMessage( entity.getName().toUpperCase() );
                msg = Utility.ReplaceString( msg, event.getEntity().getDisplayName() );
                msg = msg.replace( "%mob%", entity.getName() );
                //event.setDeathMessage( null );
                Bukkit.broadcastMessage( msg );
            } else {
                Tools.Prt( "Other Death", Tools.consoleMode.normal, programCode );
                Bukkit.broadcastMessage(
                    Utility.StringBuild(
                        ChatColor.YELLOW.toString(), "[天の声] ",
                        ChatColor.AQUA.toString(), event.getEntity().getDisplayName(),
                        ChatColor.WHITE.toString(), "は",
                        ChatColor.RED.toString(), "謎",
                        ChatColor.WHITE.toString(), "の死を遂げた"
                    )
                );
            }
        }
    }

    /**
     * フライが禁止されているプレイヤーのフライを強制解除する
     *
     * @param event
     */
    @EventHandler
    public void onFlight( PlayerToggleFlightEvent event ) {
        Player p = event.getPlayer();
        // p.sendMessage( "Catch Flight mode " + p.getDisplayName() );
        if ( !p.hasPermission( "LoginCtl.flight" ) ) FlightCommand.FlightMode( p, false );
    }

    /**
     * 看板をクリックした時に発生するイベント
     *
     * @param event
     */
    @EventHandler //    看板ブロックを右クリック
    public void onSignClick( PlayerInteractEvent event ) {

        if ( event.getAction() != Action.RIGHT_CLICK_BLOCK ) return;

        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Material material = clickedBlock.getType();
        Tools.Prt( "Material = " + material.name(), Tools.consoleMode.max, programCode);

        try {
            Sign sign = (Sign) clickedBlock.getState();
            if ( sign.getLine( 0 ).equals( "[TrashCan]" ) ) {
                Inventory inv;
                inv = Bukkit.createInventory( null, 36, "Trash Can" );
                player.openInventory( inv );
            }
        } catch ( ClassCastException e ) {}
    }
}
