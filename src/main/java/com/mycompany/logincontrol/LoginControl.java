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

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents( this, this );
        config = new Config( this );
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

    @EventHandler
    public void prePlayerLogin( AsyncPlayerPreLoginEvent event ) {
        Utility.Prt( null, "PrePlayerLogin process", config.DBFlag( 2 ) );
        date = new Date();
        StatRec.PreSavePlayer( date, event.getName(), event.getUniqueId().toString(), event.getAddress().getHostAddress(), 0 );
        StatRec.AddPlayerToSQL( event.getAddress().getHostAddress(), event.getName() );
    }

    @EventHandler
    public void onPlayerLogin( PlayerJoinEvent event ) throws UnknownHostException {

        Utility.Prt( null, "onPlayerLogin process", config.DBFlag( 2 ) );
        event.setJoinMessage( null );
        Player player = event.getPlayer();
        StatRec.ChangeStatus( date, 1 );
        StatRec.LogPrint( player, 5, false, config.getIgnoreName() );
        StatRec.AddCountHost( player.getAddress().getHostString(), -1 );
        StatRec.CheckIP( player, config.DBFlag( 1 ) );

        if ( ( config.getJump() ) && ( ( !player.hasPlayedBefore() ) || config.OpJump( player.isOp() ) ) ) {
            Utility.Prt( null, Utility.StringBuild( ChatColor.LIGHT_PURPLE.toString(), "The First Login Player" ), true );

            List<String> present = config.getPresent();
            present.stream().forEach( PR -> {
                String[] itemdata = PR.split( ",", 0 );
                player.getInventory().addItem( new ItemStack( Material.getMaterial( itemdata[0] ), Integer.parseInt( itemdata[1] ) ) );
                Utility.Prt( null, Utility.StringBuild( ChatColor.AQUA.toString(), "Present Item : ", ChatColor.WHITE.toString(), itemdata[0], "(", itemdata[1], ")" ), config.DBFlag( 2 ) );
            } );

            Utility.Prt( null, "This player is first play to teleport", config.DBFlag( 1 ) );
            World world = getWorld( config.getWorld() );
            Location loc = new Location( world, config.getX(), config.getY(), config.getZ() );
            loc.setPitch( config.getPitch() );
            loc.setYaw( config.getYaw() );
            player.teleport( loc );

            if( config.NewJoin() ) {
                String msg = StatRec.GetLocale( player.getAddress().getHostString(), config.DBFlag( 1 ) );
                Utility.Prt( null, Utility.StringBuild( "Player host = ", player.getAddress().getHostString() ), config.DBFlag( 1 ) );
                Utility.Prt( null, Utility.StringBuild( "Get Locale = ", msg ), config.DBFlag( 1 ) );
                Bukkit.broadcastMessage( Utility.ReplaceString( config.NewJoinMessage( msg ), player.getDisplayName() ) );
            }

        } else {
            Utility.Prt( null, "The Repeat Login Player", true );
            if( config.ReturnJoin() && !player.hasPermission( "LoginCtl.silentjoin" ) ) {
                Bukkit.broadcastMessage( Utility.ReplaceString( config.ReturnJoinMessage( StatRec.GetLocale( player.getAddress().getHostString(), config.DBFlag( 2 ) ) ), player.getDisplayName() ) );
            }
        }

        if ( config.Announce() ) {
            player.sendMessage( Utility.ReplaceString( config.AnnounceMessage(), player.getDisplayName() ).split( "/n" ) );
        }
    }

    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event ) {
        if ( event.getPlayer().hasPermission( "LoginCtl.silentquit" ) ) {
            event.setQuitMessage( null );
            return;
        }
        if ( config.PlayerQuti() ) {
            event.setQuitMessage( Utility.ReplaceString( config.PlayerQuitMessage(), event.getPlayer().getDisplayName() ) );
        }
    }

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

    @EventHandler
    public void onPlayerDeath( PlayerDeathEvent event ) {
        if ( config.DeathMessageFlag() ) {
            Utility.Prt( null, Utility.StringBuild( "DeathMessage: ", event.getDeathMessage() ), config.DBFlag( 2 ) );
            Utility.Prt( null, Utility.StringBuild( "DisplayName : ", event.getEntity().getDisplayName() ), config.DBFlag( 2 ) );
            if ( event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent ) {
                EntityDamageByEntityEvent lastcause = ( EntityDamageByEntityEvent ) event.getEntity().getLastDamageCause();
                Entity entity = lastcause.getDamager();
                Utility.Prt( null, Utility.StringBuild( "Killer Name : ", entity.getName() ), config.DBFlag( 2 ) );
                String msg = config.DeathMessage( entity.getName().toUpperCase() );
                msg = Utility.ReplaceString( msg, event.getEntity().getDisplayName() );
                msg = msg.replace( "%mob%", entity.getName() );
                //event.setDeathMessage( null );
                Bukkit.broadcastMessage( msg );
            } else {
                Utility.Prt( null, "Other Death", config.DBFlag( 1 ) );
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

    @EventHandler
    public void onServerListPing( ServerListPingEvent event ) throws UnknownHostException, ClassNotFoundException {
        String Names = "Unknown";   // = StatRec.GetPlayerName( event.getAddress().getHostAddress() );
        String Host;                // = ChatColor.WHITE + "Player(" + Names + ")";
	int PrtStatus = 2;          // ConsoleLog Flag 2:Full 1:Normal(Playerのみ)

        String MotdMsg = Utility.Replace( config.get1stLine() );
        String MsgColor;

        String ChkHost = config.KnownServers( event.getAddress().getHostAddress() );
        if ( ChkHost == null ) {
            //  簡易DNSからホスト名を取得
            Host = StatRec.GetHost( event.getAddress().getHostAddress() );
            int MsgNum = 0;
            if ( Host.equals( "Unknown" ) ) {
                //  DBに該当なしなので、DB登録
                //  ホスト名が取得できなかった場合は、Unknown Player を File に記録し、新規登録
                MsgColor = ChatColor.RED.toString();
                Host = StatRec.getUnknownHost( event.getAddress().getHostAddress(), config.getCheckIP(), config.DBFlag( 2 ) );
                //  新規ホストとして、Unknown.yml ファイルへ書き出し
                StatRec.WriteFileUnknown( event.getAddress().getHostAddress(), this.getDataFolder().toString() );
            } else {
                //  未知のホスト名の場合は LIGHT_PURPLE , 既知のPlayerだった場合は WHITE になる
                if ( Host.contains( "Player" ) ) {
                    MsgColor = ChatColor.WHITE.toString();
                    //  簡易DNSにプレイヤー登録されている場合は、ログイン履歴を参照して最新のプレイヤー名を取得する
                    Names = StatRec.GetPlayerName( event.getAddress().getHostAddress() );
                    MsgNum = 2;
                    PrtStatus = 1;
                } else {
                    MsgColor = ChatColor.LIGHT_PURPLE.toString();
                }
            }
            StatRec.AddCountHost( event.getAddress().getHostAddress(), 0 );

            int count = StatRec.GetcountHosts( event.getAddress().getHostAddress() );
            Host = Utility.StringBuild( Host, "(", String.valueOf( count ), ")" );

            if ( ( config.getmotDCount() != 0 ) && ( count>config.getmotDCount() ) ) MsgNum++;
            if ( ( config.getmotDMaxCount() != 0 ) && ( count>config.getmotDMaxCount() ) ) MsgNum = 1;
            MotdMsg = Utility.StringBuild( MotdMsg, config.get2ndLine( MsgNum ) );
            if ( MsgNum == 1 ) {
                MotdMsg = MotdMsg.replace( "%count", String.valueOf( count ) );
                MotdMsg = MotdMsg.replace( "%date", StatRec.getDateHost( event.getAddress().getHostAddress(), true ) );
                //  True : カウントを開始した日を指定
                //  False : 最後にカウントされた日を指定
            }
            event.setMotd( Utility.ReplaceString( MotdMsg, Names ) );
        } else {
            //  Configに既知のホスト登録があった場合
            MsgColor = ChatColor.GRAY.toString();
            Host = ChkHost;
            MotdMsg = Utility.StringBuild( MotdMsg, config.get2ndLine( 4 ) );
            event.setMotd( MotdMsg );
        }

        String msg = Utility.StringBuild( ChatColor.GREEN.toString(), "Ping from ", MsgColor, Host, ChatColor.YELLOW.toString(), " [", event.getAddress().getHostAddress(), "]" );
        Utility.Prt( null, msg, config.DBFlag( PrtStatus ) );
        Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
    }

    @Override
    public boolean onCommand( CommandSender sender,Command cmd, String commandLabel, String[] args ) {
        boolean FullFlag = false;
        Player p = ( sender instanceof Player ) ? ( Player )sender:( Player )null;

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
                        Utility.Prt( p, ChatColor.GREEN + "Fly (on/off)", config.DBFlag( 1 ) );
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
                        PrtF = ( config.getIgnoreName().contains( Param ) ? -1 : 2 );
                        Param = param[1];
                        break;
                    case "i":
                        PrtF = ( config.getIgnoreIP().contains( Param ) ? -1 : 3 );
                        Param = param[1];
                        break;
                    case "full":
                        Utility.Prt( (Player)sender, Utility.Replace( config.LogFull() ),config.DBFlag( 2 ) );
                        FullFlag = true;
                        break;
                    default:
                        Utility.Prt( (Player)sender, Utility.Replace( config.ArgsErr() ),config.DBFlag( 2 ) );
                        return false;
                }
            }

            switch ( PrtF ) {
                case 0:
                    StatRec.LogPrint( p, ( sender instanceof Player ) ? 15:30, FullFlag, config.getIgnoreName() );
                    break;
                case 1:
                    StatRec.DateLogPrint( p, Param, FullFlag, config.getIgnoreName() );
                    break;
                case 2:
                case 3:
                    StatRec.NameLogPrint( p, Param, FullFlag, PrtF );
                    break;
                default:
                    Utility.Prt( (Player)sender, Utility.Replace( config.OptError() ),config.DBFlag( 2 ) );
                    return false;
            }
            return true;
        }

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "ping" ) ) {
            if ( args.length > 0 ) {
                try {
                    String msg = "Check Ping is " + StatRec.ping( args[0] );
                    Utility.Prt( p, msg, ( p == null ) );
                    return true;
                } catch ( UnknownHostException ex ) {
                    Utility.Prt( p, ChatColor.RED + "Ping Unknown Host.", ( p == null ) );
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
                    Utility.Prt( (Player)sender, Utility.Replace( config.Reload() ), true );
                    return true;
                case "status":
                    config.Status( ( sender instanceof Player ) ? ( Player )sender:null );
                    return true;
                case "chg":
                    if ( HostName.length() < 61 ) {
                        if ( StatRec.chgUnknownHost( IP, HostName ) ) {
                            StatRec.infoUnknownHost( p, IP );
                        }
                    } else {
                        Utility.Prt( p, ChatColor.RED + "Hostname is limited to 60 characters", true );
                    }
                    break;
                case "info":
                    if ( !IP.equals( "" ) ) {
                        Utility.Prt( p, "Check Unknown IP Information [" + IP + "]", ( p == null ) );
                        StatRec.infoUnknownHost( p, IP );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: info IPAddress", ( p == null ) );
                    }
                    break;
                case "add":
                    if ( !IP.equals( "" ) ) {
                        if ( StatRec.GetHost( IP ).equals( "Unknown" ) ) {
                            if ( !HostName.equals( "" ) ) {
                                StatRec.AddHostToSQL( IP, HostName );
                            } else {
                                Utility.Prt( p, ChatColor.RED + " Host name is required", true );
                            }
                        } else {
                            Utility.Prt( p, ChatColor.RED + IP + " is already exists", true );
                        }
                        StatRec.infoUnknownHost( p, IP );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: add IPAddress [HostName]", true );
                    }
                    break;
                case "del":
                    if ( !IP.equals( "" ) ) {
                        if ( StatRec.DelHostFromSQL( IP ) ) {
                            msg = ChatColor.GREEN + "Data Deleted [";
                        } else {
                            msg = ChatColor.RED + "Failed to Delete Data [";
                        }
                        Utility.Prt( p, msg + IP + "]", true );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: del IPAddress", true );
                    }
                    break;
                case "count":
                    {
                        if ( HostName.equals( "Reset" ) ) HostName = "-1";

                        try {
                            StatRec.AddCountHost( IP, Integer.parseInt( HostName ) );
                        } catch ( UnknownHostException ex ) {
                            Utility.Prt( p, ChatColor.RED + ex.getMessage(), true );
                        }

                        StatRec.infoUnknownHost( p, IP );
                    }
                    break;
                case "search":
                    if ( !IP.equals( "" ) ) {
                        StatRec.SearchHost( p, IP );
                    } else {
                        Utility.Prt( p, ChatColor.RED + "usage: search word", ( p == null ) );
                    }
                    break;
                case "pingtop":
                    int PTLines;
                    try {
                        PTLines = Integer.parseInt( IP );
                    } catch ( NumberFormatException e ) {
                        Utility.Prt( p, ChatColor.RED + "Please specify an integer", true );
                        PTLines = 10;
                    }
                    if ( PTLines < 1 ) { PTLines = 10; }
                    StatRec.PingTop( p, PTLines );
                    break;
                case "CheckIP":
                    config.setCheckIP( !config.getCheckIP() );
                    Utility.Prt( p,
                        ChatColor.GREEN + "Unknown IP Address Check Change to " +
                        ChatColor.YELLOW + ( config.getCheckIP() ? "True":"False" ), true
                    );
                    break;
                case "Console":
                    switch ( IP ) {
                        case "full":
                            config.setDebug( 2 );
                            break;
                        case "normal":
                            config.setDebug( 1 );
                            break;
                        case "none":
                            config.setDebug( 0 );
                            break;
                        default:
                            Utility.Prt( p, "usage: loginctl Console [full/normal/none]", ( p == null ) );
                    }
                    Utility.Prt( p, 
                        ChatColor.GREEN + "System Debug Mode is [ " +
                        ChatColor.RED + config.DBString( config.getDebug() ) +
                        ChatColor.GREEN + " ]", ( p == null )
                    );
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

    public void FlightMode( Player p, boolean flag ) {
        if ( flag ) {
            Utility.Prt( p, Utility.StringBuild( ChatColor.AQUA.toString(), "You can FLY !!" ), config.DBFlag( 1 ) );
            // 飛行許可
            p.setAllowFlight( true );
            p.setFlySpeed( 0.1F );
        } else {
            Utility.Prt( p, Utility.StringBuild( ChatColor.LIGHT_PURPLE.toString(), "Stop your FLY Mode." ), config.DBFlag( 1 ) );
            // 無効化
            p.setFlying( false );
            p.setAllowFlight( false );
        }
    }

    @EventHandler
    public void onFlight( PlayerToggleFlightEvent event ) {
        Player p = event.getPlayer();
        // p.sendMessage( "Catch Flight mode " + p.getDisplayName() );
        if ( !p.hasPermission( "LoginCtl.flight" ) ) FlightMode( p, false );
    }

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
