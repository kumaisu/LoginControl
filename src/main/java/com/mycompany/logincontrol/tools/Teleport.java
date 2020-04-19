/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.tools;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import static org.bukkit.Bukkit.getWorld;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.logincontrol.config.Config;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class Teleport {
    /**
     * 初心者チュートリアルへの強制転送コマンド
     *
     * @param player 
     * @return  
     */
    public static boolean Beginner( Player player ) {
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
