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
package workbench.views.extraconsole;

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import uk.ac.ebi.rcloud.common.util.KeyUtil;
import workbench.RGui;
import workbench.completion.CompletionInterfaceAbstract;
import workbench.completion.CompletionSupport;
import workbench.manager.tooltipman.WorkbenchToolTip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Mar 4, 2010
 * Time: 1:13:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsolePanelFeatured extends ConsolePanelBase {

    private RGui rgui;

    private String CONSOLE_ID = "extra-";
    private CompletionSupport cmdComplete = null;
    private WorkbenchToolTip consoletip = new WorkbenchToolTip();

    private long ct = 0;
    private long to = 10 * 1000;

    public ConsolePanelFeatured(RGui rgui, int id) {

        this.rgui = rgui;

        setFont(new Font("Courier", Font.PLAIN, getFont().getSize()));

        setPrompt("> ");

        printPrompt();

        addActionListener(new ConsolePanelActionListener());

        initCompletion();

        CONSOLE_ID = CONSOLE_ID + Integer.toString(id);

        rgui.addConsole(CONSOLE_ID, this);

        this.setLineLimit(8000, 1000);
                
    }

    //   I N I T 
    //

    class ConsoleCompletionInterface extends CompletionInterfaceAbstract {
        public ConsoleCompletionInterface(RGui rgui){
            super(rgui);
        }

        public String getText() {
            return ConsolePanelFeatured.this.getInput();
        }

        public int getCaretPosition() {
            return ConsolePanelFeatured.this.getCaretPosition() -
                    getInputStart();
        }

        public void setText(String text) {
            setInput(text);
        }

        public void setCaretPosition(int pos) {
            ConsolePanelFeatured.this.setCaretPosition(
                    getInputStart() + pos);
        }

        public void closePopup() {
            cmdComplete.closePopups();
        }

        public boolean canShow() {
            return true;
        }

    }

    private void initCompletion() {

        this.setFocusTraversalKeysEnabled(false);

        // init popup components
        cmdComplete = new CompletionSupport("Completion", this,
                new ConsoleCompletionInterface(rgui));

        cmdComplete.setAppearanceOption(CompletionSupport.APPEARS_BELOW);

        cmdComplete.setDockingOption(CompletionSupport.FLOATING_DOCKING);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ENTER,0),
                CompletionSupport.SELECT);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_TAB, 0),
                CompletionSupport.COMPLETE);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK),
                CompletionSupport.COMPLETE);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                CompletionSupport.CANCEL);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_UP, 0),
                CompletionSupport.SCROLL_UP);

        cmdComplete.bindKeyAction(KeyUtil.getKeyStroke(KeyEvent.VK_DOWN, 0),
                CompletionSupport.SCROLL_DOWN);

    }

    //   P U B L I C
    //

    public void dispose() {
        rgui.removeConsole(CONSOLE_ID);
    }

    //   P R I V A T E
    //

    private void showCommandQueueingTip() {
        long ct0 = System.currentTimeMillis();
        if (ct0 - ct > to) {
            ct = ct0;

            consoletip.setTipLocationRelativeTo(
                    this, new Point(20,20));

            consoletip.showToolTip("Commands queued",
                    "commands you enter are being queued for execution "+
                            "and will be executed as soon as previous tasks are finished",
                    consoletip.getDefaultTimeout());
        }
    }

    //   A C T I O N S
    //

    class ConsolePanelActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            try {
                if (rgui.obtainR() != null) {
                    if (rgui.getRLock().isLocked()) {
                        showCommandQueueingTip();
                    }

                    HashMap<String, Object> attributes
                            = new HashMap<String, Object>();

                    attributes.put("originator", CONSOLE_ID);

                    rgui.obtainR().asynchronousConsoleSubmit(
                            event.getActionCommand().trim(), attributes);

                } else {
                    JOptionPaneExt.showMessageDialog(ConsolePanelFeatured.this,
                            "Sorry, but you're not connected to R server");
                }
                //printPrompt();
            } catch (RemoteException re) {
            }
        }
    }

}
