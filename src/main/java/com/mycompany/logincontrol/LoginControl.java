/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getWorld;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;
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
    
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents( this, this );
        config = new Config( this );
    }

    @Override
    public void onDisable() {
        super.onDisable(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onLoad() {
        super.onLoad(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onCommand(CommandSender sender,Command cmd, String commandLabel, String[] args) {
        boolean FullFlag = false;
        if ( cmd.getName().equalsIgnoreCase( "LoginList" ) ) {
                StatusRecord statusRecord = new StatusRecord( config.getHost(), config.getDB(), config.getPort(), config.getUsername(), config.getPassword() );
                int PrtF = 0;
                String Param = "";
                Player p = ( sender instanceof Player ) ? (Player)sender:(Player)null;
                
                for (String arg : args) {
                    String[] param = arg.split(":");
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
                            sender.sendMessage( config.LogFull() );
                            FullFlag = true;
                            break;
                        case "reload":
                            config = new Config( this );
                            sender.sendMessage( config.Reload() );
                            return true;
                        default:
                            sender.sendMessage( config.ArgsErr() );
                    }
                }

                switch ( PrtF ) {
                    case 0:
                        statusRecord.LogPrint( p, ( sender instanceof Player ) ? 15:30, FullFlag );
                        break;
                    case 1:
                        statusRecord.DateLogPrint( p, Param, FullFlag );
                        break;
                    case 2:
                        statusRecord.NameLogPrint( p, Param, FullFlag );
                        break;
                    default:
                        sender.sendMessage( config.OptError() );
                }

                return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerLogin( PlayerJoinEvent event ) {

        Player player = event.getPlayer();
        StatusRecord statusRecord = new StatusRecord( config.getHost(), config.getDB(), config.getPort(), config.getUsername(), config.getPassword() );
        statusRecord.ChangeStatus( date, "Logged in" );
        statusRecord.LogPrint( player, 5, false );
        statusRecord.CheckIP( player );

        if ( ( config.getJump() ) && ( ( !player.hasPlayedBefore() ) || config.OpJump( player.isOp() ) ) ) {
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.LIGHT_PURPLE + "The First Login Player" );

            List<String> present = config.getPresent();
            for(Iterator it = present.iterator(); it.hasNext();) {
                String item = (String)it.next();
                String[] itemdata = item.split( ",", 0 );
                player.getInventory().addItem( new ItemStack( Material.getMaterial( itemdata[0] ), Integer.parseInt( itemdata[1] ) ) );
                Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.AQUA + "Present Item : " + ChatColor.WHITE + itemdata[0] + "(" + itemdata[1] + ")" );
            }

            // Bukkit.getServer().getConsoleSender().sendMessage( player.getLocale().isEmpty() ? "Location Empty":"First Spawn Location" );

            Bukkit.getServer().getConsoleSender().sendMessage( "This player is first play to teleport");
            World world = getWorld( config.getWorld() );
            Location loc = new Location( world, config.getX(), config.getY(), config.getZ() );
            loc.setPitch( config.getPitch() );
            loc.setYaw( config.getYaw() );
            player.teleport( loc );

        } else {
            Bukkit.getServer().getConsoleSender().sendMessage( "The Repeat Login Player" );
        }
    }

    @EventHandler
    public void prePlayerLogin( AsyncPlayerPreLoginEvent event ) {

        date = new Date();

        StatusRecord statusRecord = new StatusRecord( config.getHost(), config.getDB(), config.getPort(), config.getUsername(), config.getPassword() );
        statusRecord.PreSavePlayer( date, event.getName(), event.getUniqueId().toString(), event.getAddress().getHostAddress(), "Attempted" );
    }
    
    @EventHandler
    public void onServerListPing( ServerListPingEvent event ) {
        StatusRecord statusRecord = new StatusRecord( config.getHost(), config.getDB(), config.getPort(), config.getUsername(), config.getPassword() );
        String Names;
        if ( config.KnownServers( event.getAddress().getHostAddress() ) == null ) {
            Names = statusRecord.GetPlayerName( event.getAddress().getHostAddress() );
        } else {
            Names = config.KnownServers( event.getAddress().getHostAddress() );
        }

        String msg = ChatColor.GREEN + "Ping from " + ChatColor.WHITE + Names + ChatColor.YELLOW + " [" + event.getAddress().getHostAddress() + "]";
        Bukkit.getServer().getConsoleSender().sendMessage( msg );
        if ( !config.getIgnoreName().contains( Names ) && !config.getIgnoreIP().contains( event.getAddress().getHostAddress() ) ) {
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
        }

        String MotdMsg = config.get1stLine() + "\n";
        MotdMsg += config.get2ndLine( !Names.equals( "Unknown" ) );
        event.setMotd( MotdMsg.replace( "%player%", Names ) );
    }
	
}
