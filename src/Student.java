import java.util.*;
import java.lang.Math;

public class Student {
	private int playerNumber;
	private int playerMoney;
	private int playerPosition = 0;
	private int previousPlayerPosition = 0;
	private ArrayList<Course> coursesOwned;
	private HashMap<String, ArrayList<Course>> coursesOwnedOfFaculty;
	private HashMap<String, Boolean> ownsFaculty;


	
	public Student() {
		coursesOwned = new ArrayList<Course>();
		initializeCoursesOwnedOfFaculty();
		initializeOwnsFaculty();
	}
	
	public Student(int playerNumber, int playerMoney)
	{
		coursesOwned = new ArrayList<Course>();
		if (playerNumber > 0 && playerNumber < 5) {	
			this.playerNumber = playerNumber;
		}
		if (playerMoney > 0) {
			this.playerMoney = playerMoney;
		}
		initializeCoursesOwnedOfFaculty();
		initializeOwnsFaculty();
	}
	
	private void initializeCoursesOwnedOfFaculty() {
		coursesOwnedOfFaculty = new HashMap<String, ArrayList<Course>>();
		
		ArrayList<Course> artsCoursesOwned = new ArrayList<Course>();
		ArrayList<Course> sciencesCoursesOwned = new ArrayList<Course>();
		ArrayList<Course> businessCoursesOwned = new ArrayList<Course>();
		ArrayList<Course> engineeringCoursesOwned = new ArrayList<Course>();
		
		coursesOwnedOfFaculty.put("Arts", artsCoursesOwned);
		coursesOwnedOfFaculty.put("Sciences", sciencesCoursesOwned);
		coursesOwnedOfFaculty.put("Business", businessCoursesOwned);
		coursesOwnedOfFaculty.put("Engineering", engineeringCoursesOwned);
	}
	
	private void initializeOwnsFaculty() {
		ownsFaculty = new HashMap<String, Boolean>();
		ownsFaculty.put("Arts", false);
		ownsFaculty.put("Sciences", false);
		ownsFaculty.put("Business", false);
		ownsFaculty.put("Engineering", false);
	}
	
	public int getPlayerNumber() {
		return playerNumber;
	}
	
	public int getPlayerMoney() {
		return playerMoney;
	}
	
	public int getPlayerPosition() {
		return playerPosition;
	}
	
	public int getPreviousPlayerPosition() {
		return previousPlayerPosition;
	}
	
	public ArrayList<Course> getCoursesOwned() {
		return coursesOwned;
	}
	
	public boolean doesStudentOwnCourse(Course aCourse) {
		return (coursesOwned.contains(aCourse) ? true : false);
	}
	
	private void addCourse(Course aCourse) {
		this.coursesOwned.add(aCourse);
	}
	
	private boolean removeCourse(Course aCourse) {
		return this.coursesOwned.remove(aCourse);
	}
	
	public int withdrawMoney(int money) {
		if (money <= playerMoney) {
			playerMoney -= money;
			return 1;
		}
		else if (!isBankrupt(money)) {
			return -2;
		}
		return -1;
	}
	
	public int depositMoney(int money) {
		if (money >= 0) {
			playerMoney += money;
			return 1;
		}
		return -1;
	}
	
	public int getAssetsValue() {
		int mortgageValue = 0;
		
		for (Course courseOwned: coursesOwned) {
			mortgageValue += courseOwned.getSellPrice();
		}
		
		return mortgageValue;
	}
	
	public int getNetWorth() {
		return getAssetsValue() + playerMoney;
	}
	
	public boolean isBankrupt(int money) {
		return (getNetWorth() < money ? true : false);
	}
	
	public int purchaseCourse(Course aCourse) {
		if (!aCourse.getOwnedStatus()) {
			int withdrawResult = withdrawMoney(aCourse.getBuyPrice());
			if (withdrawResult == 1) {
				aCourse.setOwnedStatus(true);
				aCourse.setOwner(this);
				this.addCourse(aCourse);
				
				String courseFaculty = aCourse.getFaculty();
				ArrayList<Course> courseOfFaculty = coursesOwnedOfFaculty.get(courseFaculty);
				courseOfFaculty.add(aCourse);
				coursesOwnedOfFaculty.put(courseFaculty, courseOfFaculty);
				if (courseOfFaculty.size() == 3) {
					ownsFaculty.put(courseFaculty, true);
					for (Course course: courseOfFaculty) {
						course.addCourseLevel();
					}
				}				
			}
			return withdrawResult;
		}
		return -3;
	}
	
	public int sellCourse(Course aCourse) {
		if (this.doesStudentOwnCourse(aCourse)) {
			aCourse.setOwnedStatus(false);
			aCourse.setOwner(null);
			aCourse.resetCourseLevel();
			depositMoney(aCourse.getSellPrice());
			return (removeCourse(aCourse) ? -2 : 0);
		}
		return -1;
	}
	
	public int upgradeFacultyLevel(String faculty) {
		if (this.ownsFaculty.get(faculty)) {
			ArrayList<Course> coursesOfFaculty = this.coursesOwnedOfFaculty.get(faculty);
			if (this.playerMoney >= coursesOfFaculty.get(0).getUpgradeCost()) {
				playerMoney -= coursesOfFaculty.get(0).getUpgradeCost();
				for (Course course: coursesOfFaculty) {
					course.addCourseLevel();
				}
				return 1;
			}
			return -2;
		}
		return -1;
	}
	
	public void studentOut() {
		for (Course course: getCoursesOwned()) {
			coursesOwned.remove(course);
		}
		previousPlayerPosition = playerPosition;
		playerPosition = -1;
	}
	
	public void moveToClosestParking(int parking1Pos, int parking2Pos) {
		int spacesToParking1 = Math.abs(parking1Pos - playerPosition);
		int spacesToParking2 = Math.abs(parking2Pos - playerPosition);
		
		previousPlayerPosition = playerPosition;
		if (spacesToParking1 < spacesToParking2) {
			playerPosition = parking1Pos;
		}
		else {
			playerPosition = parking2Pos;
		}
	}
	
	public void moveToProbation(int probationPos) {
		previousPlayerPosition = playerPosition;
		playerPosition = probationPos;
	}
	
	public void moveForward(int spaces, int boardSize) {
		previousPlayerPosition = playerPosition;
		playerPosition += spaces;
		playerPosition %= boardSize;
	}
}
