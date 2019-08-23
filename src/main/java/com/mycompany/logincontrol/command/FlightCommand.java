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
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class FlightCommand implements CommandExecutor {

    private final LoginControl instance;

    public FlightCommand( LoginControl instance ) {
        this.instance = instance;
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
        if ( ( sender instanceof Player ) ) {
            Player p = ( Player )sender;
            for ( String arg:args ) {
                switch ( arg ) {
                    case "on":
                        FlightMode( p, true );
                        break;
                    case "off":
                        FlightMode( p, false );
                        break;
                    default:
                        Tools.Prt( p, ChatColor.GREEN + "Fly (on/off)", Tools.consoleMode.normal, programCode );
                }
            }
            return true;
        }
        return false;
    }

    /**
     * プレイヤーのフライを設定または解除する
     *
     * @param p
     * @param flag
     */
    public static void FlightMode( Player p, boolean flag ) {
        if ( flag ) {
            Tools.Prt( p, Utility.StringBuild( ChatColor.AQUA.toString(), "You can FLY !!" ), Tools.consoleMode.normal, programCode );
            // 飛行許可
            p.setAllowFlight( true );
            p.setFlySpeed( 0.1F );
        } else {
            Tools.Prt( p, Utility.StringBuild( ChatColor.LIGHT_PURPLE.toString(), "Stop your FLY Mode." ), Tools.consoleMode.normal, programCode );
            // 無効化
            p.setFlying( false );
            p.setAllowFlight( false );
        }
    }
}
