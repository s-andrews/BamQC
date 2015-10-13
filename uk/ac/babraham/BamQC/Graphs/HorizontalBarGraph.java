/**
 * Copyright Copyright 2010-12 Simon Andrews
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.ac.babraham.BamQC.Utilities.AxisScale;

public class HorizontalBarGraph extends JPanel {

	private static final long serialVersionUID = -4964032131806305960L;
	private String [] labels;
	private double [] values;
	private String xLabel;
	private String title;
	private double maxX = 0f;
	private double minX = 0f;
	private double xInterval;
	private int height = -1;
	private int width = -1;
		

	public HorizontalBarGraph (String [] labels, double [] values, String xLabel, String title, double minX, double maxX) {
		this.labels = labels;
		this.values = values;
		this.xLabel = xLabel;
		this.title = title;
		this.minX = minX;		
		this.maxX = maxX;
		this.xInterval = new AxisScale (minX, maxX).getInterval();
	}
	
	public HorizontalBarGraph (String [] labels, double [] values, String xLabel, String title) {

		this.labels = labels;
		this.values = values;
		this.xLabel = xLabel;
		this.title = title;
		
		for (int v=0;v<values.length;v++) {
			if (values[v] > maxX) maxX = values[v];
			else if (values[v] < minX) minX = values[v];
		}
//		System.err.println("maxX is "+maxX);
		
		this.xInterval = new AxisScale (minX, maxX).getInterval();
	}
	
	
	@Override
	public Dimension getPreferredSize () {
		return new Dimension(800,20*labels.length);
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
		
		// First we need to find the widest label
		int widestLabel = 0;
		for (int l=0;l<labels.length;l++) {
			int width = g.getFontMetrics().stringWidth(labels[l]);
			if (width > widestLabel) widestLabel = width;
		}
		
		// Add 3px either side for a bit of space;
		widestLabel += 6;
		
		// Calculate how much space each y line is going to get.  Each sequence
		// gets a line as does the axis and there is a blank line at top and bottom.
		
		int yLineHeight = getHeight()/(labels.length+3);
		
		// Draw a title
		int titleWidth = g.getFontMetrics().stringWidth(title);
		g.drawString(title, (getWidth()/2) - (titleWidth/2), (yLineHeight/2)+(g.getFontMetrics().getAscent()/2));
		
		// Draw the axes
		
		// Y-axis
		g.drawLine(widestLabel, yLineHeight, widestLabel, getHeight()-(yLineHeight*2));
		
		// X-axis
		g.drawLine(widestLabel,getHeight()-(yLineHeight*2),getWidth()-20,getHeight()-(yLineHeight*2));

		// Draw the xLabel under the xAxis
		g.drawString(xLabel, (getWidth()/2) - (g.getFontMetrics().stringWidth(xLabel)/2), getHeight()-5);
		
	
		// draw the scale for the x axis
		double currentValue = minX;
		int currentPosition = 0;
		int lastXLabelEnd = 0;

		while (currentValue < maxX) {
			int xPos = getX((float)currentValue, widestLabel);

			String label = "" + new BigDecimal(currentValue).setScale(
					AxisScale.getFirstSignificantDecimalPosition(xInterval), RoundingMode.HALF_UP).doubleValue();	
			label = label.replaceAll(".0$", ""); // Don't leave trailing .0s where we don't need them.
						
			int labelWidth = g.getFontMetrics().stringWidth(label);

			currentPosition = xPos-(labelWidth/2);
			if(currentPosition > lastXLabelEnd) {
				g.drawLine(xPos, getHeight()-(yLineHeight*2), xPos, getHeight()-(yLineHeight*2)+3);
				g.drawString(label, currentPosition, getHeight()-(yLineHeight*2)+(g.getFontMetrics().getHeight()+3));
				lastXLabelEnd = currentPosition + labelWidth + 5;
			}	
			currentValue += xInterval;
		}
		
			
		// Now draw the data
		for (int s=0;s<labels.length;s++) {
			
			// vertical labels
			String label = labels[s];
			
			int labelWidth = g.getFontMetrics().stringWidth(label);
				
			g.drawString(label, widestLabel-(3+labelWidth), yLineHeight*(s+2)-(yLineHeight/2)+(g.getFontMetrics().getAscent()/2));
			
			int xValue = getX(Math.min(values[s], maxX), widestLabel);
			
			g.setColor(new Color(200,0,0));
			g.fillRect(widestLabel, (yLineHeight*(s+1))+3,xValue-widestLabel,yLineHeight-6);
			g.setColor(Color.BLACK);
			g.drawRect(widestLabel, (yLineHeight*(s+1))+3,xValue-widestLabel,yLineHeight-6);
		}
		
		
	}
	
	private int getX (double value, int longestLabel) {
		int lengthToUse = getWidth()-(longestLabel+20);
		
		double proportion = value/maxX;
		
		return longestLabel+(int)(lengthToUse*proportion);
		
	}
	
	
	public static void main(String[] argv) {
		JFrame f = new JFrame();
		f.setSize(600, 400);
		double[] values = new double[4];
		String[] names = new String[3];
		names[0] = "Item 1";
		values[0] = 100000;
		names[1] = "Very long Item 2";
		values[1] = 20000;
		names[2] = "Item 3";
		values[2] = 200000;

		

		f.getContentPane().add(new HorizontalBarGraph(names, values, "Counts", "Stacked Horizontal Bar Graph Test"));

		WindowListener wndCloser = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		   }
		};
	   f.addWindowListener(wndCloser);
	   f.setVisible(true);
	}
	
	
	
}
