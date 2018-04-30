/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/*
import com.mcbans.firestar.mcbans.MCBans;
import com.mcbans.firestar.mcbans.api.MCBansAPI;
import com.mcbans.firestar.mcbans.api.data.PlayerLookupData;
import com.mcbans.firestar.mcbans.callBacks.LookupCallback;
*/
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
        StatRec = new StatusRecord( config.getHost(), config.getDB(), config.getPort(), config.getUsername(), config.getPassword(), this );
    }

    @Override
    public void onDisable() {
        super.onDisable(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onLoad() {
        super.onLoad(); //To change body of generated methods, choose Tools | Templates.
    }

    public String ReplaceString( String data, String Names ) {
        String RetStr;
        RetStr = data.replace( "%player%", Names );
        RetStr = RetStr.replace( "%$", "§" );
        
        return RetStr;
    }
    
    public void Prt( Player player, String msg ) {
        Bukkit.getServer().getConsoleSender().sendMessage( msg );
        if ( player != null ) player.sendMessage( msg );
    }
    
    public void FlightMode( Player p, boolean flag ) {
        if ( flag ) {
            p.sendMessage( ChatColor.AQUA + "You can FLY !!" );
            // 飛行許可
            p.setAllowFlight( true );
            p.setFlySpeed( 0.1F );
        } else {
            p.sendMessage( ChatColor.LIGHT_PURPLE + "Stop your FLY Mode." );
            // 無効化
            p.setFlying( false );
            p.setAllowFlight( false );
        }
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
                        p.sendMessage( ChatColor.GREEN + "Fly (on/off)" );
                }
            }
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
                    case "full":
                        sender.sendMessage( config.LogFull().replace( "%$", "§" ) );
                        FullFlag = true;
                        break;
                    default:
                        sender.sendMessage( config.ArgsErr().replace( "%$", "§" ) );
                        return false;
                }
            }

            switch ( PrtF ) {
                case 0:
                    StatRec.LogPrint( p, ( sender instanceof Player ) ? 15:30, FullFlag );
                    break;
                case 1:
                    StatRec.DateLogPrint( p, Param, FullFlag );
                    break;
                case 2:
                    StatRec.NameLogPrint( p, Param, FullFlag );
                    break;
                default:
                    sender.sendMessage( config.OptError().replace( "%$", "§" ) );
                    return false;
            }
            return true;
        }
        
        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "ping" ) ) {
            if ( args.length > 0 ) {
                try {
                    String msg = "Check Ping is " + StatRec.ping( args[0] );
                    Prt( p, msg );
                    return true;
                } catch ( UnknownHostException ex ) {
                    Logger.getLogger( LoginControl.class.getName() ).log( Level.SEVERE, null, ex );
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
                    sender.sendMessage( config.Reload().replace( "%$", "§" ) );
                    return true;
                case "status":
                    config.Status( ( sender instanceof Player ) ? ( Player )sender:null );
                    return true;
                case "chg":
                    if ( StatRec.chgUnknownHost( IP, HostName ) ) {
                        StatRec.infoUnknownHost( p, IP );
                    }
                    break;
                case "info":
                    if ( !IP.equals( "" ) ) {
                        Prt( p, "Check Unknown IP Information [" + IP + "]" );
                        StatRec.infoUnknownHost( p, IP );
                    } else {
                        Prt( p, "usage: info IPAddress" );
                    }
                    break;
                case "add":
                    if ( !IP.equals( "" ) ) {
                        if ( StatRec.getUnknownHost( IP ).equals( "Unknown" ) ) {
                            if ( !HostName.equals( "" ) ) {
                                StatRec.AddHostToSQL( IP, HostName );
                            } else {
                                try {
                                    StatRec.setUnknownHost( IP, true );
                                } catch ( UnknownHostException ex ) {
                                    Logger.getLogger( LoginControl.class.getName() ).log( Level.SEVERE, null, ex );
                                }
                            }
                        } else {
                            Prt( p, ChatColor.RED + IP + " is already exists" );
                        }
                        StatRec.infoUnknownHost( p, IP );
                    } else {
                        Prt( p, "usage: add IPAddress [HostName]" );
                    }
                    break;
                case "del":
                    if ( !IP.equals( "" ) ) {
                        if ( StatRec.DelHostFromSQL( IP ) ) {
                            msg = ChatColor.GREEN + "Data Deleted [";
                        } else {
                            msg = ChatColor.RED + "Failed to Delete Data [";
                        }
                        msg += IP + "]";
                        Prt( p, msg );
                    } else {
                        Prt( p, "usage: del IPAddress" );
                    }
                    break;
                case "count":
                    {
                        try {
                            if ( StatRec.countUnknownHost( IP, Integer.parseInt( HostName ) ) > 0 ) StatRec.infoUnknownHost( p, IP );
                        } catch ( UnknownHostException ex ) {
                            Logger.getLogger( LoginControl.class.getName() ).log( Level.SEVERE, null, ex );
                        }
                    }
                    break;
                case "pingtop":
                    StatRec.PingTop( p );
                    break;
                /*
                case "conv":
                    StatRec.DataConv( sender );
                    break;
                */
                case "CheckIP":
                    config.setCheckIP( !config.getCheckIP() );
                    msg = ChatColor.GREEN + "Unknown IP Address Check Change to " + ChatColor.YELLOW + ( config.getCheckIP() ? "True":"False" );
                    Prt( p, msg );
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerLogin( PlayerJoinEvent event ) throws UnknownHostException {
        
        event.setJoinMessage( null );
        Player player = event.getPlayer();
        StatRec.ChangeStatus( date, 1 );
        StatRec.LogPrint( player, 5, false );
        if ( !config.getIgnoreName().contains( player.getName() ) && !config.getIgnoreIP().contains( player.getAddress().getHostString() ) ) {
            StatRec.CheckIP( player );
        }

        if ( ( config.getJump() ) && ( ( !player.hasPlayedBefore() ) || config.OpJump( player.isOp() ) ) ) {
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.LIGHT_PURPLE + "The First Login Player" );

            List<String> present = config.getPresent();
            present.stream().forEach( PR -> {
                String[] itemdata = PR.split( ",", 0 );
                player.getInventory().addItem( new ItemStack( Material.getMaterial( itemdata[0] ), Integer.parseInt( itemdata[1] ) ) );
                Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.AQUA + "Present Item : " + ChatColor.WHITE + itemdata[0] + "(" + itemdata[1] + ")" );
            } );

            // Bukkit.getServer().getConsoleSender().sendMessage( player.getLocale().isEmpty() ? "Location Empty":"First Spawn Location" );

            Bukkit.getServer().getConsoleSender().sendMessage( "This player is first play to teleport");
            World world = getWorld( config.getWorld() );
            Location loc = new Location( world, config.getX(), config.getY(), config.getZ() );
            loc.setPitch( config.getPitch() );
            loc.setYaw( config.getYaw() );
            player.teleport( loc );
            
            if( config.NewJoin() ) {
                //  String[] MsgStr = ReplaceString( config.NewJoinMessage(), player.getDisplayName() ).split( "/n" );
                //  player.sendMessage( MsgStr );
                Bukkit.broadcastMessage( ReplaceString( config.NewJoinMessage(), player.getDisplayName() ) );
            }

        } else {
            Bukkit.getServer().getConsoleSender().sendMessage( "The Repeat Login Player" );
            if( config.ReturnJoin() && !player.hasPermission( "LoginCtl.silentjoin" ) ) {
                //  String[] MsgStr = ReplaceString( config.ReturnJoinMessage(), player.getDisplayName() ).split( "/n" );
                //  player.sendMessage( MsgStr );
                Bukkit.broadcastMessage( ReplaceString( config.ReturnJoinMessage(), player.getDisplayName() ) );
            }
        }
        
        if ( config.Announce() ) {
            String[] MsgStr = ReplaceString( config.AnnounceMessage(), player.getDisplayName() ).split( "/n" );
            player.sendMessage( MsgStr );
        }
                    
    }

    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event ) {
        if ( event.getPlayer().hasPermission( "LoginCtl.silentquit" ) ) {
            event.setQuitMessage( null );
            return;
        }
        if ( config.PlayerQuti() ) {
            event.setQuitMessage( ReplaceString( config.PlayerQuitMessage(), event.getPlayer().getDisplayName() ) );
        }
    }
    
    @EventHandler
    public void onKickMessage( PlayerKickEvent event ) {
        if ( config.PlayerKick() ) {
            String msg = ReplaceString( config.KickMessage(),event.getPlayer().getDisplayName() );
            if ( !event.getReason().equals( "" ) ) {
                msg = msg.replace( "%Reason%", event.getReason() );
            } else {
                msg = msg.replace( "%Reason%", "事由不明" );
            }
            Bukkit.broadcastMessage( msg );
        }
    }
    
    @EventHandler
    public void onPlayerDeath( PlayerDeathEvent event ) {
        if ( config.DeathMessageFlag() ) {
            Bukkit.getServer().getConsoleSender().sendMessage( "DeathMessage: " + event.getDeathMessage() );
            Bukkit.getServer().getConsoleSender().sendMessage( "DisplayName : " + event.getEntity().getDisplayName() );
            if ( event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent ) {
                EntityDamageByEntityEvent lastcause = ( EntityDamageByEntityEvent ) event.getEntity().getLastDamageCause();
                Entity entity = lastcause.getDamager();
                Bukkit.getServer().getConsoleSender().sendMessage( "Killer Name : " + entity.getName() );
                String msg = config.DeathMessage( entity.getName().toUpperCase() );
                msg = ReplaceString( msg, event.getEntity().getDisplayName() );
                msg = msg.replace( "%mob%", entity.getName() );
                event.setDeathMessage( null );
                Bukkit.broadcastMessage( msg );
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage( "Other Death" );
            }
        }
    }

    @EventHandler
    public void onFlight( PlayerToggleFlightEvent event ) {
        Player p = event.getPlayer();
        // p.sendMessage( "Catch Flight mode " + p.getDisplayName() );
        if ( !p.hasPermission( "LoginCtl.flight" ) ) FlightMode( p, false );
    }
    
    @EventHandler
    public void prePlayerLogin( AsyncPlayerPreLoginEvent event ) {
        date = new Date();
        StatRec.PreSavePlayer( date, event.getName(), event.getUniqueId().toString(), event.getAddress().getHostAddress(), 0 );
    }
    
    @EventHandler
    public void onServerListPing( ServerListPingEvent event ) throws UnknownHostException {
        String Names = StatRec.GetPlayerName( event.getAddress().getHostAddress() );
        String Host = ChatColor.WHITE + "Player(" + Names + ")";

        String MotdMsg = ReplaceString( config.get1stLine(),Names ) + "\n";
        MotdMsg += ReplaceString( config.get2ndLine( !Names.equals( "Unknown" ) ), Names );
        event.setMotd( ReplaceString( MotdMsg, Names ) );

        if ( Names.equals("Unknown") ) {
            if ( config.KnownServers( event.getAddress().getHostAddress() ) != null ) {
                Host = ChatColor.GRAY + config.KnownServers( event.getAddress().getHostAddress() );
            } else {
                //  Unknown Player を File に記録してホストアドレスを取得する
                Host = StatRec.WriteUnknown( event.getAddress().getHostAddress(), config.getCheckIP() );
            }
        }

        String msg = ChatColor.GREEN + "Ping from " + Host + ChatColor.YELLOW + " [" + event.getAddress().getHostAddress() + "]";
        Bukkit.getServer().getConsoleSender().sendMessage( msg );
        if ( !config.getIgnoreName().contains( Names ) && !config.getIgnoreIP().contains( event.getAddress().getHostAddress() ) ) {
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
        }
    }

    @EventHandler //    看板ブロックを右クリック
    public void onSignClick( PlayerInteractEvent event ) {
               
        if ( event.getAction() != Action.RIGHT_CLICK_BLOCK ) return;
        
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        Material material = clickedBlock.getType();
        if ( material == Material.SIGN_POST || material == Material.WALL_SIGN ) {
            Sign sign = (Sign) clickedBlock.getState();
            if ( sign.getLine(0).equals( "[TrashCan]" ) ) {
                Inventory inv;
                inv = Bukkit.createInventory( null, 36, "Trash Can" );
                player.openInventory( inv );
            }
        }
    }


}
