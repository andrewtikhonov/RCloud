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
package workbench.views.rconsole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.common.graphics.GraphicsUtilities;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.generic.ImageContainer;
import workbench.graphics.GDeviceTransferHandler;
import workbench.views.filebrowser.actions.preview.ImagePreviewFrame;
import workbench.views.filebrowser.actions.preview.ImagePreviewTransferHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 28, 2010
 * Time: 2:49:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class RConsoleImageContainer extends JPanel {

    final private static Logger log = LoggerFactory.getLogger(RConsoleImageContainer.class);

    SaveAsAction saveAsAction = new SaveAsAction();
    CopyToClipboard copyAction = new CopyToClipboard();
    OpenInWindowAction openAction = new OpenInWindowAction();

    ImageContainer container;
    Icon icon;

    public RConsoleImageContainer(ImageContainer container) {
        setContainer(container);
        setIcon(new ImageIcon(container.getImage()));
        init();
    }

    public void setContainer(ImageContainer container) {
        this.container = container;
    }

    public ImageContainer getContainer() {
        return this.container;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return this.icon;
    }

    @Override
    public Dimension getPreferredSize(){
        Dimension dim = super.getPreferredSize();

        return new Dimension(icon.getIconWidth(), //Math.max(dim.width, icon.getIconWidth()),
                Math.max(dim.height, icon.getIconHeight()));
    }

    @Override
    public Dimension getMaximumSize(){
        Dimension dim = super.getPreferredSize();

        return new Dimension(icon.getIconWidth(), //Math.max(dim.width, icon.getIconWidth()),
                Math.max(dim.height, icon.getIconHeight()));
    }

    @Override
    public void paintComponent(Graphics g){
        int icon_h = icon.getIconHeight();
        int icon_w = icon.getIconWidth();

        int comp_w = getWidth();
        int comp_h = getHeight();

        int x = (comp_w - icon_w) / 2;
        int y = (comp_h - icon_h) / 2;

        icon.paintIcon(this, g, x, y);
    }

    private JButton makeButton(String iconPath, String tooltip) {
        JButton button = new JButton();

        ImageIcon icon = new ImageIcon(ImageLoader.load(iconPath));
        icon.setImage(GraphicsUtilities.scaleImage(icon.getImage(), 20));
        button.setIcon(icon);
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(26, 26));
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void openPreviewPane(){
        ImagePreviewFrame frame = new ImagePreviewFrame(getIcon());
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setVisible(true);
    }

    class SaveAsAction extends AbstractAction {

        private JFileChooser chooser = null;

        public SaveAsAction() {
            super("Save Image As..");
        }

        public void actionPerformed(final ActionEvent e) {

            if (chooser == null) {
                chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(true);
                chooser.setFileHidingEnabled(true);

                FileFilter filter = new FileFilter() {
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        } else {
                            String name = f.getName().toLowerCase();

                            boolean accept = (name.endsWith("jpg") || name.endsWith("png"));

                            //log.info("filter-accept-accept="+accept);

                            return accept;
                        }
                    }

                    public String getDescription() {
                        return "JPG and PNG Images";
                    }
                };

                chooser.setFileFilter(filter);
            }

            new Thread(new Runnable() {
                public void run() {
                    chooser.setSelectedFile(new File("image.png"));

                    int returnVal = chooser.showSaveDialog(RConsoleImageContainer.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File selectedfile = chooser.getSelectedFile();

                        String name = selectedfile.getName();

                        if (name.endsWith("png")) {
                            try {


                                ImageIO.write(getContainer().getImage(), "png", selectedfile);
                            } catch (Exception ex) {
                                log.error("Error!", ex);
                            }

                        } else if (name.endsWith("jpg")) {
                            try {
                                ImageIO.write(getContainer().getImage(), "jpg", selectedfile);
                            } catch (Exception ex) {
                                log.error("Error!", ex);
                            }
                        }
                    }

                }
            }).start();

        }

        @Override
        public boolean isEnabled() {
            return true;
        }

    }

    class CopyToClipboard extends AbstractAction {

        public CopyToClipboard() {
            super("Copy");
        }

        public void actionPerformed(ActionEvent e) {
            ImagePreviewTransferHandler.ImageSelection image =
                    new ImagePreviewTransferHandler.ImageSelection(container.getImage());

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

          	clipboard.setContents(image, image);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }


    class OpenInWindowAction extends AbstractAction {

        public OpenInWindowAction() {
            super("show in separate window");
        }

        public void actionPerformed(ActionEvent e) {
            openPreviewPane();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    class MouseOverAdapter extends MouseAdapter {
        //JButton[] buttons;
        private Color c = Color.ORANGE;

        public MouseOverAdapter(){ // JButton[] buttons
            //this.buttons = buttons;
        }

        public void mouseEntered(MouseEvent mouseevent){
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            c = getBackground();

            setBackground(Color.LIGHT_GRAY);

            /*
            for (JButton butt :buttons) {
                butt.setVisible(true);
            }
            */
        }

        public void mouseExited(MouseEvent mouseevent){
            setBorder(BorderFactory.createLineBorder(new Color(0,0,0,0)));
            setBackground(c);

            /*
            for (JButton butt :buttons) {
                butt.setVisible(false);
            }
            */
        }
    }

    class MouseClickAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent event) {
            checkPopup(event);
        }

        public void mouseReleased(MouseEvent event) {
            checkPopup(event);
        }

        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() == 2) {
                openPreviewPane();

            }
            checkPopup(event);
        }
        public void checkPopup(MouseEvent event) {
            if (event.isPopupTrigger()) {
                log.info("checkPopup");

                //JButton button = (JButton)event.getSource();

                JPopupMenu menu = new JPopupMenu("Tools");

                JMenuItem saveAsItem = new JMenuItem(saveAsAction);
                JMenuItem openImageItem = new JMenuItem(openAction);
                JMenuItem copyImageItem = new JMenuItem(copyAction);

                openImageItem.setIcon(new ImageIcon(ImageLoader.load("/views/images/rconsole/application_add.png")));
                saveAsItem.setIcon(new ImageIcon(ImageLoader.load("/views/images/rconsole/filesaveas.png")));
                copyImageItem.setIcon(new ImageIcon(ImageLoader.load("/views/images/rconsole/edit-copy.png")));


                menu.add(openImageItem);
                menu.add(copyImageItem);
                menu.add(saveAsItem);

                menu.show(RConsoleImageContainer.this, (int) event.getPoint().getX(),
                        (int) event.getPoint().getY());

            }
        }
    }


    public void init(){

        //this.setI

        setOpaque(false);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(new Color(0,0,0,0)));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        //JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        //buttons.setOpaque(false);
        //add(buttons, BorderLayout.NORTH);

        //JButton butt = makeButton("/views/images/rconsole/hammer_screwdriver.png", "tools");
        //butt.setVisible(false);
        //buttons.add(butt);

        MouseOverAdapter mouseOverAdapter = new MouseOverAdapter(); // new JButton[]{ butt }
        MouseClickAdapter mouseClickAdapter = new MouseClickAdapter();
        //ActionsMouseAdapter actionsAdapter = new ActionsMouseAdapter();

        //butt.addMouseListener(mouseOverAdapter);
        ///butt.addMouseListener(actionsAdapter);

        addMouseListener(mouseOverAdapter);
        addMouseListener(mouseClickAdapter);
    }

}

