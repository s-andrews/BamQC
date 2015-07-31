/**
 * Copyright Copyright 2010-12 Piero Dalle Pezze
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;


public class LineWithHorizontalBarGraph extends JPanel {

	private static final long serialVersionUID = -5947375412672203276L;
	protected String [] xTitles;
	protected String xLabel;
	protected String barDataLabel;	
	protected String [] xCategories;
	protected double [][] lineData;
	protected double [] barData;
	protected String graphTitle;
	protected double minY;
	protected double maxY;
	protected double maxX;
	protected double yInterval;
	protected int height = -1;
	protected int width = -1;
	
	protected static final Color [] COLOURS = new Color[] {new Color(220,0,0), new Color(0,0,220), new Color(0,220,0), Color.DARK_GRAY, Color.MAGENTA, Color.ORANGE,Color.YELLOW,Color.CYAN,Color.PINK,Color.LIGHT_GRAY};
	
	public LineWithHorizontalBarGraph(double[] barData, double[][] lineData, double minY, double maxY, String xLabel, String[] xTitles, int[] xCategories, String graphTitle, String barDataLabel) {
		this(barData,lineData,minY,maxY,xLabel,xTitles,new String[0],graphTitle, barDataLabel);
		this.xCategories = new String [xCategories.length];
		for (int i=0;i<xCategories.length;i++) {
			this.xCategories[i] = ""+xCategories[i];
		}
	}
	
	public LineWithHorizontalBarGraph(double[] barData, double[][] lineData, double minY, double maxY, String xLabel, String[] xTitles, String[] xCategories, String graphTitle, String barDataLabel) {
		this.barData = barData;
		this.lineData = lineData;
		this.minY = minY;
		this.maxY = maxY;
		this.xTitles = xTitles;
		this.xLabel = xLabel;
		this.xCategories = xCategories;
		this.graphTitle = graphTitle;
		this.barDataLabel = barDataLabel;
		this.yInterval = findOptimalYInterval(maxY);
	}
	
	private double findOptimalYInterval(double max) {
		
		int base = 1;
		double [] divisions = new double [] {1,2,2.5,5};
		
		while (true) {
			
			for (int d=0;d<divisions.length;d++) {
				double tester = base * divisions[d];
				if (max / tester <= 10) {
					return tester;
				}
			}
		
			base *=10;
			
		}
		
	}
	
	@Override
	public Dimension getPreferredSize () {
		return new Dimension(800,600);
	}

	@Override
	public Dimension getMinimumSize () {
		return new Dimension(100,200);
	}

	
	@Override
	public int getHeight () {
		if (height <0) {
			return super.getHeight();
		}
		return height;
	}

	@Override
	public int getWidth () {
		if (width < 0) {
			return super.getWidth();
		}
		return width;
	}

	public void paint (Graphics g, int width, int height) {
		this.height = height;
		this.width = width;
		paint(g);
		this.height = -1;
		this.width = -1;
	}
	

	@Override
	public void paint (Graphics g) {
		super.paint(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		
		
		int xOffsetLineGraph = 0;

		double midY = minY+((maxY-minY)/2);
		for (int d=0;d<lineData.length;d++) {
			String label = xTitles[d];
			int width = g.getFontMetrics().stringWidth(label);
			if (width > xOffsetLineGraph) {
				xOffsetLineGraph = width;
			}
			g.drawString(label, 2, getY(midY,d)+(g.getFontMetrics().getAscent()/2));
			
		}
		// calculate maxX
		maxX=0;
		for(int i=0; i<barData.length; i++) {
			maxX = maxX + barData[i];
		}
		
	
		// Give the x axis a bit of breathing space
		xOffsetLineGraph += 5;
		
		
		
		// Draw the graph title
		int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
		g.drawString(graphTitle, (xOffsetLineGraph + ((getWidth()-(xOffsetLineGraph+10))/2)) - (titleWidth/2), 30);
		
		// Now draw the axes
		// x axis
		g.drawLine(xOffsetLineGraph, getHeight()-40, getWidth()-10,getHeight()-40);
		// y axis
		g.drawLine(xOffsetLineGraph, getHeight()-40, xOffsetLineGraph, 80);

		// Draw the xLabel under the xAxis
		g.drawString(xLabel, (getWidth()/2) - (g.getFontMetrics().stringWidth(xLabel)/2), getHeight()-5);
		
		// Draw the label for the bar data
		g.drawString(barDataLabel, xOffsetLineGraph, 78);
		
		
			
		// Now draw the horizontal bar (1st plot)
		// First we need to find the widest label
		
		int leftSpace = g.getFontMetrics().stringWidth("");
		
		// Add 3px either side for a bit of space;
		leftSpace += 6;

		int xPos=0;
		int xOffsetBarGraph=0;
		// set y coordinates
		int yPos=+80;
		int yOffset=60;
		int cumulativeXOffset = 0;
		
		for(int i=0; i<barData.length; i++) {
			int xValue = getX(Math.min(barData[i], maxX), leftSpace);
			// set xPos and xOffset
			if(cumulativeXOffset==0) 
				xPos=leftSpace;
			else 
				xPos=cumulativeXOffset;
			xOffsetBarGraph=xValue-leftSpace;
			
			// draw the stacked horizontal bar scaffolds
			g.setColor(new Color(200,0,0));
			g.fillRect(xPos, yPos, xOffsetBarGraph, yOffset);
			g.setColor(Color.BLACK);
			g.drawRect(xPos, yPos, xOffsetBarGraph, yOffset);
			
			
			// increase the cumulative X offset to get a measure for this plot, 
			// as we need this for scaling the second plot.
			cumulativeXOffset = xPos+xOffsetBarGraph;
			
			// draw grey lines to annotate the second plots
			g.setColor(new Color(230, 230, 230));
			g.drawLine(cumulativeXOffset, getHeight()-40-1, cumulativeXOffset, 140);
			g.setColor(Color.BLACK);	
			
		}

		
		
		
		// Now draw horizontal lines across from the y axis (2nd plot)
		// First draw faint boxes over alternating bases so you can see which is which
		// Let's find the longest label, and then work out how often we can draw labels
		double baseWidth = 1;	
		int lastY = 0;
		// check that there is some data
		if(lineData.length > 0) {
			// Now draw the data points
			// Set the width for the plot line
			baseWidth = 1.0*(cumulativeXOffset)/lineData[0].length;			

			int lastXLabelEnd = 0;
			for(int i=0; i<lineData[0].length; i++) {
				String baseNumber = ""+xCategories[i];
				int baseNumberWidth = g.getFontMetrics().stringWidth(baseNumber);
				int baseNumberPosition =  (int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*i)-(baseNumberWidth/2));
				
				if (baseNumberPosition > lastXLabelEnd) {
					g.drawString(baseNumber,baseNumberPosition, getHeight()-25);
					lastXLabelEnd = baseNumberPosition+baseNumberWidth+5;
				}
			}
		}
		
		// Draw an horizontal line behind the line graph.
		g.setColor(new Color(180,180,180));
		g.setColor(new Color(128,128,128));
		for (int d=0;d<lineData.length;d++) {
			g.drawLine(xOffsetLineGraph, getY(midY,d), getWidth()-10, getY(midY,d));
		}
		g.setColor(Color.BLACK);
		
	
		// Now draw the datasets
		if (g instanceof Graphics2D) {
			((Graphics2D)g).setStroke(new BasicStroke(2));
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		for (int d=0;d<lineData.length;d++) {
			g.setColor(COLOURS[0]);
			if(lineData[d].length > 0) {
				lastY = getY(lineData[d][0],d);	
				for (int i=1;i<lineData[d].length;i++) {
					if (Double.isNaN(lineData[d][i])) break;
					int thisY = getY(lineData[d][i],d);
					g.drawLine((int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*(i-1))), lastY, (int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*i)), thisY);
				
					lastY = thisY;
				}
			}
			
		}
				
		
	}

	private int getY(double y, int index) {
		int totalPlotArea = getHeight()-160;
		int plotAreaPerSample = totalPlotArea/lineData.length;
		return (getHeight()-30) - ((plotAreaPerSample*index)+(int)((plotAreaPerSample/(maxY-minY))*(y-minY)));		
	}
	
	
	private int getX(double value, int longestLabel) {
		int lengthToUse = getWidth()-(longestLabel+20);
		double proportion = value/maxX;		
		return longestLabel+(int)(lengthToUse*proportion);
	}

	
	
}
