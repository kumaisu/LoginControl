/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol;

import java.net.Inet4Address;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 各プラグイン共通の関数群.....にするつもりのもの
 *
 * @author sugichan
 */
public final class Utility {

    /**
     * IPアドレスを整数化する関数
     *
     * @param ipAddr    xxx.xxx.xxx.xxx 形式のアドレス
     * @return          整数化されたアドレス
     */
    public static long ipToInt( Inet4Address ipAddr ) {
        long compacted = 0;
        byte[] bytes = ipAddr.getAddress();
        for ( int i=0 ; i<bytes.length ; i++ ) {
            compacted += ( bytes[i] * Math.pow( 256, 4-i-1 ) );
        }
        return compacted;
    }

    /**
     * 整数化されたアドレスをIPアドレスに変更する関数
     *
     * @param ipAddress 整数化されたアドレス
     * @return          xxx.xxx.xxx.xxx 形式のアドレス文字列
     */
    public static String toInetAddress( long ipAddress ) {
        long ip = ( ipAddress < 0 ) ? (long)Math.pow(2,32)+ipAddress : ipAddress;
        Inet4Address inetAddress = null;
        String addr =  String.valueOf((ip >> 24)+"."+((ip >> 16) & 255)+"."+((ip >> 8) & 255)+"."+(ip & 255));
        return addr;
    }

    /**
     * 複数の文字列を連結するる
     * 通常の＋による連結よりも若干速くなるので速度重視の場所に利用
     * @param StrItem   文字列の集合
     * @return          完成した一つの文章
     */
    public static String StringBuild( String ... StrItem ) {
        StringBuilder buf = new StringBuilder();

        for ( String StrItem1 : StrItem ) buf.append( StrItem1 );

        return buf.toString();
    }

    /**
     * カラーコードを書き換える
     * @param data  書き換え元の文章
     * @return      書き換え後の文章
     */
    public static String Replace( String data ) {
        return data.replace( "%$", "§" );
    }

    /**
     * Configで記録された%player%をプレイヤー名に差し替える
     * @param data      差し替え元の文章
     * @param Names     差し替えるプレイヤー名
     * @return          差し替え後の文章
     */
    public static String ReplaceString( String data, String Names ) {
        String RetStr;
        RetStr = data.replace( "%player%", Names );
        RetStr = Replace( RetStr );

        return RetStr;
    }

    /**
     * メッセージ表示
     * @param player    表示するプレイヤー
     * @param msg       表示内容
     * @param console   システムコンソールに表示するか？
     */
    public static void Prt( Player player, String msg, boolean console ) {
        if ( console ) Bukkit.getServer().getConsoleSender().sendMessage( msg );
        if ( player != null ) player.sendMessage( msg );
    }

}
