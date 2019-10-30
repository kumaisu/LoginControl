/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.listener;

import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.Date;
import java.net.UnknownHostException;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.logincontrol.database.Database;
import com.mycompany.logincontrol.database.HostData;
import com.mycompany.logincontrol.database.ListData;
import com.mycompany.logincontrol.tools.Teleport;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class LoginListener implements Listener {

    private Date date;
    private final Plugin plugin;

    /**
     *
     * @param plugin
     */
    public LoginListener( Plugin plugin ) {
        plugin.getServer().getPluginManager().registerEvents( this, plugin );
        this.plugin = plugin;
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
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask( plugin, () -> {
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

}
