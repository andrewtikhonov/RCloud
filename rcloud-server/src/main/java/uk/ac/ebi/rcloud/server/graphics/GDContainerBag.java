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
package uk.ac.ebi.rcloud.server.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import uk.ac.ebi.rcloud.server.callback.RActivityCallback;
import uk.ac.ebi.rcloud.server.graphics.action.*;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDImage;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDObject;
import uk.ac.ebi.rcloud.server.graphics.primitive.GDState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.rcloud.util.HexUtil;

import javax.imageio.ImageIO;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: May 12, 2010
 * Time: 4:50:51 PM
 * To change this template use File | Settings | File Templates.
 */

public class GDContainerBag implements GDContainer, RActivityCallback, Runnable {
    final private static Logger log = LoggerFactory.getLogger(GDContainerBag.class);

	private Vector<GDObject> gdActions = new Vector<GDObject>();
    private BufferedImage imageBuffer = null;
    private Graphics2D imageGraphics2D = null;
    private HashMap<String, Object> attributes = null;

	private Dimension _size = null;
	private GDState _gs;
	private int _devNr = -1;

    private Thread  objectProcessorThread = null;
    private Integer actionBufferLock = new Integer(0);

    private Vector<GDObjectListener> listenerList = new Vector<GDObjectListener>();

    private static final int RESET = 0;
    private static final int DRAWING = 1;
    private static final int COMPLETE = 2;
    private int imagestate = RESET;

    private boolean isCachingEnabled(){
        String prop = System.getProperty("graphics.cached");
        return (prop == null || !prop.equalsIgnoreCase("false"));
    }

    private boolean isEstimationEnabled(){
        String prop = System.getProperty("graphics.estimation");
        return (prop != null && prop.equalsIgnoreCase("true"));
    }

    private Color getBackground() {
        String prop = System.getProperty("graphics.background");

        if (prop == null || prop.equalsIgnoreCase("default")) {
            return null;
        } else {
            int i = Integer.decode(prop);
            return new Color(i,i,i);
        }
    }

    private void disposeContainerGraphics2D() {
        //log.info("disposeContainerGraphics2D");

        if (imageGraphics2D != null) {
            imageGraphics2D.dispose();
        }
        imageGraphics2D = null;
        imageBuffer = null;
    }

    public Graphics2D getContainerGraphics2D() {
        //log.info("getContainerGraphics2D");

        if (imageBuffer == null ||
                imageBuffer.getWidth() != _size.width ||
                imageBuffer.getHeight() != _size.height) {

            imageBuffer = new BufferedImage(_size.width, _size.height, BufferedImage.TYPE_INT_ARGB);
            imageGraphics2D = imageBuffer.createGraphics();

            imageGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

        }
        return imageGraphics2D;
    }

    public BufferedImage getContainerImage() {
        //log.info("getContainerImage");

        return imageBuffer;
    }

