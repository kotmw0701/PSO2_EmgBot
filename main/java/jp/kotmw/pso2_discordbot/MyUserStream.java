package jp.kotmw.pso2_discordbot;

import java.io.IOException;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import twitter4j.Status;
import twitter4j.UserStreamAdapter;

public class MyUserStream extends UserStreamAdapter {

	@Override
	public void onStatus(Status status) {
		super.onStatus(status);
		/*try {
			omikuzi(status);
		} catch (TwitterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		if(!status.getUser().getScreenName().equalsIgnoreCase("pso2_emg_hour") && (!status.getUser().getScreenName().equalsIgnoreCase("kotmw_sub") || !Main.debug))
			return;
		if(status.isRetweeted()) {
			System.out.println("RTのツイートのため流しません");
			System.out.println(status.getText());
			return;
		}
		String allemg = status.getText().replaceAll("#PSO2", "");
		if(!allemg.contains("緊急クエスト予告"))
			return;
		try {
			Main.manager.getMotherClient().getChannelByID("236138218955866128").sendMessage("───────────────" +Main.sepa+ allemg);
			EmgHistory.getInstance().setHistory(allemg, false);
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void serverNotice(int server, String emg) {
		IDiscordClient bot = BotClientManager.getClient(server);
		if(bot == null)
			return;
		try {
			bot.getGuildByID("190495171371204608").getChannelByID("190495171371204608").sendMessage(getMentions(server)+ emg);
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*bot.getGuilds().forEach(guild -> {
			try {
				guild.getChannelByID(channelid).sendMessage(getMentions(server)+ emg);
			} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});*/
	}
	
	private static String getMentions(int server) {
		String mentions = "";
		for(String id : BotConfiguration.getServerUsers(server))
			mentions += Main.manager.getMotherClient().getGuildByID("190495171371204608").getUserByID(id)+ " ";
		return mentions;
	}
	
	/*private void omikuzi(Status status) throws TwitterException {
		if(!status.getInReplyToScreenName().equalsIgnoreCase("kotmw0701") || (status.getText().indexOf("おみくじ")<0) )
			return;
		String[] array = {"……素晴らしい。素晴らしい、素晴らしいぞこれは！頭の中を、知識が駆け巡る！ああ、ああ！　破裂してしまいそうだ！この知識の奔流に！",
				"素晴らしく運が良いな、君は",
				"大吉",
				"中吉",
				"小吉",
				"吉",
				"末吉",
				"末小吉",
				"凶",
				"小凶",
				"半凶",
				"末凶",
				"大凶",
				"素晴らしく運が無いな、君は",
				"そんな……こぼれていく……手にしたはずの、知識が……！ああ……ああ……あああっ！全知が、宇宙の理が……僕の中から……滑り落ちていく！……終わりだ。全て終わりだ。"};
		List<String> list = Arrays.asList(array);
		Collections.shuffle(list);
		Twitter twitter = new TwitterFactory(Main.config).getInstance();
		twitter.updateStatus(new StatusUpdate("@"+status.getUser().getScreenName()+" "+list.get(0)).inReplyToStatusId(status.getId()));
	}*/
}
