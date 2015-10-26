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
package uk.ac.ebi.rcloud.common.components.panel;

import uk.ac.ebi.rcloud.common.animation.timing.Animator;
import uk.ac.ebi.rcloud.common.animation.timing.TimingTargetAdapter;
import uk.ac.ebi.rcloud.common.animation.timing.interpolation.PropertySetter;
import uk.ac.ebi.rcloud.common.util.ColorUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 14, 2009
 * Time: 5:28:41 PM
 * To change this template use File | Settings | File Templates.
 */

public class JExpandablePanel extends JPanel {

    // extension state
    public final int COLLAPSED = 0;
    public final int EXPANDED = 1;
    public int state = COLLAPSED;
    public float height = .0f;

    // extension animators
    public Animator extendAnimator;
    public Animator shrinkAnimator;

    public int animationtime = 300;

    // accessible containers
    public JPanel titleContainer;
    public JPanel expandContainer;
    public JPanel expandableContainer;
    public JPanel glass;

    public JExpandablePanel peer;

    // extension related
    public Dimension preferredTitleSize = new Dimension(730, 40);
    public Dimension preferredContainerSize = new Dimension(740, 250);

    // hand
    public static Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    public JExpandablePanel(String title) {
        initExpandablePanel();
    }

    public JExpandablePanel() {
        initExpandablePanel();
    }

