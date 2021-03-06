import java.util.*;

/**
 * This is the class that has all of the game's ruleset/logic. It takes from all
 * the classes previously built, as well as the UI, and makes the entire game
 * happen
 * 
 * @author Arnuv Mayank
 *
 */
public class Application {
	int numStudents;
	static int startingMoney = 1000;
	ArrayList<Student> students;
	CourseList courseList;
	TextVisualizer UI = new TextVisualizer();
	Dice dice = new Dice();

	public Application() {
		students = new ArrayList<Student>();
		courseList = new CourseList();
	}

	private int purchaseCourse(Student student, Course aCourse) {
		int purchaseResult = student.purchaseCourse(aCourse);

		if (purchaseResult == -2) {
			UI.insufficientMoneyError();
		} else if (purchaseResult == 1) {
			courseList.addToCoursesOwned(aCourse);
		}

		return purchaseResult;
	}

	private Tile rollDice(Student student) {
		int roll = dice.rollDice();
		student.moveForward(roll, courseList.getBoardSize());
		UI.showRoll(roll);
		return courseList.getTileAt(student.getPlayerPosition());
	}

	private void courseWalkthrough(Student student) {
		UI.displayCourseOptions();
	}

	private void removeStudentFromGame(Student student) {
		for (Course course : student.getCoursesOwned()) {
			courseList.removeFromCoursesOwned(course);
		}
		students.remove(student);
		UI.removePlayerFromUI(student);
		student.studentOut();
		UI.updateBoard(student);
	}

	private boolean sellCourse(Student student) {
		boolean ownsProperty = student.doesStudentOwnProperty();
		if (ownsProperty) {
			Course courseToSell = UI.sellCourseMenu(student);
			if (Objects.nonNull(courseToSell)) {
				student.sellCourse(courseToSell);
				courseList.removeFromCoursesOwned(courseToSell);
				return true;
			}
			return false;
		} else {
			UI.displayNoProperty();
			return false;
		}
	}

	private void sellCourseMenu(Student student) {
		while (sellCourse(student)) {
			;
		}
	}

	private void tutorialPayment(Student student, Course courseOn) {
		int amountOwed = courseOn.getTutorialPriceOwed();
		UI.displayCourseOwnedMenu(student, courseOn.getOwner(), amountOwed);
		int withdrawalResult = student.withdrawMoney(amountOwed);
		if (withdrawalResult == 1) {
			courseOn.getOwner().depositMoney(amountOwed);
			UI.displayTutorialPaidScreen(student, courseOn.getOwner(), amountOwed);
		} else if (withdrawalResult == -1) {
			UI.displayBankruptcyScreen(student);
			removeStudentFromGame(student);
		} else {
			UI.displayMustMortgageScreen(student);
			sellCourseMenu(student);
			tutorialPayment(student, courseOn);
		}
	}

	private void purchaseMenu(Student student, Course courseOn) {
		if (student.getNetWorth() >= courseOn.getBuyPrice()) {
			boolean wantsToBuy = UI.displayPurchaseScreen(courseOn);
			if (wantsToBuy) {
				int buyAttempt = purchaseCourse(student, courseOn);
				if (buyAttempt == 1) {
					;
				} else if (buyAttempt == -2) {
					if (UI.chooseToSell(true)) {
						sellCourseMenu(student);
						purchaseMenu(student, courseOn);
					}
				}
			}
		} else {
			UI.displayInsufficientAssets(student, courseOn);
		}
	}

	private void communityMenu(Student student, Community communityOn, int randCommunity) {
		int communityOptionResult = communityOn.performCommunityOption(randCommunity, student, students);
		if (communityOptionResult == 1) {
			;
		} else if (communityOptionResult == -2) {
			UI.displayMustMortgageScreen(student);
			sellCourseMenu(student);
			communityMenu(student, communityOn, randCommunity);
		} else if (communityOptionResult == -1) {
			removeStudentFromGame(student);
		}
	}

	private void chanceMenu(Student student, Chance chanceOn, int randChance) {
		int chanceOptionResult = chanceOn.performChanceOption(randChance, student, students,
				courseList.getParkingTiles(), courseList.getProbation());
		if (chanceOptionResult == 1) {
			;
		} else if (chanceOptionResult == -2) {
			UI.displayMustMortgageScreen(student);
			sellCourseMenu(student);
			chanceMenu(student, chanceOn, randChance);
		} else if (chanceOptionResult == -1) {
			removeStudentFromGame(student);
		}
	}

