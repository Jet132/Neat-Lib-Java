package main;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

import static main.main.*;

public class Label extends JLabel {
	
	float action;
	float pos, posDot, angle, angleDot;

	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		update(g);
		
		g2.setColor(new Color(0, 0, 0));
		g.drawString("G: " + String.valueOf(NeatEngine.getGeneration()), 10, 20);
		g.drawString("GS: " + String.valueOf(NeatEngine.getGenerationstep()), 10, 35);
		g.drawString("S: " + String.valueOf(speed), 10, 50);
		for (int i = 0; i < 20; i++) {
			g.drawString(i + 1 + ". " + String.valueOf(NeatEngine.getAIScorePerScoreList(i)), 100, 20 + i * 10);
		}

		try {
			Thread.sleep(7);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		repaint();
	}

	public void update(Graphics g) {
		Dimension d = getSize();
		Color cartColor = new Color(0, 20, 255);
		Color arrowColor = new Color(255, 255, 0);
		Color trackColor = new Color(100, 100, 50);

		// Draw Track.
		g.setColor(trackColor);
		g.drawLine(0, pixY(d, 0)+15, getWidth(),pixY(d, 0)+15);

		// Draw message
		String msg1 = "Position = " + pos + " ";
		String msg2 = "Angle = " + angle + " ";
		String msg3 = "angleDot = " + angleDot;
		g.drawString(msg1, 20, d.height - 20);
		g.drawString(msg2, 20, d.height - 40);
		g.drawString(msg3, 20, d.height - 60);

		// Draw cart.
		g.setColor(cartColor);
		g.fillRect(pixX(d, pos - 0.2), pixY(d, 0), pixDX(d, 0.4), pixDY(d, -0.2));

		// Draw pole
		g.drawLine(pixX(d, pos), pixY(d, 0), pixX(d, pos + Math.sin(angle) * poleLength),
				pixY(d, poleLength * Math.cos(angle)));

		// Draw action arrow.
		if (action != 0) {
			int signAction = (action > 0 ? 1 : (action < 0) ? -1 : 0);
			int tipx = pixX(d, pos + 0.2 * action);
			int tipy = pixY(d, -0.1);
			g.setColor(arrowColor);
			g.drawLine(pixX(d, pos), pixY(d, -0.1), tipx, tipy);
			g.drawLine(tipx, tipy, (int) (tipx - 4 * action), tipy + 4);
			g.drawLine(tipx, tipy, (int) (tipx - 4 * action), tipy - 4);
		}

	}

	public int pixX(Dimension d, double v) {
		return (int) Math.round((v + 2.5) / 5.0 * d.width);
	}

	public int pixY(Dimension d, double v) {
		return (int) Math.round(d.height - (v + 2.5) / 5.0 * d.height);
	}

	public int pixDX(Dimension d, double v) {
		return (int) Math.round(v / 5.0 * d.width);
	}

	public int pixDY(Dimension d, double v) {
		return (int) Math.round(-v / 5.0 * d.height);
	}

}
