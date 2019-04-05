/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author sugichan
 */
public final class Utility {

    /**
     * 
     * @param StrItem
     * @return 
     */
    public static String StringBuild( String ... StrItem ) {
        StringBuilder buf = new StringBuilder();

        for ( String StrItem1 : StrItem ) buf.append( StrItem1 );
 
        return buf.toString();
    }

    /**
     * 
     * @param data
     * @return 
     */
    public static String Replace( String data ) {
        return data.replace( "%$", "§" );
    }

    /**
     * 
     * @param data
     * @param Names
     * @return 
     */
    public static String ReplaceString( String data, String Names ) {
        String RetStr;
        RetStr = data.replace( "%player%", Names );
        RetStr = Replace( RetStr );
        
        return RetStr;
    }

    /**
     * 
     * @param player
     * @param msg
     * @param console 
     */
    public static void Prt( Player player, String msg, boolean console ) {
        if ( console ) Bukkit.getServer().getConsoleSender().sendMessage( msg );
        if ( player != null ) player.sendMessage( msg );
    }

}
