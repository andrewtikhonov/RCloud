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
package workbench.graphics;


import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.*;

import uk.ac.ebi.rcloud.http.proxy.HttpMarker;
import uk.ac.ebi.rcloud.server.ExtendedReentrantLock;
import uk.ac.ebi.rcloud.server.callback.RAction;
import uk.ac.ebi.rcloud.server.callback.RActionConst;
import uk.ac.ebi.rcloud.server.graphics.DoublePoint;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;
import uk.ac.ebi.rcloud.server.graphics.action.GDActionMarker;
import uk.ac.ebi.rcloud.server.graphics.action.GDReset;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDImage;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDState;
import workbench.ConsoleLogger;
import workbench.actions.gd.LinkedToPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workbench.generic.ImageContainer;
import workbench.generic.RConsole;

public class JGDPanelPop extends JBufferedImagePanel {
    final private Logger log = LoggerFactory.getLogger(getClass());
    
	static final long serialVersionUID = 85376389L;
	private Vector<GDObject> _l;
	private static boolean _forceAntiAliasing = true;
	private GDState _gs;
	private Dimension _lastSize;
	private Dimension _prefSize;
	private Long _lastResizeTime = null;
	private GDDevice _gdDevice = null;
	//private boolean _autoPop;
	private boolean _autoResize;
	private AbstractAction[] _actions;
	private boolean mouseInside;
	private Point2D mouseLocation = null;
	private Point2D[] realLocations = null;
	private ReentrantLock _protectR = null;
	private ConsoleLogger _consoleLogger = null;
	private boolean _stopResizeThread = false;
	private Thread _resizeThread = null;
	final private static double fx_MAX = 200;
	final private static double fy_MAX = 200;
	private double _w = Double.NaN;
	private double _h = Double.NaN;
	private double _x0 = Double.NaN;
	private double _y0 = Double.NaN;
	private double _fx = Double.NaN;
	private double _fy = Double.NaN;
	private double _zoomPower = 1.5;

	public static final int INTERACTOR_NULL = 0;
	public static final int INTERACTOR_ZOOM_IN_OUT = 1;
	public static final int INTERACTOR_ZOOM_IN_OUT_X = 2;
	public static final int INTERACTOR_ZOOM_IN_OUT_Y = 3;
	public static final int INTERACTOR_ZOOM_IN_OUT_SELECT = 4;
	public static final int INTERACTOR_ZOOM_IN_OUT_X_SELECT = 5;
	public static final int INTERACTOR_ZOOM_IN_OUT_Y_SELECT = 6;
	public static final int INTERACTOR_SCROLL_LEFT_RIGHT = 7;
	public static final int INTERACTOR_SCROLL_UP_DOWN = 8;
	public static final int INTERACTOR_SCROLL = 9;

	private int _interactor = INTERACTOR_NULL;
	private boolean _showCoordinates = false;
	private Point _mouseStartPosition = null;
	private double _x0Start;
	private double _y0Start;

	private HashSet<JGDPanelPop> coupledToSet = new HashSet<JGDPanelPop>();

	private static int _counter = 0;
	private String _name = "";

	private boolean _broadcasted;
	
	private int maxNbrGraphicPrimitives=16000;
    private GDObjectListener deviceListener = null;
    private RConsoleProvider consoleProvider = null;

	public JGDPanelPop(GDDevice gdDevice, boolean autoPop, boolean autoResize, AbstractAction[] actions) throws RemoteException {
		this(gdDevice, autoPop, autoResize, actions, null, null);
	}

    @Override
    public void addNotify() {
        super.addNotify();
        new Thread(new Runnable(){
            public void run(){
                if (_gdDevice != null) {
                    try {
                        //log.info("addNotify-_gdDevice.fireSizeChangedEvent");

                        _gdDevice.fireSizeChangedEvent(
                                JGDPanelPop.this.getWidth(), JGDPanelPop.this.getHeight());
                    } catch (RemoteException re) {
                    }
                }
            }
        }).start();
    }

    protected void finalize() throws Throwable {
        //log.info("---> JGDPanelPop-finalize()");

        super.finalize();
    }

    public void setConsoleProvider(RConsoleProvider consoleProvider) {
        this.consoleProvider = consoleProvider;
    }

