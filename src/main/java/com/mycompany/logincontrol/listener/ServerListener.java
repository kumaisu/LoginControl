/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.listener;

import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.net.UnknownHostException;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.LoginControl;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.logincontrol.database.Database;
import com.mycompany.logincontrol.database.DatabaseControl;
import com.mycompany.logincontrol.database.HostData;
import com.mycompany.logincontrol.database.ListData;
import static com.mycompany.logincontrol.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class ServerListener implements Listener {

    private String lastName = "Begin";
    private String DataFolder = "";

    /**
     *
     * @param plugin
     */
    public ServerListener( Plugin plugin ) {
        plugin.getServer().getPluginManager().registerEvents( this, plugin );
        DataFolder = plugin.getDataFolder().toString();
    }

    /**
     * サーバーへのリスト照会が来た時に起きるイベント
     * プレイヤーのサーバーリストへの文言（MotD）の内容を個別に修正して返信する
     *
     * @param event
     * @throws UnknownHostException
     * @throws ClassNotFoundException
     */
    @EventHandler
    public void onServerListPing( ServerListPingEvent event ) throws UnknownHostException, ClassNotFoundException {
        String Names = "Unknown";
        // ConsoleLog Flag 2:Full 1:Normal(Playerのみ)
	Tools.consoleMode PrtStatus = Tools.consoleMode.full;

        String MotdMsg = LoginControl.MotData.get1stLine();
        String MsgColor = ChatColor.GRAY.toString();
        String Host = Config.KnownServers.get( event.getAddress().getHostAddress() );

        if ( Host == null ) {
            //  簡易DNSからホスト名を取得
            int MsgNum = 0;
            if ( HostData.GetSQL( event.getAddress().getHostAddress() ) ) {
                Host = Database.Host;
                //  未知のホスト名の場合は LIGHT_PURPLE , 既知のPlayerだった場合は WHITE になる
                if ( Host.contains( "Player" ) ) {
                    MsgColor = ChatColor.WHITE.toString();
                    //  簡易DNSにプレイヤー登録されている場合は、ログイン履歴を参照して最新のプレイヤー名を取得する
                    Names = ListData.GetPlayerName( event.getAddress().getHostAddress() );
                    Tools.Prt( "Names    [" + Names + "]", Tools.consoleMode.max, programCode );
                    Tools.Prt( "Ignore   [" + ( Config.IgnoreReportName.contains( Names) ? "True" : "False" ) + "]", Tools.consoleMode.max, programCode);
                    if ( ( Config.playerPingB && !Config.IgnoreReportName.contains( Names ) ) && ( Names != null && !Names.equals( lastName ) ) ) {
                        Tools.Prt( "lastName [" + lastName + "]", Tools.consoleMode.max, programCode );
                        //  if ( Bukkit.getServer().getOnlinePlayers().size() > 0 ) {
                        for( Player p : Bukkit.getOnlinePlayers() ) {
                            p.sendMessage( ChatColor.GREEN + "Ping From Player " + ChatColor.WHITE + Names );
                        }
                        if ( Names == null ) {
                            Tools.Prt( ChatColor.GREEN + "(Console) Ping From Player " + ChatColor.WHITE + Names,Tools.consoleMode.full, programCode );
                        }
                        lastName = Names;
                    }
                    MsgNum = 2;
                    PrtStatus = Tools.consoleMode.normal;
                } else {
                    MsgColor = ChatColor.LIGHT_PURPLE.toString();
                }
            } else {
                //  DBに該当なしなので、DB登録
                //  ホスト名が取得できなかった場合は、Unknown Player を File に記録し、新規登録
                MsgColor = ChatColor.RED.toString();
                Host = HostData.AddHostname( event.getAddress().getHostAddress(), Config.CheckIPAddress );
                //  新規ホストとして、Unknown.yml ファイルへ書き出し
                DatabaseControl.WriteFileUnknown( event.getAddress().getHostAddress(), DataFolder );
            }

            HostData.AddCountHost( event.getAddress().getHostAddress(), 0 );

            Host = Utility.StringBuild( Host, "(", String.valueOf( Database.Count ), ")" );

            String Motd2ndLine = LoginControl.MotData.getModifyMessage( Names, event.getAddress().getHostAddress() );

            if ( "".equals( Motd2ndLine ) ) {
                if ( ( LoginControl.MotData.getmotDMaxCount() != 0 ) && ( Database.Count>LoginControl.MotData.getmotDMaxCount() ) ) {
                    MsgNum = 4;
                } else {
                    if ( ( LoginControl.MotData.getmotDCount() != 0 ) && ( Database.Count>LoginControl.MotData.getmotDCount() ) ) MsgNum++;
                }

                Motd2ndLine = LoginControl.MotData.get2ndLine( MsgNum );
                Motd2ndLine = Motd2ndLine.replace( "%count", String.valueOf( Database.Count ) );
                Motd2ndLine = Motd2ndLine.replace( "%date", Database.sdf.format( Database.NewDate ) );
                MotdMsg = Utility.StringBuild( MotdMsg, Motd2ndLine );
                Tools.Prt( Utility.StringBuild( "MotD = ", Utility.ReplaceString( Motd2ndLine, Names ) ), Tools.consoleMode.max, programCode );
            } else {
                MotdMsg = Motd2ndLine;
                Tools.Prt( Utility.StringBuild( "Change = ", Utility.ReplaceString( Motd2ndLine.replace( "\n", " " ), Names ) ), Tools.consoleMode.max, programCode );
            }

            if ( ( Config.AlarmCount != 0 ) && ( Database.Count >= Config.AlarmCount ) ) { PrtStatus = Tools.consoleMode.print; }

        } else {
            //  Configに既知のホスト登録があった場合
            MotdMsg = Utility.StringBuild( MotdMsg, LoginControl.MotData.get2ndLine( 4 ) );
        }

        event.setMotd( Utility.ReplaceString( MotdMsg, Names ) );
        // event.getNumPlayers().set( 30 );

        if ( !Config.IgnoreReportIP.contains( event.getAddress().getHostAddress() ) ) {
            String msg = Utility.StringBuild( ChatColor.GREEN.toString(), "Ping from ", MsgColor, Host, ChatColor.YELLOW.toString(), " [", event.getAddress().getHostAddress(), "]" );
            Tools.Prt( msg, PrtStatus, programCode );
            Bukkit.getOnlinePlayers().stream().filter( ( p ) -> ( p.hasPermission( "LoginCtl.view" ) || p.isOp() ) ).forEachOrdered( ( p ) -> { p.sendMessage( msg ); } );
        }
    }
}
