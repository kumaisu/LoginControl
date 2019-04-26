/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author sugichan
 */
public class MotDControl {
    private final Plugin plugin;
    private final Config config;

    private int MotD_Count;
    private int MotD_MaxCount;
    private String MotD1stLine;
    private List<String> MotD2ndLine;

    String resourceFile = "MotD.yml";
    File UKfile;
    FileConfiguration UKData;

    public MotDControl( Plugin plugin, Config config ) {
        this.plugin = plugin;
        this.config = config;
        UKfile = new File( plugin.getDataFolder(), resourceFile );
        UKData = YamlConfiguration.loadConfiguration( UKfile );
        load();
    }

    /**
     * 設定をロードします
     * @return
     */
    public void load() {
        if( !UKfile.exists() ) {
            plugin.getResource( resourceFile );
            plugin.saveResource( plugin.getDataFolder() + File.separator + resourceFile, false );
            // return false;
        }

        MotD1stLine = UKData.getString( "MotD1st", "" );
        MotD_Count = UKData.getInt( "MotD2nd-Ping-Count", 0 );
        MotD_MaxCount = UKData.getInt( "MotD2nd-Ping-Max-Count", 0 );

        MotD2ndLine = new ArrayList<>();
        MotD2ndLine.add( UKData.getString( "MotD2nd-Unknown", "Unknown" ) );
        MotD2ndLine.add( UKData.getString( "MotD2nd-Ping", "Ping" ) );
        MotD2ndLine.add( UKData.getString( "MotD2nd-Player", "Player" ) );
        MotD2ndLine.add( UKData.getString( "MotD2nd-Ping-Player", "PlayerPing" ) );
        MotD2ndLine.add( UKData.getString( "MotD2nd-Alive", "Alive" ) );

        // return true;
    }

    /**
     * これは使うと、個別設定が消えるので、サンプル
     */
    public void save() {
        UKData.set( "MotD2nd-Ping-Count", MotD_Count );
        UKData.set( "MotD2nd-Ping-Max-Count", MotD_MaxCount );
        UKData.set( "MotD1st", MotD1stLine );
        UKData.set( "MotD2nd-Unknown", MotD2ndLine.get( 0 ) );
        UKData.set( "MotD2nd-Ping", MotD2ndLine.get( 1 ) );
        UKData.set( "MotD2nd-Player", MotD2ndLine.get( 2 ) );
        UKData.set( "MotD2nd-Ping-Player", MotD2ndLine.get( 3 ) );
        UKData.set( "MotD2nd-Alive", MotD2ndLine.get( 4 ) );

        try {
            UKData.save( UKfile );
        }
        catch (IOException e) {
            plugin.getServer().getLogger().log( Level.WARNING, "{0}Could not save UnknownIP File.", ChatColor.RED );
        }
    }

    public String get1stLine() {
        return MotD1stLine;
    }

    public String get2ndLine( int num ) {
        return MotD2ndLine.get( num );
    }

    public int getmotDCount() {
        return MotD_Count;
    }

    public int getmotDMaxCount() {
        return MotD_MaxCount;
    }

    public String getModifyMessage( String name, String IP ) {
        String returnMessage = UKData.getString( name, "" );
        if ( "".equals( returnMessage ) ) { returnMessage = UKData.getString( IP, "" ); }
        return returnMessage;
    }
}
