/**
 * Copyright Copyright 2014 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
/*
 * Changelog: 
 * - Piero Dalle Pezze: Class creation.
 */
package uk.ac.babraham.BamQC.Graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.math3.util.Precision;

import uk.ac.babraham.BamQC.Utilities.LinearRegression;

/**
 * A class for drawing a scatter plot.
 * @author Piero Dalle Pezze
 *
 */
public class ScatterGraph extends JPanel {

	private static final long serialVersionUID = -7292512222510200683L;

	protected String xLabel;
	protected String yLabel;
	protected double[] data;
	protected double[] xCategories;
	protected String[] toolTipLabels;
	protected String graphTitle;
	protected double minX;
	protected double maxX;
	protected double xInterval;
	protected double minY;
	protected double maxY;
	protected double yInterval;
	protected int height = -1;
	protected int width = -1;

	// TOOL TIPS management
	private List<Rectangle> rectangles = null;
	private List<String> tips = null;
	private JWindow toolTip = null;
    private JLabel label = new JLabel();
	private Tipster tipster = null;
	
	public ScatterGraph(double[] data, double[] xCategories, String[] toolTipLabels, String xLabel, String yLabel, String graphTitle) {
		initialise(data, xCategories, toolTipLabels, xLabel, yLabel, graphTitle);
	}

	
	public ScatterGraph(double[] data, String[] xCategories, String[] toolTipLabels, String xLabel, String yLabel, String graphTitle) {
		double[] myCategories = new double[xCategories.length];
		for (int i=0; i<xCategories.length; i++) {
			myCategories[i] = Double.parseDouble(xCategories[i]);
		}
		initialise(data, myCategories, toolTipLabels, xLabel, yLabel, graphTitle);
	}
	
