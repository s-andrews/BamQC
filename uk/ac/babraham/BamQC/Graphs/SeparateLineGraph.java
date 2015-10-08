/**
 * Copyright Copyright 2010-14 Simon Andrews
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

public class SeparateLineGraph extends JPanel {

	private static final long serialVersionUID = -2880615892132541273L;
	protected String [] xTitles;
	protected String xLabel;
	protected String [] xCategories;
	protected double [][] data;
	protected String graphTitle;
	protected double minY = 0.0d;
	protected double maxY = 0.0d;
	protected double yInterval = 0.0d;
	protected int height = -1;
	protected int width = -1;
	
	protected static final Color [] COLOURS = new Color[] {new Color(220,0,0), new Color(0,0,220), new Color(0,220,0), Color.DARK_GRAY, Color.MAGENTA, Color.ORANGE,Color.YELLOW,Color.CYAN,Color.PINK,Color.LIGHT_GRAY};
	
	public SeparateLineGraph (double [] [] data, double minY, double maxY, String xLabel, String [] xTitles, int [] xCategories, String graphTitle) {
		this(data,minY,maxY,xLabel,xTitles,new String[0],graphTitle);
		this.xCategories = new String [xCategories.length];
		for (int i=0;i<xCategories.length;i++) {
			this.xCategories[i] = ""+xCategories[i];
		}
	}
	
	public SeparateLineGraph (double [] [] data, double minY, double maxY, String xLabel, String [] xTitles, String [] xCategories, String graphTitle) {
		this.data = data;
		this.minY = minY;
		this.maxY = maxY;
		this.xTitles = xTitles;
		this.xLabel = xLabel;
		this.xCategories = xCategories;
		this.graphTitle = graphTitle;
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
		if (width <0) {
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
		
		if (g instanceof Graphics2D) {
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		int lastY = 0;
		
		int xOffset = 0;

		double midY = minY+((maxY-minY)/2);
		for (int d=0;d<data.length;d++) {
			String label = xTitles[d];
			int width = g.getFontMetrics().stringWidth(label);
			if (width > xOffset) {
				xOffset = width;
			}
			
			g.drawString(label, 2, getY(midY,d)+(g.getFontMetrics().getAscent()/2));
			
		}
	
		// Give the x axis a bit of breathing space
		xOffset += 5;
		
		// Draw the graph title
		int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
		g.drawString(graphTitle, (xOffset + ((getWidth()-(xOffset+10))/2)) - (titleWidth/2), 30);
		
		
		// Draw the xLabel under the xAxis
		g.drawString(xLabel, (getWidth()/2) - (g.getFontMetrics().stringWidth(xLabel)/2), getHeight()-5);
		
		
		int baseWidth = 1;
		
		// check that there is some data
		if(data.length > 0) {
			// Now draw the data points
			baseWidth = (getWidth()-(xOffset+10))/data[0].length;
			if (baseWidth<1) baseWidth=1;

			// System.out.println("Base Width is "+baseWidth);
			// First draw faint boxes over alternating bases so you can see which is which
			// Let's find the longest label, and then work out how often we can draw labels

			int lastXLabelEnd = 0;

			for (int i=0;i<data[0].length;i++) {
				if (i%2 != 0) {
					g.setColor(new Color(230, 230, 230));
					g.fillRect(xOffset+(baseWidth*i), 40, baseWidth, getHeight()-80);
				}
				g.setColor(Color.BLACK);

				String baseNumber = ""+xCategories[i];
				int baseNumberWidth = g.getFontMetrics().stringWidth(baseNumber);
				int baseNumberPosition =  (baseWidth/2)+xOffset+(baseWidth*i)-(baseNumberWidth/2);

				if (baseNumberPosition > lastXLabelEnd) {
					g.drawString(baseNumber,baseNumberPosition, getHeight()-25);
					lastXLabelEnd = baseNumberPosition+baseNumberWidth+5;
				}
			}
		}
		
		// Now draw horizontal lines across from the y axis

		g.setColor(new Color(180,180,180));
		for (int d=0;d<data.length;d++) {
			g.drawLine(xOffset, getY(midY,d), getWidth()-10, getY(midY,d));
		}
		g.setColor(Color.BLACK);
		
		// Now draw the axes
		g.drawLine(xOffset, getHeight()-40, getWidth()-10,getHeight()-40);
		g.drawLine(xOffset, getHeight()-40, xOffset, 40);
		
		// Now draw the datasets
		
		if (g instanceof Graphics2D) {
			((Graphics2D)g).setStroke(new BasicStroke(2));
			//((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		for (int d=0;d<data.length;d++) {
			g.setColor(COLOURS[0]);
						
			lastY = getY(data[d][0],d);
			for (int i=1;i<data[d].length;i++) {
				if (Double.isNaN(data[d][i])) break;
				
				int thisY = getY(data[d][i],d);
				
				g.drawLine((baseWidth/2)+xOffset+(baseWidth*(i-1)), lastY, (baseWidth/2)+xOffset+(baseWidth*i), thisY);
				lastY = thisY;
			}
			
		}
				
		
	}

	private int getY(double y, int index) {
		
		int totalPlotArea = getHeight()-80;
		int plotAreaPerSample = totalPlotArea/data.length;
				
		return (getHeight()-40) - ((plotAreaPerSample*index)+(int)((plotAreaPerSample/(maxY-minY))*(y-minY)));
	}
	
}
