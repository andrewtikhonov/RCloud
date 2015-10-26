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
package workbench.views.filebrowser.actions.preview;

import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.graphics.DragMouseAdapter;
import workbench.util.ButtonUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 4, 2010
 * Time: 1:23:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImagePreviewTransferHandler extends TransferHandler {

    JComponent component = null;

    public ImagePreviewTransferHandler(JComponent component) {
        this.component = component;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        /*
        for (int i = 0; i < transferFlavors.length; i++) {
            if (DataFlavor.javaFileListFlavor.equals(transferFlavors[i])) {
                return true;
            }
        }
        */
        return false;
    }

    public boolean importData(JComponent comp, Transferable t) {
         return false;
    }

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public static final DataFlavor IMAGE_FLAVOR = DataFlavor.imageFlavor;
    private static final DataFlavor[] FLAVORS = { IMAGE_FLAVOR };

    public Transferable createTransferable(JComponent c) {

        //return new StringSelection(c.getSelection());
        //java.awt.datatransfer.

        BufferedImage image = new BufferedImage(component.getWidth(),
                component.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        component.paint(g2d);

        g2d.dispose();

        return new ImageSelection(image);
    }

    public void exportDone(JComponent c, Transferable t, int action) {
        if (action == MOVE) {
            //c.removeSelection();
        }
    }

    public static class ImageSelection implements Transferable, ClipboardOwner {
        public static final DataFlavor IMAGE_FLAVOR = DataFlavor.imageFlavor;
        private static final DataFlavor[] FLAVORS = { IMAGE_FLAVOR };

        private Image image;
        public ImageSelection(Image image) {
            this.image = image;
        }
        public void lostOwnership(Clipboard clipboard, Transferable transferable) {
            // don't care
        }
        public Object getTransferData(DataFlavor flavor) {
            return isDataFlavorSupported(flavor) ? image : null;
        }
        public DataFlavor[] getTransferDataFlavors() { return FLAVORS; }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(IMAGE_FLAVOR);
        }
    }

    private static void initUI2(){
        //System.setProperty("apple.awt.draggableWindowBackground", "true");

        JFrame frame = new JFrame();

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(true);

        JButton exitButton = ButtonUtil.makeButton("exit");
        exitButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        content.setTransferHandler(new ImagePreviewTransferHandler(content));
        content.addMouseListener(new DragMouseAdapter());


        JTextField textField = new JTextField("dsfg");
        textField.setEditable(true);
        textField.setFocusable(true);
        textField.setEnabled(true);

        JButton testButton = ButtonUtil.makeButton("Test");
        testButton.setIcon(new ImageIcon(ImageLoader.load("/images/searchpanel/zoom.png")));
        testButton.setBorderPainted(false);
        testButton.setContentAreaFilled(false);


        content.add(testButton, BorderLayout.NORTH);
        //content.add(textField, BorderLayout.CENTER);
        content.add(exitButton, BorderLayout.SOUTH);

        frame.add(content, BorderLayout.CENTER);
        frame.setSize(new Dimension(240,300));
        frame.setPreferredSize(new Dimension(240,300));
        //frame.setPreferredSize(new Dimension(240,300));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                initUI2();
            }
        });
    }

}

