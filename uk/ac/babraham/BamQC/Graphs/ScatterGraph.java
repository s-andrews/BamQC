/**
 * Copyright Copyright 2014 Bart Ailey Eagle Genomics Ltd
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

package uk.ac.babraham.BamQC.Graphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Utilities.LinearRegression;

public class ScatterGraph extends JPanel {

	private static final long serialVersionUID = -7292512222510200683L;

	private static Logger log = Logger.getLogger(ScatterGraph.class);

	private String xLabel;
	private String[] xCategories;
	private double[] data;
	private String graphTitle;
	private double minY;
	private double maxY;
	private double yInterval;
	private int height = -1;
	private int width = -1;

	public ScatterGraph(double[] data, double minY, double maxY, String xLabel, int[] xCategories, String graphTitle) {
		this(data, minY, maxY, xLabel, new String[0], graphTitle);
		this.xCategories = new String[xCategories.length];

		for (int i = 0; i < xCategories.length; i++) {
			this.xCategories[i] = "" + xCategories[i];
		}
	}

	public ScatterGraph(double[] data, double minY, double maxY, String xLabel, String[] xCategories, String graphTitle) {
		this.data = data;
		this.minY = minY;
		this.maxY = maxY;
		this.xLabel = xLabel;
		this.xCategories = xCategories;
		this.graphTitle = graphTitle;
		this.yInterval = findOptimalYInterval(maxY);
	}

	private double findOptimalYInterval(double max) {
		int base = 1;
		double[] divisions = new double[] { 1, 2, 2.5, 5 };

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

	public void paint(Graphics g, int width, int height) {
		this.height = height;
		this.width = width;
		paint(g);
		this.height = -1;
		this.width = -1;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);

		if (g instanceof Graphics2D) {
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		int lastY = 0;

		double yStart;

		if (minY % yInterval == 0) {
			yStart = minY;
		}
		else {
			yStart = yInterval * (((int) minY / yInterval) + 1);
		}

		int xOffset = 0;

		
		// Draw the y axis labels
		for (double i = yStart; i <= maxY; i += yInterval) {
			String label = "" + i;
			label = label.replaceAll(".0$", ""); // Don't leave trailing .0s
													// where we don't need them.
			int width = g.getFontMetrics().stringWidth(label);
			if (width > xOffset) {
				xOffset = width;
			}

			g.drawString(label, 2, getY(i) + (g.getFontMetrics().getAscent() / 2));
		}

		// Give the x axis a bit of breathing space
		xOffset += 5;

		
		// Draw the graph title
		int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
		g.drawString(graphTitle, (xOffset + ((getWidth() - (xOffset + 10)) / 2)) - (titleWidth / 2), 30);



		// Draw the xLabel under the xAxis
		g.drawString(xLabel, (getWidth() / 2) - (g.getFontMetrics().stringWidth(xLabel) / 2), getHeight() - 5);

		// Now draw the data points
		double baseWidth = (getWidth() - (xOffset + 10)) / data.length;
		if (baseWidth < 1) baseWidth = 1;

		// System.out.println("Base Width is "+baseWidth);
		// Let's find the longest label, and then work out how often we can draw
		// labels
		int lastXLabelEnd = 0;
		
		// Draw the x axis labels
		for (int i = 0; i < data.length; i++) {
			g.setColor(Color.BLACK);
			String baseNumber = "" + xCategories[i];
			int baseNumberWidth = g.getFontMetrics().stringWidth(baseNumber);
			int baseNumberPosition = (int)((baseWidth / 2) + xOffset + (baseWidth * i) - (baseNumberWidth / 2));

			if (baseNumberPosition > lastXLabelEnd) {
				g.drawString(baseNumber, baseNumberPosition, getHeight() - 25);
				lastXLabelEnd = baseNumberPosition + baseNumberWidth + 5;
			}
		}
		
		
		// Now draw horizontal lines across from the y axis
//		g.setColor(new Color(180, 180, 180));
//		for (double i = yStart; i <= maxY; i += yInterval) {
//			g.drawLine(xOffset, getY(i), getWidth() - 10, getY(i));
//		}
//		g.setColor(Color.BLACK);

		
		
		// Now draw the axes
		g.drawLine(xOffset, getHeight() - 40, getWidth() - 10, getHeight() - 40);
		g.drawLine(xOffset, getHeight() - 40, xOffset, 40);
		

		// Draw the data points
		double ovalSize = 4;
		double[] inputVar = new double[data.length];
		double[] responseVar = new double[data.length];
		for (int d = 0; d < data.length; d++) {
			double x = xOffset + ((baseWidth * d) + (baseWidth * (d+1)))/2;
			double y = getY(data[d])-ovalSize/2;
			g.fillOval((int)x, (int)y, (int)(ovalSize), (int)(ovalSize));
			inputVar[d] = Double.valueOf(xCategories[d]);
			responseVar[d] = data[d];	
		}

		
		// Draw the intercept
		if(data.length > 1) {
			LinearRegression linReg = new LinearRegression(inputVar, responseVar);
			double intercept = linReg.intercept();
			double slope = linReg.slope();
			double y1=0;
			int index = 0;
			for(int i=0; i<inputVar.length; i++) {
				y1 = slope*inputVar[i] + intercept;
				index = i;
				if(y1>=0d) break;
			}
			double y2 = slope*inputVar[responseVar.length-1] + intercept;
			
			g.setColor(Color.RED);
			g.drawLine((int)(xOffset + ((baseWidth * (index) + (baseWidth * (index+1)))/2)), 
					   getY(y1), 
					   (int)(xOffset + ((baseWidth * (inputVar.length-1)) + (baseWidth * (inputVar.length)))/2), 
					   getY(y2));
			g.setColor(Color.BLACK);
			
			// Draw the legend for the intercept
			// First we need to find the widest label
			String interceptString = "y = " + (float)(slope) + " * x ";
			if(intercept < 0) 
				interceptString = interceptString + " - " + (float)(-intercept);
			else 
				interceptString = interceptString + " + " + (float)intercept;
			int width = g.getFontMetrics().stringWidth(interceptString);
			
			// First draw a box to put the legend in
			g.setColor(Color.WHITE);
			g.fillRect(xOffset+10, 40, width+8, 23);
			g.setColor(Color.LIGHT_GRAY);
			g.drawRect(xOffset+10, 40, width+8, 23);
	
			// Now draw the intercept label
			g.setColor(Color.RED);
			g.drawString(interceptString, xOffset+13, 60);
			g.setColor(Color.BLACK);
		}
		
	}
	
	
	
	private int getY(double y) {
		return (getHeight() - 40) - (int) (((getHeight() - 80) / (maxY - minY)) * y);
	}
	
	

	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				Random r = new Random();
				int sampleSize = 1000;
				double[] data = new double[sampleSize];
				String[] xCategories = new String[sampleSize];
				double minY = Double.MAX_VALUE;
				double maxY = Double.MIN_VALUE;
				for(int i=0; i<sampleSize; i++) {
					data[i] = (r.nextGaussian()*0.9 + 10)*i;
					xCategories[i] = Integer.toString(i);
					if(data[i] > maxY) maxY = data[i];
					else if(data[i] < minY) minY = data[i];
				}
					
				String xLabel = "xLabel";
				String graphTitle = "Graph Title";

				JFrame frame = new JFrame();
				ScatterGraph scatterGraph = new ScatterGraph(data, minY, maxY, xLabel, xCategories, graphTitle);

				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(500, 500);
				frame.add(scatterGraph);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

}