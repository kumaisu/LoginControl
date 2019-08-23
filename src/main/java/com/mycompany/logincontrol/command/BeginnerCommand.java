/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.mycompany.logincontrol.LoginControl;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.logincontrol.config.Config.programCode;
import com.mycompany.logincontrol.tools.Teleport;

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
        if ( Config.JumpStats ) {
            if ( args.length > 0 ) {
                Player targetPlayer = Bukkit.getPlayer( args[0] );
                if ( !( targetPlayer == null ) ) {
                    Teleport.Beginner( targetPlayer );
                } else { Tools.Prt( "No Match Target Player", Tools.consoleMode.full, programCode); }
            } else { Tools.Prt( "Select Target Player", Tools.consoleMode.full, programCode ); }
            return true;
        }
        return false;
    }    

}