	private void initialise(double[] data, double[] xCategories, String[] toolTipLabels, String xLabel, String yLabel, String graphTitle) {
		this.data = data;
		this.xCategories = xCategories;
		this.toolTipLabels = toolTipLabels;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.graphTitle = graphTitle;		

		// calculate minX-maxX, minY-maxY and xInterval-yInterval
		double[] minmax = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
		calculateMinMax(this.data, minmax);
		minY = minmax[0];
		maxY = minmax[1] + minmax[1]*0.1;  // let's give some extra 10% space
		yInterval = findOptimalYInterval(maxY);
		
		minmax = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
		calculateMinMax(this.xCategories, minmax);
		minX = minmax[0];
		maxX = minmax[1] + minmax[1]*0.1;  // let's give some extra 10% space
		xInterval = findOptimalYInterval(maxX);
		
		// TOOL TIPS management
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
    	label.setBackground(Color.WHITE);
        label.setBorder(UIManager.getBorder("ToolTip.border"));
        if(!GraphicsEnvironment.isHeadless()) {
        	toolTip = new JWindow();
        	toolTip.add(label);
    		// Tool tips
            tipster = new Tipster(this);
            addMouseMotionListener(tipster);
        }
        setOpaque(true);
	}

	
	private double findOptimalYInterval(double max) {
		int base = 1;
		double[] divisions = new double[] { 0.5, 1, 2, 2.5, 5 };

		while (true) {

			for (int d = 0; d < divisions.length; d++) {
				double tester = base * divisions[d];
				if (max / tester <= 10) {
					return tester;
				}
			}
			base *= 10;
		}
	}
	
	
	private void calculateMinMax(double[] myData, double[] minmax) {
		if(myData.length == 1) {
			// let's deal with this case separately.
			if(myData[0] >= 0) {
				minmax[0] = 0.0d;
				minmax[1] = myData[0];
			} else {
				minmax[0] = myData[0];
				minmax[1] = 0.0d;
			}
			return;
		}
		for(int i=0; i<myData.length; i++) {
			if(minmax[0] > myData[i]) {
				minmax[0] = myData[i];
			} else if(minmax[1] < myData[i]) {
				minmax[1] = myData[i];
			}
		}
		if(minmax[0] > 0) minmax[0] = 0.0d;
	}
	
	

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 600);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(100, 200);
	}

	@Override
	public int getHeight() {
		if (height < 0) {
			return super.getHeight();
		}
		return height;
	}

	@Override
	public int getWidth() {
		if (width < 0) {
			return super.getWidth();
		}
		return width;
	}

	@Override
	protected void paintComponent(Graphics g) {
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);

		if (g instanceof Graphics2D) {
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		double yStart, xStart;
		if (minY % yInterval == 0) {
			yStart = minY;
		} else {
			yStart = yInterval * (((int) minY / yInterval) + 1);
		}
		
		if (minX % xInterval == 0) {
			xStart = minX;
		} else {
			xStart = xInterval * (((int) minX / xInterval) + 1);
		}
		

		int xOffset = 0;

		// Draw the yLabel on the left of the yAxis
		int yLabelRightShift = 12;
		if(yLabel == null || yLabel.isEmpty()) {
			yLabelRightShift = 0;
		} else {
			if (g instanceof Graphics2D) {
				Graphics2D g2 = (Graphics2D)g;
				AffineTransform orig = g2.getTransform();
				g2.rotate(-Math.PI/2);
				g2.setColor(Color.BLACK);
				g2.drawString(yLabel, -getY(-yInterval)/2 - (g.getFontMetrics().stringWidth(yLabel)/2), yLabelRightShift);
				g2.setTransform(orig);
			}
		}
		
		
		// Draw the y axis labels
		int lastYLabelEnd = Integer.MAX_VALUE;
		for (double i=yStart; i<=maxY; i+=yInterval) {
			String label = "" + i;
			label = label.replaceAll(".0$", ""); // Don't leave trailing .0s where we don't need them.
			// Calculate the new xOffset depending on the widest ylabel.
			int width = g.getFontMetrics().stringWidth(label);
			if (width > xOffset) {
				xOffset = width;
			}
			// place the y axis labels so that they don't overlap when the plot is resized.
			int baseNumberHeight = g.getFontMetrics().getHeight();
			int baseNumberPosition = getY(i)+(baseNumberHeight/2);
			if (baseNumberPosition + baseNumberHeight < lastYLabelEnd) {
				// Draw the y axis labels
				g.drawString(label, yLabelRightShift+6, baseNumberPosition);
				lastYLabelEnd = baseNumberPosition + 2;
			}
		}

		
		// Give the x axis a bit of breathing space
		xOffset = xOffset + yLabelRightShift + 8;
		
		
		// Now draw horizontal lines across from the y axis
		g.setColor(new Color(180,180,180));
		for (double i=yStart; i<=maxY; i+=yInterval) {
			g.drawLine(xOffset, getY(i), getWidth()-10, getY(i));
		}
		g.setColor(Color.BLACK);
		

		
		// Draw the graph title
		int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
		g.drawString(graphTitle, (xOffset + ((getWidth() - (xOffset + 10)) / 2)) - (titleWidth / 2), 30);



		// Draw the xLabel under the xAxis
		g.drawString(xLabel, (getWidth() / 2) - (g.getFontMetrics().stringWidth(xLabel) / 2), getHeight() - 5);

		
		// Now draw the data points
		double baseWidth = (getWidth() - (xOffset + 10)) / (maxX-minX);

//		System.out.println("Base Width is "+baseWidth);
		// Let's find the longest label, and then work out how often we can draw labels
		int lastXLabelEnd = 0;
		
		// Draw the x axis labels
		for (double i=xStart; i<=maxX; i+=xInterval) {
			g.setColor(Color.BLACK);
			String baseNumber = "" + i;	
			baseNumber = baseNumber.replaceAll(".0$", ""); // Don't leave trailing .0s where we don't need them.
			// Calculate the new xOffset depending on the widest ylabel.
			int baseNumberWidth = g.getFontMetrics().stringWidth(baseNumber);
			int baseNumberPosition = (int)(xOffset + (baseWidth * i) - (baseNumberWidth / 2));

			if (baseNumberPosition > lastXLabelEnd) {
				g.drawString(baseNumber, baseNumberPosition, getHeight() - 25);
				lastXLabelEnd = baseNumberPosition + baseNumberWidth + 5;
			}
			// Now draw vertical lines across from the y axis
			g.setColor(new Color(180,180,180));
			g.drawLine((int)(xOffset + (baseWidth * i)), getHeight() - 40, (int)(xOffset + (baseWidth * i)), 40);
			g.setColor(Color.BLACK);
		}
		
		
		
		// Now draw the axes
		g.drawLine(xOffset, getHeight() - 40, getWidth() - 10, getHeight() - 40);
		g.drawLine(xOffset, getHeight() - 40, xOffset, 40);
		
		
		// Initialise the arrays containing the tooltips
		rectangles = new ArrayList<Rectangle>();
		tips = new ArrayList<String>();
		

		g.setColor(Color.BLUE);
		// Draw the data points
		double ovalSize = 5;
		// We distinguish two inputs since the x label does not start from 0.
		// used for computing the actual line points as if they were starting from 0.
		double[] inputVar = new double[data.length];
		double[] responseVar = new double[data.length];
		for (int d = 0; d < data.length; d++) {
			double x = getX(xCategories[d], xOffset)-ovalSize/2;
			double y = getY(data[d])-ovalSize/2;
			g.fillOval((int)x, (int)y, (int)(ovalSize), (int)(ovalSize));
			inputVar[d] = Double.valueOf(xCategories[d]); 
			responseVar[d] = data[d];

			// Tool tips
			Rectangle r = new Rectangle((int)x, (int)y, (int)(ovalSize), (int)(ovalSize));
			rectangles.add(r);
			tips.add(toolTipLabels[d]);
		}
		g.setColor(Color.BLACK);
		
		
		
		
		// Draw the intercept 
		
		// WARNING: Is drawing a least squares regression line asserting that "the distribution follows a power law" correct?
		// This is our case if we plot log-log..
		// It seems not in this paper (Appendix A) http://arxiv.org/pdf/0706.1062v2.pdf
		
		if(data.length > 1) {
			LinearRegression linReg = new LinearRegression(inputVar, responseVar);
			double intercept = linReg.intercept();
			double slope = linReg.slope();
			double rSquare = linReg.R2();
			
			// Let's now calculate the two points (x1, y1) and (xn, yn)
			// (x1, y1). We need to skip the areas where x1<minY and y1>maxY
			double x1 = minX;		
			double y1 = slope*minX + intercept;
			if(y1 < minY) {
				x1 = (minY - intercept)/slope;
				y1 = minY;
			} else if(y1 > maxY) {
				x1 = (maxY - intercept)/slope;
				y1 = maxY;
			}					
			// (xn, yn). maxX which essentially is inputVar[inputVar.length-1]
			double xn = maxX;
			double yn = slope*maxX + intercept;

			if (g instanceof Graphics2D) {
				((Graphics2D)g).setStroke(new BasicStroke(1.5f));
			}
			g.setColor(Color.RED);
			g.drawLine(getX(x1, xOffset),
					   getY(y1),
					   getX(xn, xOffset),
					   getY(yn));
			g.setColor(Color.BLACK);
			if (g instanceof Graphics2D) {
				((Graphics2D)g).setStroke(new BasicStroke(1));
			}			
			
			// Draw the legend for the intercept
			String legendString = "y = " + Precision.round(slope, 3) + "x";
			if(intercept < 0) 
				legendString += " - " + Precision.round(-intercept, 3);
			else 
				legendString += " + " + Precision.round(intercept, 3);
			int width = g.getFontMetrics().stringWidth(legendString);
			
			// First draw a box to put the legend in
			g.setColor(Color.WHITE);
			g.fillRect(xOffset+10, 45, width+8, 35);
			g.setColor(Color.LIGHT_GRAY);
			g.drawRect(xOffset+10, 45, width+8, 35);
	
			// Now draw the legend label
			g.setColor(Color.RED);
			g.drawString(legendString, xOffset+13, 60);
			g.drawString("R^2 = " + Precision.round(rSquare, 3), xOffset+13, 76);
			g.setColor(Color.BLACK);
		}
		
	}
	
	
	
	private int getY(double y) {
		return (getHeight() - 40) - (int) (((getHeight() - 80) / (maxY - minY)) * y);
	}
	
	private int getX(double x, int xOffset) {
		return  xOffset + (int) (((getWidth() - 40) / (maxX - minX)) * x);
	}

	
	///////////////////////
	// TOOL TIPS management
    ///////////////////////
	public void showToolTip(int index, Point p) {
        if(GraphicsEnvironment.isHeadless()) {
        	return;
        }
    	p.setLocation(p.getX()+10, p.getY()+25);
        label.setText(tips.get(index));
        toolTip.pack();
        toolTip.setLocation(p);
        toolTip.setVisible(true);
    }
 
    public void hideToolTip() {
        if(GraphicsEnvironment.isHeadless()) {
        	return;
        }
        toolTip.dispose();
    }
 
    public boolean isToolTipShowing() {
        if(GraphicsEnvironment.isHeadless()) {
        	return false;
        }
        return toolTip.isShowing();
    }
     
    class Tipster extends MouseInputAdapter {
        private ScatterGraph toolTips;
     
        public Tipster(ScatterGraph tt) {
            toolTips = tt;
        }
     
        @Override
    	public void mouseMoved(MouseEvent e) {
            if(GraphicsEnvironment.isHeadless()) {
            	return;
            }
            Point p = e.getPoint();
            boolean traversing = false;
            for(int j = 0; j < toolTips.rectangles.size(); j++) {
                Rectangle r = toolTips.rectangles.get(j);
                if(r.contains(p)) {
                    SwingUtilities.convertPointToScreen(p, toolTips);
                    toolTips.showToolTip(j, p);
                    traversing = true;
                    break;
                }
            }
            if(!traversing && toolTips.isToolTipShowing())
            	toolTips.hideToolTip();
        }
        
    }
	
	
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				Random r = new Random();
				int sampleSize = 1000;
				double[] data = new double[sampleSize];
				double[] xCategories = new double[sampleSize];
				String[] toolTipsLabels = new String[sampleSize];
				for(int i=0; i<sampleSize; i++) {
					data[i] = Math.log((r.nextGaussian()*1.5 + 10)*i + 50);
					xCategories[i] = Math.log(i + 50);
					toolTipsLabels[i] = String.valueOf(i);
				}
					
				String xLabel = "xLabel";
				String yLabel = "yLabel";
				//String yLabel = null;
				String graphTitle = "Graph Title";

				JFrame frame = new JFrame();
				ScatterGraph scatterGraph = new ScatterGraph(data, xCategories, toolTipsLabels, xLabel, yLabel, graphTitle);

				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(500, 500);
				frame.add(scatterGraph);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}
