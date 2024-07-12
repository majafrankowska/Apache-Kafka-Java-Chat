import org.apache.kafka.clients.producer.ProducerRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;

public class Chat extends JFrame {
    private JTextArea Chatview;
    private JTextField massage;
    private JButton sendButton;
    private JButton loginButton;
    private JTextField loginfield;
    private JList<String> activeusers;
    private JPanel MainPanel;
    private JButton logoutButton;
    private JLabel chatLabel;
    private JLabel ActibeUsersLabel;

    private final MassageConsumer massageConsumer;
    private final DefaultListModel<String> userModel;
    private final String id;
    private final String topic;

    public Chat(String id, String topic) throws HeadlessException {
        JFrame frame = this;
        this.id = id;
        this.topic = topic;
        massageConsumer = new MassageConsumer(topic, id);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.add(MainPanel);
        this.setVisible(true);
        this.setTitle(id);
        this.pack();

        chatLabel.setHorizontalAlignment(JLabel.CENTER);
        ActibeUsersLabel.setHorizontalAlignment(JLabel.CENTER);

        userModel = new DefaultListModel<>();
        activeusers.setModel(userModel);
        //addUser(id); // Add the current user to the user list

        // Notify others that this user has logged in
        MassageProducer.send(new ProducerRecord<>(topic, id + " logged in"));
        // Request current list of active users
        MassageProducer.send(new ProducerRecord<>(topic, id + " requests active users"));

        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                massageConsumer.kafkaConsumer.poll(Duration.of(1, ChronoUnit.SECONDS)).forEach(
                        m -> {
                            System.out.println(m);
                            String message = m.value();

                            if (message.contains("logged in")) {
                                String newUser = message.split(" ")[0];
                                addUser(newUser);
                                Chatview.append(newUser + " has logged in." + System.lineSeparator());
                            } else if (message.contains("logged out")) {
                                String leavingUser = message.split(" ")[0];
                                removeUser(leavingUser);
                                Chatview.append(leavingUser + " has logged out." + System.lineSeparator());
                            } else if (message.contains("requests active users")) {
                                // Send the current list of active users
                                String requestingUser = message.split(" ")[0];
                                StringBuilder response = new StringBuilder("active users response:");
                                for (int i = 0; i < userModel.size(); i++) {
                                    response.append(" ").append(userModel.get(i));
                                }
                                MassageProducer.send(new ProducerRecord<>(topic, response.toString()));
                            } else if (message.contains("active users response:")) {
                                // Update the list of active users
                                String[] parts = message.split(" ");
                                for (int i = 3; i < parts.length; i++) {
                                    addUser(parts[i]);
                                }
                            } else {
                                Chatview.append(message + System.lineSeparator());
                            }
                        }
                );
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = getTime() + " - " + id + ": " + massage.getText();
                MassageProducer.send(new ProducerRecord<>(topic, message));
                massage.setText("");
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newUser = loginfield.getText();
                if (!newUser.isEmpty()) {
                    //addUser(newUser);
                    //MassageProducer.send(new ProducerRecord<>(topic, newUser + " logged in"));
                    loginfield.setText("");
                    SwingUtilities.invokeLater(()->new Chat(newUser,"chat"));
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeUser(id);
                MassageProducer.send(new ProducerRecord<>(topic, id + " logged out"));
                frame.dispose();
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Notify others that this user is logging out
                removeUser(id);
                MassageProducer.send(new ProducerRecord<>(topic, id + " logged out"));
                super.windowClosing(e);
            }
        });
    }

    private void addUser(String user) {
        if (!userModel.contains(user)) {
            userModel.addElement(user);
        }
    }

    private void removeUser(String user) {
        userModel.removeElement(user);
    }

    private String getTime(){
        int godz = LocalDateTime.now().getHour();
        int min = LocalDateTime.now().getMinute();
        String output = godz+":";
        if(min<10){
            output = output+"0";
        }
        return output+min;

    }
}
