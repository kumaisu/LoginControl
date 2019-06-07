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
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.tool.Tools;

/**
 * MotDメッセージ関するライブラリ
 *
 * @author sugichan
 */
public class MotDControl {
    private final Plugin plugin;

    private int MotD_Count;
    private int MotD_MaxCount;
    private String MotD1stLine;
    private List<String> MotD2ndLine;

    private final String resourceFile = "MotD.yml";
    private final File UKfile;
    private FileConfiguration UKData; // = new YamlConfiguration();

    public MotDControl( Plugin plugin ) {
        this.plugin = plugin;
        UKfile = new File( plugin.getDataFolder(), resourceFile );
        // UKData = YamlConfiguration.loadConfiguration( UKfile );
        load();
    }

    /**
     * 設定をロードします
     */
    public void load() {
        if ( !UKfile.exists() ) { plugin.saveResource( resourceFile, false ); }
        if ( UKData == null ) { UKData = YamlConfiguration.loadConfiguration( UKfile ); }

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
        catch ( IOException e ) {
            plugin.getServer().getLogger().log( Level.WARNING, "{0}Could not save MotD.yaml File.", ChatColor.RED );
        }
    }

    /**
     * MotD 1行目のメッセージ取得
     *
     * @return 
     */
    public String get1stLine() {
        return MotD1stLine;
    }

    /**
     * MotD 2行目のメッセージ取得
     * 0:Unknownへのメッセージ
     * 1:参照回数が規定を越えた時の0へのメッセージ
     * 2:参加プレイヤーへのメッセージ
     * 3:参照回数が規定を越えた時の参加プレイヤーへのメッセージ
     * 4:Configで指定されたIPへのメッセージ
     *
     * @param num
     * @return 
     */
    public String get2ndLine( int num ) {
        return MotD2ndLine.get( num );
    }

    /**
     * 参照規定回数
     *
     * @return 
     */
    public int getmotDCount() {
        return MotD_Count;
    }

    /**
     * 参照回数最大値設定
     * この回数を越えると、強制的に2ndLineの設定が4になる
     *
     * @return 
     */
    public int getmotDMaxCount() {
        return MotD_MaxCount;
    }

    /**
     * 個別指定がある場合のメッセージ内容
     * 指定が無い場合は""が返される
     * プレイヤー名指定が優先されるので注意
     *
     * @param name
     * @param IP
     * @return 
     */
    public String getModifyMessage( String name, String IP ) {
        String returnMessage = UKData.getString( name, "" );
        if ( "".equals( returnMessage ) ) { returnMessage = UKData.getString( IP, "" ); }
        return returnMessage;
    }

    /**
     * MotD Message Setting Print
     *
     * @param p
     */
    public void getStatus( Player p ) {
        Utility.consoleMode consolePrintFlag = ( ( p == null ) ? Utility.consoleMode.none:Utility.consoleMode.max );
        Tools.Prt( p, ChatColor.GREEN + "=== LoginControl MotD Messages ===", consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Ping Count : " + ChatColor.YELLOW + String.valueOf( MotD_Count ), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Max Count  : " + ChatColor.YELLOW + String.valueOf( MotD_MaxCount ), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "MotD Message:", consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "1st Line : " + ChatColor.YELLOW + MotD1stLine.replace( "\n", "*" ), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "2nd Line:", consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Unknown       : " + ChatColor.YELLOW + MotD2ndLine.get( 0 ), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Unknown Count : " + ChatColor.YELLOW + MotD2ndLine.get( 1 ), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Player        : " + ChatColor.YELLOW + MotD2ndLine.get( 2 ), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Player Count  : " + ChatColor.YELLOW + MotD2ndLine.get( 3 ), consolePrintFlag );
        Tools.Prt( p, ChatColor.WHITE + "Alive         : " + ChatColor.YELLOW + MotD2ndLine.get( 4 ), consolePrintFlag );
        Tools.Prt( p, ChatColor.GREEN + "==================================", consolePrintFlag );
    }
}
