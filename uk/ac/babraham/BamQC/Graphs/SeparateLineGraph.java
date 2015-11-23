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
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import uk.ac.babraham.BamQC.Utilities.FormatNumber;

public class SeparateLineGraph extends JPanel {

	private static final long serialVersionUID = -2880615892132541273L;
	protected String [] xTitles;
	protected String xLabel;
	protected String yLabel;
	protected String [] xCategories;
	protected double [][] data;
	protected String graphTitle;
	protected double minY = 0.0d;
	protected double maxY = 0.0d;
	protected double yInterval = 0.0d;
	protected int height = -1;
	protected int width = -1;
	
	protected static final Color [] COLOURS = new Color[] {new Color(220,0,0), new Color(0,0,220), new Color(0,220,0), Color.DARK_GRAY, Color.MAGENTA, Color.ORANGE,Color.YELLOW,Color.CYAN,Color.PINK,Color.LIGHT_GRAY};
	
	public SeparateLineGraph (double [] [] data, double minY, double maxY, String xLabel, String yLabel, String [] xTitles, int [] xCategories, String graphTitle) {
		this(data,minY,maxY,xLabel,yLabel,xTitles,new String[0],graphTitle);
		this.xCategories = new String [xCategories.length];
		for (int i=0;i<xCategories.length;i++) {
			this.xCategories[i] = ""+xCategories[i];
		}
	}
	
	public SeparateLineGraph (double [] [] data, double minY, double maxY, String xLabel, String yLabel, String [] xTitles, String [] xCategories, String graphTitle) {
		this.data = data;
		this.minY = minY;
		this.maxY = maxY;
		this.xTitles = xTitles;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
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
				g2.drawString(yLabel, -getY(-yInterval, 0)/2 - (g.getFontMetrics().stringWidth(yLabel)/2), yLabelRightShift);
				g2.setTransform(orig);
			}
		}
		
		double midY = minY+((maxY-minY)/2);
		for (int d=0;d<data.length;d++) {
			String label = xTitles[d];
			int width = g.getFontMetrics().stringWidth(label);
			if (width > xOffset) {
				xOffset = width;
			}
			
			g.drawString(label, yLabelRightShift+6, getY(midY, d) + (g.getFontMetrics().getAscent() / 2));
		}

		// Give the x axis a bit of breathing space
		xOffset = xOffset + yLabelRightShift + 8;
		
		// Draw the graph title
		int titleWidth = g.getFontMetrics().stringWidth(graphTitle);
		g.drawString(graphTitle, (xOffset + ((getWidth()-(xOffset+10))/2)) - (titleWidth/2), 30);
		
		
		// Draw the xLabel under the xAxis
		g.drawString(xLabel, (getWidth()/2) - (g.getFontMetrics().stringWidth(xLabel)/2), getHeight()-5);
		
		
		double baseWidth = 1d;
		
		// check that there is some data
		if(data.length > 0) {
			// Now draw the data points
			baseWidth = 1.0d*(getWidth()-(xOffset+10))/data[0].length;
			if (baseWidth<1) baseWidth=1;

			// System.out.println("Base Width is "+baseWidth);
			// First draw faint boxes over alternating bases so you can see which is which
			// Let's find the longest label, and then work out how often we can draw labels

			int lastXLabelEnd = 0;

			for (int i=0;i<data[0].length;i++) {
				if (i%2 != 0) {
					g.setColor(new Color(230, 230, 230));
					g.fillRect((int)(xOffset+(baseWidth*i)), 40, (int)baseWidth, getHeight()-80);
				}
				g.setColor(Color.BLACK);

				//String baseNumber = ""+xCategories[i];
				//baseNumber = FormatNumber.compactInteger(baseNumber);
				String baseNumber = FormatNumber.convertToScientificNotation(xCategories[i]);
				baseNumber = baseNumber.replaceAll(".0$", ""); // Don't leave trailing .0s where we don't need them.
				int baseNumberWidth = g.getFontMetrics().stringWidth(baseNumber);
				int baseNumberPosition =  (int)((baseWidth/2)+xOffset+(baseWidth*i)-(baseNumberWidth/2));

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

		g.setColor(COLOURS[0]);
		for (int d=0;d<data.length;d++) {
			g.setColor(COLOURS[0]);			
			// First check whether we are starting with points having 0 coverage.
			int i=0;
			lastY = getY(data[d][i],d);
			for (; i<data[d].length && Double.isInfinite(data[d][i]); i++) {
				// TODO 
				// Darken the area if these points have 0 coverage
//				g.setColor(new Color(100, 100, 100));
//				g.fillRect((baseWidth/2)+xOffset+(baseWidth*(i)), (int)(minY), (baseWidth/2)+xOffset+(baseWidth*(i+1)), getY(minY,d));
				g.setColor(Color.BLACK);				
				g.drawLine((int)((baseWidth/2)+xOffset+(baseWidth*(i))), getY(minY*0.75,d), (int)((baseWidth/2)+xOffset+(baseWidth*(i+1))), getY(minY*0.75,d));
				g.setColor(COLOURS[0]);
			}
			
			if(i<data[d].length) {
				// This point has non-zero coverage
				lastY = getY(data[d][i],d);
		    }
			
			// Now we continue with the plot.
			for (i++; i<data[d].length; i++) {
				// Check whether we are at the end
				if (Double.isNaN(data[d][i])) break;
				// Check whether we have points with null coverage
				if (Double.isInfinite(data[d][i]) ) {
					// TODO 
					// Darken the area if these points have 0 coverage
//					g.setColor(new Color(100, 100, 100));
//					g.fillRect((baseWidth/2)+xOffset+(baseWidth*(i-1)), (int)(minY), (baseWidth/2)+xOffset+(baseWidth*i), getY(minY,d));
					g.setColor(Color.BLACK);
					g.drawLine((int)((baseWidth/2)+xOffset+(baseWidth*(i-1))), getY(minY*0.75,d), (int)((baseWidth/2)+xOffset+(baseWidth*i)), getY(minY*0.75,d));
					g.setColor(COLOURS[0]);
					lastY = getY(midY,d);
					continue;
				}
				
				int thisY = getY(data[d][i],d);
				
				g.drawLine((int)((baseWidth/2)+xOffset+(baseWidth*(i-1))), lastY, (int)((baseWidth/2)+xOffset+(baseWidth*i)), thisY);
				lastY = thisY;
			}
		}
		g.setColor(Color.BLACK);
				
		
	}

	private int getY(double y, int index) {
		
		int totalPlotArea = getHeight()-80;
		int plotAreaPerSample = totalPlotArea/data.length;
				
		return (getHeight()-40) - ((plotAreaPerSample*index)+(int)((plotAreaPerSample/(maxY-minY))*(y-minY)));
	}
	
}
