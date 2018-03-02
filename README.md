# LoginControl
Minecraft Plugin LoginControl Repository  

**Overview**  

As for player login, it is a plug-in that supports variously  
  
1.Ability to display referenced players to console or authorized player in real time.  
2.Return Modified MotD-Message for player's server query.  
3.Teleport new players to a specific location.  
4.Ability to give items to new players.  
5.History record and reference function of logged-in player and failed player.  
6.Ability to display the list of users from the same IP to authorized player.  
  
LoginControlは、プレイヤーログインに付いて、サポートをするプラグインです  
1.クライアントのサーバーリストを更新した際に、ホストに対して行われる参照をコンソールまたは許可されたプレイヤー画面にリアルタイムに表示します  
2.参照されたプレイヤーに対し、MotD内容を成形し送信します
3.新規ログインプレイヤーに対して、特定場所へテレポートする機能  
4.新規ログインプレイヤーに特定のアイテムをプレゼントする機能  
5.ログイン時にデータベース記録し、参照する機能（直近５人）またはコマンドによって１０人  
6.同一IPからのアクセスを監視し、プレイヤー名を列挙する機能  

**Usage**  

/Loginlist [d:yymmdd] [u:player name] [full]  

/LoginCtl reload  
/LoginCtl info:[ip address]  
/LoginCtl chg:[ip address]:[host name]  
/LoginCtl PingTop  
