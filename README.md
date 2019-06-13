# LoginControl
Minecraft Plugin LoginControl Repository  

## Overview  
  
As for player login, it is a plug-in that supports variously  
LoginControl は、プレイヤーログインに付いて、サポートをするプラグインです  
  
## Support  
Open a new issue here: [https//github.com/kumaisu/LoginControl/issues](https://github.com/kumaisu/LoginControl/issues)  
  
## Features  
none  
  
## Releases  
Github projects have a "releases" link on their home page.  
If you still don't see it, [click here](https://github.com/kumaisu/LoginControl/releases) for PremisesEvent releases.  
  
## Wikis  
[Login Control Wiki](https://github.com/kumaisu/LoginControl/wiki)  
  
## Function
1.Ability to display referenced players to console or authorized player in real time.  
2.Return Modified MotD-Message for player's server query.  
3.Teleport new players to a specific location.  
4.Ability to give items to new players.  
5.History record and reference function of logged-in player and failed player.  
6.Ability to display the list of users from the same IP to authorized player.  
  
1.クライアントのサーバーリストを更新した際に、ホストに対して行われる参照をコンソールまたは許可されたプレイヤー画面にリアルタイムに表示します  
2.参照されたプレイヤーに対し、MotD内容を成形し送信します  
3.新規ログインプレイヤーに対して、特定場所へテレポートする機能  
4.新規ログインプレイヤーに特定のアイテムをプレゼントする機能  
5.ログイン時にデータベース記録し、参照する機能（直近５人）またはコマンドによって１０人  
6.同一IPからのアクセスを監視し、プレイヤー名を列挙する機能  

## Extra function
1. World Spawn Teleport  
2. Flight mode / flight [on / off]  
3. Trash Can Signboard [Trash Can]  
4. Death announcement (incomplete)  
  
1.フライトモード /flight [on/off]  
2.ゴミ箱看板    [Trash Can]  
3.死亡アナウンス（不完全）  
  
## Usage  
  
/Loginlist [d:yymmdd] [u:player name] [full]  
  
/LoginCtl reload  
/LoginCtl status  
/LoginCtl info IPAddress  
/LoginCtl chg IPAddress HostName  
/LoginCtl add IPAddress HostName  
/LoginCtl del IPAddress  
/LoginCtl PingTop [MaxCount]  
/LoginCtl count IPAddress ( num or Reset )  
/LoginCtl search word  
/LoginCtl checkIP  
/LoginCtl Console [full/normal/none]  
  
**How to Install**  
1.サーバーのプラグインディレクトリにLoginControl.jarを入れて起動します  
2.一旦終了し、作成されたConfig.ymlを編集します  
3.config設定の通りMySQLにデーターベースをCreateします  
4.再度サーバーを起動  
  
Contact is Discord Kitsune#5955  
Discord Server https://discord.gg/AgX3UxR  
