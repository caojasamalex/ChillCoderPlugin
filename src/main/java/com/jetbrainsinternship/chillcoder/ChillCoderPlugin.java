package com.jetbrainsinternship.chillcoder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class ChillCoderPlugin implements ToolWindowFactory {
    private int pomodoroSessionSeconds = 1500; // Pomodoro productivity session time
    private int shortBreakSeconds = 300; // Short break time - 5 minutes Default
    private int longBreakSeconds = 900; // Long break time - 15 minutes Default

    private int currentTimerTime = pomodoroSessionSeconds;

    boolean isPomodoroActive = true;
    boolean isBreakActive = false;

    boolean breakSessionType = false; // False - Short break; True - Long break

    boolean popUpNotification = true;
    boolean soundNotification = true;

    private JLabel timeLabel;
    //private JSlider volumeSlider;

    private final Timer timer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPomodoroActive && currentTimerTime > 0) {
                currentTimerTime--;
                timeLabel.setText(formatTime(currentTimerTime));
            } else if (!isPomodoroActive && isBreakActive && currentTimerTime > 0) {
                currentTimerTime--;
                timeLabel.setText(formatTime(currentTimerTime));
            } else {
                timer.stop();
                String message = null;

                if(isPomodoroActive){
                    message = "Pomodoro ended. Take a short break !";
                    isPomodoroActive = false;
                    isBreakActive = true;
                } else {
                    if(isBreakActive) message = "Your break ended, it's time to be productive again !";
                    isPomodoroActive = true;
                    isBreakActive = false;
                }

                if (soundNotification) {
                    Toolkit.getDefaultToolkit().beep();
                }

                JOptionPane.showMessageDialog(null, message);
                resetTimer();
            }
        }
    });

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 2));

        panel.add(getSessionTypeButtonPanel());

        timeLabel = new JLabel(formatTime(currentTimerTime), SwingConstants.CENTER);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        panel.add(timeLabel);

        panel.add(getControlButtonPanel(toolWindow));

