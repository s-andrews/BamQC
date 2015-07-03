/** Implementation from open domain source code: 
 * http://www.java2s.com/Code/Java/Swing-Components/MultiLineCellExample.htm
 * with edits from 
 * http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
 */
package uk.ac.babraham.BamQC.Utilities;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Multi line Table Cell Renderer.
 */
public class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {

	private static final long serialVersionUID = 7631990850449943723L;
	private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();
  
  public MultiLineTableCellRenderer() {
    setLineWrap(true);
    setWrapStyleWord(true);
    setOpaque(true);
  }
  
  @Override
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    } else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setFont(table.getFont());
    if (hasFocus) {
      setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
      if (table.isCellEditable(row, column)) {
        setForeground(UIManager.getColor("Table.focusCellForeground"));
        setBackground(UIManager.getColor("Table.focusCellBackground"));
      }
    } else {
      setBorder(new EmptyBorder(1, 2, 1, 2));
    }
 
    // Edited here by BotungeOctober 09, 2009 18:57 - Open Domain source code.
    // http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
    if (value != null) {
      setText(value.toString());
    } else {
      setText("");
    }
    adjustRowHeight(table, row, column);
    return this;
  }
  
  /**
   * Calculate the new preferred height for a given row, and sets the height on the table.
   * @author Botunge October 09, 2009 18:57 - Open Domain source code.
   * http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
   */
  private void adjustRowHeight(JTable table, int row, int column) {
    //The trick to get this to work properly is to set the width of the column to the 
    //text area. The reason for this is that getPreferredSize(), without a width tries 
    //to place all the text in one line. By setting the size with the with of the column, 
    //getPreferredSize() returns the proper height which the row should have in
    //order to make room for the text.
    int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
    setSize(new Dimension(cWidth, 1000));
    int prefH = getPreferredSize().height;
    while (rowColHeight.size() <= row) {
      rowColHeight.add(new ArrayList<Integer>(column));
    }
    List<Integer> colHeights = rowColHeight.get(row);
    while (colHeights.size() <= column) {
      colHeights.add(0);
    }
    colHeights.set(column, prefH);
    int maxH = prefH;
    for (Integer colHeight : colHeights) {
      if (colHeight > maxH) {
        maxH = colHeight;
      }
    }
    if (table.getRowHeight(row) != maxH) {
      table.setRowHeight(row, maxH);
    }
  }

}