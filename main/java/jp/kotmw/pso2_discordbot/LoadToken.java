package jp.kotmw.pso2_discordbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LoadToken {
	
	private String mothertoken;
	private Map<Integer, String> tokens = new HashMap<>();
	private static final LoadToken instance = new LoadToken();
	
	private LoadToken() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("tokens.ini")));
		String str;
		try {
			while((str = reader.readLine()) != null) {
				if(str.contains("mother"))
					mothertoken = str.split(":")[1];
				else if(str.contains("ship"))
					tokens.put(Integer.valueOf(str.replaceAll("ship", "").split(":")[0]), str.split(":")[1]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static LoadToken getInstance(){
		return instance;
	}
	
	public String getMotherToken() {
		return mothertoken;
	}
	
	public String getToken(int server) {
		return tokens.get(server);
	}
}
