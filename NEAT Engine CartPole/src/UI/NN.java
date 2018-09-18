package UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import NEAT_Engine.AI;
import NEAT_Engine.NEAT_Engine;
import NEAT_Engine.node;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class NN extends JComponent{

	Layout<Integer, Integer> layout;
	VisualizationImageServer<Integer, Integer> vs;

	int x_distance = 100;
	int y_distance = 80;
	float SCT = 0.1f;
	float zoom = 1;
	int x = 0, y = 0;
	int m_x = 0, m_y = 0, l_m_x = 0, l_m_y = 0;
	static int maxNodesinLayer;
	int[] l;

	boolean update;

	AI AI;

	public NN(int px, int py, int width, int height,AI ai) {
		setVisible(true);
		setBounds(px, py, width, height);
		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				float zoomLevel = e.getWheelRotation() * 0.1f;
				zoom -= zoomLevel / (1 / zoom);
				update = true;
			}
		});
		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				l_m_x = m_x;
				l_m_y = m_y;
				m_x = (int) e.getX();
				m_y = (int) e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {

				x += (m_x - l_m_x) * (1 / zoom);
				y += (m_y - l_m_y) * (1 / zoom);
				l_m_x = m_x;
				l_m_y = m_y;
				m_x = (int) e.getX();
				m_y = (int) e.getY();
				update = true;
			}
		});

		layout = new StaticLayout<Integer, Integer>(new DirectedSparseGraph<>());
		vs = new VisualizationImageServer(layout, new Dimension(width, height));

		AI = ai;
		setAI(AI);

		Transformer<Integer, Shape> vertexSize = new Transformer<Integer, Shape>() {
			public Shape transform(Integer i) {
				int size = (int) (40 * zoom);
				return new Ellipse2D.Double(-(size / 2), -(size / 2), size, size);
			}
		};

		DefaultVertexLabelRenderer vertexLableRenderer = new DefaultVertexLabelRenderer(null) {
			@Override
			public <V> Component getVertexLabelRendererComponent(JComponent vv, Object value, Font font,
					boolean isSelected, V vertex) {
				super.getVertexLabelRendererComponent(vv, value, font, isSelected, vertex);
				setForeground(new Color(0, 0, 0));
				setFont(new Font(null, 0, (int) (zoom * 9)));
				return this;
			}
		};
		Transformer<Integer, Paint> vertexColor = new Transformer<Integer, Paint>() {

			@Override
			public Paint transform(Integer ID) {
				if (ID < l[0]) {
					return Color.GREEN;
				}
				if (AI.findNode(ID).getLayer() == AI.layers - 1) {
					return Color.GRAY;
				}
				return Color.RED;
			}
		};
		Transformer<Integer, String> vertexLabel = new Transformer<Integer, String>() {

			@Override
			public String transform(Integer ID) {
				node temp = AI.findNode(ID);
				//System.out.println(temp.name);
				if (temp.getLayer() == 0) {
					if (temp.name == -1) {
						return "Bias";
					}
					return NEAT_Engine.inputNames[temp.name];
				}
				if (temp.getLayer() == AI.layers - 1) {
					return NEAT_Engine.outputNames[temp.name];
				}

				return ID.toString();
			}
		};
		Transformer<Integer, Stroke> edgeStroke = new Transformer<Integer, Stroke>() {

			@Override
			public Stroke transform(Integer i) {
				return new BasicStroke(Math.abs(AI.cons.get(i).getWeight() * 3.5f * zoom));
			}
		};

		Transformer<Integer, Paint> edgeColor = new Transformer<Integer, Paint>() {

			@Override
			public Paint transform(Integer name) {
				if (AI.cons.get(name).getWeight() < 0) {
					return Color.BLACK;
				}

				return Color.WHITE;
			}
		};

		vs.getRenderContext().setVertexShapeTransformer(vertexSize);
		vs.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vs.getRenderContext().setVertexLabelRenderer(vertexLableRenderer);
		vs.getRenderContext().setVertexFillPaintTransformer(vertexColor);
		vs.getRenderContext().setVertexLabelTransformer(vertexLabel);
		vs.getRenderContext().setEdgeStrokeTransformer(edgeStroke);
		vs.getRenderContext().setEdgeArrowStrokeTransformer(edgeStroke);
		vs.getRenderContext().setEdgeDrawPaintTransformer(edgeColor);

		vs.setBackground(new Color(120, 120, 120));
		add(vs);

		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (update) {
					update();
				}

			}
		}, 0, 30);
	}

	void update() {
		int[] temp_l = new int[l.length];
		for (int i = 0; i < AI.nodes.size(); i++) {
			layout.setLocation(AI.nodes.get(i).getID(), new Point2D.Double(
					(AI.nodes.get(i).getLayer() * x_distance + 50 + x) * zoom,
					((temp_l[AI.nodes.get(i).getLayer()]) * y_distance
							+ ((maxNodesinLayer - 1) * y_distance - (l[AI.nodes.get(i).getLayer()] - 1) * y_distance)
									/ 2
							+ 50 + y) * zoom));
			temp_l[AI.nodes.get(i).getLayer()]++;
		}
		vs.updateUI();
	}

	public void setAI(AI AI) {
		this.AI = AI;

		Graph<Integer, Integer> g = new DirectedSparseGraph();
		layout.setGraph(g);

		l = AI.createLayout();

		int max = 0;
		for (int i = 0; i < l.length; i++) {
			if (l[i] > max) {
				max = l[i];
			}
		}
		maxNodesinLayer = max;

		for (int i = 0; i < AI.nodes.size(); i++) {
			g.addVertex(AI.nodes.get(i).getID());
		}

		for (int i = 0; i < AI.cons.size(); i++) {
			if (AI.cons.get(i).isEnabled()) {
				g.addEdge(i, AI.cons.get(i).getFromNode().getID(), AI.cons.get(i).getToNode().getID());
			}
		}

		update = true;
	}

}
