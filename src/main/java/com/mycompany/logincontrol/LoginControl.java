/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.mycompany.logincontrol.listener.LoginListener;
import com.mycompany.logincontrol.listener.ServerListener;
import com.mycompany.logincontrol.listener.ExtraListener;
import com.mycompany.logincontrol.command.LoginlistCommand;
import com.mycompany.logincontrol.command.AdminCommand;
import com.mycompany.logincontrol.command.SpawnCommand;
import com.mycompany.logincontrol.command.PingCommand;
import com.mycompany.logincontrol.command.FlightCommand;
import com.mycompany.logincontrol.config.ConfigManager;
import com.mycompany.logincontrol.config.MotDControl;
import com.mycompany.logincontrol.database.DatabaseControl;

/**
 *
 * @author sugichan
 */
public class LoginControl extends JavaPlugin implements Listener {

    public static ConfigManager config;
    public static MotDControl MotData;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents( this, this );
        config = new ConfigManager( this );
        MotData = new MotDControl( this );
        DatabaseControl.connect();
        DatabaseControl.TableUpdate();

        new LoginListener( this );
        new ServerListener( this );
        new ExtraListener( this );

        getCommand( "loginlist" ).setExecutor( new LoginlistCommand( this ) );
        getCommand( "loginctl" ).setExecutor( new AdminCommand( this ) );
        getCommand( "spawn" ).setExecutor( new SpawnCommand( this ) );
        getCommand( "flight" ).setExecutor( new FlightCommand( this ) );
        getCommand( "ping" ).setExecutor( new PingCommand( this ) );
    }

    @Override
    public void onDisable() {
        DatabaseControl.disconnect();
        super.onDisable(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onLoad() {
        super.onLoad(); //To change body of generated methods, choose Tools | Templates.
    }
}
