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
				channel.sendMessage("**%emergency [server]** 現在予告が出てる緊急を表示します");
			} else if(command.equalsIgnoreCase("emg") || command.equalsIgnoreCase("emergency")) {
				int server = 2;
				if(args.length == 2) {
					if(!NumberUtils.isNumber(args[1]) || (Integer.valueOf(args[1]) > 10 || Integer.valueOf(args[1]) < 1)) {
						RemoveTimer(channel.sendMessage("1-10の範囲で入れてください"), second);
						return;
					}
					server = Integer.valueOf(args[1]);
				}
				RemoveTimer(channel.sendMessage(server+" 鯖 : "+ "**"+Main.history.getEmergency(server)+"**"), second);
			} else if(command.equalsIgnoreCase("startup") || command.equalsIgnoreCase("shutdown")) {
				if(args.length != 2)
					return;
				if(!isActiveArks(msg)) {
					RemoveTimer(channel.sendMessage("権限所持者以外実行不可能です"), second);
					return;
				}
				int cooltime = canRun(command);
				if(cooltime > 0) {
					RemoveTimer(channel.sendMessage("クールタイムが終わっていません 【残り "+cooltime+" 秒】"), second);
					return;
				}
				if(!NumberUtils.isNumber(args[1]) || (Integer.valueOf(args[1]) > 10 || Integer.valueOf(args[1]) < 1)) {
					RemoveTimer(channel.sendMessage("1-10の範囲で入れてください"), second);
					return;
				}
				boolean enable = command.equalsIgnoreCase("startup");
				final int server = Integer.valueOf(args[1]);
				if(!BotClientManager.changeEnable(Integer.valueOf(args[1]), enable)) {
					RemoveTimer(channel.sendMessage(enable ? "稼働中です" : "稼働してません"), second);
					return;
				}
				Platform.runLater(() ->FxControllers.getServer(server).togglechange());
				RemoveTimer(channel.sendMessage(server+"鯖のBotを"+(enable ? "稼働" : "停止")+"させました"), second);
			} else if(command.equalsIgnoreCase("geturl")) {
				if(args.length != 2)
					return;
				if(!isActiveArks(msg)) {
					RemoveTimer(channel.sendMessage("権限所持者以外実行不可能です"), second);
					return;
				}
				if(!NumberUtils.isNumber(args[1]) || (Integer.valueOf(args[1]) > 10 || Integer.valueOf(args[1]) < 1)) {
					RemoveTimer(channel.sendMessage("1-10の範囲で入れてください"), second);
					return;
				}
				final int server = Integer.valueOf(args[1]);
				channel.sendMessage(server+"鯖Botの認証URL [ https://discordapp.com/oauth2/authorize?client_id="+getID(server)+"&scope=bot&permissions=84992 ]");
			} else if(command.equalsIgnoreCase("setemg")) {
				if(args.length < 3)
					return;
				if(!isActiveArks(msg)) {
					RemoveTimer(channel.sendMessage("権限所持者以外実行不可能です"), second);
					return;
				}
				int cooltime = canRun(command);
				if(cooltime > 0) {
					String time = (cooltime/60)+"分"+(cooltime%60)+"秒";
					RemoveTimer(channel.sendMessage("クールタイムが終わっていません\r\n【残り "+time+" 】"), second);
					return;
				}
				if("random".equalsIgnoreCase(args[1])) {
					if(args.length == 4) {
						if(!NumberUtils.isNumber(args[3]) || (Integer.valueOf(args[3]) > 10 || Integer.valueOf(args[3]) < 1)) {
							RemoveTimer(channel.sendMessage("1-10の範囲で入れてください"), second);
							return;
						}
						Main.history.setEmergency(Integer.valueOf(args[3]), args[2]);
					} else {
						Main.history.setAllEmergency("random", args[2]);
					}
				} else if("notice".equalsIgnoreCase(args[1])) {
					Main.history.setAllEmergency("notice", args[2]);
				}
				BotClientManager.updateStaus();
				Main.setemg = new ToggleCoolTime(60*60);
				Main.setemg.start();
			} else if(command.equalsIgnoreCase("update")) {
				Twitter twitter = new TwitterFactory(Main.config).getInstance();
				User user = twitter.showUser("@pso2_emg_hour");
				MyUserStream.setHistory(twitter.getUserTimeline(user.getId()).get(0).getText().replaceAll("#PSO2", ""), true);
			}
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException | TwitterException | IOException e1) {
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
	
	void RemoveTimer(IMessage imessage, int second) {
		new CommandTextRemover(imessage, second).start();
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
	
	private String getID(int server) {
		switch(server) {
		case 1:return "277491963731640320";
		case 2:return "277492031658655745";
		case 3:return "277492089598771200";
		case 4:return "277492147639287811";
		case 5:return "277492220188426240";
		case 6:return "277492390548340748";
		case 7:return "277492440418615297";
		case 8:return "277492489836036097";
		case 9:return "277720913363861504";
		case 10:return "277721163877056512";
		default:
			break;
		}
		return "236118009159090176";
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
