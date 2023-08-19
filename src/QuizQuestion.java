
public class QuizQuestion {

	String question;
	String imagePath;
	String optionA;
	String optionB;
	String optionC;
	String optionD;
	String answer;
	
	public QuizQuestion(String question, String imagePath, String optionA, String optionB, String optionC, String optionD, String answer) {
		this.question = question;
		this.imagePath = imagePath;
		this.optionA = optionA;
		this.optionB = optionB;
		this.optionC = optionC;
		this.optionD = optionD;
		this.answer = answer;
	}
}
