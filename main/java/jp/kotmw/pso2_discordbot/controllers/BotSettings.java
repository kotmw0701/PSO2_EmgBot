package jp.kotmw.pso2_discordbot.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import jp.kotmw.pso2_discordbot.BotClientManager;
import jp.kotmw.pso2_discordbot.util.MessageUtil;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class BotSettings extends MessageUtil implements Initializable {

	@FXML
	TextArea updateMessage;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		updateMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.END)) {
					String text = updateMessage.getText();
					if(text == "")
						return;
					for(int i = 1; i<=10; i++) {
						IDiscordClient client = BotClientManager.getClient(i);
						if(client == null)
							continue;
						try {
							sendGuildsMessageforNoticeChannel(client.getGuilds(), text);
						} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
							FxControllers.addLog(e);
						}
					}
					updateMessage.setText("");
				}
			}
		});
	}
}
