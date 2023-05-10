package guess_num;

public class NumGameManager {
	static NumGameManager instance = null;
	
	private int targetNum;
	private int life = 5;
	private long record = 30000;
	
	/* Constructor */
	private NumGameManager() {
		
	}
	
	public static NumGameManager getInstance() {		
		if(instance == null) {
			instance = new NumGameManager();
		}

		return instance;
	}
	
	/* Getter and Setter */
	public int getLife() {
		return life;
	}
	
	public int getTargetNum() {
		return targetNum;
	} // life, targetNum 테스트 용도
	
	public long getRecord() {
		return record;
	}
	
	public void setTargetNum() {
		targetNum = (int)(Math.random()*100 + 1);
	}
	
	public void setLife(int life) {
		this.life = life;
	}
	
	public void minusLife() {
		life--;
	}
	
	public boolean setNewRecord(long curRecord) {
		if(curRecord < record) {
			record = curRecord;
			return true;
		}
		return false;
	}
	
	/* method */
	public void initGame() {
		setLife(5); // 초기 남은 횟수
		setTargetNum(); // 맞춰야 할 숫자 설정.
	}
	
	// 게임 1회 -> 결과 판정.
	public String getResult(int userNum) {
		System.out.println("컴퓨터의 숫자 : " + targetNum);
		String result = "";
		life--; // 게임 1회 마다 하트 소모.
		
		// 결과 판정 이후 사용자에게 힌트.
		if (userNum > targetNum) {
			result = "down";
		} else if (userNum == targetNum) {
			result = "correct";
		} else if (userNum < targetNum) {
			result = "up";
		}
		return result; 
	}
	
	
	public String getHeart() {
		String hearts = "";
		for(int i = 0; i < life; i++) {
			hearts += "♥";
		}
		return hearts;
	}
}