	public JGDPanelPop(GDDevice gdDevice, boolean autoPop, boolean autoResize, AbstractAction[] actions,
                       ReentrantLock protectR, ConsoleLogger consoleLogger)
			throws RemoteException {

        //log.info("---> JGDPanelPop-new()");

		_gdDevice = gdDevice;
		_protectR = protectR;
		_consoleLogger = consoleLogger;
		Dimension sz = null;
		try {
			sz = gdDevice.getSize();
			_broadcasted = gdDevice.isBroadcasted();
		} catch (Exception e) {
		}

		setSize(sz);

		_prefSize = getSize();
		_l = new Vector<GDObject>();
		_gs = new GDState(Color.black, Color.white, new Font(null, 0, 10));
		_lastSize = getSize();
		setBackground(Color.white);
		setOpaque(true);
		_actions = actions;
		if (_actions != null) {
			for (int i = 0; i < _actions.length; ++i) {
				if (_actions[i] instanceof LinkedToPanel)
					((LinkedToPanel) _actions[i]).setPanel(this);
			}
		}

		_x0 = sz.getWidth() / 2;
		_y0 = sz.getHeight() / 2;
		_fx = 1;
		_fy = 1;
		_w = sz.getWidth();
		_h = sz.getHeight();

		_name = "panel_" + (++_counter);
		this.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				mouseInside = true;
				if (_interactor == INTERACTOR_SCROLL) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			public void mouseExited(MouseEvent e) {
				mouseInside = false;

				if (_interactor != INTERACTOR_NULL || _showCoordinates) {
					repaint();
				}
			}

			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					_mouseStartPosition = e.getPoint();
					if (_interactor == INTERACTOR_SCROLL) {
						_x0Start = _x0;
						_y0Start = _y0;
					}
				}
			}

			public void mouseClicked(final MouseEvent e) {

				if (e.isPopupTrigger()) {
					showPopup(e);
				}

				if (e.getButton() == MouseEvent.BUTTON3) {
					showPopup(e);
				}


				if (e.getButton() == MouseEvent.BUTTON1 && (_interactor == INTERACTOR_NULL && _showCoordinates)) {
					new Thread(new Runnable() {
						public void run() {
							try {
                                //log.info("JGDPanelPop-_gdDevice.putLocation");

								_gdDevice.putLocation(new Point(e.getX(), e.getY()));
								if (_consoleLogger != null)
									_consoleLogger.printAsOutput("Location saved, use locator() to retrieve it\n");
							} catch (Exception ex) {
                                log.error("Error!", ex);
							}
						}
					}).start();
				} else if (_interactor == INTERACTOR_ZOOM_IN_OUT) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (e.getModifiersEx() == 0) {

							if (_fx == fx_MAX && _fy == fy_MAX) {
								Toolkit.getDefaultToolkit().beep();
							} else {

								/*
								 * if (e.getX()>_w/2)
								 * scrollXRight((e.getX()-_w/2)); if (e.getX()<_w/2)
								 * scrollXRight((_w/2-e.getX())); if
								 * (e.getY()>_h/2) scrollYDown((e.getY()-_h/2));
								 * if (e.getY()<_h/2)
								 * scrollYUp((_h/2-e.getY()));
								 */

								Runnable action = new Runnable() {
									public void run() {
										double w1 = _w / _zoomPower;
										double h1 = _h / _zoomPower;
										selectZoomX(e.getX() - w1 / 2, e.getX() + w1 / 2);
										selectZoomY(e.getY() - h1 / 2, e.getY() + h1 / 2);
									}
								};
								resizeLater(action);
							}
						} else {
							if (_fx == 1 && _fy == 1) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								Runnable action = new Runnable() {
									public void run() {
										double w1 = _w / _zoomPower;
										double h1 = _h / _zoomPower;
										selectUnzoomX(e.getX() - w1 / 2, e.getX() + w1 / 2);
										selectUnzoomY(e.getY() - h1 / 2, e.getY() + h1 / 2);
									}
								};
								resizeLater(action);
							}
						}

					}
				} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_X) {

					if (e.getButton() == MouseEvent.BUTTON1) {
						if (e.getModifiersEx() == 0) {
							if (_fx == fx_MAX) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								Runnable action = new Runnable() {
									public void run() {
										double w1 = _w / _zoomPower;
										selectZoomX(e.getX() - w1 / 2, e.getX() + w1 / 2);
									}
								};
								resizeLater(action);
							}
						} else {
							if (_fx == 1) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								Runnable action = new Runnable() {
									public void run() {
										double w1 = _w / _zoomPower;
										selectUnzoomX(e.getX() - w1 / 2, e.getX() + w1 / 2);
									}
								};
								resizeLater(action);
							}
						}
					}

				} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_Y) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (e.getModifiersEx() == 0) {
							if (_fy == fy_MAX) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								Runnable action = new Runnable() {
									public void run() {
										double h1 = _h / _zoomPower;
										selectZoomY(e.getY() - h1 / 2, e.getY() + h1 / 2);
									}
								};
								resizeLater(action);
							}
						} else {
							if (_fy == 1) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								Runnable action = new Runnable() {
									public void run() {
										double h1 = _h / _zoomPower;
										selectUnzoomY(e.getY() - h1 / 2, e.getY() + h1 / 2);
									}
								};
								resizeLater(action);
							}
						}
					}
				} else if (_interactor == INTERACTOR_SCROLL_LEFT_RIGHT) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (e.getModifiersEx() == 0) {
							if (_x0 == _w / 2) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								scrollXLeft(0.8 * _w);
							}
						} else {
							if (_x0 == ((_w * _fx) - _w / 2)) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								scrollXRight(0.8 * _w);
							}
						}

					}
				} else if (_interactor == INTERACTOR_SCROLL_UP_DOWN) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						if (e.getModifiersEx() == 0) {
							if (_y0 == _h / 2) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								scrollYUp(0.8 * _h);
							}
						} else {
							if (_y0 == ((_h * _fy) - _h / 2)) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								scrollYDown(0.8 * _h);
							}
						}
					}
				}

			}

			public void mouseReleased(final MouseEvent e) {

				if (e.isPopupTrigger()) {
					showPopup(e);
				}
				if (_mouseStartPosition == null)
					return;
				final Point startPosition = _mouseStartPosition;
				_mouseStartPosition = null;

				if (_interactor == INTERACTOR_NULL && _showCoordinates) {
					repaint();
				} else {
					if (e.getButton() == MouseEvent.BUTTON1) {

						if (startPosition.getX() == e.getPoint().getX() && startPosition.getY() == e.getY()) {
							if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT
									|| _interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {
								Toolkit.getDefaultToolkit().beep();
							}
							return;
						}

						if (e.getModifiersEx() == 0) {

							if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT) {

								if (_fx == fx_MAX && _fy == fy_MAX) {
									Toolkit.getDefaultToolkit().beep();
									repaint();
								} else {
									Runnable action = new Runnable() {
										public void run() {
											selectZoomX(startPosition.getX(), e.getX());
											selectZoomY(startPosition.getY(), e.getY());
										}
									};
									resizeLater(action);
								}

							} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT) {

								if (_fx == fx_MAX) {
									Toolkit.getDefaultToolkit().beep();
									repaint();
								} else {
									Runnable action = new Runnable() {
										public void run() {
											selectZoomX(startPosition.getX(), e.getX());
										}
									};
									resizeLater(action);
								}
							} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {

								if (_fy == fy_MAX) {
									Toolkit.getDefaultToolkit().beep();
									repaint();
								} else {
									Runnable action = new Runnable() {
										public void run() {
											selectZoomY(startPosition.getY(), e.getY());
										}
									};
									resizeLater(action);
								}
							}

						} else {

							if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT) {

								if (_fx == 1 && _fy == 1) {
									Toolkit.getDefaultToolkit().beep();
									repaint();
								} else {
									Runnable action = new Runnable() {
										public void run() {
											selectUnzoomX(startPosition.getX(), e.getX());
											selectUnzoomY(startPosition.getY(), e.getY());
										}
									};
									resizeLater(action);
								}

							} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT) {

								if (_fx == 1) {
									Toolkit.getDefaultToolkit().beep();
									repaint();
								} else {
									Runnable action = new Runnable() {
										public void run() {
											selectUnzoomX(startPosition.getX(), e.getX());
										}
									};
									resizeLater(action);
								}

							} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {

								if (_fy == 1) {
									Toolkit.getDefaultToolkit().beep();
									repaint();
								} else {
									Runnable action = new Runnable() {
										public void run() {
											selectUnzoomY(startPosition.getY(), e.getY());
										}
									};
									resizeLater(action);
								}
							}
						}
					}
				}

			}

			private void showPopup(MouseEvent e) {

                //log.info("showPopup : " + e);
				JPopupMenu popupMenu = new JPopupMenu();

				if (_actions != null) {
					for (int i = 0; i < _actions.length; ++i) {
						if (_actions[i] == null)
							popupMenu.addSeparator();
						else
							popupMenu.add(_actions[i]);
					}
				}

				popupMenu.show(JGDPanelPop.this, e.getX(), e.getY());

			}
		});

        GDeviceTransferHandler transferHandler = new GDeviceTransferHandler(this);

        this.setTransferHandler(transferHandler);
        //this.addMouseListener(new DragMouseAdapter());
        this.addMouseMotionListener(new DragMouseAdapter());

		this.addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				mouseLocation = e.getPoint();

				if (_interactor == INTERACTOR_SCROLL && _mouseStartPosition != null && mouseInside) {
					_x0 = _x0Start + (_mouseStartPosition.getX() - e.getX());
					if (_x0 < _w / 2)
						_x0 = _w / 2;
					if (_x0 > ((_w * _fx) - _w / 2))
						_x0 = ((_w * _fx) - _w / 2);

					_y0 = _y0Start + (_mouseStartPosition.getY() - e.getY());
					if (_y0 < _h / 2)
						_y0 = _h / 2;
					if (_y0 > ((_h * _fy) - _h / 2))
						_y0 = ((_h * _fy) - _h / 2);
					recreateBufferedImage();
					repaint();
					notifyCoupledViewScroll();
				} else if ((_interactor == INTERACTOR_NULL && _showCoordinates) || _interactor != INTERACTOR_NULL) {
					repaint();
				}
			}

			public void mouseMoved(MouseEvent e) {
				mouseLocation = e.getPoint();
				if (_showCoordinates || _interactor != INTERACTOR_NULL) {
					repaint();
				}
			}

		});

		//_autoPop = autoPop;
		_autoResize = autoResize;

        deviceListener = new GDObjectListener(){
            public void pushObjects(Vector<GDObject> gdObjects) {

                if (gdObjects != null && gdObjects.size() > 0) {
                    int size = gdObjects.size();
                    for (int i = 0; i < size; i++) {
                        GDObject obj = gdObjects.elementAt(i);
                        if (obj instanceof GDReset) {
                            _l = new Vector<GDObject>();
                        } else {
                            if (obj instanceof GDImage) {

                                GDImage imageObject = (GDImage) obj;

                                BufferedImage img = ((GDImage)obj).getImage();

                                String id = (String) imageObject.
                                        getAttributes().get(RActionConst.ORIGINATOR);

                                if (id != null && consoleProvider != null) {
                                    RConsole console = consoleProvider.getColsoleMap().get(id);

                                    if (console != null) {
                                        console.printImage(new ImageContainer(
                                                imageObject.getImageData(), img));
                                    }
                                }
                            }

                            _l.add(obj);
                        }
                    }

                    recreateBufferedImage();
                    repaint();
                    //SwingUtilities.invokeLater(new Runnable() {
                    //    public void run() {
                    //    }
                    //});
                }
            }
        };

        try {
            //log.info("JGDPanelPop-_gdDevice.addGraphicListener");

            _gdDevice.addGraphicListener(deviceListener);
        } catch (RemoteException re) {
            log.error("Error!", re);
        }

        if (_autoResize) {
			_resizeThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (_stopResizeThread) {
                            log.info("resize thread stopped");
							break;
                        }
						if (_autoResize) {
							if (_lastResizeTime != null && ((System.currentTimeMillis() - _lastResizeTime) > 500)) {

								synchronized (JGDPanelPop.this) {
									resize();
								}
								notifyCoupledViewResize();

								_lastResizeTime = null;
							}
						}
						try {
							//Thread.sleep(250);
							Thread.sleep(500);
						} catch (Exception e) {
						}
					}

				}
			});
			_resizeThread.start();
		}

	}

	public JGDPanelPop(GDDevice gdDevice) throws RemoteException {
		this(gdDevice, true, true, null, null, null);
	}

	private void selectZoomX(double startX, double endX) {
		double zx = _w / Math.abs(startX - endX);
		double x1 = (startX + endX) / 2;
		if (_fx * zx > fx_MAX) {
			zx = fx_MAX / _fx;
			_fx = fx_MAX;
		} else {
			_fx = _fx * zx;
		}

		_x0 = _x0 - (_w / 2 - x1);
		_x0 = _x0 * zx;
		if (_x0 < _w / 2)
			_x0 = _w / 2;
		if (_x0 > ((_w * _fx) - _w / 2))
			_x0 = ((_w * _fx) - _w / 2);
	}

	private void selectZoomY(double startY, double endY) {
		double zy = _h / Math.abs(startY - endY);
		double y1 = (startY + endY) / 2;
		if (_fy * zy > fy_MAX) {
			zy = fy_MAX / _fy;
			_fy = fy_MAX;
		} else {
			_fy = _fy * zy;
		}
		_y0 = _y0 - (_h / 2 - y1);
		_y0 = _y0 * zy;
		if (_y0 < _h / 2)
			_y0 = _h / 2;
		if (_y0 > ((_h * _fy) - _h / 2))
			_y0 = ((_h * _fy) - _h / 2);
	}

	private void selectUnzoomX(double startX, double endX) {
		double zx = _w / Math.abs(startX - endX);
		double x1 = (startX + endX) / 2;
		if (_fx / zx < 1) {
			_fx = 1;
			_x0 = _w / 2;
		} else {
			_x0 = _x0 - (_w / 2 - x1);
			_fx = _fx / zx;
			_x0 = _x0 / zx;
		}
		if (_x0 < _w / 2)
			_x0 = _w / 2;
		if (_x0 > ((_w * _fx) - _w / 2))
			_x0 = ((_w * _fx) - _w / 2);
	}

	private void selectUnzoomY(double startY, double endY) {
		double zy = _h / Math.abs(startY - endY);
		double y1 = (startY + endY) / 2;
		if (_fy / zy < 1) {
			_fy = 1;
			_y0 = _h / 2;
		} else {
			_y0 = _y0 - (_h / 2 - y1);
			_fy = _fy / zy;
			_y0 = _y0 / zy;
		}

		if (_y0 < _h / 2)
			_y0 = _h / 2;
		if (_y0 > ((_h * _fy) - _h / 2))
			_y0 = ((_h * _fy) - _h / 2);
	}

	private void updateRatios() {
		new Thread(new Runnable() {

			public void run() {
				realLocations = null;

				try {
					if (_protectR != null)
						//_protectR.lock();
						((ExtendedReentrantLock)_protectR).rawLock();

                    //log.info("updateRatios-_gdDevice.getRealPoints");


                    realLocations = _gdDevice.getRealPoints(new Point2D[] { new DoublePoint(0, 0), new DoublePoint(1, 1) });
				} catch (Exception ex) {
                    log.error("Error!", ex);
				} finally {
					if (_protectR != null)
						((ExtendedReentrantLock)_protectR).rawUnlock();
				}

                repaint();

				//SwingUtilities.invokeLater(new Runnable() {
				//	public void run() {
				//	}
				//});

			}
		}).start();

	}

	public void setAutoModes(boolean autoPop, boolean autoResize) {
		//_autoPop = autoPop;
		_autoResize = autoResize;
	}

	
	synchronized public void popNow() {

		try {
            //log.info("popNow-_gdDevice.popAllGraphicObjects");

            Vector<GDObject> gdObjects = _gdDevice.popAllGraphicObjects(maxNbrGraphicPrimitives);
			if (gdObjects != null && gdObjects.size() > 0) {

				_l.addAll(gdObjects);

				Integer resetIdx = null;
				for (int i = _l.size() - 1; i >= 0; --i) {
					if (_l.elementAt(i) instanceof GDReset) {
						resetIdx = i;
						break;
					}
				}

				if (resetIdx != null) {

					Vector<GDObject> accurateEvents = new Vector<GDObject>();
					for (int i = resetIdx + 1; i < _l.size(); ++i) {
						GDObject gdObj = (GDObject) _l.elementAt(i);
						accurateEvents.add(gdObj);
					}
					_l = accurateEvents;
				}

				recreateBufferedImage();
                repaint();

				//SwingUtilities.invokeLater(new Runnable() {
				//	public void run() {
				//	}
				//});
				

			}

		} catch (RemoteException e) {

		}
	}

	public void resizeLater(final Runnable preResizeAction) {
		new Thread(new Runnable() {

			public void run() {

				int savedInteractor = _interactor;
				boolean savedShowCoordinates = _showCoordinates;
				_interactor = INTERACTOR_NULL;
				_showCoordinates = false;

				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							paintAll(getGraphics());
						}
					});
				} catch (Exception e) {
                    log.error("Error!", e);
				}

				synchronized (JGDPanelPop.this) {
					preResizeAction.run();
					resize();

				}
				_interactor = savedInteractor;
				_showCoordinates = savedShowCoordinates;

				if (_showCoordinates) {
					updateRatios();
				}

				recreateBufferedImage();

                repaint();
				//SwingUtilities.invokeLater(new Runnable() {
				//	public void run() {
				//		repaint();
				//	}
				//});

				notifyCoupledViewResize();

			}
		}).start();
	}

	public void recalibrate() {
		if (_x0 < _w / 2)
			_x0 = _w / 2;
		if (_x0 > ((_w * _fx) - _w / 2))
			_x0 = ((_w * _fx) - _w / 2);

		if (_y0 < _h / 2)
			_y0 = _h / 2;
		if (_y0 > ((_h * _fy) - _h / 2))
			_y0 = ((_h * _fy) - _h / 2);
	}

	public synchronized void resize() {
		if (_protectR != null)
			//_protectR.lock();
            ((ExtendedReentrantLock)_protectR).rawLock();
		try {

			if (getWidth() != _w) {
				double px = getWidth() / _w;
				_x0 = _x0 * px;
			}

			if (getHeight() != _h) {
				double py = getHeight() / _h;
				_y0 = _y0 * py;
			}

            //log.info("resize-_gdDevice.fireSizeChangedEvent");
            //new Exception().printStackTrace();

            _gdDevice.fireSizeChangedEvent((int) (getWidth() * _fx), (int) (getHeight() * _fy));

			_w = getWidth();
			_h = getHeight();

            //recreateBufferedImage();
            
			recalibrate();
		} catch (Exception e) {
            repaint();
			//SwingUtilities.invokeLater(new Runnable() {
			//	public void run() {
			//		repaint();
			//	}
			//});
		} finally {
			if (_protectR != null)
				((ExtendedReentrantLock)_protectR).rawUnlock();
		}

		//if (!_autoPop)
			//popNow();

		if (_showCoordinates) {
			updateRatios();
		}
	}

	public void fit() {
		_x0 = getWidth() / 2;
		_y0 = getHeight() / 2;
		_fx = 1;
		_fy = 1;
		synchronized (this) {
			resize();
		}
		repaint();

		notifyCoupledViewResize();

	}

	public void scrollXLeft(double deltax) {

		// double deltax = _w * (_fx - 1) / 10;
		_x0 = _x0 - deltax;
		if (_x0 < _w / 2) {
			_x0 = _w / 2;
		}
		recreateBufferedImage();
		repaint();
		notifyCoupledViewScroll();
	}

	public void scrollXRight(double deltax) {
		// double deltax = _w * (_fx - 1) / 10;
		_x0 = _x0 + deltax;
		if (_x0 > (_w * _fx) - _w / 2) {
			_x0 = (_w * _fx) - _w / 2;
		}
		recreateBufferedImage();
		repaint();
		notifyCoupledViewScroll();
	}

	public void scrollYUp(double deltay) {

		// double deltay = _h * (_fy - 1) / 10;
		_y0 = _y0 - deltay;
		if (_y0 < _h / 2) {
			_y0 = _h / 2;
		}
		recreateBufferedImage();
		repaint();
		notifyCoupledViewScroll();
	}

	public void scrollYDown(double deltay) {
		// double deltay = _h * (_fy - 1) / 10;
		_y0 = _y0 + deltay;
		if (_y0 > (_h * _fy) - _h / 2) {
			_y0 = (_h * _fy) - _h / 2;
		}
		recreateBufferedImage();
		repaint();
		notifyCoupledViewScroll();
	}

	void notifyCoupledViewScroll() {
		if (coupledToSet.size() > 0) {
			for (JGDPanelPop coupledTo : coupledToSet) {
				coupledTo._x0 = _x0;
				coupledTo._y0 = _y0;
				coupledTo.recalibrate();
				coupledTo.recreateBufferedImage();
				coupledTo.repaint();
			}
		}
	}

	void notifyCoupledViewResize() {
		if (coupledToSet.size() > 0) {
			for (JGDPanelPop coupledTo : coupledToSet) {

				final JGDPanelPop coupledToFinal = coupledTo;
				Runnable action = new Runnable() {
					public void run() {

						double fxnew = _fx * _w / coupledToFinal._w;
						if (fxnew >= 1) {
							coupledToFinal._fx = fxnew;
						} else {
							coupledToFinal._fx = 1;
						}
						coupledToFinal._x0 = _x0;

						double fynew = _fy * _h / coupledToFinal._h;
						if (fynew >= 1) {
							coupledToFinal._fy = fynew;
						} else if (coupledToFinal._y0 != _y0) {
							coupledToFinal._fy = 1;
						}
						coupledToFinal._y0 = _y0;

						coupledToFinal.recalibrate();
					}
				};
				coupledTo.resizeLater(action);

			}
		}
	}

	public synchronized Vector<GDObject> getGDOList() {
		return _l;
	}

	public Dimension getPreferredSize() {
		return new Dimension(_prefSize);
	}

	synchronized public void recreateBufferedImage() {
		if (getWidth() > 0 && getHeight() > 0) {
			bufferedImage = null;

            Runtime.getRuntime().gc();

            // bufferedImage = new BufferedImage((int) (getWidth() * _fx), (int)
			// (getHeight() * _fy), BufferedImage.TYPE_INT_RGB);

			bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedImage.createGraphics();
			paintAllObjects(g2d);
			g2d.dispose();
		} else {
			bufferedImage = null;
		}
	}

	private void paintAllObjects(Graphics2D g) {
		if (_forceAntiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		int i = 0, j = _l.size();
		g.setFont(_gs.f);
		// g.setClip(0, 0, (int) (getWidth() * _fx), (int) (getHeight() * _fy));
		// // reset
		g.setClip(0, 0, getWidth(), getHeight()); // reset
		// clipping
		// rect
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.translate(-(_x0 - getWidth() / 2), -(_y0 - getHeight() / 2));
		while (i < j) {
			GDObject o = (GDObject) _l.elementAt(i++);
			if (o instanceof GDActionMarker) {

			} else {
				o.paint(this, _gs, g);
			}
		}
	}

	Color _transparentBlack = new Color(Color.black.getColorSpace(), new float[] { Color.black.getRed(), Color.black.getGreen(), Color.black.getBlue() },
			(float) 0.2);

	public String getRealX(double mX) {
		return new Double((realLocations[1].getX() - realLocations[0].getX()) * (mX - _w / 2 + _x0) + realLocations[0].getX()).toString();
	}

	public String getRealY(double mY) {
		return new Double((realLocations[1].getY() - realLocations[0].getY()) * (mY - _h / 2 + _y0) + realLocations[0].getY()).toString();
	}

	private static int cnt = 0;

    public synchronized void paintComponent(Graphics g) {

		Dimension d = getSize();

		if (!d.equals(_lastSize)) {
            //log.info("_lastSize="+_lastSize);
            //log.info("getSize="+getSize());

			_lastResizeTime = System.currentTimeMillis();
			_lastSize = d;
		}
		Graphics2D g2 = (Graphics2D) g;

		if (bufferedImage != null) {
			g2.setColor(Color.white);
			g2.setBackground(Color.white);
			g2.fillRect(0, 0, getWidth(), getHeight());

            /*
            if (bufferedImage.getWidth() != getWidth() ||
                    bufferedImage.getHeight() != getHeight()) {

                log.info("JGDPanelPop-paintComponent-b.w="+bufferedImage.getWidth()+
                        " w="+getWidth()+" bh="+bufferedImage.getHeight()+" h="+getHeight());

                recreateBufferedImage();
            }
            */

            // ((Graphics2D) g).drawRenderedImage(bufferedImage, new
			// AffineTransform(1, 0, 0, 1, -(_x0 - getWidth() / 2), -(_y0 -
			// getHeight() / 2)));
			g2.drawRenderedImage(bufferedImage, null);
			//g2.drawImage(bufferedImage, 0, 0, null);


		} else {
			g2.setColor(Color.white);
			g2.setBackground(Color.white);
			g2.fillRect(0, 0, getWidth(), getHeight());
		}

		if (mouseInside && mouseLocation != null) {

			if (_mouseStartPosition != null) {

				int x1 = 0;
				int y1 = 0;
				int w1 = 0;
				int h1 = 0;

				if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || (_interactor == INTERACTOR_NULL && _showCoordinates)) {
					x1 = (int) Math.min(_mouseStartPosition.getX(), mouseLocation.getX());
					y1 = (int) Math.min(_mouseStartPosition.getY(), mouseLocation.getY());
					w1 = (int) Math.abs(_mouseStartPosition.getX() - mouseLocation.getX());
					h1 = (int) Math.abs(_mouseStartPosition.getY() - mouseLocation.getY());

					((Graphics2D) g).setColor(_transparentBlack);
					((Graphics2D) g).fillRect(x1, y1, w1, h1);

				} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT) {
					x1 = (int) Math.min(_mouseStartPosition.getX(), mouseLocation.getX());
					y1 = 0;
					w1 = (int) Math.abs(_mouseStartPosition.getX() - mouseLocation.getX());
					h1 = (int) _h;
					((Graphics2D) g).setColor(_transparentBlack);
					((Graphics2D) g).fillRect(x1, y1, w1, h1);
				} else if (_interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {
					x1 = 0;
					y1 = (int) Math.min(_mouseStartPosition.getY(), mouseLocation.getY());
					w1 = (int) _w;
					h1 = (int) Math.abs(_mouseStartPosition.getY() - mouseLocation.getY());
					((Graphics2D) g).setColor(_transparentBlack);
					((Graphics2D) g).fillRect(x1, y1, w1, h1);
				}

				((Graphics2D) g).setColor(Color.black);
				((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 4, 4 }, 20));

				if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT
						|| (_interactor == INTERACTOR_NULL && _showCoordinates)) {
					((Graphics2D) g).setColor(_interactor == INTERACTOR_NULL ? Color.red : Color.black);
					((Graphics2D) g).drawLine((int) x1, 0, (int) x1, getHeight());
					((Graphics2D) g).drawLine((int) x1 + w1, 0, (int) x1 + w1, getHeight());
				}

				if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT
						|| (_interactor == INTERACTOR_NULL && _showCoordinates)) {
					((Graphics2D) g).setColor(_interactor == INTERACTOR_NULL ? Color.red : Color.black);
					((Graphics2D) g).drawLine(0, (int) y1, getWidth(), (int) y1);
					((Graphics2D) g).drawLine(0, (int) y1 + h1, getWidth(), (int) y1 + h1);
				}

				if (_showCoordinates && realLocations != null) {

					x1 = (int) Math.min(_mouseStartPosition.getX(), mouseLocation.getX());
					y1 = (int) Math.min(_mouseStartPosition.getY(), mouseLocation.getY());
					w1 = (int) Math.abs(_mouseStartPosition.getX() - mouseLocation.getX());
					h1 = (int) Math.abs(_mouseStartPosition.getY() - mouseLocation.getY());

					if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT
							|| (_interactor == INTERACTOR_NULL && _showCoordinates)) {
						((Graphics2D) g).setColor(Color.black);
						((Graphics2D) g).drawString("X : " + getRealX(x1), (int) x1 + 8,
								(int) (_interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT ? 0 + ((Graphics2D) g).getFontMetrics().getHeight() + 2 : y1 - 6));
						((Graphics2D) g).drawString("X : " + getRealX(x1 + w1), (int) x1 + w1 + 8, y1 + h1 - 6);
					}

					if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT
							|| (_interactor == INTERACTOR_NULL && _showCoordinates)) {
						((Graphics2D) g).setColor(Color.black);
						((Graphics2D) g).drawString("Y : " + getRealY(y1), _interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT ? 0 : ((int) x1 + 8), (int) y1
								+ ((Graphics2D) g).getFontMetrics().getHeight() + 2);
						((Graphics2D) g).drawString("Y : " + getRealY(y1 + h1), ((int) x1 + w1 + 8), (int) y1 + h1
								+ ((Graphics2D) g).getFontMetrics().getHeight() + 2);
					}

				}

			} else if (_showCoordinates) {

				((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 4, 4 }, 20));

				if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor == INTERACTOR_ZOOM_IN_OUT_X_SELECT
						|| (_interactor == INTERACTOR_NULL && _showCoordinates)) {
					((Graphics2D) g).setColor(_interactor == INTERACTOR_NULL ? Color.red : Color.black);
					((Graphics2D) g).drawLine((int) mouseLocation.getX(), 0, (int) mouseLocation.getX(), getHeight());
					if (realLocations != null) {
						((Graphics2D) g).setColor(Color.black);
						((Graphics2D) g).drawString("X : " + getRealX(mouseLocation.getX()), (int) mouseLocation.getX() + 10, (int) mouseLocation.getY() - 4);
					}
				}

				if (_interactor == INTERACTOR_ZOOM_IN_OUT_SELECT || _interactor == INTERACTOR_ZOOM_IN_OUT_Y_SELECT
						|| (_interactor == INTERACTOR_NULL && _showCoordinates)) {
					((Graphics2D) g).setColor(_interactor == INTERACTOR_NULL ? Color.red : Color.black);
					((Graphics2D) g).drawLine(0, (int) mouseLocation.getY(), getWidth(), (int) mouseLocation.getY());
					if (realLocations != null) {
						((Graphics2D) g).setColor(Color.black);
						((Graphics2D) g).drawString("Y : " + getRealY(mouseLocation.getY()), (int) mouseLocation.getX() + 10, (int) mouseLocation.getY()
								+ ((Graphics2D) g).getFontMetrics().getHeight() + 2);
					}
				}

			}

			if (_interactor != INTERACTOR_NULL) {
				String mouseDecorator = "";
				switch (_interactor) {
				case INTERACTOR_ZOOM_IN_OUT:
					mouseDecorator = "+ / -";
					break;
				case INTERACTOR_ZOOM_IN_OUT_X:
					mouseDecorator = "+ / - X";
					break;
				case INTERACTOR_ZOOM_IN_OUT_Y:
					mouseDecorator = "+ / - Y";
					break;
				case INTERACTOR_ZOOM_IN_OUT_SELECT:
					mouseDecorator = "+ / - selection";
					break;
				case INTERACTOR_ZOOM_IN_OUT_X_SELECT:
					mouseDecorator = "+ / - X selection";
					break;
				case INTERACTOR_ZOOM_IN_OUT_Y_SELECT:
					mouseDecorator = "+ / - Y selection";
					break;
				case INTERACTOR_SCROLL_LEFT_RIGHT:
					mouseDecorator = "<< X >>";
					break;
				case INTERACTOR_SCROLL_UP_DOWN:
					mouseDecorator = "<< Y >>";
					break;
				default:
					break;
				}

				((Graphics2D) g).setColor(Color.black);
				if (!_showCoordinates) {
					((Graphics2D) g).drawString(mouseDecorator, (int) mouseLocation.getX() + 2, (int) mouseLocation.getY() - 2);
				} else {
					((Graphics2D) g).drawString(mouseDecorator, (int) mouseLocation.getX()
							- SwingUtilities.computeStringWidth(((Graphics2D) g).getFontMetrics(), mouseDecorator) - 4, (int) mouseLocation.getY() - 4);
				}
			}

		}

	}

	public GDDevice getGdDevice() {
		return _gdDevice;
	}

	public void stopThreads() {
        //log.info("stopThreads-stopThreads");

		_stopResizeThread = true;


        try {

            if (_resizeThread != null) {
                _resizeThread.interrupt();
                _resizeThread.join();
                _resizeThread = null;
            }
            
        } catch (InterruptedException ie) {
            log.error("Error!", ie);
        }
	}


    // disconnects the device and
    // unmounts the device from R
    //
	public void dispose() {

        //log.info("dispose()");
        //log.info("dispose-stopThreads()");

        stopThreads();

        //log.info("dispose-removing listeners");

        if (_gdDevice != null) {
            try {
                //log.info("dispose-_gdDevice.removeGraphicListener");

                _gdDevice.removeGraphicListener(deviceListener);
            } catch (Exception e) {
                log.error("Error!", e);
            }

            try {
                //log.info("dispose-_gdDevice.dispose()");

                _gdDevice.dispose();
            } catch (Exception e) {
                log.error("Error!", e);
            }

            try {
                if (_gdDevice instanceof HttpMarker) {
                    //log.info("dispose-_gdDevice.stopThreads()");

                    ((HttpMarker) _gdDevice).stopThreads();
                }
            } catch (Exception e) {
                log.error("Error!", e);
            }

            _gdDevice = null;
        }
	}

    // disconnects the device
    //
	public void disconnect() {
		//log.info("disconnect-disconnect");

        stopThreads();

        if (_gdDevice != null) {

            try {
                if (_gdDevice instanceof HttpMarker) {
                    //log.info("((HttpMarker) _gdDevice).stopThreads()");
                    //log.info("disconnect-_gdDevice.stopThreads()");

                    ((HttpMarker) _gdDevice).stopThreads();
                }
            } catch (Exception e) {
                log.error("Error!", e);
            }

            _gdDevice = null;
        }
	}

	public int getInteractor() {
		return _interactor;
	}

	public void setInteractor(int interactor) {
		_interactor = interactor;
		_mouseStartPosition = null;
		repaint();
	}

	public boolean isShowCoordinates() {
		return _showCoordinates;
	}

	public void setShowCoordinates(boolean showCoordinates) {
		_showCoordinates = showCoordinates;
		if (showCoordinates) {
			updateRatios();
		}
	}

	public boolean isCoupledTo(JGDPanelPop panel) {
		return coupledToSet.contains(panel);
	}

	synchronized public void addCoupledTo(JGDPanelPop panel) {
		coupledToSet.add(panel);
		notifyCoupledViewResize();
	}

	synchronized public void removeCoupledTo(JGDPanelPop panel) {
		coupledToSet.remove(panel);
	}

	synchronized public void removeAllCoupledTo() {
		coupledToSet = new HashSet<JGDPanelPop>();
	}

	public String toString() {
		return _name;
	}

}
