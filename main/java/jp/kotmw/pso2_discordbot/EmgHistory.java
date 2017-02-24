package jp.kotmw.pso2_discordbot;

import java.util.HashMap;
import java.util.Map;

public class EmgHistory {

	private String notice;
	private Map<Integer,String> ships = new HashMap<>();

	public EmgHistory(String allemg) {
		for(int i = 1; i <= 10; i++) {
			if(allemg.startsWith("Notice:")) {
				ships.put(i, allemg.replaceAll("Notice:", ""));
				notice = allemg.replaceAll("Notice:", "");
			} else if(allemg.startsWith("Random:")) {
				ships.put(i, allemg.replaceAll("Random:", "").split("/")[i-1].split(":")[1]);
				notice = "ランダム緊急がある可能性";
			}
		}
	}
	
	public void setAllEmergency(String type, String emg) {
		for(int i = 1; i <= 10; i++) {
			if(type.equalsIgnoreCase("notice")) {
				ships.put(i, emg);
				notice = emg;
			} else if(type.equalsIgnoreCase("random")) {
				ships.put(i, emgTextConverter(emg)[i-1]);
				notice = "ランダム緊急がある可能性";
			}
		}
	}
	
	private String[] emgTextConverter(String emg) {
		emg = emg.replace(",", "/");
		String[] emgarray = new String[10];
		int server = 0;
		for(String emgs : emg.split("/")) {
			if(emgs.indexOf(":") > -1) {
				server = Integer.valueOf(emgs.split(":")[0])-1;
				emgs = emgs.split(":")[1];
			}
			emgarray[server] = emgs;
			server++;
		}
		return emgarray;
	}
	
	public void setEmergency(int server, String emg) {
		ships.put(server, emg);
	}

	public String getEmergency(int server) {
		if(!ships.containsKey(server))
			return "無し";
		return ships.get(server);
	}
	
	public String getNotice() {
		return notice;
	}
}
