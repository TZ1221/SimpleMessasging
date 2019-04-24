package edu.northeastern.ccs.im.chatter;

import java.awt.*;
import java.util.Scanner;

import edu.northeastern.ccs.im.*;

/**
 * Class which can be used as a command-line IM client.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class CommandLineMain {

	/**
	 * This main method will perform all of the necessary actions for this phase of
	 * the course project.
	 *
	 * @param args Command-line arguments which we ignore
	 */
	public static void main(String[] args) {
		IMConnection connect;
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		String username, password, repassword, action;

		ChatterMessage loginValidateMessage, registerResponse;

		do {
			// Create a Connection to the IM server.
			connect = new IMConnection(args[0], Integer.parseInt(args[1]));

		} while (!connect.connect());

		// Create the objects needed to read IM messages.
		MessageScanner mess = connect.getMessageScanner();

		// Repeat the following loop
		if (connect.connectionActive()) {
			while (true) {

				// Prompt the user to type in a username.
				System.out.println("Register Or Login?");
				action = in.nextLine();
				if ("Register".equals(action)) {
					while (true) {
						System.out.println("What username would you like?");
						username = in.nextLine();
						if (!ChatterUtil.validateUsernameAndPassword(username)) {
							System.out.println("Username can only contains Character and Digit.");
							continue;
						}
						break;
					}

					while (true) {
						System.out.println("What password would you like?");
						System.out.println("Password can only contain Characters and Digits.");
						password = in.nextLine();
						System.out.println("Confirm password");
						repassword = in.nextLine();
						if (password.equals(repassword) && ChatterUtil.validateUsernameAndPassword(password)) {
							break;
						}
						System.out.println("Your new password entries do not match. Please try again");
					}
					connect.sendUserInfoMessage(username, password, action);
					while (true) {
						if (mess.hasNext()) {
							registerResponse = mess.next();
							break;
						}
					}
					if (registerResponse.getText().equals("succeed to register")) {
						System.out.println(registerResponse.getText());
						System.out.println(" You have logged in.");
						break;
					} else if (registerResponse.getText().equals("fail to register")) {
						System.out.println(registerResponse.getText());
						continue;
					}
				} else if ("Login".equals(action)) {
					while (true) {
						System.out.println("What is your username?");
						username = in.nextLine();
						if (ChatterUtil.validateUsernameAndPassword(username)) {
							break;
						}
						System.out.println("Username can only contains Character and Digit.");
					}
					while (true) {
						System.out.println("What is your password?");
						password = in.nextLine();
						if (ChatterUtil.validateUsernameAndPassword(password)) {
							break;
						}
						System.out.println("Password can only contains Character and Digit.");
					}
					connect.sendUserInfoMessage(username, password, action);
					while (true) {
						if (mess.hasNext()) {
							loginValidateMessage = mess.next();
							break;
						}
					}
					if (loginValidateMessage.getText().equals("succeed to log in")) {
						System.out.println(loginValidateMessage.getText());
						System.out.println("You have logged in.");
						break;
					} else if (loginValidateMessage.getText().equals("fail to log in")) {
						System.out.println(loginValidateMessage.getText());
						continue;
					}
				}
			}
			// Create the objects needed to write IM messages.
			KeyboardScanner scan = connect.getKeyboardScanner();

			System.out.println("You can make following request:");
			System.out.println("create groupName");
			System.out.println("join groupName");
			System.out.println("group groupName message");
			System.out.println("private userName message");
			System.out.println("leave groupName");

			// Check if the user has typed in a line of text to broadcast to the IM server.
			// If there is a line of text to be
			// broadcast:
			while (true) {
				if (scan.hasNext()) {
					// Read in the text they typed
					String line = scan.nextLine();

					// If the line equals "/quit", close the connection to the IM server.
					if (line.equals("/quit")) {
						connect.disconnect();
						break;
					} else {
						// Else, send the text so that it is broadcast to all users logged in to the IM
						// server.

						if (!connect.sendMessage(line)) {
							System.out.println("Illegal action");
						}
					}
				}
				// Get any recent messages received from the IM server.
				if (mess.hasNext()) {
					ChatterMessage chatterMessage = mess.next();
					if (!chatterMessage.getSender().equals(connect.getUserName())) {
						System.out.println(chatterMessage.getSender() + ": " + chatterMessage.getText());
					}
				}
			}
			System.out.println("Program complete.");
			System.exit(0);
		}
	}
}
