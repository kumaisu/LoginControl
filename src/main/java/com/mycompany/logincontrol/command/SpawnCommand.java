/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.mycompany.logincontrol.LoginControl;
import com.mycompany.logincontrol.tools.Teleport;

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
            Teleport.Spawn( ( Player ) sender );
            return true;
        }
        return false;
    }
}
