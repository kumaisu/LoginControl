/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getWorld;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author sugichan
 */
public class LoginControl extends JavaPlugin implements Listener {

    private Config config;
    private Date date;
    private StatusRecord StatRec;
    private MotDControl MotData;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents( this, this );
        config = new Config( this );
        MotData = new MotDControl( this, config );
        StatRec = new StatusRecord( config.getHost(), config.getDatabase(), config.getPort(), config.getUsername(), config.getPassword(), config.getKumaisu() );
    }

    @Override
    public void onDisable() {
        super.onDisable(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onLoad() {
        super.onLoad(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * コンソール表示のみを実施する場合で、デバッグフラグをチェックしてからするもの
     *
     * @param msg
     * @param mode
     */
    public void consolePrint( String msg, Utility.consoleMode mode ) {
        if ( config.isDebugFlag( mode ) ) {
            Bukkit.getServer().getConsoleSender().sendMessage( msg );
        }
    }

    /**
     * プレイヤーがログインしようとした時に起きるイベント
     * BANなどされていてもこのイベントは発生する
     *
     * @param event
     */
    @EventHandler
    public void prePlayerLogin( AsyncPlayerPreLoginEvent event ) {
        consolePrint( "PrePlayerLogin process", Utility.consoleMode.full );
        date = new Date();
        StatRec.listPreSave( date, event.getName(), event.getUniqueId().toString(), event.getAddress().getHostAddress(), 0 );
        StatRec.AddPlayerToSQL( event.getAddress().getHostAddress(), event.getName() );
    }

    /**
     * プレイヤーがログインを成功すると発生するイベント
     * ここでプレイヤーに対して、様々な処理を実行する
     *
     * @param event
     * @throws UnknownHostException
     */
    @EventHandler
    public void onPlayerLogin( PlayerJoinEvent event ) throws UnknownHostException {

        consolePrint( "onPlayerLogin process", Utility.consoleMode.full );
        event.setJoinMessage( null );
        Player player = event.getPlayer();
        StatRec.listChangeStatus( date, 1 );
        StatRec.LogPrint( player, 5, false, config.getIgnoreName() );
        StatRec.AddCountHost( player.getAddress().getHostString(), -1 );
        StatRec.listCheckIP( player, config.isDebugFlag( Utility.consoleMode.normal ) );

        if ( !player.hasPlayedBefore() || config.OpJump( player.isOp() ) ) {
            // Utility.Prt( null, ChatColor.LIGHT_PURPLE + "The First Login Player", true );
            consolePrint( ChatColor.LIGHT_PURPLE + "The First Login Player", Utility.consoleMode.none );

            List<String> present = config.getPresent();
            present.stream().forEach( PR -> {
                String[] itemdata = PR.split( ",", 0 );
                player.getInventory().addItem( new ItemStack( Material.getMaterial( itemdata[0] ), Integer.parseInt( itemdata[1] ) ) );
                consolePrint( ChatColor.AQUA + "Present Item : " + ChatColor.WHITE + itemdata[0] + "(" + itemdata[1] + ")", Utility.consoleMode.full );
            } );

            if ( config.getJump() ) {
                consolePrint( "This player is first play to teleport", Utility.consoleMode.normal );
                World world = getWorld( config.getWorld() );
                Location loc = new Location( world, config.getX(), config.getY(), config.getZ() );
                loc.setPitch( config.getPitch() );
                loc.setYaw( config.getYaw() );
                player.teleport( loc );
            }

            if( config.NewJoin() ) {
                String msg = StatRec.GetLocale( player.getAddress().getHostString(), config.isDebugFlag( Utility.consoleMode.normal ) );
                consolePrint( "Player host = " + player.getAddress().getHostString(), Utility.consoleMode.normal );
                consolePrint( "Get Locale = " + msg, Utility.consoleMode.normal );
                Bukkit.broadcastMessage( Utility.ReplaceString( config.NewJoinMessage( msg ), player.getDisplayName() ) );
            }

        } else {
            consolePrint( "The Repeat Login Player", Utility.consoleMode.none );
            if( config.ReturnJoin() && !player.hasPermission( "LoginCtl.silentjoin" ) ) {
                Bukkit.broadcastMessage( Utility.ReplaceString( config.ReturnJoinMessage( StatRec.GetLocale( player.getAddress().getHostString(), config.isDebugFlag( Utility.consoleMode.full ) ) ), player.getDisplayName() ) );
            }
        }

        if ( config.Announce() ) {
            player.sendMessage( Utility.ReplaceString( config.AnnounceMessage(), player.getDisplayName() ).split( "/n" ) );
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
	Utility.consoleMode PrtStatus = Utility.consoleMode.full;

        String MotdMsg = MotData.get1stLine();
        String MsgColor = ChatColor.GRAY.toString();
        String Host = config.KnownServers( event.getAddress().getHostAddress() );

        if ( Host == null ) {
            //  簡易DNSからホスト名を取得
            Host = StatRec.GetHost( event.getAddress().getHostAddress() );
            int MsgNum = 0;
            if ( Host.equals( "Unknown" ) ) {
                //  DBに該当なしなので、DB登録
                //  ホスト名が取得できなかった場合は、Unknown Player を File に記録し、新規登録
                MsgColor = ChatColor.RED.toString();
                Host = StatRec.getUnknownHost( event.getAddress().getHostAddress(), config.getCheckIP(), config.isDebugFlag( Utility.consoleMode.full ) );
                //  新規ホストとして、Unknown.yml ファイルへ書き出し
                StatRec.WriteFileUnknown( event.getAddress().getHostAddress(), this.getDataFolder().toString() );
            } else {
                //  未知のホスト名の場合は LIGHT_PURPLE , 既知のPlayerだった場合は WHITE になる
                if ( Host.contains( "Player" ) ) {
                    MsgColor = ChatColor.WHITE.toString();
                    //  簡易DNSにプレイヤー登録されている場合は、ログイン履歴を参照して最新のプレイヤー名を取得する
                    Names = StatRec.listGetPlayerName( event.getAddress().getHostAddress() );
                    MsgNum = 2;
                    PrtStatus = Utility.consoleMode.normal;
                } else {
                    MsgColor = ChatColor.LIGHT_PURPLE.toString();
                }
            }
            StatRec.AddCountHost( event.getAddress().getHostAddress(), 0 );

            int count = StatRec.GetcountHosts( event.getAddress().getHostAddress() );
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
                Motd2ndLine = Motd2ndLine.replace( "%date", StatRec.getDateHost( event.getAddress().getHostAddress(), true ) );
                MotdMsg = Utility.StringBuild( MotdMsg, Motd2ndLine );
                consolePrint( Utility.StringBuild( "MotD = ", Utility.ReplaceString( Motd2ndLine, Names ) ), Utility.consoleMode.full );
            } else {
                MotdMsg = Motd2ndLine;
                consolePrint( Utility.StringBuild( "Change = ", Utility.ReplaceString( Motd2ndLine.replace( "\n", " " ), Names ) ), Utility.consoleMode.full );
            }

        } else {
            //  Configに既知のホスト登録があった場合
            MotdMsg = Utility.StringBuild( MotdMsg, MotData.get2ndLine( 4 ) );
        }

        event.setMotd( Utility.ReplaceString( MotdMsg, Names ) );
        // event.getNumPlayers().set( 30 );

        String msg = Utility.StringBuild( ChatColor.GREEN.toString(), "Ping from ", MsgColor, Host, ChatColor.YELLOW.toString(), " [", event.getAddress().getHostAddress(), "]" );
        consolePrint( msg, PrtStatus );
        Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
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
        boolean FullFlag = false;
        int lineSet = 30;
        Player p = ( sender instanceof Player ) ? ( Player )sender:( Player )null;
        boolean checkConsoleFlag;
        checkConsoleFlag = ( config.isDebugFlag( Utility.consoleMode.normal ) || ( p == null ) );

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "flight" ) ) {
            if ( p == null ) return false;
            for ( String arg:args ) {
                switch ( arg ) {
                    case "on":
                        FlightMode( p, true );
                        break;
                    case "off":
                        FlightMode( p, false );
                        break;
                    default:
                        Utility.Prt( p, ChatColor.GREEN + "Fly (on/off)", config.isDebugFlag( Utility.consoleMode.normal ) );
                }
            }
            return true;
        }

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "loginlist" ) ) {
            int PrtF = 0;
            String Param = "";

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
                        Utility.Prt( p, Utility.ReplaceString( config.LogFull() ),config.isDebugFlag( Utility.consoleMode.full ) );
                        FullFlag = true;
                        break;
                    default:
                        Utility.Prt( p, Utility.ReplaceString( config.ArgsErr() ),config.isDebugFlag( Utility.consoleMode.full ) );
                        return false;
                }
            }

            switch ( PrtF ) {
                case 0:
                    StatRec.LogPrint( p, ( sender instanceof Player ) ? 15:30, FullFlag, config.getIgnoreName() );
                    break;
                case 1:
                case 2:
                case 3:
                    StatRec.exLogPrint( p, Param, FullFlag, config.getIgnoreName(), config.getIgnoreIP(), PrtF, lineSet );
                    break;
                default:
                    Utility.Prt( p, Utility.ReplaceString( config.OptError() ),config.isDebugFlag( Utility.consoleMode.full ) );
                    return false;
            }
            return true;
        }

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "ping" ) ) {
            if ( args.length > 0 ) {
                try {
                    String msg = "Check Ping is " + StatRec.ping( args[0] );
                    Utility.Prt( p, msg, checkConsoleFlag );
                    return true;
                } catch ( UnknownHostException ex ) {
                    Utility.Prt( p, ChatColor.RED + "Ping Unknown Host.", checkConsoleFlag );
                }
            }
        }

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "loginctl" ) ) {
            String msg;
            String IP = "127.0.0.0";
            String HostName = "";
            String CtlCmd = "None";

            if ( args.length > 0 ) CtlCmd = args[0];
            if ( args.length > 1 ) IP = args[1];
            if ( args.length > 2 ) HostName = args[2];

            switch ( CtlCmd ) {
                case "reload":
                    config = new Config( this );
                    Utility.Prt( p, Utility.ReplaceString( config.Reload() ), checkConsoleFlag );
                    return true;
                case "status":
                    config.Status( p );
                    return true;
                case "motd":
                    MotData.getStatus( p );
                    return true;
                case "chg":
                    if ( HostName.length() < 61 ) {
                        if ( StatRec.chgUnknownHost( IP, HostName ) ) {
                            StatRec.infoUnknownHost( p, IP );
                        }
                    } else {
                        Utility.Prt( p, ChatColor.RED + "Hostname is limited to 60 characters", checkConsoleFlag );
                    }
                    break;
                case "info":
                    if ( !IP.equals( "" ) ) {
                        Utility.Prt( p, "Check Unknown IP Information [" + IP + "]", checkConsoleFlag );
                        StatRec.infoUnknownHost( p, IP );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: info IPAddress", checkConsoleFlag );
                    }
                    break;
                case "add":
                    if ( !IP.equals( "" ) ) {
                        if ( StatRec.GetHost( IP ).equals( "Unknown" ) ) {
                            if ( !HostName.equals( "" ) ) {
                                StatRec.AddHostToSQL( IP, HostName );
                            } else {
                                Utility.Prt( p, ChatColor.RED + " Host name is required", checkConsoleFlag );
                            }
                        } else {
                            Utility.Prt( p, ChatColor.RED + IP + " is already exists", checkConsoleFlag );
                        }
                        StatRec.infoUnknownHost( p, IP );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: add IPAddress [HostName]", checkConsoleFlag );
                    }
                    break;
                case "del":
                    if ( !IP.equals( "" ) ) {
                        if ( StatRec.DelHostFromSQL( IP ) ) {
                            msg = ChatColor.GREEN + "Data Deleted [";
                        } else {
                            msg = ChatColor.RED + "Failed to Delete Data [";
                        }
                        Utility.Prt( p, msg + IP + "]", checkConsoleFlag );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: del IPAddress", checkConsoleFlag );
                    }
                    break;
                case "count":
                    {
                        if ( HostName.equals( "Reset" ) ) HostName = "-1";

                        try {
                            StatRec.AddCountHost( IP, Integer.parseInt( HostName ) );
                        } catch ( UnknownHostException ex ) {
                            Utility.Prt( p, ChatColor.RED + ex.getMessage(), checkConsoleFlag );
                        }

                        StatRec.infoUnknownHost( p, IP );
                    }
                    break;
                case "search":
                    if ( !IP.equals( "" ) ) {
                        StatRec.SearchHost( p, IP );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: search word", checkConsoleFlag );
                    }
                    break;
                case "pingtop":
                    int PTLines;
                    try {
                        PTLines = Integer.parseInt( IP );
                    } catch ( NumberFormatException e ) {
                        Utility.Prt( p, ChatColor.RED + "Please specify an integer", checkConsoleFlag );
                        PTLines = 10;
                    }
                    if ( PTLines < 1 ) { PTLines = 10; }
                    StatRec.PingTop( p, PTLines );
                    break;
                case "CheckIP":
                    config.setCheckIP( !config.getCheckIP() );
                    Utility.Prt( p,
                        ChatColor.GREEN + "Unknown IP Address Check Change to " +
                        ChatColor.YELLOW + ( config.getCheckIP() ? "True":"False" ), checkConsoleFlag
                    );
                    break;
                case "Console":
                    config.setDebug( IP );
                    Utility.Prt( p,
                        ChatColor.GREEN + "System Debug Mode is [ " +
                        ChatColor.RED + config.getDebug().toString() +
                        ChatColor.GREEN + " ]", checkConsoleFlag
                    );
                    break;
                case "Convert":
                    StatRec.convertHostName( p );
                    break;
                default:
                    return false;
            }
            return true;
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
            String msg = Utility.ReplaceString( config.KickMessage(),event.getPlayer().getDisplayName() );
            if ( !event.getReason().equals( "" ) ) {
                msg = msg.replace( "%Reason%", event.getReason() );
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
            consolePrint( Utility.StringBuild( "DeathMessage: ", event.getDeathMessage() ), Utility.consoleMode.full );
            consolePrint( Utility.StringBuild( "DisplayName : ", event.getEntity().getDisplayName() ), Utility.consoleMode.full );
            if ( event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent ) {
                EntityDamageByEntityEvent lastcause = ( EntityDamageByEntityEvent ) event.getEntity().getLastDamageCause();
                Entity entity = lastcause.getDamager();
                consolePrint( Utility.StringBuild( "Killer Name : ", entity.getName() ), Utility.consoleMode.full );
                String msg = config.DeathMessage( entity.getName().toUpperCase() );
                msg = Utility.ReplaceString( msg, event.getEntity().getDisplayName() );
                msg = msg.replace( "%mob%", entity.getName() );
                //event.setDeathMessage( null );
                Bukkit.broadcastMessage( msg );
            } else {
                consolePrint( "Other Death", Utility.consoleMode.normal );
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
     * プレイヤーのフライを設定または解除する
     *
     * @param p
     * @param flag
     */
    public void FlightMode( Player p, boolean flag ) {
        if ( flag ) {
            Utility.Prt( p, Utility.StringBuild( ChatColor.AQUA.toString(), "You can FLY !!" ), config.isDebugFlag( Utility.consoleMode.normal ) );
            // 飛行許可
            p.setAllowFlight( true );
            p.setFlySpeed( 0.1F );
        } else {
            Utility.Prt( p, Utility.StringBuild( ChatColor.LIGHT_PURPLE.toString(), "Stop your FLY Mode." ), config.isDebugFlag( Utility.consoleMode.normal ) );
            // 無効化
            p.setFlying( false );
            p.setAllowFlight( false );
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
        if ( !p.hasPermission( "LoginCtl.flight" ) ) FlightMode( p, false );
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
        if ( material == Material.SIGN_POST || material == Material.WALL_SIGN ) {
            Sign sign = (Sign) clickedBlock.getState();
            if ( sign.getLine( 0 ).equals( "[TrashCan]" ) ) {
                Inventory inv;
                inv = Bukkit.createInventory( null, 36, "Trash Can" );
                player.openInventory( inv );
            }
        }
    }
}
