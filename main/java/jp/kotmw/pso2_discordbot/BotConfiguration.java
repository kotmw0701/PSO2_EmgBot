package jp.kotmw.pso2_discordbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class BotConfiguration {
	
	private static String mothertoken;
	private static Map<Integer, String> tokens = new HashMap<>();
	private static Map<String, String> users = new HashMap<>();
	private static Properties config;
	private static String settingpath = "C:\\Discord_bot\\PSO2_EmgBot\\setting.properties";//コンパイル前には必ず置き換えること！
	private static String userspath = "C:\\Discord_bot\\PSO2_EmgBot\\users.properties";
	private static boolean saveflag = false;
	
	static {
		//Propertiesクラス使用
		config = new Properties();
		File file = new File(settingpath);
		try {
			if(!file.exists())
				file.createNewFile();
			config.load(new FileInputStream(file));
			if(!config.containsKey("mother")) {
				config.setProperty("mother", "");
				saveflag = true;
			}
			mothertoken = config.getProperty("mother");//中枢BotのToken設定
			for(int i = 1; i <= 10; i++) {
				if(!config.containsKey("ship"+i)) {
					config.setProperty("ship"+i, "");
					saveflag = true;
				}
				tokens.put(i, config.getProperty("ship"+i));//鯖別BotのToken設定
			}
			if(saveflag) config.store(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), "data update");
			config = new Properties();
			file = new File(userspath);
			if(!file.exists())
				file.createNewFile();
			config.load(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			config.entrySet().forEach(map -> users.put(checkBOM((String)map.getKey()), (String)map.getValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//BufferedReader使ってファイル自体読み込んでごにょごにょする
		/*BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("setting.properties")));
		String str;
		try {
			while((str = reader.readLine()) != null) {
				if(str.contains("mother"))
					mothertoken = str.split(":")[1];//中枢BotのToken設定
				else if(str.contains("ship"))
					tokens.put(Integer.valueOf(str.replaceAll("ship", "").split(":")[0]), str.split(":")[1]);//鯖別BotのToken設定
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public static String getMotherToken() {
		if(mothertoken.isEmpty())
			return null;
		return mothertoken;
	}
	
	public static String getToken(int server) {
		if(!tokens.containsKey(server))
			return null;
		else if(tokens.get(server).isEmpty())
			return null;
		return tokens.get(server);
	}
	
	public static List<String> getServerUsers(int server) {
		List<String> userlist = new ArrayList<>();
		for(Entry<String, String> entry : users.entrySet())
			if(entry.getValue().contains(String.valueOf(server)))
				userlist.add(entry.getKey());
		return userlist;
	}
	
	static void updateToken(String token, int server) {
		tokens.put(server, token);
	}
	
	public static void saveMotherToken(String token) throws FileNotFoundException, IOException {
		mothertoken = token;
		config.setProperty("mother", token);
		config.store(new FileOutputStream(settingpath), "comments");
	}
	
	private static String checkBOM(String text) {
		if(Integer.toHexString(text.charAt(0)).equals("feff"))
			return text.substring(1);
		return text;
	}
}
