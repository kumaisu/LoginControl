/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.command;

import static org.bukkit.Bukkit.getWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.mycompany.logincontrol.LoginControl;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class BeginnerCommand implements CommandExecutor {
    private final LoginControl instance;

    public BeginnerCommand( LoginControl instance ) {
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
        int lineSet = 30;
        Player p = ( sender instanceof Player ) ? ( Player )sender:( Player )null;
        Tools.consoleMode checkConsoleFlag = ( ( p == null ) ? Tools.consoleMode.none : Tools.consoleMode.stop );

        if ( cmd.getName().toLowerCase().equalsIgnoreCase( "beginner" ) && ( Config.JumpStats ) ) {
            if ( args.length > 0 ) {
                Player targetPlayer = Bukkit.getPlayer( args[0] );
                if ( !( targetPlayer == null ) ) {
                    BeginnerTeleport( targetPlayer );
                } else { Tools.Prt( "No Match Target Player", Tools.consoleMode.full, programCode); }
            } else { Tools.Prt( "Select Target Player", Tools.consoleMode.full, programCode ); }
            return true;
        }
        return false;
    }    

    /**
     * 初心者チュートリアルへの強制転送コマンド
     *
     * @param player 
     * @return  
     */
    public static boolean BeginnerTeleport( Player player ) {
        Tools.Prt( player, "This player " + player.getDisplayName() + " is first play to teleport", Tools.consoleMode.normal, programCode );
        World world = getWorld( Config.fworld );
        Tools.Prt( "World = " + Config.fworld + " : " + world.toString(), Tools.consoleMode.max, programCode);
        Location loc = new Location( world, Config.fx, Config.fy, Config.fz );
        loc.setYaw( Config.fyaw );
        loc.setPitch( Config.fpitch );
        Tools.Prt(
            "player Teleport=" + world.getName() +
            " X=" + Config.fx +
            " Y=" + Config.fy +
            " Z=" + Config.fz +
            " Yaw=" + Config.fyaw +
            " Pitch=" + Config.fpitch,
            Tools.consoleMode.max, programCode
        );
        return player.teleport( loc );
    }

}
