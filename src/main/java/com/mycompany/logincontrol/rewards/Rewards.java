/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.logincontrol.rewards;

import java.util.Random;
import org.bukkit.Sound;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.logincontrol.config.Reward;

/**
 *
 * @author sugichan
 */
public class Rewards {

    public static void Reward( Player player ) {
        if ( Reward.sound_play ) {
            Tools.Prt( player, "Sound Play !!", Tools.consoleMode.full, Config.programCode );
            Location loc = null;
            loc.getWorld().playSound(
                player.getLocation(),                   // 鳴らす場所
                Sound.valueOf( Reward.sound_type ),     // 鳴らす音
                Reward.sound_volume,                    // 音量
                Reward.sound_pitch                      // 音程
            );
        }

        Tools.Prt( player, Utility.ReplaceString( Reward.basic_message, player.getName() ), Config.programCode );
        Reward.basic_command.stream().forEach( BR -> {
            Tools.ExecOtherCommand( player, Utility.ReplaceString( BR, player.getName() ), "" );
            Tools.Prt( ChatColor.AQUA + "Command Execute : " + ChatColor.WHITE + BR, Tools.consoleMode.max, Config.programCode );
        } );

        if ( Reward.advance_command.size() > 0 ) {
            Tools.Prt( player, Utility.ReplaceString( Reward.basic_message, player.getName() ), Config.programCode );
            if ( Reward.advance_random ) {
                Random random = new Random();
                int randomValue = random.nextInt( Reward.advance_command.size() );
                String AR = Utility.ReplaceString( Reward.advance_command.get( randomValue ), player.getName() );
                Tools.ExecOtherCommand( player, AR, "" );
                Tools.Prt( ChatColor.AQUA + "Command Execute : " + ChatColor.WHITE + AR, Tools.consoleMode.max, Config.programCode );
            } else {
                Reward.basic_command.stream().forEach( AR -> {
                    String ARC = Utility.ReplaceString( AR, player.getName() );
                    Tools.ExecOtherCommand( player, ARC, "" );
                    Tools.Prt( ChatColor.AQUA + "Command Execute : " + ChatColor.WHITE + ARC, Tools.consoleMode.max, Config.programCode );
                } );
            }
        }
    }

}
