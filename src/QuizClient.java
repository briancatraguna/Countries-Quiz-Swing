
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class QuizClient extends JFrame implements Runnable  {
	
	private static final int WIDTH = 600;
	private static final int HEIGHT = 450;
	private static final int HEIGHT_IMAGE = 200;
	
	// Network
	private static final String HOST = "localhost";
	private static final int PORT_NUMBER = 8080;
	Socket socket = null;
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;
	
	// UI components
    private JLabel questionLabel;
    private ImageIcon imageIcon;
    private JLabel imageLabel;
    private JRadioButton optionA, optionB, optionC, optionD;
    private ButtonGroup optionsGroup;
    private JButton submitButton;
    private JPanel optionsPanel;
    
    // UI State
    private String correctAnswer;
    private int score;
	
	public QuizClient() {
		super("Countries Quiz");
		this.setSize(WIDTH, HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.createMenu();
		this.correctAnswer = null;
		this.score = 0;
	
		setHomeScreen();
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public class HomePanel extends JPanel {
	    private Image backgroundImage;

	    public HomePanel(ActionListener startGameListener) {
	        backgroundImage = new ImageIcon("home_background.jpeg").getImage();

	        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	        setOpaque(false); 

	        JLabel welcomeLabel = new JLabel("Welcome to the Countries Quiz!");
	        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24));
	        welcomeLabel.setForeground(Color.WHITE);
	        
	        JButton startButton = new JButton("Start");
	        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
	        startButton.addActionListener(startGameListener);
	        
	        add(welcomeLabel);
	        add(Box.createRigidArea(new Dimension(0, 20)));
	        add(startButton);
	        add(Box.createRigidArea(new Dimension(0, 20)));
	    }

	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        if (backgroundImage != null) {
	            g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
	        }
	    }
	}
	
	private void setHomeScreen() {
        this.add(new HomePanel(new StartGameListener()));
	}
	
	private void clearComponents() {
	    this.getContentPane().removeAll();
	    this.revalidate();
	    this.repaint();
	}

	private void setQuizComponents(String question, ImageIcon imageIcon, String optionAText, String optionBText, String optionCText, String optionDText) {
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		// Question
        questionLabel = new JLabel(question);
        JPanel questionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        questionPanel.add(questionLabel);
        
        // Image
        int originalWidth = imageIcon.getIconWidth();
        int originalHeight = imageIcon.getIconHeight();
        double ratio = (double) originalWidth / originalHeight;
        int newWidth = (int) (HEIGHT_IMAGE * ratio);
        Image resizedImage = imageIcon.getImage().getScaledInstance(newWidth, HEIGHT_IMAGE, Image.SCALE_SMOOTH);
        imageIcon = new ImageIcon(resizedImage);
        imageLabel = new JLabel(imageIcon);
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.add(imageLabel);

        
        optionA = new JRadioButton(optionAText);
        optionB = new JRadioButton(optionBText);
        optionC = new JRadioButton(optionCText);
        optionD = new JRadioButton(optionDText);

        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionA);
        optionsGroup.add(optionB);
        optionsGroup.add(optionC);
        optionsGroup.add(optionD);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.add(optionA);
        radioPanel.add(optionB);
        radioPanel.add(optionC);
        radioPanel.add(optionD);
        optionsPanel.add(radioPanel);

        submitButton = new JButton("Submit");
        submitButton.addActionListener(new SubmitButtonListener());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);
        
        // Update addComponents to use these panels
        this.add(questionPanel);
        this.add(imagePanel);
        this.add(optionsPanel);
        this.add(buttonPanel);
        
        this.revalidate();
	    this.repaint();
    }
	
	private void displayScore() {
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

	    class ImagePanel extends JPanel {
	        private Image bgImage;

	        public ImagePanel(ImageIcon imageIcon) {
	            this.bgImage = imageIcon.getImage();
	        }

	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            g.drawImage(bgImage, 0, 0, this.getWidth(), this.getHeight(), this);
	        }
	    }

	    ImageIcon backgroundIcon = new ImageIcon("score_background.jpeg");
	    JPanel scorePanel = new ImagePanel(backgroundIcon);
	    scorePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	    // Score display
	    JLabel congratsLabel = new JLabel("Congrats! You have finished the quiz!");
	    congratsLabel.setFont(new Font("Arial", Font.BOLD, 20));
	    congratsLabel.setForeground(Color.WHITE);
	    scorePanel.add(congratsLabel);
	    
	    JLabel scoreLabel = new JLabel("Your score is " + score + "/10");
	    scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
	    scoreLabel.setForeground(Color.WHITE);
	    scorePanel.add(scoreLabel);

	    this.add(scorePanel);

	    this.revalidate();
	    this.repaint();
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
	
	class StartGameListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				socket = new Socket(HOST, PORT_NUMBER);
				fromServer = new DataInputStream(socket.getInputStream());
				toServer = new DataOutputStream(socket.getOutputStream());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(QuizClient.this, 
				        "Unable to connect to the server. Please try again later.", 
				        "Connection Error", 
				        JOptionPane.ERROR_MESSAGE);
			}
			
		}
		
	}
	
	class SubmitButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!optionA.isSelected() && !optionB.isSelected() && !optionC.isSelected() && !optionD.isSelected()) {
				JOptionPane.showMessageDialog(QuizClient.this, 
				        "You haven't chosen any options!", 
				        "Please choose the answer", 
				        JOptionPane.ERROR_MESSAGE);
				return;
			}
			boolean isACorrect = optionA.isSelected() && correctAnswer.equals("A");
			boolean isBCorrect = optionB.isSelected() && correctAnswer.equals("B");
			boolean isCCorrect = optionC.isSelected() && correctAnswer.equals("C");
			boolean isDCorrect = optionD.isSelected() && correctAnswer.equals("D");
			if (isACorrect || isBCorrect || isCCorrect || isDCorrect) {
				score++;
				JOptionPane.showMessageDialog(QuizClient.this, 
				        "Congrats! You got this question correct!", 
				        "Your score is " + score, 
				        JOptionPane.INFORMATION_MESSAGE);
			} else {
				score--;
				JOptionPane.showMessageDialog(QuizClient.this, 
				        "Oops you got this one wrong!", 
				        "Your score is " + score, 
				        JOptionPane.INFORMATION_MESSAGE);
			}
			
			
			try {
				toServer.writeBoolean(true);
				toServer.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
		
	}
	
	
	
	public static void main(String[] args) {
		QuizClient quizClient = new QuizClient();
		quizClient.setVisible(true);
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(300);
				while (socket != null && fromServer != null && toServer != null) {
					// Is there more questions?
					boolean moreQuestions = fromServer.readBoolean();
					if (!moreQuestions) {
						SwingUtilities.invokeLater(() -> {
							clearComponents();
							displayScore();
						});
					}
					// Question
					String question = fromServer.readUTF();
					
					// Image
					int imageSize = fromServer.readInt();
					byte[] imageBytes = new byte[imageSize];
					fromServer.readFully(imageBytes);
					ImageIcon imageIcon = new ImageIcon(imageBytes);
					
					// Options
					String optionAText = fromServer.readUTF();
					String optionBText = fromServer.readUTF();
					String optionCText = fromServer.readUTF();
					String optionDText = fromServer.readUTF();
					
					this.correctAnswer = fromServer.readUTF();
					
					SwingUtilities.invokeLater(() -> {
						clearComponents();
						setQuizComponents(question, imageIcon, optionAText, optionBText, optionCText, optionDText);
					});
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
