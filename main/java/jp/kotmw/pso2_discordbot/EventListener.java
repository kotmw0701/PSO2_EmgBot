package jp.kotmw.pso2_discordbot;

import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;

import javafx.application.Platform;
import jp.kotmw.pso2_discordbot.controllers.FxControllers;
import jp.kotmw.pso2_discordbot.controllers.ToggleCoolTime;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class EventListener {

	@SuppressWarnings("deprecation")
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent e) {
		IMessage msg = e.getMessage();
		String txt = msg.getContent();
		try {
			anga(msg);
		} catch (MissingPermissionsException | RateLimitException | DiscordException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if(!txt.startsWith("%"))
			return;
		String args[] = txt.substring(1).split(" ");
		int second = 5;
		if(txt.indexOf("-") > -1) {
			args = txt.substring(1).substring(0, txt.indexOf("-")-1).split(" ");
			second = getSecond(txt.substring(txt.indexOf("-")));
		}
		String command = args[0];
		IChannel channel = msg.getChannel();
		FxControllers.addLog(msg.getAuthor().getName()+" issued command: "+txt);
		try {
			if(command.equalsIgnoreCase("help")) {
				sendText(channel, "------Commands------"
						,"**`< >`** 必須引数, **`[ ]`** 任意引数"
						,"**`\"\"`**で囲まれているものがある場合はどれかを選択してください"
						,"**`%emergency [server]`**	現在予告が出てる緊急を表示します"
						,"**`%startup <server>`**	指定したサーバーのBotを稼働させます"
						,"**`%shutdown <server>`**	指定したサーバーのBotを停止させます"
						,"**`%geturl <server>`**	指定したサーバーの認証URLを取得します"
						,"**`%setemg <\"random\"/\"notice\"> <emgs> [server]`**	説明めんどいから聞いて(おい");
			} else if(command.equalsIgnoreCase("emg") || command.equalsIgnoreCase("emergency")) {
				int server = 2;
				if(args.length == 2) {
					if(!NumberUtils.isNumber(args[1]) || (Integer.valueOf(args[1]) > 10 || Integer.valueOf(args[1]) < 1)) {
						RemoveTimer(channel, second, "1-10の範囲で入れてください");
						return;
					}
					server = Integer.valueOf(args[1]);
				}
				RemoveTimer(channel, second, (server+" 鯖 : "+ "**"+Main.history.getEmergency(server)+"**"));
			} else if(command.equalsIgnoreCase("startup") || command.equalsIgnoreCase("shutdown")) {
				if(args.length != 2)
					return;
				if(!isActiveArks(msg)) {
					RemoveTimer(channel, second, "権限所持者以外実行不可能です");
					return;
				}
				int cooltime = canRun(command);
				if(cooltime > 0) {
					String time = (cooltime/60)+"分"+(cooltime%60)+"秒";
					RemoveTimer(channel, second, "クールタイムが終わっていません\r\n【残り "+time+" 】");
					return;
				}
				if(!NumberUtils.isNumber(args[1]) || (Integer.valueOf(args[1]) > 10 || Integer.valueOf(args[1]) < 1)) {
					RemoveTimer(channel, second, "1-10の範囲で入れてください");
					return;
				}
				boolean enable = command.equalsIgnoreCase("startup");
				final int server = Integer.valueOf(args[1]);
				if(BotConfiguration.getToken(server) == null) {
					RemoveTimer(channel, second, "対象のBotのTokenが設定されていないので、操作することが出来ません","管理者に問い合わせてください");
					return;
				}
				if(!BotClientManager.changeEnable(Integer.valueOf(args[1]), enable)) {
					RemoveTimer(channel, second, (enable ? "稼働中です" : "稼働してません"));
					return;
				}
				Platform.runLater(() ->FxControllers.getServer(server).togglechange());
				RemoveTimer(channel, second, server+"鯖のBotを"+(enable ? "稼働" : "停止")+"させました");
			} else if(command.equalsIgnoreCase("geturl")) {
				if(args.length != 2)
					return;
				if(!isActiveArks(msg)) {
					RemoveTimer(channel, second, "権限所持者以外実行不可能です");
					return;
				}
				if(!NumberUtils.isNumber(args[1]) || (Integer.valueOf(args[1]) > 10 || Integer.valueOf(args[1]) < 1)) {
					RemoveTimer(channel, second, "1-10の範囲で入れてください");
					return;
				}
				final int server = Integer.valueOf(args[1]);
				String applicationId = BotClientManager.getID(server);
				if(applicationId == null) {
					RemoveTimer(channel, second, "対象のBotを立ち上げてから再試行してください");
					return;
				}
				channel.sendMessage(server+"鯖Botの認証URL [ https://discordapp.com/oauth2/authorize?client_id="+applicationId+"&scope=bot&permissions=84992 ]");
			} else if(command.equalsIgnoreCase("setemg")) {
				if(args.length < 3)
					return;
				if(!isActiveArks(msg)) {
					RemoveTimer(channel, second, "権限所持者以外実行不可能です");
					return;
				}
				int cooltime = canRun(command);
				if(cooltime > 0) {
					String time = (cooltime/60)+"分"+(cooltime%60)+"秒";
					RemoveTimer(channel, second, "クールタイムが終わっていません\r\n【残り "+time+" 】");
					return;
				}
				if("random".equalsIgnoreCase(args[1])) {
					if(args.length == 4) {
						if(!NumberUtils.isNumber(args[3]) || (Integer.valueOf(args[3]) > 10 || Integer.valueOf(args[3]) < 1)) {
							RemoveTimer(channel, second,"1-10の範囲で入れてください");
							return;
						}
						Main.history.setEmergency(Integer.valueOf(args[3]), args[2]);
					} else {
						Main.history.setAllEmergency("random", args[2]);
					}
				} else if("notice".equalsIgnoreCase(args[1])) {
					if(args.length == 3)
						Main.history.setAllEmergency("notice", args[2]);
				}
				BotClientManager.updateStatus(false);
				Main.setemg = new ToggleCoolTime(60*60);
				Main.setemg.start();
			} else if(command.equalsIgnoreCase("update")) {
				Twitter twitter = new TwitterFactory().getInstance();
				User user = twitter.showUser("@pso2_emg_hour");
				MyUserStream.setHistory(twitter.getUserTimeline(user.getId()).get(0).getText().replaceAll("#PSO2", ""), true);
			}
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException | TwitterException | IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	@EventSubscriber
	public void onJoin(UserJoinEvent e) {
		IGuild guild = e.getGuild();
		try {
			if(e.getUser().isBot()) {
				guild.getChannelByID("190495171371204608").sendMessage("Botが追加されました Name: "+e.getUser().getName());
				return;
			}
			guild.getChannelByID("190495171371204608").sendMessage(e.getUser().mention()+"さん、いらっしゃい!"+e.getClient().getChannelByID("190789370335199232").mention()+" に自己紹介を書いてくれると感謝感謝です！");
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException e1) {
			e1.printStackTrace();
		}
	}

	IMessage sendText(IChannel channel, String... msgs) throws MissingPermissionsException, RateLimitException, DiscordException {
		String separator = "\r\n";
		String text = "";
		for(String msg : msgs)
			text = text+msg+separator;
		return channel.sendMessage(text);
	}
	
	void anga(IMessage imsg) throws MissingPermissionsException, RateLimitException, DiscordException {
		if(imsg.getContent().indexOf("アンガ") < 0)
			return;
		imsg.getChannel().sendMessage("ｷﾞｭﾙﾙﾙﾙﾙ ﾀﾞﾝﾀﾞﾝｯ↓ﾀｯﾀｯﾀ ﾀﾞﾀﾞﾝ↑×4ﾁｬﾗﾗﾗﾗﾗ～→ﾗ～↑ﾗ～↓ﾗ～→");
	}
	
	void RemoveTimer(IChannel channel, int second, String... msgs) throws MissingPermissionsException, RateLimitException, DiscordException {
		new CommandTextRemover(sendText(channel, msgs), second).start();
	}

	
	/*
	 * ちゃんと数字が入っているかのチェック
	 * -t <second>でsecondの入れ忘れの時の分岐
	 * 
	 */
	public int getSecond(String option) {
		String options[] = option.split("-");
		int second = 5;
		for(String str : options) {
			if(str.startsWith("t"))
				second = Integer.valueOf(str.split(" ")[1]);
		}
		return second;
	}
	
	private boolean isActiveArks(IMessage imsg) {
		if(!imsg.getGuild().getID().equalsIgnoreCase("190495171371204608"))
			return false;
		for(IRole role : imsg.getAuthor().getRolesForGuild(imsg.getGuild())) {
			if(role.getName().equalsIgnoreCase("Active-Arks"))
				return true;
		}
		return false;
	}
	
	private int canRun(String type) {
		if(type.equalsIgnoreCase("startup") || type.equalsIgnoreCase("shutdown")) {
			if(Main.toggleenable == null)
				return 0;
			return Main.toggleenable.getCooltime();
		} else if(type.equalsIgnoreCase("setemg")) {
			if(Main.setemg == null)
				return 0;
			return Main.setemg.getCooltime();
		} return 0;
	}
}