	private void parkingMenu(Student student, Parking parkingOn) {
		UI.displayInParking(student, parkingOn);
		int parkingFeeResult = parkingOn.payParkingFee(student);
		if (parkingFeeResult == 1) {
			;
		} else if (parkingFeeResult == -2) {
			sellCourseMenu(student);
			parkingMenu(student, parkingOn);
		} else if (parkingFeeResult == -1) {
			removeStudentFromGame(student);
		}
	}

	private void probationMenu(Student student, Probation probationOn, int counter) {
		UI.displayInProbation(student, probationOn);
		int probationResult = probationOn.probationPayment(student, counter);
		if (probationResult == 1) {
			;
		} else if (probationResult == -2) {
			UI.displayMustMortgageScreen(student);
			sellCourseMenu(student);
			probationMenu(student, probationOn, 2);
		} else if (probationResult == -1) {
			removeStudentFromGame(student);
		}
	}

	private void completeTurn(Student student, int counter) {
		UI.displayBoard();
		UI.displayStudentStats(student);
		if (!student.isInJail()) {
			UI.turnMainMenu(student);
			boolean ownsProperty = student.doesStudentOwnProperty();
			boolean initialChoice = UI.chooseToSell(ownsProperty);
			if (!initialChoice) {
				UI.rollDiceMenu(student);
				Tile landingTile = rollDice(student);
				UI.updateBoard(student);
				UI.displayBoard();
				if (landingTile instanceof Course) {
					Course courseOn = courseList.getCourseAt(landingTile.getTileID());
					if (courseOn.getOwnedStatus()) {
						if (!student.doesStudentOwnCourse(courseOn)) {
							tutorialPayment(student, courseOn);
						}
					} else {
						purchaseMenu(student, courseOn);
					}
				} else if (landingTile instanceof Community) {
					Community communityOn = courseList.getCommunityAt(landingTile.getTileID());
					int randCommunity = new Random().nextInt(communityOn.getCommunityOptions().length);
					UI.displayCommunityOption(communityOn, randCommunity);
					communityMenu(student, communityOn, randCommunity);
					UI.updateBoard(student);
					UI.displayBoard();
				} else if (landingTile instanceof Chance) {
					Chance chanceOn = courseList.getChanceAt(landingTile.getTileID());
					int randChance = new Random().nextInt(chanceOn.getChanceOptions().length);
					UI.displayChanceOption(chanceOn, randChance);
					chanceMenu(student, chanceOn, randChance);
					UI.updateBoard(student);
					UI.displayBoard();
				} else if (landingTile instanceof Parking) {
					Parking parkingOn = courseList.getParkingAt(landingTile.getTileID());
					parkingMenu(student, parkingOn);
				} else if (landingTile instanceof Probation) {
					Probation probationOn = courseList.getProbation();
					probationMenu(student, probationOn, 1);
				} else if (landingTile instanceof Go) {
					Go goOn = courseList.getGo();
					goOn.depositGoAmmount(student);
				}
			} else {
				sellCourseMenu(student);
				completeTurn(student, 2);
			}
		} else {
			UI.displayStillInJail(student);
			Probation probationOn = courseList.getProbation();
			probationMenu(student, probationOn, 1);
		}

		for (Student aStudent : students) {
			UI.displayStudentStats(aStudent);
		}
		if (counter == 1) {
			UI.displayTurnComplete();
			UI.continuePlaying();
		}
	}

	public void run() {
		numStudents = UI.askForNumPlayers();
		students = new ArrayList<Student>();
		Student student;
		int turn = 0;

		for (int i = 1; i <= numStudents; i++) {
			students.add(new Student(i, startingMoney));
			UI.updateBoard(students.get(i - 1));
		}

		while (students.size() != 1) {
			student = students.get(turn);
			completeTurn(student, 1);
			if (student.getPlayerPosition() == -1) {
				students.remove(student);
				numStudents--;
				turn--;
			}

			turn++;
			turn %= numStudents;
		}
		UI.closeScanner();
		UI.displayWinner(students.get(0));
	}
}
