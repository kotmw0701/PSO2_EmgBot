package jp.kotmw.pso2_discordbot.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jp.kotmw.pso2_discordbot.BotClientManager;
import jp.kotmw.pso2_discordbot.BotConfiguration;

public class ShipController implements Initializable {

	@FXML
	Label shipNum;
	
	@FXML
	Pane pane;
	
	@FXML
	ToggleButton toggle;
	
	int server;
	boolean canuse;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		toggle.setDisable(true);
	}
	
	@FXML
	public void toggleStatus(ActionEvent event) {
		if(BotConfiguration.getToken(server) == null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("エラー!");
			alert.setContentText("Tokenを設定してください");
			alert.show();
			toggle.setSelected(!toggle.isSelected());
			event.consume();
			return;
		}
		if(!changeStatus()) return;
		BotClientManager.changeEnable(server, toggle.isSelected());
	}
	
	private boolean changeStatus() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("確認");
		alert.setContentText("Botを"+ (toggle.isSelected() ? "稼働" : "停止") +"させてよろしいですか？");
		if(alert.showAndWait().get() == ButtonType.OK) {
			toggle(pane, toggle);
			return true;
		}
		toggle.setSelected(!toggle.isSelected());
		return false;
	}
	
	private void toggle(Pane pane, ToggleButton enable) {
		change(pane, enable.isSelected() ? StatusType.ENABLE : StatusType.DISABLE);
	}
	
	private void change(Pane pane, StatusType type) {
		Platform.runLater(() -> {
			Label label = (Label)pane.getChildren().get(0);
			label.setTextFill(type.equals(StatusType.STARTUP) ? Color.BLACK : Color.WHITE);
			switch(type) {
			case ENABLE:
				pane.setStyle("-fx-background-color: #0C0; -fx-border-style: solid;");
				label.setText("稼働中");
				break;
			case DISABLE:
				pane.setStyle("-fx-background-color: #C00; -fx-border-style: solid;");
				label.setText("停止中");
				break;
			case STARTUP:
				pane.setStyle("-fx-background-color: #FFD700; -fx-border-style: solid;");
				label.setText("準備中");
				break;
			case UNAVAILABLE:
				pane.setStyle("-fx-background-color: #003; -fx-border-style: solid;");
				label.setText("使用不可");
				label.setLayoutX(23.0);
				label.setLayoutY(6.0);
				break;
			}
			if(!type.equals(StatusType.UNAVAILABLE)) {
				label.setLayoutX(40.0);
				label.setLayoutY(6.0);
			}
		});
	}
	
	public void togglechange() {
		toggle.setSelected(!toggle.isSelected());
		toggle(pane, toggle);
	}
	
	public ShipController setServer(int server) {
		this.server = server;
		shipNum.setText("Ship"+server);
		return this;
	}
	
	public int getServer() {
		return server;
	}
	
	public void enable() {
		if(BotConfiguration.getToken(server) == null) {
			change(pane, StatusType.UNAVAILABLE);
			return;
		}
		change(pane, StatusType.DISABLE);
		toggle.setDisable(false);
	}
	
	private enum StatusType {
		ENABLE, DISABLE, STARTUP, UNAVAILABLE
	}
}
