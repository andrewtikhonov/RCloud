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
package uk.ac.ebi.rcloud.server.graphics.rmi;

import java.awt.*;
import java.awt.event.*;
import uk.ac.ebi.rcloud.server.graphics.LocalGraphicNotifier;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import uk.ac.ebi.rcloud.rpf.RemotePanel;
import uk.ac.ebi.rcloud.server.graphics.GDContainer;
import uk.ac.ebi.rcloud.server.graphics.GraphicNotifier;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGraphicsPanelRemote extends RemotePanel implements GDContainer {
    final private static Logger log = LoggerFactory.getLogger(RGraphicsPanelRemote.class);

    static final long serialVersionUID = 85376389L;
	private Vector<GDObject> _l;
	public static boolean forceAntiAliasing = true;
	private GDState _gs;
	private Dimension _lastSize;
	private int _devNr = -1;
	private Dimension _prefSize;
	private GraphicNotifier _graphicNotifier;
	private Long _lastResizeTime = null;
	private GDContainerAdapterImpl _gdcServer = null;

	public RGraphicsPanelRemote(double w, double h, GraphicNotifier graphicNotifier) {
		this((int) w, (int) h, graphicNotifier);
	}

	public RGraphicsPanelRemote(int w, int h, GraphicNotifier graphicNotifier) {
		super(true);
		setOpaque(true);
		setSize(w, h);
		_prefSize = new Dimension(w, h);
		_l = new Vector<GDObject>();
		_gs = new GDState();
		_gs.f = new Font(null, 0, 12);
		setSize(w, h);
		_lastSize = getSize();
		setBackground(Color.white);
		_graphicNotifier = graphicNotifier;

	}

	public void init() {
		try {
			if (!(_graphicNotifier instanceof LocalGraphicNotifier)) {
				_gdcServer = new GDContainerAdapterImpl(this);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				_graphicNotifier.registerContainer(_gdcServer);
			} else {
				_graphicNotifier.registerContainer(this);
			}
		} catch (Exception e) {
            log.error("Error!", e);
		}

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (_lastResizeTime != null && ((System.currentTimeMillis() - _lastResizeTime) > 1000)) {
						try {
							_graphicNotifier.fireSizeChangedEvent(_devNr);
						} catch (RemoteException e) {
                            log.error("Error!", e);
						}
						_lastResizeTime = null;
					}

					try {
						Thread.sleep(20);
					} catch (Exception e) {
					}
				}

			}
		}).start();

		addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					new Thread(new Runnable() {
						public void run() {
							try {
								_graphicNotifier.putLocation(new Point(e.getX(), e.getY()));
							} catch (Exception ex) {
                                log.error("Error!", ex);
							}
						}
					}).start();
				}

			}

			public void mouseReleased(MouseEvent e) {
			}

		});

	}

	public void dispose() throws RemoteException {
		_graphicNotifier.executeDevOff(_devNr);
		if (_gdcServer != null) {
			UnicastRemoteObject.unexportObject((Remote) _gdcServer, true);
		}
	}

	public void setGFont(Font f) {
		_gs.f = f;
	}

	public Font getGFont() throws RemoteException {
		return _gs.f;
	}

	public FontMetrics getGFontMetrics() throws RemoteException {
		if (getGraphics() == null)
			return null;
		return getGraphics().getFontMetrics(_gs.f);
	}

    public void addGraphicListener(GDObjectListener objectListener) throws RemoteException {
        throw new RemoteException("Not to be called");
    }

    public void removeGraphicListener(GDObjectListener objectListener) throws RemoteException {
        throw new RemoteException("Not to be called");
    }

	public void setDeviceNumber(int dn) {
		this._devNr = dn;
	}

	public int getDeviceNumber() {
		return this._devNr;
	}

	public void closeDisplay() {
	}

	public void syncDisplay(boolean finish) {
		repaint();
	}

	public synchronized Vector getGDOList() {
		return _l;
	}

	public synchronized void add(GDObject o) {
		_l.add(o);
	}

	public synchronized void reset() {
		_l.removeAllElements();
	}

	public Dimension getPreferredSize() {
		return new Dimension(_prefSize);
	}

	public synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);

		Dimension d = getSize();

		if (!d.equals(_lastSize)) {
			_lastResizeTime = System.currentTimeMillis();
			_lastSize = d;
			return;
		} else {

		}

		if (forceAntiAliasing) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		int i = 0, j = _l.size();
		g.setFont(_gs.f);
		g.setClip(0, 0, d.width, d.height); // reset clipping rect
		g.setColor(Color.white);
		g.fillRect(0, 0, d.width, d.height);
		while (i < j) {
			GDObject o = (GDObject) _l.elementAt(i++);
			o.paint(this, _gs, g);
		}
	}

}
