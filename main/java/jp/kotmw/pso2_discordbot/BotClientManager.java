package jp.kotmw.pso2_discordbot;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import jp.kotmw.pso2_discordbot.controllers.FxControllers;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;

public class BotClientManager {
	
	private static IDiscordClient mother;
	private static Map<Integer, IDiscordClient> client = new HashMap<>();
	
	public BotClientManager(boolean allenable) {
		mother = getClient("MjM2MTE4MDA5MTU5MDkwMTc2.CuEq4A.gOD-LT-12JFxq09zAujx4qxRwY0", true);
		if(!allenable)
			return;
		for(int i = 1; i <= 10; i++) {
			final int server = i;
			FxControllers.addLog("Server No."+server+" Enabling...");
			client.put(i, getClient(getToken(i), true));
			Platform.runLater(() -> FxControllers.getServer(server).togglechange());
			FxControllers.addLog("Server No."+server+" Enabled!");
			//System.out.println("---------------------------------------------------------  ship"+i+"----------------------------------");
		}
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
						client.put(server, getClient(getToken(server), true));
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
	
	private static String getToken(int server) {
		switch(server) {
		case 1:
			return "Mjc3NDkxOTYzNzMxNjQwMzIw.C3ek2Q.8JH6ykH9q0oWSH-FS6c63InVIK8";
		case 2:
			return "Mjc3NDkyMDMxNjU4NjU1NzQ1.C3ekyg.J7-rotRgypNiLqc8ME3HeFg1s0w";
		case 3:
			return "Mjc3NDkyMDg5NTk4NzcxMjAw.C3ekuQ.dX717HKkPVIKIco7qKPYv10_MFE";
		case 4:
			return "Mjc3NDkyMTQ3NjM5Mjg3ODEx.C3ekoA.gYBP5o8Ck1RagmneumA_KcLg4II";
		case 5:
			return "Mjc3NDkyMjIwMTg4NDI2MjQw.C3h3yA.3qO_HH1zHnSWX2H_L0UIvfxsZVE";
		case 6:
			return "Mjc3NDkyMzkwNTQ4MzQwNzQ4.C3h4Ew.8Lot6XW_UUJAPBqgp-WZ-0gKJEo";
		case 7:
			return "Mjc3NDkyNDQwNDE4NjE1Mjk3.C3h4Kw.zEFuOLo4dEA_regI8Kqvq7ZeN3w";
		case 8:
			return "Mjc3NDkyNDg5ODM2MDM2MDk3.C3h4UA.BjiJ5MtVoGhBpT2J4cw_f07aTXc";
		case 9:
			return "Mjc3NzIwOTEzMzYzODYxNTA0.C3h4aQ.JBVHPNgHcjBuBEBO0iusfyWgimU";
		case 10:
			return "Mjc3NzIxMTYzODc3MDU2NTEy.C3ipuA.1woEPQOdmokOTkppzBAmM85uAO8";
		default:
			break;
		}
		return null;
	}
}
