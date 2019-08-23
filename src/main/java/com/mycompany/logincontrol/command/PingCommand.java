/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.command;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import org.bukkit.ChatColor;
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
public class PingCommand implements CommandExecutor {

    private final LoginControl instance;

    public PingCommand( LoginControl instance ) {
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
        Player p = ( sender instanceof Player ) ? ( Player )sender:( Player )null;
        Tools.consoleMode checkConsoleFlag = ( ( p == null ) ? Tools.consoleMode.none : Tools.consoleMode.stop );

        if ( args.length > 0 ) {
            Inet4Address inet;
            try {
                inet = ( Inet4Address ) Inet4Address.getByName( args[0] );
                String msg = "Check Ping is " + inet.getHostName();
                Tools.Prt( p, msg, checkConsoleFlag, programCode );
            } catch (UnknownHostException ex) {
                Tools.Prt( p, ChatColor.RED + "Ping Unknown Host : " + ex.getMessage(), checkConsoleFlag, programCode );
            }
            return true;
        }
        return false;
    }
}
