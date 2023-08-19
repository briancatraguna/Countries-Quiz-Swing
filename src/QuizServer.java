
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;

public class QuizServer extends JFrame implements Runnable {
	
	private static final int WIDTH = 400;
	private static final int HEIGHT = 300;
	
	private static final int PORT_NUMBER = 8080;
	
	JTextArea textArea = null;
	List<QuizQuestion> questions;
	
	public QuizServer() {
		super("Quiz Server");
		this.setSize(WIDTH, HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.createMenu();
		this.initQuestions();
		
		textArea = new JTextArea(30, 30);
		textArea.setEditable(false);
		this.add(textArea);
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener((e) -> System.exit(0));
		menu.add(exitItem);
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}
	
	private void initQuestions() {
		questions = new ArrayList();
		questions.add(new QuizQuestion("Which country is known as the \"Land of the Rising Sun\"", "mount_fuji.jpeg", "China", "Australia", "Japan", "South Korea", "C"));
		questions.add(new QuizQuestion("Which country boasts the ancient city of Petra?", "petra.jpeg", "Egypt", "Greece", "Jordan", "Iraq", "C"));
		questions.add(new QuizQuestion("The Great Barrier Reef is located off the coast of which country?", "great_barrier_reef.jpeg", "Brazil", "Australia", "India", "Indonesia", "B"));
		questions.add(new QuizQuestion("Which country is famous for its Carnival festival and Samba dance?", "samba_dancers.jpeg", "Spain", "Mexico", "Brazil", "Argentina", "C"));
		questions.add(new QuizQuestion("The Pyramids of Giza are located in which country?", "pyramids_of_giza.jpeg", "Egypt", "Saudi Arabia", "Sudan", "Israel", "A"));
		questions.add(new QuizQuestion("Which country is the homeland of the Maori people and has the city of Wellington as its capital?", "haka.jpeg", "Australia", "Fiji", "Samoa", "New Zealand", "D"));
		questions.add(new QuizQuestion("Which country has a historical region called Transylvania?", "bran_castle.jpeg", "Hungary", "Romania", "Bulgaria", "Ukraine", "B"));
		questions.add(new QuizQuestion("Which country has a city known as the \"City of Canals\"", "venice.jpg", "Italy", "Netherlands", "Belgium", "Russia", "A"));
		questions.add(new QuizQuestion("The ancient city of Machu Picchu is in which country?", "machu_picchu.jpeg", "Brazil", "Argentina", "Peru", "Chile", "C"));
		questions.add(new QuizQuestion("Which country is known as the \"Land of a Thousand Lakes\"?", "finland_lakeland.jpeg", "Canada", "Sweden", "Norway", "Finland", "D"));
		

		Collections.shuffle(questions);
	}
	
	public static void main(String[] args) {
		QuizServer quizServer = new QuizServer();
		quizServer.setVisible(true);
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
			Socket socket = serverSocket.accept();
			textArea.append("Player has entered the game. Setting up network...\n");
			
			DataInputStream fromClient = new DataInputStream(socket.getInputStream());
			DataOutputStream toClient = new DataOutputStream(socket.getOutputStream());
			
			int questionNo = 1;
			for (QuizQuestion quizQuestion: questions) {
				// There is more questions
				toClient.writeBoolean(true);
				
				textArea.append("Sending question number " + questionNo + "\n");
				// Question
				toClient.writeUTF(quizQuestion.question);
				toClient.flush();
				
				// Image
				Path imagePath = Paths.get(quizQuestion.imagePath);
				byte[] imageBytes = Files.readAllBytes(imagePath);
				toClient.writeInt(imageBytes.length);
				toClient.flush();
				toClient.write(imageBytes);
				toClient.flush();
				

				// Options
				toClient.writeUTF(quizQuestion.optionA);
				toClient.flush();
				toClient.writeUTF(quizQuestion.optionB);
				toClient.flush();
				toClient.writeUTF(quizQuestion.optionC);
				toClient.flush();
				toClient.writeUTF(quizQuestion.optionD);
				toClient.flush();
				
				
				// Correct Answer
				toClient.writeUTF(quizQuestion.answer);
				toClient.flush();
								
				// Should go next
				boolean shouldGoNext = fromClient.readBoolean();
				textArea.append("User has answered question number " + questionNo++ + "\n");

			}
			// No more questions
			toClient.writeBoolean(false);
			textArea.append("Player has completed the quiz.\n");
			
			
		} catch(Exception e) {
			textArea.append(e.getMessage() + "\n");
		}
		
	}

}