    public void initExpandablePanel() {

        setLayout(new BorderLayout());

        titleContainer = new TitlePanel();
        titleContainer.setLayout(new BorderLayout());
        titleContainer.setOpaque(false);

        expandableContainer = new JPanel(new BorderLayout(10,10)) {
            @Override
            public void paint(Graphics g) {

                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, height));

                super.paint(g2d);

            }
        };

        expandableContainer.setOpaque(false);
        expandableContainer.setBorder(new EmptyBorder(5,10,5,10));

        add(titleContainer, BorderLayout.NORTH);
        add(expandableContainer, BorderLayout.CENTER);

        initAnimators();

        MouseExpandAdapter expandAdapter = new MouseExpandAdapter();

        titleContainer.setCursor(hand);

        titleContainer.addMouseListener(expandAdapter);

        Color b0 = getBackground();
        setBackground(new Color(b0.getRed(), b0.getGreen(), b0.getBlue(), 255));
        setOpaque(true);
    }

    public int computeExtensibleHeight() {
        return  (int) (preferredContainerSize.height * height);
    }

    public void setHeightNoRepaint(float height) {
        this.height = height;
    }

    public void setHeight(float height) {
        setHeightNoRepaint(height);

        if (getPeerPanel() != null) {
            getPeerPanel().setHeightNoRepaint(1.0f - height);
        }

        revalidate();

        Runnable repainter = (Runnable) getClientProperty("repainter");
        if (repainter != null) {
            repainter.run();
        }
    }

    public void setPreferredTitleSize(Dimension dim) {
        this.preferredTitleSize = dim;
    }

    public void setPreferredContainerSize(Dimension dim) {
        this.preferredContainerSize = dim;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width,
                preferredTitleSize.height + computeExtensibleHeight());
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void expand() {
        //System.out.println("collapse-panel="+this.toString());

        if (getState() == COLLAPSED) {
            if (shrinkAnimator.isRunning()) {
                shrinkAnimator.stop();
            }
            firePanelExpanding();
            if (getPeerPanel() != null) {
                getPeerPanel().firePanelCollapsing();
            }
            extendAnimator.start();
        }
    }

    public void collapse() {
        //System.out.println("collapse-panel="+this.toString());

        if (getState() == EXPANDED) {
            if (extendAnimator.isRunning()) {
                extendAnimator.stop();
            }
            firePanelCollapsing();
            if (getPeerPanel() != null) {
                getPeerPanel().firePanelExpanding();
            }
            shrinkAnimator.start();
        }
    }

    public boolean isExpanded() {
        return (getState() == EXPANDED);
    }

    public void setPeerPanel(JExpandablePanel peer) {
        //System.out.println("setPeerPanel-panel="+this.toString()+" peer="+(peer != null ? peer.toString(): "null"));

        this.peer = peer;
    }

    public JExpandablePanel getPeerPanel() {
        return peer;
    }

    public void initAnimators() {
        extendAnimator = PropertySetter.createAnimator(
                animationtime, this, "height", .0f, 1.0f);
        extendAnimator.setAcceleration(0.2f);
        extendAnimator.setDeceleration(0.3f);
        extendAnimator.addTarget(new TimingTargetAdapter() {
            public void end() {
                setState(EXPANDED);
                firePanelExpanded();
                if (getPeerPanel() != null) {
                    getPeerPanel().firePanelCollapsed();
                    getPeerPanel().setState(COLLAPSED);
                    setPeerPanel(null);
                }
            }
        });

        shrinkAnimator = PropertySetter.createAnimator(
                animationtime, this, "height", 1.0f, .0f);
        shrinkAnimator.setAcceleration(0.2f);
        shrinkAnimator.setDeceleration(0.3f);
        shrinkAnimator.addTarget(new TimingTargetAdapter() {
            public void end() {
                setState(COLLAPSED);
                firePanelExpanded();
                if (getPeerPanel() != null) {
                    getPeerPanel().firePanelCollapsed();
                    getPeerPanel().setState(COLLAPSED);
                    setPeerPanel(null);
                }
            }
        });
    }

    public Color getTitleBackActive() {
        return ((TitlePanel)titleContainer).backActive;
    }

    public Color getTitleBackInactive() {
        return ((TitlePanel)titleContainer).backActive;
    }

    public void setTitleBackActive(Color backActive) {
        ((TitlePanel)titleContainer).backActive = backActive;
    }

    public void setTitleBackInactive(Color backInactive) {
        ((TitlePanel)titleContainer).backInactive = backInactive;
    }

    public void setExpandableBack(Color back) {
        expandableContainer.setBackground(back);
    }

    class TitlePanel extends JPanel  {
        public Color backActive;
        public Color backInactive;
        public boolean active = false;

        public TitlePanel() {
        }

        @Override
        public void paintComponent(Graphics g) {

            int w = getWidth();
            int h = getHeight();

            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(active ? backActive : backInactive);
            g2d.fillRect(0,0,w-1,h-1);

        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width,
                    preferredTitleSize.height);
        }
    }

    class MouseHighlightAdapter extends MouseAdapter {
        public void mouseEntered(MouseEvent mouseEvent) {
            ((TitlePanel) titleContainer).setActive(true);
            repaint();
        }

        public void mouseExited(MouseEvent mouseEvent) {
            ((TitlePanel) titleContainer).setActive(false);
            repaint();
        }
    }

    class MouseExpandAdapter extends MouseAdapter {
        public void mouseClicked(MouseEvent mouseEvent) {
            switch (getState()) {
                case COLLAPSED :
                    expand();
                    break;

                case EXPANDED :
                    collapse();
                    break;

                default: throw new RuntimeException(getState() + " not supported");
            }
        }
    }

    private Vector<JExpandablePanelListener> listenerList
            = new Vector<JExpandablePanelListener>();

    public void removeListener(JExpandablePanelListener listener) {
        listenerList.remove(listener);
    }

    public void removeAllListeners() {
        listenerList.removeAllElements();
    }

    public void addListener(JExpandablePanelListener listener) {
        //System.out.println("addListener-panel="+this.toString());

        listenerList.add(listener);
    }

    public void firePanelExpanding() {
        //System.out.println("firePanelExpanding-panel="+this.toString());

        for(JExpandablePanelListener l : listenerList) {
            l.expanding(this);
        }
    }

    public void firePanelExpanded() {
        //System.out.println("firePanelExpanded-panel="+this.toString());

        for(JExpandablePanelListener l : listenerList) {
            l.expanded(this);
        }
    }

    public void firePanelCollapsing() {
        //System.out.println("firePanelCollapsing-panel="+this.toString());

        for(JExpandablePanelListener l : listenerList) {
            l.collapsing(this);
        }
    }

    public void firePanelCollapsed() {
        //System.out.println("firePanelCollapsed-panel="+this.toString());

        for(JExpandablePanelListener l : listenerList) {
            l.collapsed(this);
        }
    }

    class LinkedPanelListener implements JExpandablePanelListener {

        private Container container = null;
        private JExpandablePanel panel;

        public LinkedPanelListener(JExpandablePanel panel){
            super();
            this.panel = panel;
        }

        public Container getContainer() {
            if (container == null) {
                container = panel.getParent();
            }
            return container;
        }

        public void collapsing(JExpandablePanel panel) {}
        public void collapsed(JExpandablePanel panel) {}

        public void expanding(JExpandablePanel panel) {
            Component[] components = getContainer().getComponents();
            for (Component c : components) {
                if (c instanceof JExpandablePanel) {
                    JExpandablePanel panel0 = (JExpandablePanel) c;
                    if (panel0.isExpanded()) {
                        panel.setPeerPanel(panel0);
                        return;
                    }
                }
            }
        }

        public void expanded(JExpandablePanel panel) {}
    }

    private MouseHighlightAdapter highlightAdapter = null;

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            if (highlightAdapter == null) {
                highlightAdapter = new MouseHighlightAdapter();
                titleContainer.addMouseListener(highlightAdapter);
            }
        } else {
            if (highlightAdapter != null) {
                titleContainer.removeMouseListener(highlightAdapter);
                highlightAdapter = null;
            }
        }
    }

    public boolean isHighlighted() {
        return (highlightAdapter != null);
    }


    private LinkedPanelListener linkedListener = null;

    public void setLinked(boolean linked) {
        if (linked) {
            if (linkedListener == null) {
                linkedListener = new LinkedPanelListener(this);
                addListener(linkedListener);
            }
        } else {
            if (linkedListener != null) {
                removeListener(linkedListener);
                linkedListener = null;
            }
        }
    }

    public boolean isLinked() {
        return (linkedListener != null);
    }

    public Color inactive0 = new Color(220,220,220);
    public Color inactive1 = new Color(190,190,190);
    public Color active0 = new Color(180,120,140);

    public void updatePalette() {
        Color c0 = getParent().getBackground();

        active0 = ColorUtil.darker(c0, 5);
        inactive0 = ColorUtil.brighter(c0, 10);
        inactive1 = ColorUtil.darker(c0, 25);

        setTitleBackActive(active0);
        setTitleBackInactive(inactive0);
        setExpandableBack(inactive0);
    }

    public void updateLinkedPalette(boolean even) {
        if (even) {
            setTitleBackInactive(inactive0);
        } else {
            setTitleBackInactive(inactive1);
        }
    }

    public void updateLinkedComponents() {
        //System.out.println("relink");

        Component[] components = this.getParent().getComponents();
        for (int i = 0; i < components.length;i++){
            if (components[i] instanceof JExpandablePanel) {
                //System.out.println("relink-updatePalette");
                ((JExpandablePanel)components[i]).updateLinkedPalette(i % 2 == 0);
            }
        }
    }

    @Override
    public void addNotify() {
        //System.out.println("addNotify");

        super.addNotify();

        updatePalette();

        if (isLinked()) {
            updateLinkedComponents();
        }
    }

    @Override
    public void removeNotify() {
        //System.out.println("removeNotify");

        super.removeNotify();

        if (isLinked()) {
            updateLinkedComponents();
        }
    }

    public static JExpandablePanel createDummyPanel(String title) {
        JExpandablePanel panel = new JExpandablePanel();
        panel.titleContainer.add(new JLabel(title));
        panel.setHighlighted(true);
        //panel.setLinked(true);
        return panel;
    }

    public static void createFrame() {
        final JFrame frame = new JFrame();

        JPanel container = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        //gbc.anchor = GridBagConstraints.NORTH;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        //gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        container.add(createDummyPanel("  Bio Research Project"),gbc);
        container.add(createDummyPanel("  My yet another test project"),gbc);
        container.add(createDummyPanel("  Project MARS"),gbc);
        container.add(createDummyPanel("  Jeta"),gbc);
        container.add(createDummyPanel("  Influenza sequencing analysis"),gbc);
        container.add(createDummyPanel("  Project 6"),gbc);
        container.add(createDummyPanel("  Project 7"),gbc);

        //container.setPreferredSize(new Dimension(800,400));

        JScrollPane scroll = new JScrollPane(container);
        scroll.setPreferredSize(new Dimension(800,400));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(scroll, BorderLayout.CENTER);

        frame.setSize(new Dimension(800,400));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable(){
            public void run() {
                createFrame();
            }
        });
    }

}
