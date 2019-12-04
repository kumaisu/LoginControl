/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.logincontrol.tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.config.Config;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class LogMessage {
 
    public static void joinMessage( Plugin plugin, Player player ) {
        //  プレイヤーの言語設定を取得するために遅延処理の後 Welcome メッセージの表示を行う
        //  ラグが大きいが現状はこれが精一杯の状態
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask( plugin, () -> {
            String getLocale = Tools.getLanguage( player );
            String locale2byte = getLocale.substring( 0, 2 ).toUpperCase();

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
                    String ReturnMessage = ( Config.ReturnJoinMessage.get( locale2byte ) == null ? Config.Returning_Join_Message : Config.ReturnJoinMessage.get( locale2byte ) );
                    Bukkit.broadcastMessage( Utility.ReplaceString( ReturnMessage, player.getDisplayName() ) );
                }
            }
        }, 100 );
    }
}
