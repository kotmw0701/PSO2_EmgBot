package jp.kotmw.pso2_discordbot.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jp.kotmw.pso2_discordbot.EmgHistory;
import jp.kotmw.pso2_discordbot.Main;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class FxControllers implements Initializable {
	
	public static FxControllers controller;
	
	@FXML
	TextField textfield;
	
	@FXML
	TextArea logfield;
	
	@FXML
	Label cooltime;
	
	@FXML
	private ShipController ship1Controller;
	@FXML
	private ShipController ship2Controller;
	@FXML
	private ShipController ship3Controller;
	@FXML
	private ShipController ship4Controller;
	@FXML
	private ShipController ship5Controller;
	@FXML
	private ShipController ship6Controller;
	@FXML
	private ShipController ship7Controller;
	@FXML
	private ShipController ship8Controller;
	@FXML
	private ShipController ship9Controller;
	@FXML
	private ShipController ship10Controller;
	
	private static List<ShipController> ships = new ArrayList<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		controller = this;
		ships.add(ship1Controller.setServer(1));
		ships.add(ship2Controller.setServer(2));
		ships.add(ship3Controller.setServer(3));
		ships.add(ship4Controller.setServer(4));
		ships.add(ship5Controller.setServer(5));
		ships.add(ship6Controller.setServer(6));
		ships.add(ship7Controller.setServer(7));
		ships.add(ship8Controller.setServer(8));
		ships.add(ship9Controller.setServer(9));
		ships.add(ship10Controller.setServer(10));
		textfield.setText(null);
		textfield.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)) {
					sendText();
				}
			}
		});
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Platform.runLater(() -> {
		logfield.lookup(".content").setStyle("-fx-background-color: #000;");
		});
	}
	
	@FXML
	public void allStop(ActionEvent event) throws DiscordException {
		Main.shutdownConfirnation(event);
	}
	
	@FXML
	public void sendText(ActionEvent event) throws MissingPermissionsException, RateLimitException, DiscordException {
		sendText();
	}
	
	@FXML
	public void openSettingWindow(ActionEvent event) throws IOException {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(Main.getPrimary());
		stage.setTitle("設定");
		stage.getIcons().add(new Image("arks_logo.jpg"));
		stage.setResizable(false);
		stage.setScene(new Scene(FXMLLoader.load(ClassLoader.getSystemResource("SettingWindow.fxml"))));
		stage.show();
	}
	
	private void sendText() {
		String text = textfield.getText();
		if(text == "")
			return;
		try {
			if(text.startsWith("%")) {
				if(text.startsWith("%pattern")) 
					addLog(""+text.split(" ")[1].matches("メンテナンス\\d時間前です"));
				else if(text.startsWith("%debug")) {
					String txt = String.valueOf(Main.toggleDebugmode());
					addLog("デバッグモードを "+txt+" に変更しました");
				} else if(text.startsWith("%notice"))
					EmgHistory.getInstance().noticeTimer(Calendar.SECOND, 1);
				textfield.setText("");
				return;
			}
			Main.manager.getMotherClient().getChannelByID("236138218955866128").sendMessage(text);
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addLog(text);
		textfield.setText("");
	}
	
	public static ShipController getServer(int server) {
		return ships.get(server-1);
	}
	
	public static void addLog(Object content) {
		if(content instanceof Exception) {
			System.out.println("error?");
			PrintWriter writer = new PrintWriter(new StringWriter());
			Exception ex = (Exception) content;
			ex.printStackTrace(writer);
			content = ex.toString();
			System.out.println(ex.toString());
		}
		final String text = String.valueOf(content);
		Calendar calendar = new GregorianCalendar();
		SimpleDateFormat simpledate = new SimpleDateFormat("HH:mm:ss");
		Platform.runLater(()-> {
			controller.logfield.appendText("["+simpledate.format(calendar.getTime())+"]: "+text+"\r\n");
		});
	}
	
	public static void updateCooltime(int second) {
		String time = (String.valueOf((second/60)).length() == 2 ? (second/60) : "0"+(second/60))+":"+(String.valueOf((second%60)).length() == 2 ? (second%60) : "0"+(second%60));
		Platform.runLater(()-> {
			controller.cooltime.setText("Cooltime: "+time);
		});
	}
}
