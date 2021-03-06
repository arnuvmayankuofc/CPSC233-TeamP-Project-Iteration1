import java.util.ArrayList;

/**
 * This is a Community Tile that randomly chooses something for the student to do.
 * @author Arnuv Mayank and Victor Manuel Campos Goitia Campos
 *
 */

public class Community extends Tile {
	private String[] communityOptions = {
            "Your good grades have been rewarded, you collect 400$ from scholarships",
            "It's your lucky day you find 50$ on the ground",
            "Grants have been doubled everyone collect 200$",
            "Someone hits your car on campus, luckily there were cameras collect 225$",
            "Your student loans have caught up with you, pay $400"};
	
	public Community(int position) {
		super(position, "Community");
	}
	
	public String[] getCommunityOptions() {
		return communityOptions;
	}
		
	public int performCommunityOption(int selection, Student studentOn, ArrayList<Student> allStudents) {
		switch (selection) {
		case 0:
			studentOn.depositMoney(400);
			return 1;
		case 1:
			studentOn.depositMoney(50);
			return 1;
		case 2:
			for (Student student: allStudents) {
				student.depositMoney(200);
			}
			return 1;
		case 3:
			studentOn.depositMoney(225);
			return 1;
		case 4:
			return studentOn.withdrawMoney(400);
		}
		return 1;
	}
}