//        // Pretraga muzike
//        JButton searchButton = new JButton("Search Music");
//        searchButton.addActionListener(e -> showSearchDialog(toolWindow));
//        panel.add(searchButton);
//
//        // Volume podešavanje
//        volumeSlider = new JSlider(0, 100, 50); // Početna vrednost: 50
//        panel.add(new JLabel("Music Volume"));
//        panel.add(volumeSlider);

        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory().createContent(panel, "Productivity", false);
        contentManager.addContent(content);
    }

    private @NotNull JPanel getSessionTypeButtonPanel(){
        JPanel sessionTypeButtonPanel = new JPanel(new FlowLayout());

        JButton pomodoroButton = new JButton("Pomodoro");
        pomodoroButton.addActionListener(e -> selectSession("p")); // Select Pomodoro session
        sessionTypeButtonPanel.add(pomodoroButton);

        JButton shortBreakButton = new JButton("Short Break");
        shortBreakButton.addActionListener(e -> selectSession("sb")); // Select Short Break session
        sessionTypeButtonPanel.add(shortBreakButton);

        JButton longBreakButton = new JButton("Long Break");
        longBreakButton.addActionListener(e -> selectSession("lb"));
        sessionTypeButtonPanel.add(longBreakButton);

        return sessionTypeButtonPanel;
    }

    private @NotNull JPanel getControlButtonPanel(ToolWindow toolWindow) {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetTimer());
        buttonPanel.add(resetButton);

        JButton startButton = new JButton("Start/Stop");
        startButton.addActionListener(e -> startStopTimer());
        buttonPanel.add(startButton);

        JButton stopButton = new JButton("Skip");
        stopButton.addActionListener(e -> skipTimer());
        buttonPanel.add(stopButton);

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> showSettingsTab(toolWindow));
        buttonPanel.add(settingsButton);

        return buttonPanel;
    }

    private void showSettingsTab(ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        for (int i = 0; i < contentManager.getContentCount(); i++) {
            Content content = contentManager.getContent(i);
            if (content != null && "Settings".equals(content.getDisplayName())) {
                contentManager.setSelectedContent(content);
                return;
            }
        }

        JPanel settingsPanel = new JPanel(new FlowLayout());
        settingsPanel.add(createSettingsPanel(toolWindow));
        Content content = contentManager.getFactory().createContent(settingsPanel, "Settings", false);
        contentManager.addContent(content);
    }

    private @NotNull JPanel createSettingsPanel(ToolWindow toolWindow) {
        JPanel settingsPanel = new JPanel(new GridLayout(8,1));

        settingsPanel.add(new JLabel("Pomodoro Duration (min): "));
        JTextField pomodoroField = new JTextField(String.valueOf(pomodoroSessionSeconds / 60), 5);
        settingsPanel.add(pomodoroField);

        settingsPanel.add(new JLabel("Short Break Duration (min): "));
        JTextField shortBreakField = new JTextField(String.valueOf(shortBreakSeconds / 60), 5);
        settingsPanel.add(shortBreakField);

        settingsPanel.add(new JLabel("Long Break Duration (min): "));
        JTextField longBreakField = new JTextField(String.valueOf(longBreakSeconds / 60), 5);
        settingsPanel.add(longBreakField);

        settingsPanel.add(new JLabel("Primary Break: "));
        JComboBox<String> breakTypeCombo = new JComboBox<>(new String[]{"Short Break", "Long Break"});
        breakTypeCombo.setSelectedIndex(breakSessionType ? 1 : 0);
        settingsPanel.add(breakTypeCombo);

        JCheckBox popupCheckBox = new JCheckBox("Enable Popup Notification", popUpNotification);
        JCheckBox soundCheckBox = new JCheckBox("Enable Sound Notification", soundNotification);
        settingsPanel.add(popupCheckBox);
        settingsPanel.add(soundCheckBox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                int pomodoroDuration = Integer.parseInt(pomodoroField.getText());
                if (pomodoroDuration > 0) pomodoroSessionSeconds = pomodoroDuration * 60;
                else throw new IllegalArgumentException("Pomodoro session duration must be a positive number.");

                int shortBreakDuration = Integer.parseInt(shortBreakField.getText());
                int longBreakDuration = Integer.parseInt(longBreakField.getText());

                if (shortBreakDuration > 0 && longBreakDuration > 0 && shortBreakDuration < longBreakDuration) {
                    shortBreakSeconds = shortBreakDuration * 60;
                    longBreakSeconds = longBreakDuration * 60;
                } else {
                    throw new IllegalArgumentException("Short break duration must be positive and less than the long break duration.");
                }

                breakSessionType = breakTypeCombo.getSelectedIndex() == 1;

                popUpNotification = popupCheckBox.isSelected();
                soundNotification = soundCheckBox.isSelected();

                JOptionPane.showMessageDialog(null, "Settings saved successfully!", "Settings", JOptionPane.INFORMATION_MESSAGE);

                resetTimer();

                ContentManager contentManager = toolWindow.getContentManager();
                for (int i = 0; i < contentManager.getContentCount(); i++) {
                    Content content = contentManager.getContent(i);
                    if (content != null && "Settings".equals(content.getDisplayName())) {
                        contentManager.removeContent(content, true);
                        break;
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error saving settings: " + ex.getMessage(), "Settings", JOptionPane.ERROR_MESSAGE);
            }
        });
        settingsPanel.add(saveButton);

        return settingsPanel;
    }


    private void skipTimer(){
        if(isPomodoroActive){
            isBreakActive = true;
            isPomodoroActive = false;
        } else {
            isBreakActive = false;
            isPomodoroActive = true;
        }

        resetTimer();
    }

    private void selectSession(final String p) {
        switch (p) {
            case "p" -> {
                isPomodoroActive = true;
                isBreakActive = false;
                if (timer.isRunning()) {
                    timer.stop();
                }
                resetTimer();
            }
            case "sb" -> {
                isPomodoroActive = false;
                isBreakActive = true;
                if (timer.isRunning()) {
                    timer.stop();
                }
                currentTimerTime = shortBreakSeconds;
                timeLabel.setText(formatTime(currentTimerTime));
            }
            case "lb" -> {
                isPomodoroActive = false;
                isBreakActive = true;
                if (timer.isRunning()) {
                    timer.stop();
                }
                currentTimerTime = longBreakSeconds;
                timeLabel.setText(formatTime(currentTimerTime));
            }
            default -> {
            }
        }
    }

    private void startStopTimer() {
        if (timer.isRunning()) {
            timer.stop();
        } else {
            timer.start();
        }
    }

    private void resetTimer() {
        if(timer.isRunning()) timer.stop();

        currentTimerTime = isPomodoroActive ? pomodoroSessionSeconds : breakSessionType ? longBreakSeconds : shortBreakSeconds;

        timeLabel.setText(formatTime(currentTimerTime));
    }

    private @NotNull String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

//    private void showSearchDialog(ToolWindow toolWindow) {
//        String query = JOptionPane.showInputDialog("Enter search term:");
//        if (query != null && !query.trim().isEmpty()) {
//            java.util.List<VideoResult> results = YouTubeSearch.searchVideos(query);
//
//            if (!results.isEmpty()) {
//                VideoResult selectedVideo = (VideoResult) JOptionPane.showInputDialog(
//                        null, "Select a video:", "Search Results",
//                        JOptionPane.PLAIN_MESSAGE, null,
//                        results.toArray(), results.get(0)
//                );
//
//                if (selectedVideo != null) {
//                    playVideo(selectedVideo.getVideoUrl(), toolWindow);
//                }
//            } else {
//                JOptionPane.showMessageDialog(null, "No results found.");
//            }
//        }
//    }
//
//    private void playVideo(String videoUrl, ToolWindow toolWindow) {
//        // Create a new panel for video playback
//        JPanel videoPanel = new JPanel(new BorderLayout());
//        JEditorPane editorPane = new JEditorPane();
//        editorPane.setEditable(false);
//
//        try {
//            editorPane.setPage(videoUrl);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        videoPanel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
//
//        ContentManager contentManager = toolWindow.getContentManager();
//        Content content = contentManager.getFactory().createContent(videoPanel, "Video Playback", false);
//        contentManager.addContent(content);
//    }
}