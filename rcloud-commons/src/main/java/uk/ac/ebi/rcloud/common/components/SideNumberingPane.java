/*
 * R Cloud - R-based Cloud Platform for Computational Research
 * at EMBL-EBI (European Bioinformatics Institute)
 *
 * Copyright (C) 2007-2015 European Bioinformatics Institute
 * Copyright (C) 2009-2015 Andrew Tikhonov - andrew.tikhonov@gmail.com
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ebi.rcloud.common.components;

import javax.swing.event.DocumentListener;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.CaretEvent;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Element;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 29, 2009
 * Time: 5:03:31 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 *  This class will display line numbers for a related text component. The text
 *  component must use the same line height for each line. TextLineNumber
 *  supports wrapped lines and will highlight the line number of the current
 *  line in the text component.
 *
 *  This class was designed to be used as a component added to the row header
 *  of a JScrollPane.
 */

public class SideNumberingPane extends JPanel
	implements CaretListener, DocumentListener, PropertyChangeListener {

    public final static float LEFT = 0.0f;
	public final static float CENTER = 0.5f;
	public final static float RIGHT = 1.0f;

	private final static Border OUTER = new MatteBorder(0, 0, 0, 2, Color.GRAY);

	private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

	//  Text component this TextTextLineNumber component is in sync with

	private JTextComponent component;

	//  Properties that can be changed

	private boolean updateFont = true;
	private int borderGap;
	private Color currentLineForeground;
    private Color lineForeground;
    private float digitAlignment;
	private int minimumDisplayDigits;

	//  Keep history information to reduce the number of times the component
	//  needs to be repainted

    private int lastDigits;
    private int lastHeight;
    private int lastLine;

	/**
	 *	Create a line number component for a text component. This minimum
	 *  display width will be based on 3 digits.
	 *
	 *  @param component  the related text component
	 */
	public SideNumberingPane(JTextComponent component) {
		this(component, 3);
	}

	/**
	 *	Create a line number component for a text component.
	 *
	 *  @param component  the related text component
	 *  @param minimumDisplayDigits  the number of digits used to calculate
	 *                               the minimum width of the component
	 */
	public SideNumberingPane(JTextComponent component, int minimumDisplayDigits) {
		this.component = component;

		setFont( component.getFont() );

		setBorderGap( 5 );
        setLineForeground( Color.GRAY );
		setCurrentLineForeground( Color.BLACK );
		setDigitAlignment( RIGHT );
		setMinimumDisplayDigits( minimumDisplayDigits );

		component.getDocument().addDocumentListener(this);
		component.addCaretListener( this );
		component.addPropertyChangeListener("font", this);
	}

	/**
	 *  Gets the update font property
	 *
	 *  @return the update font property
	 */
	public boolean getUpdateFont() {
		return updateFont;
	}

	/**
	 *  Set the update font property. Indicates whether this Font should be
	 *  updated automatically when the Font of the related text component
	 *  is changed.
	 *
	 *  @param updateFont  when true update the Font and repaint the line
	 *                     numbers, otherwise just repaint the line numbers.
	 */
	public void setUpdateFont(boolean updateFont) {
		this.updateFont = updateFont;
	}

	/**
	 *  Gets the border gap
	 *
	 *  @return the border gap in pixels
	 */
	public int getBorderGap() {
		return borderGap;
	}

	/**
	 *  The border gap is used in calculating the left and right insets of the
	 *  border. Default value is 5.
	 *
	 *  @param borderGap  the gap in pixels
	 */
	public void setBorderGap(int borderGap) {
		this.borderGap = borderGap;
		Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
		setBorder( new CompoundBorder(OUTER, inner) );
		lastDigits = 0;
		setPreferredWidth();
	}

	/**
	 *  Gets the current line rendering Color
	 *
	 *  @return the Color used to render the current line number
	 */
	public Color getCurrentLineForeground() {
		return currentLineForeground == null ? getForeground() : currentLineForeground;
	}

	/**
	 *  The Color used to render the current line digits. Default is Coolor.RED.
	 *
	 *  @param currentLineForeground  the Color used to render the current line
	 */
	public void setCurrentLineForeground(Color currentLineForeground) {
		this.currentLineForeground = currentLineForeground;
	}

    /**
     *  Gets the current line rendering Color
     *
     *  @return the Color used to render the current line number
     */
    public Color getLineForeground() {
        return lineForeground == null ? getForeground() : lineForeground;
    }

    /**
     *  The Color used to render the current line digits. Default is Coolor.RED.
     *
     *  @param lineForeground  the Color used to render the current line
     */
    public void setLineForeground(Color lineForeground) {
        this.lineForeground = lineForeground;
    }

	/**
	 *  Gets the digit alignment
	 *
	 *  @return the alignment of the painted digits
	 */
	public float getDigitAlignment() {
		return digitAlignment;
	}

	/**
	 *  Specify the horizontal alignment of the digits within the component.
	 *  Common values would be:
	 *  <ul>
	 *  <li>TextLineNumber.LEFT
	 *  <li>TextLineNumber.CENTER
	 *  <li>TextLineNumber.RIGHT (default)
	 *	</ul>
	 *  @param currentLineForeground  the Color used to render the current line
	 */
	public void setDigitAlignment(float digitAlignment) {
		this.digitAlignment =
			digitAlignment > 1.0f ? 1.0f : digitAlignment < 0.0f ? -1.0f : digitAlignment;
	}

	/**
	 *  Gets the minimum display digits
	 *
	 *  @return the minimum display digits
	 */
	public int getMinimumDisplayDigits() {
		return minimumDisplayDigits;
	}

	/**
	 *  Specify the mimimum number of digits used to calculate the preferred
	 *  width of the component. Default is 3.
	 *
	 *  @param minimumDisplayDigits  the number digits used in the preferred
	 *                               width calculation
	 */
	public void setMinimumDisplayDigits(int minimumDisplayDigits) {
		this.minimumDisplayDigits = minimumDisplayDigits;
		setPreferredWidth();
	}

	/**
	 *  Calculate the width needed to display the maximum line number
	 */
	private void setPreferredWidth() {
		Element root = component.getDocument().getDefaultRootElement();
		int lines = root.getElementCount();
		int digits = Math.max(String.valueOf(lines).length(), minimumDisplayDigits);

		//  Update sizes when number of digits in the line number changes

		if (lastDigits != digits)
		{
			lastDigits = digits;
			FontMetrics fontMetrics = getFontMetrics( getFont() );
			int width = fontMetrics.charWidth( '0' ) * digits;
			Insets insets = getInsets();
			int preferredWidth = insets.left + insets.right + width;

			Dimension d = getPreferredSize();
			d.setSize(preferredWidth, HEIGHT);
			setPreferredSize( d );
			setSize( d );
		}
	}

	/**
	 *
	 */
	//@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		//	Determine the width of the space available to draw the line number

//		FontMetrics fontMetrics = getFontMetrics( getFont() );
		FontMetrics fontMetrics = component.getFontMetrics( component.getFont() );
		Insets insets = getInsets();
		int availableWidth = getSize().width - insets.left - insets.right;

		//  Determine the number of lines to draw within the clipped bounds.
		//  and the starting "Y" offset of the first line

		Rectangle clip = g.getClipBounds();
		int lineHeight = fontMetrics.getHeight();
		int startOffset = component.getInsets().top + fontMetrics.getAscent();
		int linesToDraw = clip.height / lineHeight + 1;
		int y = (clip.y / lineHeight) * lineHeight + startOffset;

		//  These fields are used to determine the actual line number to draw

		Point viewPoint = new Point(0, y);
		int preferredHeight = component.getPreferredSize().height;

		for (int i = 0; i <= linesToDraw; i++)
		{
			if (isCurrentLine(viewPoint))
				g.setColor( getCurrentLineForeground() );
			else
				g.setColor( getLineForeground() );

			//  Get the line number as a string and then determine the "X"
			//  offset for drawing the string.

			String lineNumber = getTextLineNumber(viewPoint, preferredHeight);
			int stringWidth = fontMetrics.stringWidth( lineNumber );
			int x = getOffsetX(availableWidth, stringWidth) + insets.left;
			g.drawString(lineNumber, x, y);

			//  Update the "Y" offset for the next line to be drawn

			y += lineHeight;
			viewPoint.y = y;

			if (y > preferredHeight) break;
		}
	}

	/*
	 *  We need to know if the caret is currently positioned on the line we
	 *  are about to paint so the line number can be highlighted.
	 */
	private boolean isCurrentLine(Point viewPoint)
	{
		//  The viewPoint represents the model view of the first character
		//  on the line.

		int offset = component.viewToModel(viewPoint);
		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();

		if (root.getElementIndex( offset ) == root.getElementIndex(caretPosition))
			return true;
		else
			return false;
	}

	/*
	 *	Get the line number to be drawn. The empty string will be returned
	 *  when a line of text has wrapped.
	 */
	protected String getTextLineNumber(Point viewPoint, int preferredHeight)
	{
		//  The viewPoint represents the model view of the first character
		//  on the line. When the model offset of this view matches the
		//  offset of the first character on the line we have a line number to
		//  draw, otherwise the line has wrapped so there is nothing to draw.

		int offset = component.viewToModel(viewPoint);
		Element root = component.getDocument().getDefaultRootElement();
		int index = root.getElementIndex( offset );
		Element line = root.getElement( index );

		if (line.getStartOffset() == offset)
			return String.valueOf(index + 1);
		else
			return "";
	}

	/*
	 *  Determine the X offset to properly align the line number when drawn
	 */
	private int getOffsetX(int availableWidth, int stringWidth) {
		return (int)((availableWidth - stringWidth) * digitAlignment);
	}

    //
    //  Implement CaretListener interface
    //
	//@Override
	public void caretUpdate(CaretEvent e) {
		//  Get the line the caret is positioned on

		int caretPosition = component.getCaretPosition();
		Element root = component.getDocument().getDefaultRootElement();
		int currentLine = root.getElementIndex( caretPosition );

		//  Need to repaint so the correct line number can be highlighted

		if (lastLine != currentLine)
		{
			repaint();
			lastLine = currentLine;
		}
	}

    //
    //  Implement DocumentListener interface
    //
	//@Override
	public void changedUpdate(DocumentEvent e) {}

	//@Override
	public void insertUpdate(DocumentEvent e) {
		documentChanged();
	}

	//@Override
	public void removeUpdate(DocumentEvent e) {
		documentChanged();
	}

	/*
	 *  A document change may affect the number of displayed lines of text.
	 *  Therefore the lines numbers will also change.
	 */
	private void documentChanged() {
		//  Preferred size of the component has not been updated at the time
		//  the DocumentEvent is fired

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
        		int preferredHeight = component.getPreferredSize().height;

				//  Document change has caused a change in the number of lines.
				//  Repaint to reflect the new line numbers

        		if (lastHeight != preferredHeight) {
        			setPreferredWidth();
        			repaint();
        			lastHeight = preferredHeight;
        		}
			}
		});
	}

    //
    //  Implement PropertyChangeListener interface
    //
	//@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof Font) {
			if (updateFont) {
				Font newFont = (Font) evt.getNewValue();
				setFont(newFont);
				lastDigits = 0;
				setPreferredWidth();
			} else {
				repaint();
			}
		}
	}

	public static void setupGUI(){

		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());

		JTextPane textPane = new JTextPane();
		textPane.setText("dfgsdfgfsdg\ndsgsdfgsdfgsdf\nsdfgsdfg\n");
		JScrollPane scrollPane = new JScrollPane(textPane);

		SideNumberingPane numberPane = new SideNumberingPane(textPane);
		scrollPane.setRowHeaderView( numberPane );


		//container.setOpaque(false);
		frame.add(scrollPane, BorderLayout.CENTER);
		//frame.add(numberPane, BorderLayout.WEST);


		frame.setPreferredSize(new Dimension(500, 400));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		//JScrollPane scroll = JScrollPane(text);
	}

	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				setupGUI();
			}
		});
	}
}

