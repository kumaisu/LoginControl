/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.tools;

import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.logincontrol.config.Config;
import static com.mycompany.logincontrol.config.Config.programCode;
import static org.bukkit.Bukkit.getWorld;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author sugichan
 */
public class Teleport {
    /**
     * ワールドの初期スポーン地点へ強制転送コマンド
     *
     * @param player 
     */
    public static void Spawn( Player player ) {
        if ( player.hasPermission( "LoginCtl.spawn" ) ) {
            Tools.Prt( player, ChatColor.YELLOW + "Teleport to World Spawn", Tools.consoleMode.full, programCode );

            //  player.setBedSpawnLocation(location);
            World world = player.getWorld();
            Location worldLocation = world.getSpawnLocation();
            Tools.Prt(
                "spawn World=" + worldLocation.getWorld().getName() +
                " X=" + worldLocation.getX() +
                " Y=" + worldLocation.getY() +
                " Z=" + worldLocation.getZ() +
                " Yaw=" + worldLocation.getYaw() +
                " Pitch=" + worldLocation.getPitch(),
                Tools.consoleMode.max, programCode
            );

            Location loc = player.getLocation();
            Tools.Prt(
                "player World=" + loc.getWorld().getName() +
                " X=" + loc.getX() +
                " Y=" + loc.getY() +
                " Z=" + loc.getZ() +
                " Yaw=" + loc.getYaw() +
                " Pitch=" + loc.getPitch(),
                Tools.consoleMode.max, programCode
            );
            player.teleport( worldLocation );
        }
    }

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
