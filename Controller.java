package sample;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import javax.swing.JFileChooser;
import java.awt.Frame;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    private String path;
    private File dir ;
    private String[] files;
    private Pattern expr;
    private MediaPlayer player;
    private Duration duration;

    @FXML
    public MediaView view;
    @FXML
    public Label dirName;
    @FXML
    public Slider sltime;
    @FXML
    public Button start;
    @FXML
    public ListView lstDir;
    @FXML
    private Slider slsound;
    @FXML
    private Label playTime;
    @FXML
    private Label showName;

    @FXML
    public void initialize() {
    }

    @FXML
    public void changeFile(ActionEvent actionEvent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int flag = chooser.showOpenDialog(new Frame());
        if (flag == JFileChooser.APPROVE_OPTION) {
            path = chooser.getSelectedFile().toString();
            dirName.setText("Directory: " + path);
            dir = new File(path);
            files = dir.list();
            lstDir.getItems().clear();
            for (String file : files) {
                if (check(file)){
                    lstDir.getItems().add(file);
                }
            }
        }
    }

    @FXML
    public void selectFile(MouseEvent mouseEvent) {
        if (view.getMediaPlayer() != null){
            view.getMediaPlayer().stop();
        }
        if (lstDir.getSelectionModel().getSelectedItem() == null){
            return;
        }
        sltime.setValue(0);
        slsound.setValue(100);
        String filename = path + "\\" + lstDir.getSelectionModel().getSelectedItem();
        start.setText("start");
        showName.setText((String) lstDir.getSelectionModel().getSelectedItem());
        Media pick = new Media(new File(filename).toURI().toString());
        player = new MediaPlayer(pick);
        view.setMediaPlayer(player);
        sltime.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                player.seek(duration.multiply(sltime.getValue() / 100.0));
            }
        });
        sltime.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                player.seek(duration.multiply(sltime.getValue() / 100.0));
            }
        });
        sltime.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                player.seek(duration.multiply(sltime.getValue() / 100.0));
            }
        });
        slsound.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (slsound.isValueChanging()) {
                    player.setVolume(slsound.getValue() / 100.0);
                }
            }
        });
        player.setOnReady(new Runnable() {
            public void run() {
                duration = player.getMedia().getDuration();
                updateValues();
            }
        });
        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
                updateValues();
            }
        });

        player.setOnPlaying(new Runnable() {
            public void run() {
                start.setText("pause");
            }
        });
        player.setOnPaused(new Runnable() {
            public void run() {
                start.setText("continue");
            }
        });
        player.setOnEndOfMedia(new Runnable() {
            public void run() {
                start.setText("replay");
            }
        });
        view.setPreserveRatio(true);
    }

    private boolean check(String text) {
        expr = Pattern.compile("\\.mp4|\\.mp3|\\.m4a|\\.m4v|\\.wav");
        Matcher m = expr.matcher(text);
        return m.find();
    }

    @FXML
    public void startMedia(ActionEvent actionEvent) {
        if (start.getText().equals("start") || start.getText().equals("replay")){
            sltime.setValue(0);
            player.seek(duration.multiply(0));
            view.getMediaPlayer().play();
        }
        else if (start.getText().equals("pause")){
            view.getMediaPlayer().pause();
        }
        else if(start.getText().equals("continue")){
            view.getMediaPlayer().play();
        }
    }


    protected void updateValues() {

        if (playTime != null && sltime != null && slsound != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = player.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    sltime.setDisable(duration.isUnknown());
                    if (!sltime.isDisabled() && duration.greaterThan(Duration.ZERO) && !sltime.isValueChanging()) {
                        sltime.setValue(currentTime.divide(duration).toMillis() * 100.0);
                    }
                    if (!slsound.isValueChanging()) {
                        slsound.setValue((int)Math.round(player.getVolume() * 100));
                    }
                }
            });
        }
    }
    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int)Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int)Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 -
                    durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
}
