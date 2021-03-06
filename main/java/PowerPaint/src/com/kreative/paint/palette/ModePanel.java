/*
 * Copyright &copy; 2009-2011 Rebecca G. Bettencourt / Kreative Software
 * <p>
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <a href="http://www.mozilla.org/MPL/">http://www.mozilla.org/MPL/</a>
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Alternatively, the contents of this file may be used under the terms
 * of the GNU Lesser General Public License (the "LGPL License"), in which
 * case the provisions of LGPL License are applicable instead of those
 * above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the LGPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the LGPL License.
 * @since PowerPaint 1.0
 * @author Rebecca G. Bettencourt, Kreative Software
 */

package com.kreative.paint.palette;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.kreative.paint.ToolContext;
import com.kreative.paint.util.OSUtils;

public class ModePanel extends ToolContextPanel {
	private static final long serialVersionUID = 1L;
	
	private JToggleButton paint_button;
	private JToggleButton draw_button;
	private ButtonGroup pdbg;
	
	public ModePanel(ToolContext tc, boolean small) {
		super(tc, CHANGED_MODE);
		paint_button = new JToggleButton(new ImageIcon(this.getClass().getResource(small ? "CTIPaint.png" : "CTIPaint32.png")));
		paint_button.setToolTipText(PaletteUtilities.messages.getString("tools.paint"));
		squareOffButton(paint_button);
		paint_button.setSelected(tc.isInPaintMode());
		paint_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ModePanel.this.tc == null) return;
				ModePanel.this.tc.setPaintMode(true);
			}
		});
		draw_button = new JToggleButton(new ImageIcon(this.getClass().getResource(small ? "CTIDraw.png" : "CTIDraw32.png")));
		draw_button.setToolTipText(PaletteUtilities.messages.getString("tools.draw"));
		squareOffButton(draw_button);
		draw_button.setSelected(tc.isInDrawMode());
		draw_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ModePanel.this.tc == null) return;
				ModePanel.this.tc.setDrawMode(true);
			}
		});
		pdbg = new ButtonGroup();
		pdbg.add(paint_button);
		pdbg.add(draw_button);
		setLayout(new GridLayout(0,2));
		add(paint_button);
		add(draw_button);
	}
	
	public void update() {
		paint_button.setSelected(tc.isInPaintMode());
		draw_button.setSelected(tc.isInDrawMode());
	}
	
	private static void squareOffButton(JComponent c) {
		int h = OSUtils.isWindows() ? (c.getPreferredSize().height+1) : c.getPreferredSize().height;
		c.setMinimumSize(new Dimension(h,h));
		c.setPreferredSize(new Dimension(h,h));
		c.setMaximumSize(new Dimension(h,h));
	}
}
