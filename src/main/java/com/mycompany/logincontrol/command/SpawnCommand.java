/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.logincontrol.LoginControl;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class SpawnCommand implements CommandExecutor {

    private final LoginControl instance;

    public SpawnCommand( LoginControl instance ) {
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
        if ( sender instanceof Player ) {
            spawnTeleport( ( Player ) sender );
            return true;
        }
        return false;
    }

    /**
     * ワールドの初期スポーン地点へ強制転送コマンド
     *
     * @param player 
     */
    public void spawnTeleport( Player player ) {
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
}
