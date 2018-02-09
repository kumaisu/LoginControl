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
                if ( args.length > 0 ) {
                    if ( args[0].equals( "d" ) ) {
                        if ( args.length > 1 ) {
                            sender.sendMessage( "d = " + args[1] );
                            statusRecord.DateLogPrint( sender, args[1] );
                        }
                        return true;
                    }
                    if ( args[0].equals( "full" ) ) {
                        sender.sendMessage( ChatColor.GREEN + "ログをフル表示します" );
                        FullFlag = true;
                    }
                }
                
		statusRecord.LogPrint( ( sender instanceof Player ) ? (Player)sender:(Player)null, ( sender instanceof Player ) ? 15:30, FullFlag );

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
            Bukkit.getServer().getConsoleSender().sendMessage( ChatColor.LIGHT_PURPLE + "Present Item" );

            List<String> present = config.getPresent();
            for(Iterator it = present.iterator(); it.hasNext();) {
                String item = (String)it.next();
                String[] itemdata = item.split( ",", 0 );
                player.getInventory().addItem( new ItemStack( Material.getMaterial( itemdata[0] ), Integer.parseInt( itemdata[1] ) ) );
            }

            getLogger().info( player.getLocale().isEmpty() ? "Location Empty":"First Spawn Location" );

            getLogger().info( "This player is first play to teleport");
            World world = getWorld( config.getWorld() );
            Location loc = new Location( world, config.getX(), config.getY(), config.getZ() );
            loc.setPitch( config.getPitch() );
            loc.setYaw( config.getYaw() );
            player.teleport( loc );

        } else {
            getLogger().info( "The Repeat Login Player" );
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
        Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
        String MotdMsg = ChatColor.LIGHT_PURPLE + "スナック・クマイスサーバー(1.12.2)\n";
        // String MotdMsg = config.get1stLine() + "\n";
        if ( Names.equals( "Unknown" ) ) {
            MotdMsg += ChatColor.DARK_AQUA + "ゆっくりしていってね";
            //  MotdMsg += config.get2ndLine( false );
        } else {
            MotdMsg += ChatColor.YELLOW + Names + ChatColor.GREEN + "さん、まってるよ";
            //  MotdMsg += String.format( config.get2ndLine( true ),Names );
        }
        event.setMotd( MotdMsg );
    }
	
}