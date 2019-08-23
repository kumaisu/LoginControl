/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import static org.bukkit.Bukkit.getWorld;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import com.mycompany.logincontrol.command.SpawnCommand;
import com.mycompany.logincontrol.command.FlightCommand;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.logincontrol.database.DatabaseControl;
import com.mycompany.logincontrol.database.FileRead;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Tools.consoleMode;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class LoginControl extends JavaPlugin implements Listener {

    private Config config;
    private Date date;
    private MotDControl MotData;
    private String lastName = "";
    private DatabaseControl DBA = null;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents( this, this );
        config = new Config( this );
        MotData = new MotDControl( this );
        DBA = new DatabaseControl();
        getCommand( "spawn" ).setExecutor( new SpawnCommand( this ) );
        getCommand( "flight" ).setExecutor( new FlightCommand( this ) );
    }

    @Override
    public void onDisable() {
        DBA.disconnect();
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
        Tools.Prt( "PrePlayerLogin process", consoleMode.full, programCode );
        date = new Date();
        DBA.listSave( date, event.getName(), event.getUniqueId().toString(), event.getAddress().getHostAddress(), 0 );
        DBA.AddPlayerToSQL( event.getAddress().getHostAddress(), event.getName() );
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

        Tools.Prt( "onPlayerLogin process", consoleMode.max, programCode );
        event.setJoinMessage( null );
        Player player = event.getPlayer();
        DBA.listChangeStatus( date, 1 );
        DBA.LogPrint( player, 5, false );
        DBA.AddCountHost( player.getAddress().getHostString(), -1 );
        DBA.listCheckIP( player );

        if ( config.Announce() ) {
            Tools.Prt( player, Utility.ReplaceString( config.AnnounceMessage(), player.getDisplayName() ), consoleMode.max, programCode );
        }

        if ( !player.hasPlayedBefore() || ( Config.OpJumpStats && player.isOp() ) ) {
            Tools.Prt( ChatColor.LIGHT_PURPLE + "The First Login Player", consoleMode.normal, programCode );

            if ( Config.JumpStats ) {
                if ( !BeginnerCommand.BeginnerTeleport( player ) ) {
                    Tools.Prt( player, "You failed the beginner teleport", Tools.consoleMode.full, programCode);
                }
            } else Tools.Prt( "not Beginner Teleport", Tools.consoleMode.max, programCode );
    
            if( config.NewJoin() ) {
                String msg = DBA.GetLocale( player.getAddress().getHostString() );
                Tools.Prt( "Player host = " + player.getAddress().getHostString(), consoleMode.normal, programCode );
                Tools.Prt( "Get Locale = " + msg, consoleMode.normal, programCode );
                Bukkit.broadcastMessage( Utility.ReplaceString( config.NewJoinMessage( msg ), player.getDisplayName() ) );
            }

            Config.present.stream().forEach( CP -> {
                Tools.ExecOtherCommand( player, CP, "" );
                Tools.Prt( ChatColor.AQUA + "Command Execute : " + ChatColor.WHITE + CP, consoleMode.max, programCode );
            } );
            
        } else {
            Tools.Prt( "The Repeat Login Player", consoleMode.normal, programCode );
            if( config.ReturnJoin() && !player.hasPermission( "LoginCtl.silentjoin" ) ) {
                Bukkit.broadcastMessage( Utility.ReplaceString( config.ReturnJoinMessage( DBA.GetLocale( player.getAddress().getHostString() ) ), player.getDisplayName() ) );
            }
        }
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
        if ( config.PlayerQuit() ) {
            event.setQuitMessage( Utility.ReplaceString( config.PlayerQuitMessage(), event.getPlayer().getDisplayName() ) );
            Bukkit.broadcastMessage( Utility.ReplaceString( config.PlayerQuitMessage(), event.getPlayer().getDisplayName() ) );
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
	consoleMode PrtStatus = consoleMode.full;

        String MotdMsg = MotData.get1stLine();
        String MsgColor = ChatColor.GRAY.toString();
        String Host = config.KnownServers( event.getAddress().getHostAddress() );

        if ( Host == null ) {
            //  簡易DNSからホスト名を取得
            Host = DBA.GetHost( event.getAddress().getHostAddress() );
            int MsgNum = 0;
            if ( Host.equals( "Unknown" ) ) {
                //  DBに該当なしなので、DB登録
                //  ホスト名が取得できなかった場合は、Unknown Player を File に記録し、新規登録
                MsgColor = ChatColor.RED.toString();
                Host = DBA.getUnknownHost( event.getAddress().getHostAddress(), Config.CheckIPAddress );
                //  新規ホストとして、Unknown.yml ファイルへ書き出し
                DBA.WriteFileUnknown( event.getAddress().getHostAddress(), this.getDataFolder().toString() );
            } else {
                //  未知のホスト名の場合は LIGHT_PURPLE , 既知のPlayerだった場合は WHITE になる
                if ( Host.contains( "Player" ) ) {
                    MsgColor = ChatColor.WHITE.toString();
                    //  簡易DNSにプレイヤー登録されている場合は、ログイン履歴を参照して最新のプレイヤー名を取得する
                    Names = DBA.listGetPlayerName( event.getAddress().getHostAddress() );
                    Tools.Prt( "Names    [" + Names + "]", Tools.consoleMode.max, programCode );
                    Tools.Prt( "Ignore   [" + ( Config.IgnoreReportName.contains( Names) ? "True" : "False" ) + "]", Tools.consoleMode.max, programCode);
                    if ( ( Config.playerPingB && !Config.IgnoreReportName.contains( Names ) ) && ( Names != null && !Names.equals( lastName ) ) ) {
                        Tools.Prt( "lastName [" + lastName + "]", Tools.consoleMode.max, programCode );
                        if ( Bukkit.getOnlinePlayers().size() > 0 ) Bukkit.broadcastMessage( ChatColor.GREEN + "Ping From Player " + ChatColor.WHITE + Names );
                        lastName = Names;
                    }
                    MsgNum = 2;
                    PrtStatus = consoleMode.normal;
                } else {
                    MsgColor = ChatColor.LIGHT_PURPLE.toString();
                }
            }

            DBA.AddCountHost( event.getAddress().getHostAddress(), 0 );

            int count = DBA.GetCountHosts( event.getAddress().getHostAddress() );
            Host = Utility.StringBuild( Host, "(", String.valueOf( count ), ")" );

            String Motd2ndLine = MotData.getModifyMessage( Names, event.getAddress().getHostAddress() );

            if ( "".equals( Motd2ndLine ) ) {
                if ( ( MotData.getmotDMaxCount() != 0 ) && ( count>MotData.getmotDMaxCount() ) ) {
                    MsgNum = 4;
                } else {
                    if ( ( MotData.getmotDCount() != 0 ) && ( count>MotData.getmotDCount() ) ) MsgNum++;
                }

                Motd2ndLine = MotData.get2ndLine( MsgNum );
                Motd2ndLine = Motd2ndLine.replace( "%count", String.valueOf( count ) );
                //  True : カウントを開始した日を指定
                //  False : 最後にカウントされた日を指定
                Motd2ndLine = Motd2ndLine.replace( "%date", DBA.getDateHost( event.getAddress().getHostAddress(), true ) );
                MotdMsg = Utility.StringBuild( MotdMsg, Motd2ndLine );
                Tools.Prt( Utility.StringBuild( "MotD = ", Utility.ReplaceString( Motd2ndLine, Names ) ), consoleMode.max, programCode );
            } else {
                MotdMsg = Motd2ndLine;
                Tools.Prt( Utility.StringBuild( "Change = ", Utility.ReplaceString( Motd2ndLine.replace( "\n", " " ), Names ) ), consoleMode.max, programCode );
            }

            if ( ( Config.AlarmCount != 0 ) && ( count >= Config.AlarmCount ) ) { PrtStatus = consoleMode.none; }

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
        int lineSet = 30;
        Player p = ( sender instanceof Player ) ? ( Player )sender:( Player )null;
        consoleMode checkConsoleFlag = ( ( p == null ) ? consoleMode.none : consoleMode.stop );

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "loginlist" ) ) {
            int PrtF = 0;
            String Param = "";
            boolean FullFlag = false;

            for ( String arg : args ) {
                String[] param = arg.split( ":" );
                switch ( param[0] ) {
                    case "d":
                        PrtF = 1;
                        Param = param[1];
                        break;
                    case "u":
                        PrtF = 2;
                        Param = param[1];
                        break;
                    case "i":
                        PrtF = 3;
                        Param = param[1];
                        break;
                    case "l":
                        try {
                            lineSet = Integer.parseInt( param[1] );
                        } catch ( NumberFormatException e ) {
                            lineSet = 30;
                        }
                        break;
                    case "full":
                        Tools.Prt( p, Utility.ReplaceString( config.LogFull() ), consoleMode.full, programCode );
                        FullFlag = true;
                        break;
                    default:
                        Tools.Prt( p, Utility.ReplaceString( config.ArgsErr() ), consoleMode.full, programCode );
                        return false;
                }
            }

            switch ( PrtF ) {
                case 0:
                    DBA.LogPrint( p, ( sender instanceof Player ) ? 15:30, FullFlag );
                    break;
                case 1:
                case 2:
                case 3:
                    DBA.exLogPrint( p, Param, FullFlag, PrtF, lineSet );
                    break;
                default:
                    Tools.Prt( p, Utility.ReplaceString( config.OptError() ), consoleMode.full, programCode );
                    return false;
            }
            return true;
        }

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "ping" ) ) {
            if ( args.length > 0 ) {
                Inet4Address inet;
                try {
                    inet = ( Inet4Address ) Inet4Address.getByName( args[0] );
                    String msg = "Check Ping is " + inet.getHostName();
                    Tools.Prt( p, msg, checkConsoleFlag, programCode );
                } catch (UnknownHostException ex) {
                    Tools.Prt( p, ChatColor.RED + "Ping Unknown Host : " + ex.getMessage(), checkConsoleFlag, programCode );
                }
                return true;
            }
        }

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
                        config = new Config( this );
                        Tools.Prt( p, Utility.ReplaceString( config.Reload() ), checkConsoleFlag, programCode );
                        return true;
                    case "Dupcheck":
                        DBA.DuplicateCheck( p );
                        return true;
                    case "CheckIP":
                        Config.CheckIPAddress = !Config.CheckIPAddress;
                        Tools.Prt( p,
                            ChatColor.GREEN + "Unknown IP Address Check Change to " +
                            ChatColor.YELLOW + ( Config.CheckIPAddress ? "True":"False" ),
                            checkConsoleFlag,
                            programCode
                        );
                        return true;
                    case "Console":
                        Tools.setDebug( IP, programCode );
                        Tools.Prt( p,
                            ChatColor.GREEN + "System Debug Mode is [ " +
                            ChatColor.RED + Tools.consoleFlag.get( programCode ) +
                            ChatColor.GREEN + " ]",
                            checkConsoleFlag,
                            programCode
                        );
                        return true;
                    case "Getlog":
                        FileRead.GetLogFile( IP, DBA );
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
                        DBA.SQLCommand( p, SQL_Cmd );
                        return true;
                    case "chg":
                        if ( HostName.length() < 61 ) {
                            if ( DBA.chgUnknownHost( IP, HostName ) ) {
                                DBA.infoUnknownHost( p, IP );
                            }
                        } else {
                            Tools.Prt( p, ChatColor.RED + "Hostname is limited to 60 characters", checkConsoleFlag, programCode );
                        }
                        return true;
                    case "info":
                        if ( !IP.equals( "" ) ) {
                            Tools.Prt( p, "Check Unknown IP Information [" + IP + "]", checkConsoleFlag, programCode );
                            DBA.infoUnknownHost( p, IP );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: info IPAddress", checkConsoleFlag, programCode );
                        }
                        return true;
                    case "add":
                        if ( !IP.equals( "" ) ) {
                            if ( DBA.GetHost( IP ).equals( "Unknown" ) ) {
                                if ( !HostName.equals( "" ) ) {
                                    DBA.AddHostToSQL( IP, HostName );
                                } else {
                                    Tools.Prt( p, ChatColor.RED + " Host name is required", checkConsoleFlag, programCode );
                                }
                            } else {
                                Tools.Prt( p, ChatColor.RED + IP + " is already exists", checkConsoleFlag, programCode );
                            }
                            DBA.infoUnknownHost( p, IP );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: add IPAddress [HostName]", checkConsoleFlag, programCode );
                        }
                        return true;
                    case "del":
                        if ( !IP.equals( "" ) ) {
                            if ( DBA.DelHostFromSQL( IP ) ) {
                                msg = ChatColor.GREEN + "Data Deleted [";
                            } else {
                                msg = ChatColor.RED + "Failed to Delete Data [";
                            }
                            Tools.Prt( p, msg + IP + "]", checkConsoleFlag, programCode );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: del IPAddress", checkConsoleFlag, programCode );
                        }
                        return true;
                    case "count":
                        if ( HostName.equals( "Reset" ) ) HostName = "-1";
                        DBA.AddCountHost( IP, Integer.parseInt( HostName ) );
                        DBA.infoUnknownHost( p, IP );
                        return true;
                    case "search":
                        if ( !IP.equals( "" ) ) {
                            DBA.SearchHost( p, IP );
                        } else {
                            Tools.Prt( p, ChatColor.RED + "usage: search word", checkConsoleFlag, programCode );
                        }
                        return true;
                    case "pingtop":
                        int PTLines;
                        try {
                            PTLines = Integer.parseInt( IP );
                        } catch ( NumberFormatException e ) {
                            Tools.Prt( p, ChatColor.RED + "Please specify an integer", checkConsoleFlag, programCode );
                            PTLines = 10;
                        }
                        if ( PTLines < 1 ) { PTLines = 10; }
                        DBA.PingTop( p, PTLines );
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
        if ( config.PlayerKick() ) {
            String msg = Utility.ReplaceString( config.KickMessage(), event.getPlayer().getDisplayName() );
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
        if ( config.DeathMessageFlag() ) {
            Tools.Prt( Utility.StringBuild( "DeathMessage: ", event.getDeathMessage() ), consoleMode.full, programCode );
            Tools.Prt( Utility.StringBuild( "DisplayName : ", event.getEntity().getDisplayName() ), consoleMode.full, programCode );
            if ( event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent ) {
                EntityDamageByEntityEvent lastcause = ( EntityDamageByEntityEvent ) event.getEntity().getLastDamageCause();
                Entity entity = lastcause.getDamager();
                Tools.Prt( Utility.StringBuild( "Killer Name : ", entity.getName() ), consoleMode.full, programCode );
                String msg = config.DeathMessage( entity.getName().toUpperCase() );
                msg = Utility.ReplaceString( msg, event.getEntity().getDisplayName() );
                msg = msg.replace( "%mob%", entity.getName() );
                //event.setDeathMessage( null );
                Bukkit.broadcastMessage( msg );
            } else {
                Tools.Prt( "Other Death", consoleMode.normal, programCode );
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
