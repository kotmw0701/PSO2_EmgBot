package jp.kotmw.pso2_discordbot;

import java.util.HashMap;
import java.util.Map;

import jp.kotmw.pso2_discordbot.controllers.FxControllers;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;

public class BotClientManager {
	
	private static IDiscordClient mother;
	private static Map<Integer, IDiscordClient> client = new HashMap<>();
	
	public BotClientManager() {
		mother = getClient(BotConfiguration.getMotherToken(), true);
	}
	
	public IDiscordClient getMotherClient() {
		return mother;
	}
	
	private static IDiscordClient getClient(int server) {
		if(!client.containsKey(server))
			return null;
		return client.get(server);
	}
	
	public static boolean changeEnable(int server, boolean enable) {
		if(!enable && (!client.containsKey(server)))
			return false;
		if(enable)
			if(client.containsKey(server) && client.get(server).isLoggedIn())
				return false;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(enable) {
						FxControllers.addLog("Server No."+server+" Enabling...");
						client.put(server, getClient(BotConfiguration.getToken(server), true));
						setServerStatus(server);
						FxControllers.addLog("Server No."+server+" Enabled!");
					} else {
						FxControllers.addLog("Server No."+server+" Disabling...");
						client.get(server).logout();
						client.remove(server);
						FxControllers.addLog("Server No."+server+" Disabled!");
					}
				} catch (DiscordException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		return true;
	}
	
	public void shutdown() throws DiscordException {
		mother.logout();
		for(int i = 1; i <= 10; i++)
			getClient(i).logout();
	}
	
	private static IDiscordClient getClient(String token, boolean login) { // Returns an instance of the Discord client
		if(token == null || token.isEmpty())
			return null;
		ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
		clientBuilder.withToken(token); // Adds the login info to the builder
		try {
			if (login) {
				return clientBuilder.login();
				// Creates the client instance and logs the client in
			} else {
				return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
			}
		} catch (DiscordException e) {
			Main.enableerror++;
			e.printStackTrace();
		}
		return null;
	}
	
	/*public static void changeStatus(String emgtext, String hour, boolean delay){
		try {
			if(delay)
				Thread.sleep(5*1000);
			String text = null;
			boolean notice = false;
			if(emgtext.startsWith("Notice:")) {
				text = emgtext.replaceAll("Notice:", "").equalsIgnoreCase("無し") ? hour+"時の緊急はありません" : emgtext.replaceAll("Notice:", "");
				notice = true;
			}
			setStatus(text, notice);
			if(Main.setemg != null)
				Main.setemg.reset();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/
	
	public static String getID(int server) throws DiscordException {
		IDiscordClient client = getClient(server);
		if(client == null)
			return null;
		return client.getApplicationClientID();
	}
	
	/*private static void setStatus(boolean notice) {
		Main.manager.getMotherClient().changeStatus(Status.game(notice ? "" : Main.history.getNotice()));
		for(int i = 1; i <= 10; i++) {
			IDiscordClient client = getClient(i);
			if(client == null)
				continue;
			client.changeStatus(Status.game(notice ? Main.history.getNotice() : Main.history.getEmergency(i)));
		}
	}*/
	
	public static void updateStatus(boolean delay) throws InterruptedException {
		if(delay)
			Thread.sleep(5*1000);
		Main.manager.getMotherClient().changeStatus(Status.game(Main.history.getNotice()));
		for(int i = 1; i <= 10; i++) {
			IDiscordClient client = getClient(i);
			if(client == null)
				continue;
			client.changeStatus(Status.game(Main.history.getEmergency(i)));
		}
	}
	
	private static void setServerStatus(int server) throws InterruptedException {
		Thread.sleep(3*1000);
		getClient(server).changeStatus(Status.game(Main.history.getEmergency(server)));
	}
}
