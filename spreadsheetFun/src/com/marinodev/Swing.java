package com.marinodev;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Swing {

    public static void main(String[] args) throws InterruptedException {
        var frame = new JFrame("Pixel Art Generator");
        initFrame(frame);

        JPanel panel = new JPanel();
        frame.add(panel);
        JPanel second = new JPanel();
        frame.add(second);

        placeComponents(panel);
        Component[] components = panel.getComponents();
        Arrays.stream(components).forEach(System.out::println);

        // Setting the frame visibility to true
        frame.setVisible(true);
        Thread.sleep(1_000);
        second.add(new JLabel("User"));
        frame.setVisible(true);
    }

    private static void initFrame(JFrame frame) {
        // Setting the width and height of frame
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        // Creating JLabel
        JLabel userLabel = new JLabel("User");
        /* This method specifies the location and size
         * of component. setBounds(x, y, width, height)
         * here (x,y) are coordinates from the top left
         * corner and remaining two arguments are the width
         * and height of the component.
         */
        userLabel.setBounds(10,20,80,25);
        panel.add(userLabel);

        /* Creating text field where user is supposed to
         * enter user name.
         */
        JTextField userText = new JTextField(20);
        userText.setBounds(100,20,165,25);
        panel.add(userText);

        // Same process for password label and text field.
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10,50,80,25);
        panel.add(passwordLabel);

        /*This is similar to text field but it hides the user
         * entered data and displays dots instead to protect
         * the password like we normally see on login screens.
         */
        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100,50,165,25);
        panel.add(passwordText);

        // Creating login button
        JButton loginButton = new JButton("login");
        loginButton.setBounds(10, 80, 80, 25);
        panel.add(loginButton);
    }

}