    public void clearContainerImage() {
        //log.info("clearContainerImage");

        if (imageGraphics2D != null) {
            imageGraphics2D.setComposite(AlphaComposite.Clear);
            imageGraphics2D.fillRect(0, 0, imageBuffer.getWidth(), imageBuffer.getHeight());
            imageGraphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public void makeBackground(Color c) {
        //log.info("makeBackground");

        if (imageGraphics2D != null && c != null) {
            Color c0 = imageGraphics2D.getColor();
            imageGraphics2D.setColor(c);
            imageGraphics2D.fillRect(0, 0, imageBuffer.getWidth(), imageBuffer.getHeight());
            imageGraphics2D.setColor(c0);
        }
    }

    public void immediateDrawPrimitive(GDObject object) {
        object.paint(null, _gs, getContainerGraphics2D());
    }

    private int graphicsdatasize = 0;
    private boolean datasizeestimation = false;

    public GDContainerBag(int w, int h) {
		_size = new Dimension(w, h);
		_gs = new GDState(Color.black, Color.white, new Font(null, 0, 10));

        objectProcessorThread = new Thread(this);
        objectProcessorThread.start();
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    public HashMap<String, Object> getAttributes() {
        return this.attributes;
    }

    public void prepareAndSendImage() {
        Stopwatch sw = new Stopwatch();

        sw.start();

        GDImage imageobj = new GDImage(0, 0, getContainerImage()).putAll(getAttributes());

        addGDAction(imageobj);

        //log.info("prepareAndSendImage addGDAction GDImage");

        sw.stop();

        if (datasizeestimation) {
            graphicsdatasize = graphicsdatasize +
                    (HexUtil.objectToHex(imageobj).length() / 2);
        }

        if (datasizeestimation) {
            log.info("graphics encoded and streamed: " + sw.elapsedTimeMillis() + " ms");
        }

        if (datasizeestimation) {
            log.info("graphics encoded size: " + (graphicsdatasize / 1024) + " kb");
        }
    }

    public void notify(int busy, HashMap<String, Object> attributes) {
        //log.info("notify busy " + busy + " attributes " + attributes);

        setAttributes(attributes);

        if (busy == 0 && imagestate == DRAWING) {

            //log.info("notify COMPLETE");

            imagestate = COMPLETE;

            prepareAndSendImage();
        }
    }

	synchronized public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) {
		//log.info("popAllGraphicObjects");

        if (gdActions.size() == 0) {
            return null;
        }

        Vector<GDObject> result = (Vector<GDObject>) gdActions.clone();

        if (maxNbrGraphicPrimitives!=-1 && result.size() > maxNbrGraphicPrimitives) {
			int delta = result.size() - maxNbrGraphicPrimitives;
			for (int i = 0; i < delta; ++i) {
				result.remove(result.size() - 1);
			}
		}

		for (int i = 0; i < result.size(); ++i) {
            gdActions.remove(0);
        }

        return result;
	}


	public boolean hasGraphicObjects() {
		return gdActions.size() > 0;
	}

	public Dimension getSize() throws RemoteException {
		return _size;
	}

	public void setSize(int w, int h) {
        //log.info("GDContainerBag-setSize-w="+w+" h="+h);

		_size = new Dimension(w, h);
	}

	public void add(GDObject o) throws RemoteException {
        //log.info("add");

        if (imagestate == COMPLETE) {
            imagestate = DRAWING;
            //log.info("add-cleaning-image");
            clearContainerImage();
        }
        immediateDrawPrimitive(o);
	}

	public void closeDisplay() throws RemoteException {
        //log.info("closeDisplay");

		addGDAction(new GDCloseDisplay());
	}

	public int getDeviceNumber() throws RemoteException {
		return _devNr;
	}

	public Font getGFont() throws RemoteException {
		return _gs.f;
	}

	public FontMetrics getGFontMetrics() throws RemoteException {
		return null;
	}

	synchronized public void reset() throws RemoteException {
        //log.info("reset");

        if (imagestate == DRAWING) {
            prepareAndSendImage();
        }

        imagestate         = RESET;
        datasizeestimation = isEstimationEnabled();
        graphicsdatasize   = 0;

        disposeContainerGraphics2D();
        getContainerGraphics2D();
        makeBackground(getBackground());

        addGDAction(new GDReset());

        //log.info("reset addGDAction GDReset");
	}


	public void setDeviceNumber(int dn) throws RemoteException {
		if (_devNr == -1)
			_devNr = dn;
		// addGDAction(new GDSetDeviceNumber(dn));
	}

	public void setGFont(Font f) throws RemoteException {
		_gs.f = f;
	}

	public void syncDisplay(boolean finish) throws RemoteException {
        //log.info("syncDisplay");
        imagestate = DRAWING;
	}

    private void addGDAction(GDObject object) {
        //log.info("addGDAction "+object.toString());

        synchronized (actionBufferLock) {
            gdActions.add(object);
        }

        if (objectProcessorThread != null) {
            objectProcessorThread.interrupt();
        }
    }

    //   I M A G E   A C C E S S
    //

    public byte[] getBmp() throws RemoteException {
        if (imageBuffer != null) {
            return encodeBufferedImage(imageBuffer, "bmp");
        }
        return null;
    }

    public byte[] getJpg() throws RemoteException {
        if (imageBuffer != null) {
            return encodeBufferedImage(imageBuffer, "jpg");
        }
        return null;
    }

    public byte[] getPng() throws RemoteException {
        if (imageBuffer != null) {
            return encodeBufferedImage(imageBuffer, "png");
        }
        return null;
    }

    private byte[] encodeBufferedImage(BufferedImage image, String encoder) {
        if(image != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, encoder, baos);
            } catch (IOException e) {
                throw new IllegalStateException(e.toString());
            }
            return baos.toByteArray();
        }
        return new byte[0];
    }


    //   D I S P O S E / C L E A N U P
    //

    public void dispose() {
        //log.info("dispose");
        try {
            removeAllGraphicListeners();
            Thread d = objectProcessorThread;
            objectProcessorThread = null;
            d.interrupt();
        } catch (Exception ex) {
        }
    }

    //   L I S T E N E R S
    //

    public void addGraphicListener(GDObjectListener objectListener) throws RemoteException {
        listenerList.add(objectListener);
        //log.info("addGraphicListener");
    }

    public void removeAllGraphicListeners() throws RemoteException {
        listenerList.removeAllElements();
        //log.info("removeAllGraphicListeners");
    }

    public void removeGraphicListener(GDObjectListener objectListener) throws RemoteException {
        listenerList.remove(objectListener);
        //log.info("removeGraphicListener");
    }

    //   P R O C E S S O R
    //

    public void run() {

        while(objectProcessorThread == Thread.currentThread()) {
            //log.info("objectProcessorThread");


            //log.info("GDContainerBag-"+_devNr+"-objectProcessorThread-running");
            //log.info("gdActions.size()="+gdActions.size()+" listenerList.size()="+listenerList.size());

            if (gdActions.size() > 0 && listenerList.size() > 0) {

                Vector<GDObject> result;

                synchronized (actionBufferLock) {
                    result   = gdActions;
                    gdActions = new Vector<GDObject>();
                }

                //log.info("objectProcessorThread gdActions = new Vector<GDObject>");
                //log.info("objectProcessorThread result.size() " + result.size());

                for (int i = 0; i < listenerList.size(); i++) {
                    try {
                        listenerList.elementAt(i).pushObjects(result);
                    } catch (RemoteException re) {
                        listenerList.removeElementAt(i);
                        i--;
                    }
                }
            }

            if (gdActions.size() > 0 && listenerList.size() > 0) {
                Thread.yield();
            } else {
                try {
                    Thread.sleep(10000);
                } catch (Exception ex) {
                }
            } 
        }

        //log.info("GDContainerBag-"+_devNr+"-objectProcessorThread-normal-shutdown");
        //log.info("objectProcessorThread stopped");

        objectProcessorThread = null;
    }

    class Stopwatch {

        private long start;
        private long stop;

        public Stopwatch start() {
            start = System.currentTimeMillis(); // start timing
            return this;
        }

        public void stop() {
            stop = System.currentTimeMillis(); // stop timing
        }

        public long elapsedTimeMillis() {
            return stop - start;
        }

        public String toString() {
            return "elapsedTimeMillis: " + Long.toString(elapsedTimeMillis()); // print execution time
        }
    }

}
