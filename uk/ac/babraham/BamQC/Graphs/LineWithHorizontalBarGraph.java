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
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;

import uk.ac.babraham.BamQC.Utilities.FormatNumber;



public class LineWithHorizontalBarGraph extends JPanel {

	private static final long serialVersionUID = -5947375412672203276L;
	protected String [] barLabels;
	protected String xTitle;
	protected String xLabel;
	protected String barDataLabel;	
	protected String [] xCategories;
	protected double [] lineData;
	protected double [] barData;
	protected String graphTitle;
	protected double minY;
	protected double maxY;
	protected double maxX;
	protected double yInterval;
	protected int height = -1;
	protected int width = -1;
	
	// TOOL TIPS management
	private List<Rectangle> rectangles = null;
	private List<String> tips = null;
	private JWindow toolTip = null;
    private JLabel label = new JLabel();
	private Tipster tipster = null;
	
	protected static final Color [] COLOURS = new Color[] {new Color(220,0,0), new Color(0,0,220), new Color(0,220,0), Color.DARK_GRAY, Color.MAGENTA, Color.ORANGE,Color.YELLOW,Color.CYAN,Color.PINK,Color.LIGHT_GRAY};
	
	public LineWithHorizontalBarGraph(double[] barData, double[] lineData, double minY, double maxY, String xLabel, String[] barLabels, String xTitle, int[] xCategories, String graphTitle, String barDataLabel) {
		this(barData,lineData,minY,maxY,xLabel,barLabels, xTitle,new String[0],graphTitle, barDataLabel);
		this.xCategories = new String [xCategories.length];
		for (int i=0;i<xCategories.length;i++) {
			this.xCategories[i] = ""+xCategories[i];
		}
	}
	
