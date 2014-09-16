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

import javax.swing.JPanel;

import uk.ac.babraham.BamQC.Utilities.AxisScale;

public class HorizontalBarGraph extends JPanel {

	private String [] labels;
	private float [] values;
	private String title;
	private float maxValue = 1;
	private int height = -1;
	private int width = -1;
		

	public HorizontalBarGraph (String [] labels, float [] values, String title, float maxValue) {
		this.labels = labels;
		this.values = values;
		this.title = title;
		this.maxValue = maxValue;
	}
	
	public HorizontalBarGraph (String [] labels, float [] values, String title) {

		this.labels = labels;
		this.values = values;
		this.title = title;
		
		for (int v=0;v<values.length;v++) {
			if (values[v] > maxValue) maxValue = values[v];
		}
		
//		System.err.println("Max value is "+maxValue);
	}
	
	
	public Dimension getPreferredSize () {
		return new Dimension(800,20*labels.length);
	}

	public Dimension getMinimumSize () {
		return new Dimension(100,200);
	}

	
	public int getHeight () {
		if (height <0) {
			return super.getHeight();
		}
		return height;
	}

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
		
		// Y-axis
		g.drawLine(widestLabel, yLineHeight, widestLabel, getHeight()-(yLineHeight*2));
		
		// X-axis
		g.drawLine(widestLabel,getHeight()-(yLineHeight*2),getWidth()-20,getHeight()-(yLineHeight*2));

		// Add the scale to the x-axis
		AxisScale scale = new AxisScale(0, maxValue);
		
		double currentValue = scale.getStartingValue();
		
		while (currentValue < maxValue) {
			int xPos = getX((float)currentValue, widestLabel);
			g.drawLine(xPos, getHeight()-(yLineHeight*2), xPos, getHeight()-(yLineHeight*2)+3);
			
			String label = scale.format(currentValue);
			int labelWidth = g.getFontMetrics().stringWidth(label);
			
			g.drawString(label, xPos-(labelWidth/2), getHeight()-(yLineHeight*2)+(g.getFontMetrics().getHeight()+3));
			
			currentValue += scale.getInterval();
		}
		
		// Now draw the data
		for (int s=0;s<labels.length;s++) {
			
			String label = labels[s];
			
			int labelWidth = g.getFontMetrics().stringWidth(label);
			
			g.drawString(label, widestLabel-(3+labelWidth), yLineHeight*(s+2)-(yLineHeight/2)+(g.getFontMetrics().getAscent()/2));
			
			int xValue = getX(Math.min(values[s], maxValue), widestLabel);
			
			g.setColor(new Color(200,0,0));
			g.fillRect(widestLabel, (yLineHeight*(s+1))+3,xValue-widestLabel,yLineHeight-6);
			g.setColor(Color.BLACK);
			g.drawRect(widestLabel, (yLineHeight*(s+1))+3,xValue-widestLabel,yLineHeight-6);
		}
		
		
	}
	
	private int getX (float value, int longestLabel) {
		int lengthToUse = getWidth()-(longestLabel+20);
		
		float proportion = value/maxValue;
		
		return longestLabel+(int)(lengthToUse*proportion);
		
	}
	
}
