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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.http.proxy.HttpMarker;
import uk.ac.ebi.rcloud.server.callback.RActionConst;
import uk.ac.ebi.rcloud.server.graphics.GDDevice;
import uk.ac.ebi.rcloud.server.graphics.GDObjectListener;
import uk.ac.ebi.rcloud.server.graphics.action.GDActionMarker;
import uk.ac.ebi.rcloud.server.graphics.action.GDReset;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDImage;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDState;
import workbench.generic.ImageContainer;
import workbench.generic.RConsole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Dec 3, 2010
 * Time: 2:19:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class GDContainerPanel extends JPanel {
    
    final private static Logger log = LoggerFactory.getLogger(GDContainerPanel.class);

    // objects
    private Vector<GDObject> _l = new Vector<GDObject>();

    // graphics state
    private GDState _gs = new GDState(Color.black, Color.white, new Font(null, 0, 10));;

    // image
    protected BufferedImage bufferedImage = null;

	private Dimension _prefSize;

    // device
	private GDDevice _gdDevice = null;

    // actions
	private AbstractAction[] _actions;

    // resize handling
	private Thread resizeThread = null;
    private boolean stopResizeThread = false;

    // identification
	private static int _counter = 0;
	private String _name = "";

    // translation and scaling
    private double translateX = 0;
    private double translateY = 0;
    private double zoom = 1;

    private Dimension gdSize = new Dimension(200, 200);

    // object listener
    private GDObjectListener deviceListener = null;

    // console provider
    private RConsoleProvider consoleProvider = null;

    // lock object
    protected Integer lockObject = new Integer(0);


    /*
    @Override
    public void addNotify() {
        super.addNotify();
        new Thread(new Runnable(){
            public void run(){
                if (_gdDevice != null) {
                    try {
                        //log.info("addNotify-_gdDevice.fireSizeChangedEvent");

                        //gdSize.width = GDContainerPanel.this.getWidth();
                        //gdSize.height = GDContainerPanel.this.getHeight();

                        //_gdDevice.fireSizeChangedEvent(gdSize.width, gdSize.height);

                    } catch (RemoteException re) {
                    }
                }
            }
        }).start();
    }
    */

    public void setConsoleProvider(RConsoleProvider consoleProvider) {
        this.consoleProvider = consoleProvider;
    }

    public GDContainerPanel(GDDevice gdDevice) throws RemoteException {
        this(gdDevice, null);
    }

	public GDContainerPanel(GDDevice gdDevice, AbstractAction[] actions) throws RemoteException {

        //log.info("JGDPanelPop-new()");

		_gdDevice = gdDevice;

        setOpaque(true);

        setDoubleBuffered(true);

		try {
			gdSize = gdDevice.getSize();
		} catch (Exception e) {
		}

		setSize(gdSize);

		_prefSize = getSize();

		setBackground(Color.white);
		setOpaque(true);

		_name = "panel_" + (++_counter);

        ImageTranslateHandler translator = new ImageTranslateHandler();
        ImageScaleHandler scaler = new ImageScaleHandler();


        this.addMouseListener(translator);
        this.addMouseMotionListener(translator);
        this.addMouseWheelListener(scaler);

        deviceListener = new GDContainerObjectListener();

        try {
            //log.info("JGDPanelPop-_gdDevice.addGraphicListener");

            _gdDevice.addGraphicListener(deviceListener);
        } catch (RemoteException re) {
            log.error("Error!", re);
        }

        resizeThread = new Thread(new Runnable() {

            private int timeout = 20000;

            public void run() {
                while (true) {
                    if (stopResizeThread) {
                        log.info("resize thread stopped");
                        break;
                    }

                    Double scaleObject = null;

                    try {
                        scaleObject = notifyQueue.poll(timeout, TimeUnit.MILLISECONDS);

                        //log.info("resizeThread-gdSize.width="+gdSize.width+" gdSize.height="+gdSize.height);

                    } catch (InterruptedException ie) {
                        log.info("timeout");
                    } catch (Exception e) {
                        log.error("Error!", e);
                    }

                    if (scaleObject != null) {
                        while (notifyQueue.peek() != null) {
                            scaleObject = notifyQueue.poll();
                        }

                        try {
                            _gdDevice.fireSizeChangedEvent((int) (gdSize.width * scaleObject), 
                                    (int) (gdSize.height * scaleObject));

                        } catch (Exception ex) {
                            log.error("Error!", ex);
                        }
                    }
                }

            }
        });
        resizeThread.start();

	}

    private ArrayBlockingQueue<Double> notifyQueue = new ArrayBlockingQueue<Double>(100);


    class GDContainerObjectListener implements GDObjectListener {
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

                if (_l.size() > 0) {
                    recreateBufferedImage();
                    repaint();

                    //log.info("pushObjects-repaint-objects");

                } else {

                    //log.info("pushObjects-repaint-reset");
                }
            }
        }
    }


    public BufferedImage getImage() {
        return bufferedImage == null ? new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB) : bufferedImage;
    }

	public Dimension getPreferredSize() {
		return new Dimension(_prefSize);
	}

	public void recreateBufferedImage() {

        synchronized(lockObject) {

            if (getWidth() > 0 && getHeight() > 0) {

                Dimension newsize = new Dimension(0, 0);

                if (_l.size() > 0) {

                    //log.info("recreateBufferedImage-BEFORE-newsize.w="+newsize.width+" newsize.h="+newsize.height);

                    for(GDObject o : _l) {
                        if (o instanceof GDImage) {
                            BufferedImage img = ((GDImage) o).getImage();
                            newsize.width = Math.max(newsize.width, img.getWidth());
                            newsize.height = Math.max(newsize.height, img.getHeight());
                        }
                    }

                    //log.info("recreateBufferedImage-AFTER-newsize.w="+newsize.width+" newsize.h="+newsize.height);
                }

                if (bufferedImage != null) {

                    int w = bufferedImage.getWidth();
                    int h = bufferedImage.getHeight();


                    if (w != newsize.width || h != newsize.height) {

                        //log.info("recreateBufferedImage-NEW-SIZE-NEW-IMAGE");

                        translateX -= (newsize.width - w)/2;
                        translateY -= (newsize.height - h)/2;

                        bufferedImage = new BufferedImage(newsize.width, newsize.height, BufferedImage.TYPE_INT_RGB);
                    }


                } else {
                    //log.info("recreateBufferedImage-DEFAULT-IMAGE");

                    bufferedImage = new BufferedImage(Math.max(newsize.width, getWidth()),
                            Math.max(newsize.height, getHeight()), BufferedImage.TYPE_INT_RGB);
                }

                Runtime.getRuntime().gc();

                // bufferedImage = new BufferedImage((int) (getWidth() * _fx), (int)
                // (getHeight() * _fy), BufferedImage.TYPE_INT_RGB);

                //bufferedImage = new BufferedImage(gdSize.width, gdSize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = bufferedImage.createGraphics();
                paintAllObjects(g2d);
                g2d.dispose();
            } else {
                bufferedImage = null;
            }
        }
	}

	private void paintAllObjects(Graphics2D g) {

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


		int i = 0, j = _l.size();
		g.setFont(_gs.f);
		// g.setClip(0, 0, (int) (getWidth() * _fx), (int) (getHeight() * _fy));
		// // reset
		//g.setClip(0, 0, getWidth(), getHeight()); // reset
		// clipping
		// rect

        g.setColor(Color.white);
		g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

		//g.translate(-(_x0 - getWidth() / 2), -(_y0 - getHeight() / 2));
		while (i < j) {
			GDObject o = _l.elementAt(i++);
			if (o instanceof GDActionMarker) {
			} else {
				o.paint(this, _gs, g);
			}
		}
	}

    public void paintComponent(Graphics g) {

        synchronized(lockObject) {

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

                AffineTransform tx = new AffineTransform();
                tx.translate(translateX, translateY);
                //tx.scale(scale, scale);

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                //g2.setTransform(tx);

                g2.drawRenderedImage(bufferedImage, tx);

            } else {
                g2.setColor(Color.white);
                g2.setBackground(Color.white);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

        }
	}

    class ImageTranslateHandler implements MouseListener,
            MouseMotionListener {
        private int lastOffsetX;
        private int lastOffsetY;

        public void mousePressed(MouseEvent e) {
            // capture starting point
            lastOffsetX = e.getX();
            lastOffsetY = e.getY();
        }

        public void mouseDragged(MouseEvent e) {

            // new x and y are defined by current mouse location subtracted
            // by previously processed mouse location
            int newX = e.getX() - lastOffsetX;
            int newY = e.getY() - lastOffsetY;

            // increment last offset to last processed by drag event.
            lastOffsetX += newX;
            lastOffsetY += newY;

            // update the canvas locations
            translateX += newX;
            translateY += newY;

            // schedule a repaint.
            repaint();
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mouseMoved(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
    }

    class ImageScaleHandler implements MouseWheelListener {
        public void mouseWheelMoved(MouseWheelEvent e) {

            if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {

                double newzoom = getZoom() + (-.01 * e.getWheelRotation());

                setZoom(newzoom);
            }
        }
    }

    public double getZoom() {
        return zoom;
    }

    public final static String ZOOM_PROPERY = "ZOOM";

    public void setZoom(double zoom) {


        synchronized(lockObject) {

            this.zoom = zoom;

            this.zoom = Math.max(0.3, Math.min(this.zoom, 4));

            notifyQueue.add(this.zoom);
        }

        firePropertyChange(ZOOM_PROPERY, 0, this.zoom);
    }

	public GDDevice getGdDevice() {
		return _gdDevice;
	}

	public void stopThreads() {
        //log.info("stopThreads-stopThreads");

		stopResizeThread = true;

        try {

            if (resizeThread != null) {
                resizeThread.interrupt();
                resizeThread.join();
                resizeThread = null;
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

	public String toString() {
		return _name;
	}

}
