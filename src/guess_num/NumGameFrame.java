package guess_num;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.State;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class NumGameFrame extends JFrame implements ActionListener {
	
	// Model
	NumGameManager manager = NumGameManager.getInstance();
	
	// field
	long startTime;
	long endTime;
	long judgeStartTime;
	long judgeEndTime;
	
	int degree = 0;
	
	boolean flag = false;
	
	// JComponents 
	Container c = getContentPane(); // default : BorderLayout
	
	JPanel pnlNorth = new JPanel();
	JTextField tfInput = new JTextField(5);
	JTextField tfRecord = new JTextField("30000", 5);	
	JButton btnInput = new JButton("입력");
	JButton btnNewGame = new JButton("새게임");

	JPanel pnlCenter = new JPanel();
	JTextArea taScreen = new JTextArea(10,20);
	
	JPanel pnlSouth = new JPanel();
	JPanel pnlSouthLeft = new JPanel();
	JTextField tfLife = new JTextField("♥♥♥♥♥",10);
	TimeLabel lblTime = new TimeLabel();
	
	// Event Listener
	BtnMouseAdapter btnAdapter = new BtnMouseAdapter();
	
	// Thread
	Thread thTime = new Thread(lblTime);
	
	public NumGameFrame() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Guess a number!");
		setSize(500, 500);
		
		setPnlNorth();
		setPnlCenter();
		setPnlSouth();
		setListener();
		
		thTime.start();
		
		setVisible(true);
	}
	
	class TimeLabel extends JLabel implements Runnable {
		int timeSize = 50; // 시간 제한 5초
		
		public TimeLabel() {
			this.setBackground(Color.YELLOW);
			this.setOpaque(true);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			int lblWidth = this.getWidth();
			int lblHeight = this.getHeight();
			
			double timeoutBar = lblWidth - ((lblWidth * degree) / timeSize);
			
			g.setColor(Color.GREEN);
			g.fillRect(0, 0, (int)timeoutBar, lblHeight);
		}
		
		@Override
		public synchronized void run() {
			while(flag) {
				// 게임 종료 case2 ) 기회 소진
				if(manager.getLife() == 0) {
					JOptionPane.showMessageDialog(taScreen, "기회를 모두 사용하였습니다.", "Failed", JOptionPane.INFORMATION_MESSAGE);
					flag = false;
				}
				
				repaint();
				degree++;
			
				// 5초 경과
				if(degree > timeSize) {
					degree = 0;
					
					// timeout 시 하트 1개 소진.
					manager.minusLife();
					String hearts = manager.getHeart();
					tfLife.setText(hearts);
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
	}
	
	public void setPnlNorth() {
		JLabel lblInput = new JLabel("입력:");
		JLabel lblRecord = new JLabel("기록:");
		JComponent[] northComp = {lblInput, tfInput, btnInput, lblRecord, tfRecord, btnNewGame};
		
		pnlNorth.setBackground(Color.MAGENTA);
		
		// 새 게임 시작으로만 입력이 활성화 되어야함.
		tfInput.setEditable(false); 
		btnInput.setEnabled(false);
		tfRecord.setEditable(false);
		
		for(int i = 0; i < northComp.length; i++) {
			pnlNorth.add(northComp[i]);
		}
		
		c.add(pnlNorth, BorderLayout.NORTH);
	}
	
	public void setPnlCenter() {
		pnlCenter.setLayout(new GridLayout());
		// name style size
		taScreen.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		taScreen.setText("1부터 100까지의 수를 맞춰보세요. \n");
		taScreen.setEditable(false);
		
		JScrollPane screenScroll = new JScrollPane(taScreen, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				
		pnlCenter.add(screenScroll);
		c.add(pnlCenter);
	}
	
	public void setPnlSouth() {
		JLabel lblLife = new JLabel("남은 횟수:");
		pnlSouth.setLayout(new GridLayout());
		
		pnlSouthLeft.setBackground(Color.CYAN);
		
		tfLife.setEditable(false);
	
		pnlSouthLeft.add(lblLife);
		pnlSouthLeft.add(tfLife);
		
		pnlSouth.add(pnlSouthLeft);
		pnlSouth.add(lblTime);
		
		c.add(pnlSouth, BorderLayout.SOUTH);
	}
	
	public void setListener() {	
		btnInput.addMouseListener(btnAdapter);
		btnNewGame.addMouseListener(btnAdapter);
		
		tfInput.addActionListener(this);
	}
	
	public class BtnMouseAdapter extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			Object obj = e.getSource();
			// case 1 ) 사용자의 숫자 입력.
			if (obj == btnInput) {
				degree = 0;
				try {
					int userNum = Integer.parseInt(tfInput.getText());
					// 1회 게임 시행 - > 하트 차감, 유저에게 힌트.
					String result = manager.getResult(userNum);
					
					// 게임 종료 case 1) 성공
					if(result.equals("correct")) {						
						flag = false;
						endTime = System.currentTimeMillis();
						long curRecord = endTime - startTime;
						boolean newRecord = manager.setNewRecord(curRecord);
						if(newRecord) {
							JOptionPane.showMessageDialog(taScreen, "새로운 기록 달성", "New Record", JOptionPane.INFORMATION_MESSAGE);
							String resultRecord = String.valueOf(manager.getRecord());
							taScreen.append("걸린 시간:" + resultRecord + "ms");
							tfRecord.setText(resultRecord);
							// 기록을 파일에 저장.
							try {
								FileOutputStream fout2 = new FileOutputStream("G:/workspace/java/guess_num/record.dat");
								String recordTime = tfRecord.getText();
								int[] buffers = new int[recordTime.length()];
								
								for(int i = 0; i < recordTime.length(); i++) {
										buffers[i] = Character.getNumericValue(recordTime.charAt(i));
										try {
											fout2.write(buffers[i]);
										} catch (IOException error) {
											// TODO Auto-generated catch block
											error.printStackTrace();
										}
								}
								try {
									fout2.close();
								} catch (IOException error) {
									error.printStackTrace();
								}
							} catch (FileNotFoundException error) {
								error.printStackTrace();
							}
						}
						
						
						tfInput.setEditable(false);
						btnInput.setEnabled(false);
						
						JOptionPane.showMessageDialog(taScreen, "정답입니다.", "Congratulation", JOptionPane.INFORMATION_MESSAGE);
						
					} else {
						String hint = userNum + "보다 " + result + "\n";
						taScreen.append(hint);
					}
					
					String hearts = manager.getHeart();
					tfLife.setText(hearts);
					
				} catch (NumberFormatException error) {
					System.out.println("문자열이 아닌 숫자를 입력하세요.");
				}
		
			// case 2 ) 게임을 재시작.
			} else if (obj == btnNewGame) {	
				System.out.println("New Game!");
				/* 초기화 작업 */
				// 0. tfInput : 입력 가능하도록 -> setEditable(true); 
				// 게임 재시작 버튼은 활성화 X
				tfInput.setEditable(true);
				btnInput.setEnabled(true);
				// 1. 게임 필드 초기화.
				manager.initGame();
				taScreen.setText("1부터 100까지의 수를 맞춰보세요. \n");
				// 2. 하트 초기화
				String hearts = manager.getHeart();
				tfLife.setText(hearts);	
				// 3. 시작 시간 초기화
				startTime = System.currentTimeMillis();
				// 4. timeoutBar 활성화 (Thread)
				System.out.println(thTime.getState());
				if(thTime.getState() == State.TERMINATED) {					
					flag = true;
					Thread thTime = new Thread(lblTime);
					thTime.start();
				}
			}
		}
	} // BtnMouseAdapter
				
	@Override
	public void actionPerformed(ActionEvent e) {
		JTextField targetField = (JTextField)e.getSource();
		String targetText = targetField.getText();
		
		System.out.println(targetText);
	}
	
	public static void main(String[] args) {
		new NumGameFrame();
	}


}
