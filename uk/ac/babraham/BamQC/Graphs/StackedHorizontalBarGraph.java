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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.ac.babraham.BamQC.Utilities.AxisScale;

public class StackedHorizontalBarGraph extends JPanel {

	private static final long serialVersionUID = -5947375412672203276L;
	protected String [] labels;
	protected double [][] values;
	protected String title;
	protected double maxX = 0.0d;
	protected double minX = 0.0d;
	protected double xInterval;
	protected int height = -1;
	protected int width = -1;
		

	public StackedHorizontalBarGraph (String [] labels, double [][] values, String title, double minX, double maxX) {
		this.labels = labels;
		this.values = values;
		this.title = title;
		this.minX = minX;		
		this.maxX = maxX;
		this.xInterval = new AxisScale (minX, maxX).getInterval();
	}
	
	public StackedHorizontalBarGraph (String [] labels, double [][] values, String title) {

		this.labels = labels;
		this.values = values;
		this.title = title;

		double tempSum;
		for(int i=0; i<values.length; i++) {
			tempSum = 0.0d;
			for(int j=0; j<values[i].length; j++) {
				tempSum = tempSum + values[i][j];
			}
			if(tempSum > maxX) maxX = tempSum;
			else if(tempSum < minX) minX = tempSum;				
		}
//		System.err.println("maxX is "+maxX);
		this.xInterval = new AxisScale (minX, maxX).getInterval();
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
		// Original methods from HorizontalBarGraph to try X and Y axes.
		// y axis
		g.drawLine(widestLabel, yLineHeight, widestLabel, getHeight()-(yLineHeight*2));
		// x axis
		g.drawLine(widestLabel, getHeight()-(yLineHeight*2), getWidth()-20, getHeight()-(yLineHeight*2));
		
		// remove
		// Add the scale to the x-axis
		//AxisScale scale = new AxisScale(0, maxValue);
		double currentValue = minX;
		//double currentValue = scale.getStartingValue();
		
		while (currentValue < maxX) {
			int xPos = getX((float)currentValue, widestLabel);
			g.drawLine(xPos, getHeight()-(yLineHeight*2), xPos, getHeight()-(yLineHeight*2)+3);
			
			//String label = scale.format(currentValue);
			String label = "" + new BigDecimal(currentValue).setScale(
					AxisScale.getFirstSignificantDecimalPosition(xInterval), RoundingMode.HALF_UP).doubleValue();	
			label = label.replaceAll(".0$", ""); // Don't leave trailing .0s where we don't need them.
						
			int labelWidth = g.getFontMetrics().stringWidth(label);
				
			g.drawString(label, xPos-(labelWidth/2), getHeight()-(yLineHeight*2)+(g.getFontMetrics().getHeight()+3));
			
			currentValue += xInterval;
		}
		
		
		// Now draw the data
		// set yOffset
		int xOffset=0, yOffset=yLineHeight-6;
		for(int i=0; i<values.length; i++) {
			
			String label = labels[i];
			
			int labelWidth = g.getFontMetrics().stringWidth(label);
				
			g.drawString(label, widestLabel-(3+labelWidth), yLineHeight*(i+2)-(yLineHeight/2)+(g.getFontMetrics().getAscent()/2));

			//set yPos (the y coordinate are fine)
			int xPos=0, yPos=(yLineHeight*(i+1))+3;
			int cumulativeXOffset = 0;
	
			for(int j=0; j<values[i].length; j++) {
				int xValue = getX(Math.min(values[i][j], maxX), widestLabel);
				
				// set xPos and xOffset
				if(cumulativeXOffset==0) 
					xPos=widestLabel;
				else 
					xPos=cumulativeXOffset;

				xOffset=xValue-widestLabel;
				
				g.setColor(new Color(200,0,0));
				g.fillRect(xPos, yPos, xOffset, yOffset);
				g.setColor(Color.BLACK);
				g.drawRect(xPos, yPos, xOffset, yOffset);
				
				cumulativeXOffset = xPos+xOffset;
			}
		}
	}
	
	
	private int getX(double value, int longestLabel) {
		int lengthToUse = getWidth()-(longestLabel+20);
		double proportion = value/maxX;		
		return longestLabel+(int)(lengthToUse*proportion);
	}
	
	public static void main(String[] argv) {
		JFrame f = new JFrame();
		f.setSize(600, 400);
		double[][] values = new double[3][4];
		String[] names = new String[3];
		values[0][0] = 2;
		values[0][1] = 1;
		values[0][2] = 10;
		values[0][3] = 1;
		names[0] = "Item 1";

		values[1][0] = 4;
		values[1][1] = 1;
		values[1][2] = 8;
		values[1][3] = 2;
		names[1] = "Very long Item 2";

		values[2][0] = 1;
		values[2][1] = 7;
		values[2][2] = 2;
		values[2][3] = 6;
		names[2] = "Item 3";
		

		f.getContentPane().add(new StackedHorizontalBarGraph(names, values, "Stacked Horizontal Bar Graph Test"));

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