	public LineWithHorizontalBarGraph(double[] barData, double[] lineData, double minY, double maxY, String xLabel, String[] barLabels, String xTitle, String[] xCategories, String graphTitle, String barDataLabel) {
		this.barData = barData;
		this.lineData = lineData;
		this.minY = minY;
		this.maxY = maxY;
		this.barLabels = barLabels;		
		this.xTitle = xTitle;
		this.xLabel = xLabel;
		this.xCategories = xCategories;
		this.graphTitle = graphTitle;
		this.barDataLabel = barDataLabel;
		this.yInterval = findOptimalYInterval(maxY);
		
		// TOOL TIPS management
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(UIManager.getColor("ToolTip.white"));
        label.setBorder(UIManager.getBorder("ToolTip.border"));
        if(!GraphicsEnvironment.isHeadless()) {
        	toolTip = new JWindow();
        	toolTip.add(label);
        }
        setOpaque(true);
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

	@Override
	protected void paintComponent(Graphics g) {
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		
		if (g instanceof Graphics2D) {
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		int xOffsetLineGraph = 0;

		double midY = minY+((maxY-minY)/2);

		String label = xTitle;
		int width = g.getFontMetrics().stringWidth(label);
		if (width > xOffsetLineGraph) {
			xOffsetLineGraph = width;
		}
		g.drawString(label, 2, getY(midY)+(g.getFontMetrics().getAscent()/2));

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
		


		// Draw the xLabel under the xAxis
		g.drawString(xLabel, (getWidth()/2) - (g.getFontMetrics().stringWidth(xLabel)/2), getHeight()-5);
		
		// Draw the label for the bar data
		g.drawString(barDataLabel, xOffsetLineGraph, 78);
		
		
			
		// Now draw the horizontal bar (1st plot)
		// First we need to find the widest label
		
		// Initialise the arrays containing the tooltips
		rectangles = new ArrayList<Rectangle>();
		tips = new ArrayList<String>();
		int chrPosition = 0;
		String chrPositionStr = "";
		
		
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
			Rectangle r = new Rectangle(xPos, yPos, xOffsetBarGraph, yOffset);
			g.setColor(new Color(200,0,0));
			g.fillRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
			g.setColor(Color.BLACK);
			g.drawRect((int)r.getX(), (int)r.getY(), (int)r.getWidth(), (int)r.getHeight());
			// TOOL TIPS management
			// add rectangle coordinates and tooltip to these two lists
			//chrPositionStr = FormatNumber.compactIntegerRange(chrPosition, (int)barData[i]);
			chrPositionStr = FormatNumber.convertToScientificNotation(chrPosition) + "-" + 
							 FormatNumber.convertToScientificNotation(chrPosition+(int)(barData[i]));
			chrPositionStr = chrPositionStr.replaceAll(".0$", ""); // Don't leave trailing .0s where we don't need them.
			
			rectangles.add(r);
			tips.add(barLabels[i] + " : " + chrPositionStr);
			chrPosition = chrPosition + (int)barData[i];
			
			
			// increase the cumulative X offset to get a measure for this plot, 
			// as we need this for scaling the second plot.
			cumulativeXOffset = xPos+xOffsetBarGraph;
			
			// draw grey lines to annotate the second plots
			g.setColor(new Color(230, 230, 230));
			g.drawLine(cumulativeXOffset, getHeight()-40-1, cumulativeXOffset, 140);
			g.setColor(Color.BLACK);	
			
		}
		
        tipster = new Tipster(this);
        addMouseListener(tipster);
        addMouseMotionListener(tipster);
		
		
		
		// Now draw horizontal lines across from the y axis (2nd plot)
		// First draw faint boxes over alternating bases so you can see which is which
		// Let's find the longest label, and then work out how often we can draw labels
		double baseWidth = 1;	
		int lastY = 0;

		// Now draw the data points
		// Set the width for the plot line
		baseWidth = 1.0*(cumulativeXOffset)/lineData.length;			

		int lastXLabelEnd = 0;
		for(int i=0; i<lineData.length; i++) {
			//String baseNumber = ""+xCategories[i];
			//baseNumber = FormatNumber.compactInteger(baseNumber);
			String baseNumber = FormatNumber.convertToScientificNotation(xCategories[i]);
			baseNumber = baseNumber.replaceAll(".0$", ""); // Don't leave trailing .0s where we don't need them.
			int baseNumberWidth = g.getFontMetrics().stringWidth(baseNumber);
			int baseNumberPosition =  (int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*i)-(baseNumberWidth/2));
			
			if (baseNumberPosition > lastXLabelEnd) {
				g.drawString(baseNumber,baseNumberPosition, getHeight()-25);
				lastXLabelEnd = baseNumberPosition+baseNumberWidth+5;
			}
		}
		
		// Draw an horizontal line behind the line graph.
		g.setColor(new Color(128,128,128));
		// TODO remove once finished
		//g.drawLine(xOffsetLineGraph, getYOld(midY,0), getWidth()-10, getYOld(midY,0));
		g.drawLine(xOffsetLineGraph, getY(midY), getWidth()-10, getY(midY));
		g.setColor(Color.BLACK);
		
		
		// Now draw the axes
		// x axis
		g.drawLine(xOffsetLineGraph, getHeight()-40, getWidth()-10,getHeight()-40);
		// y axis
		g.drawLine(xOffsetLineGraph, getHeight()-40, xOffsetLineGraph, 80);
	
		// Now draw the datasets
		if (g instanceof Graphics2D) {
			((Graphics2D)g).setStroke(new BasicStroke(2));
		}
		
		g.setColor(COLOURS[0]);
		
		// First check whether we are starting with points having 0 coverage.
		int i=0;
		lastY = getY(lineData[i]);
		for (; i<lineData.length && Double.isInfinite(lineData[i]); i++) {
			// TODO not sure about this
			// Darken the area if these points have 0 coverage
//				g.setColor(new Color(100, 100, 100));
//				g.fillRect((baseWidth/2)+xOffset+(baseWidth*(i)), (int)(minY), (baseWidth/2)+xOffset+(baseWidth*(i+1)), getY(minY,d));
			
			// if there is no coverage in the beginning we don't plot anything 
//				g.setColor(Color.BLACK);				
//				// NOTE HORIZONTAL POSITION!!
//				g.drawLine((int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*(i))), getY(minY*0.75,d), (int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*(i+1))), getY(minY*0.75,d));
//				g.setColor(COLOURS[0]);
		}
		
		if(i<lineData.length) {
			// This point has non-zero coverage
			lastY = getY(lineData[i]);
	    }
			
		for(i++; i<lineData.length; i++) {
			if (Double.isNaN(lineData[i])) break;
			// Check whether we have points with null coverage (the commented code removes 
			// an additional spike found at the beginning.
			if (Double.isInfinite(lineData[i]) ) { // || 
				// TODO not sure about this
				// Darken the area if these points have 0 coverage
//				g.setColor(new Color(100, 100, 100));
//				g.fillRect((baseWidth/2)+xOffset+(baseWidth*(i-1)), (int)(minY), (baseWidth/2)+xOffset+(baseWidth*i), getY(minY,d));
				g.setColor(Color.BLACK);
				// NOTE HORIZONTAL POSITION!!
				g.drawLine((int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*(i-1))), getY(minY*0.75), (int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*i)), getY(minY*0.75));
				g.setColor(COLOURS[0]);
				lastY = getY(midY);
				continue;
			}
				
			int thisY = getY(lineData[i]);
			g.drawLine((int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*(i-1))), lastY, (int)((baseWidth/2)+xOffsetLineGraph+(baseWidth*i)), thisY);
		
			lastY = thisY;
		}	
	}


	private int getY(double y) {
		int totalPlotArea = getHeight()-160;
		return (getHeight()-30) - (int)((totalPlotArea/(maxY-minY))*(y-minY));		
	}
	
	
	private int getX(double value, int longestLabel) {
		int lengthToUse = getWidth()-(longestLabel+20);
		double proportion = value/maxX;		
		return longestLabel+(int)(lengthToUse*proportion);
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
        private LineWithHorizontalBarGraph toolTips;
     
        public Tipster(LineWithHorizontalBarGraph tt) {
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

}







