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
package workbench.views.helpview;

import uk.ac.ebi.rcloud.common.components.JTextPaneExt;
import uk.ac.ebi.rcloud.common.components.panel.SearchPanel;
import uk.ac.ebi.rcloud.common.util.ImageLoader;
import workbench.Workbench;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import workbench.RGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.util.ButtonUtil;

public class HelpBrowserPanel extends JPanel implements HyperlinkListener, ClipboardOwner {
    final static private Logger log = LoggerFactory.getLogger(HelpBrowserPanel.class);

	private JButton homeButton;
	private JButton backButton;
	private JButton forwardButton;

    private JLabel urlHintLabel;
	private JTextField urlField;
    private JTextPaneExt htmlPane;
	private RGui rgui;
    private Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	private String getDefaultHelpUrl() {
        if (rgui != null) {
            return  rgui.getHelpRootUrl() + "/doc/html/index.html";
        } else {
            return "empty";
        }
	}

	public HelpBrowserPanel(RGui rgui) {
		this.rgui = rgui;

		JPanel topPanel = new JPanel();
		topPanel.setBackground(Color.lightGray);
		topPanel.setLayout(new BorderLayout());

		homeButton = ButtonUtil.makeButton(null, "/views/images/helpviewer/house.png", "Home");
        homeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){

                setURL(getDefaultHelpUrl());
            }
        });


        backButton = ButtonUtil.makeButton(null, "/views/images/helpviewer/go-previous.png", "Back");
        backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cursor > 0) {
					--cursor;
					try {
						setURL(history.elementAt(cursor), false);
					} catch (Exception ex) {
                        log.error("Error!", ex);
					}
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});

        forwardButton = ButtonUtil.makeButton(null, "/views/images/helpviewer/go-next.png", "Forward");
        forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cursor < (history.size() - 1)) {
					++cursor;

					try {
						setURL(history.elementAt(cursor), false);
					} catch (Exception ex) {
                        log.error("Error!", ex);
					}
				} else {
					Toolkit.getDefaultToolkit().beep();
				}

			}
		});

		JLabel urlLabel = new JLabel("   URL:  ");
		urlField = new JTextField(30);
		urlField.setText(getDefaultHelpUrl());
		urlField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (urlField.getText().trim().equals(""))
                    return;

                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            setURL(urlField.getText());
                        } catch (Exception e) {
                            log.error("Error!", e);
                        }
                    }
                });
            }
        });

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		buttonPanel.add(backButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(homeButton);

		JPanel blPanel = new JPanel(new BorderLayout());
		blPanel.add(buttonPanel, BorderLayout.WEST);
		blPanel.add(urlLabel, BorderLayout.EAST);
		topPanel.add(blPanel, BorderLayout.WEST);
		topPanel.add(urlField, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);

		try {
			//_htmlPane = new JEditorPane(getDefaultHelpUrl() + ";jsessionid=" + applet.getSessionId());
			htmlPane = new JTextPaneExt();//EditorPane(getDefaultHelpUrl() + ";jsessionid=" + applet.getSessionId());
			htmlPane.setEditable(false);
			htmlPane.addHyperlinkListener(this);

			JPopupMenu menu = new JPopupMenu();
			menu.add(new AbstractAction("Copy") {
				public void actionPerformed(ActionEvent e) {
					copySelectionToClipboard();
				}
			});

			htmlPane.addMouseListener(new Workbench.PopupListener(menu));

			htmlPane.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
					if (((byte) e.getKeyChar()) == 3 && e.getModifiers() == 2) {
						copySelectionToClipboard();
					}
				}
			});

			JScrollPane scrollPane = new JScrollPane(htmlPane);
			add(scrollPane, BorderLayout.CENTER);

            urlHintLabel = new JLabel();
            urlHintLabel.setText(" ");

            add(urlHintLabel, BorderLayout.SOUTH);

		//} catch (IOException ioe) {
		//	warnUser("Can't build HTML pane for " + getDefaultHelpUrl() + ": " + ioe);
		} finally {
        }

		Dimension screenSize = getToolkit().getScreenSize();
		int width = screenSize.width * 8 / 10;
		int height = screenSize.height * 8 / 10;
		setBounds(width / 8, height / 8, width, height);

	}

	public void copySelectionToClipboard() {
		StringSelection stringSelection = new StringSelection(htmlPane.getSelectedText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, HelpBrowserPanel.this);
	}

	int cursor = -1;
	Vector<String> history = new Vector<String>();

    private void setURL(final String url, final boolean appendToHistory) {
        new Thread(new Runnable(){
            public void run(){
                setURLInternal(url, appendToHistory);
            }
        }).start();
    }

	private void setURLInternal(String url, boolean appendToHistory) {

		if (appendToHistory) {
			history.setSize(cursor + 1);
			history.add(url);
			cursor = history.size() - 1;
		}

		if (rgui.getSessionId() == null) {
			JOptionPane.showMessageDialog(this, "Sorry, you are not connected to R server");
			return;
		}

		int sp = url.indexOf(";jsessionid");
		if (sp != -1) {
			url = url.substring(0, sp) + url.substring(sp + ";jsessionid=".length() + 32, url.length());
		}

		int sref = url.indexOf("#");
		String ref = null;
		if (sref != -1) {
			ref = url.substring(sref + 1);
			url = url.substring(0, sref);
		}

        try {
            urlField.setText(new URL(url).toExternalForm());

            if (url.startsWith(rgui.getHelpRootUrl())) {
                htmlPane.setPage(new URL(url + ";jsessionid=" + rgui.getSessionId()));
            } else {
                htmlPane.setPage(new URL(url));
            }

            if (ref != null) {
                htmlPane.scrollToReference(ref);
            }
        } catch (Exception ex) {
        }
	}

	public void setURL(String url) {
		setURL(url, true);
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {

		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

			try {
				setURL(event.getURL().toString()); // setURL(url);
			} catch (Exception ioe) {
				warnUser("Can't follow link to " + event.getURL().toExternalForm() + ": " + ioe);
			}
		}

        if(event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            urlHintLabel.setText(event.getURL().toString());
        }

        if(event.getEventType() == HyperlinkEvent.EventType.EXITED) {
            urlHintLabel.setText(" ");
        }
	}

	private void warnUser(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

    public JTextPane getTextComponent() {
        return htmlPane;
    }

    private JButton makeButton(String imagePath, String toolTip) {
        //JButtonEx b = new JButtonEx();
        JButton b = new JButton();

        b.setIcon(new ImageIcon(ImageLoader.load(imagePath)));

        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusable(false);
        b.setPreferredSize(new Dimension(24, 24));
        b.setToolTipText(toolTip);
        b.setCursor(hand);

        return(b);
    }

    // test GUI
    //
    //

    public static void createGUI(){

        JFrame frame = new JFrame();

        HelpBrowserPanel browser = new HelpBrowserPanel(null);

        try {
            browser.getTextComponent().setPage("http://rss.acs.unt.edu/Rdoc/library/base/html/print.default.html");
        } catch (IOException ioe) {
            log.error("Error!", ioe);
        }

        SearchPanel search = new SearchPanel(browser.getTextComponent());

        frame.setLayout(new BorderLayout());
        frame.add(browser, BorderLayout.CENTER);
        frame.add(search, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    public static void main(String args[]){
        EventQueue.invokeLater(new Runnable(){
            public void run(){
                createGUI();
            }
        });
    }

}
