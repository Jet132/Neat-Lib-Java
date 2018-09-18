package main;

import static main.main.*;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;

import NEAT_Engine.NEAT_Engine;

class game implements Runnable {

	float action;
	float pos, posDot, angle, angleDot;

	float fitness;
	int ID;

	public game(int ID) {
		// Initialize pole state.
		pos = 0;
		posDot = 0;
		angle = randomAngle(); // Pole starts off at an angle
		angleDot = 0;
		action = 0;
		fitness = 0;
		this.ID = ID;
	}

	public void run() {
		for (int i = 0; i < duration; i++) {
			NeatEngine.setInput(0, pos, ID);
			NeatEngine.setInput(1, posDot, ID);
			NeatEngine.setInput(2, angle, ID);
			NeatEngine.setInput(3, angleDot, ID);

			NeatEngine.runNN(ID);

			action = NeatEngine.getOutput(0, ID) * 2 - 1;

			// Update the state of the pole;
			// First calc derivatives of state variables
			float force = forceMag * action;
			// double force = action;
			float sinangle = (float) Math.sin(angle);
			float cosangle = (float) Math.cos(angle);
			float angleDotSq = angleDot * angleDot;
			float common = (force + poleMassLength * angleDotSq * sinangle - fricCart * (posDot < 0 ? -1 : 0))
					/ totalMass;
			float angleDDot = (float) ((9.8 * sinangle - cosangle * common - fricPole * angleDot / poleMassLength)
					/ (halfPole * (fourthirds - poleMass * cosangle * cosangle / totalMass)));
			float posDDot = common - poleMassLength * angleDDot * cosangle / totalMass;

			// Now update current state.
			pos += posDot * tau;
			posDot += posDDot * tau;
			angle += angleDot * tau;
			angleDot += angleDDot * tau;

			if (angle < -0.50 || angle > 0.50) {
				break;
			}
			if (pos > 2 || pos < -2) {
				break;
			}

			//fitness++;
			fitness += (0.50 - Math.abs(angle))+(2-Math.abs(pos))/4;			
			
			if (ID == 0) {
				//System.out.println(0.51 - (angle < 0 ? -angle : angle > 0 ? angle : 0));
				Label.action = action;
				Label.pos = pos;
				Label.posDot = posDot;
				Label.angle = angle;
				Label.angleDot = angleDot;
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		NeatEngine.setFitness(fitness, ID);
	}

}

public class main {

	// Constants used for physics
	public static final float cartMass = 1;
	public static final float poleMass = 0.1f;
	public static final float poleLength = 1;
	public static final float forceMag = 10;
	public static final float tau = 0.02f;
	public static final float fricCart = 0.00005f;
	public static final float fricPole = 0.005f;
	public static final float totalMass = cartMass + poleMass;
	public static final float halfPole = 0.5f * poleLength;
	public static final float poleMassLength = halfPole * poleMass;
	public static final float fourthirds = 4 / 3;

	static final int duration = 3000;
	static final int AIs = 800;
	static final Random random = new Random();

	static int speed = 1;

	static NEAT_Engine NeatEngine = new NEAT_Engine();

	static JFrame Frame;
	static Label Label;

	static float randomAngle() {
		float temp = random.nextFloat() - 0.5f;
		if (temp > -0.2 && temp < 0) {
			temp = -0.2f;
		}
		if (temp < 0.2 && temp > 0) {
			temp = 0.2f;
		}

		return temp;
	}

	public static void main(String[] args) {
		setup();
		while (true) {
			run();
		}
	}

	static void run() {
		Thread[] games = new Thread[AIs];
		for (int i = 0; i < AIs; i++) {
			games[i] = new Thread(new game(i));
			games[i].run();
		}

		for (Thread t : games) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		NeatEngine.nextGenerationstep();
	}

	static void setup() {
		NeatEngine.setup(new int[] { 4, 1 }, false, null, null, AIs, 4, 80, 5, 10);
		Frame = new JFrame();
		Frame.setVisible(true);
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Frame.setTitle("CartPole NEAT Engine");
		Frame.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if (speed != 1) {
						speed /= 2;
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					if (speed < 262144) {
						speed *= 2;
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}
		});

		Label = new Label();
		Label.setVisible(true);
		Label.setPreferredSize(new Dimension(800, 800));
		Frame.getContentPane().add(Label);
		Frame.pack();
		Frame.requestFocus();
	}

}
