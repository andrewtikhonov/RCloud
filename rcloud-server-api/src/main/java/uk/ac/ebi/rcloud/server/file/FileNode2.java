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
package uk.ac.ebi.rcloud.server.file;

import uk.ac.ebi.rcloud.server.file.FileNode;

import java.io.Serializable;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Sep 4, 2009
 * Time: 11:25:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileNode2 implements Serializable {

    private static final long serialVersionUID = 1L;

    public  int     ec = 0;
    private int     s = 0;
    private String  p;
    private long    l;
    private long    m;
    private boolean d;

    private Object[] k = null;
    private Object   a = null;

    //private Vector<Object>  k = new Vector<Object>();

    public FileNode2(File file) {
        p = file.getPath();
        l = file.length();
        m = file.lastModified();
        d = file.isDirectory();

        //log.info("FileNode2-p="+p+" n="+getName()+" l="+l+" m="+m+" d="+d);
    }

    public FileNode2(File file, FileNode2 ancestor) {
        p = file.getName();
        l = file.length();
        m = file.lastModified();
        d = file.isDirectory();
        a = ancestor;

        //log.info("FileNode2-p="+p+" n="+getName()+" l="+l+" m="+m+" d="+d);
    }

    public Object[] getChildren() {
        //return k != null ? k : new Object[0];
        return k;
    }

    /*
    public Vector<Object> getChildren() {
        return k;
    }
    */

    public void addChildrenNodes(File[] list) {
        //log.info("FileNode2-addChildrenNodes");

        if (list.length > 0) {
            //k = new Object[list.length];
            for(int i=0;i<list.length;i++) {
                //k[i] = new FileNode2(list[i], this);
                add(new FileNode2(list[i], this));
            }
        }
    }

    /*

    public void addChildrenNodes(File[] list) {
        //k = new Object[list.length];
        for(int i=0;i<list.length;i++) {
            k.add(new FileNode2(list[i], this));
        }
    }
    */


    public String toString() {
        return getName();
    }

    public String getName() {
        if (a != null) {
            return p;
        } else {
            String path = getPath();
            return path.substring(path.lastIndexOf(FileNode.separator) + 1);
        }
    }

    public String getPath() {
        if (a != null) {
            return ((FileNode2)a).getPath() + FileNode.separator + p;
        } else {
            return p;
        }
    }

    public void setPath(String p) {
        this.p = p;
    }

    public long length() {
        return l;
    }

    public void setLength(long l) {
        this.l = l;
    }

    public long lastModified() {
        return m;
    }

    public void setLastModified(long m) {
        this.m = m;
    }

    public boolean isDirectory() {
        return d;
    }


    public void setIsDirectory(boolean d) {
        this.d = d;
    }

    public int getSQN() {
        return s;
    }

    public void setSQN(int sqn) {
        this.s = sqn;
    }

    public boolean isSameFile(FileNode2 node) {
        return (this.getPath().equals(node.getPath()));
    }

    public boolean isUpToDate(FileNode2 node) {
        return (this.length()       == node.length() &&
                this.lastModified() == node.lastModified());
    }

    public void update(FileNode2 node) {
        this.setLength(node.length());
        this.setLastModified(node.lastModified());
    }

    public void add(int index, Object element) {
        insertElementAt(element, index);
    }

    public synchronized boolean add(Object obj) {
        ensureCapacityHelper(ec + 1);
        k[ec++] = obj;
        return true;
    }
    
    public synchronized void insertElementAt(Object obj, int index) {
        if (index > ec) {
            throw new ArrayIndexOutOfBoundsException(index
                    + " > " + ec);
        }

        ensureCapacityHelper(ec + 1);
        System.arraycopy(k, index, k, index + 1, ec - index);
        k[index] = obj;
        ec++;
    }

    public synchronized void removeElementAt(int index) {
        if (index >= ec) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + ec);
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = ec - index - 1;
        if (j > 0) {
            System.arraycopy(k, index + 1, k, index, j);
        }
        ec--;
        k[ec] = null;
    }

    // capacity helper
    private void ensureCapacityHelper(int minCapacity) {
        if (k == null) {
            k = new Object[minCapacity];
        } else {
            int capacityIncrement = 0;
            int oldCapacity = k.length;
            if (minCapacity > oldCapacity) {
                Object[] oldData = k;
                int newCapacity = (capacityIncrement > 0) ?
                   (oldCapacity + capacityIncrement) : (oldCapacity * 2);
                if (newCapacity < minCapacity) {
                     newCapacity = minCapacity;
                }
                k = copyOf(k, newCapacity);
            }
        }
    }

    // aux Arrays
    public Object[] copyOf(Object[] original, int newLength) {
        if (newLength < 0)
            throw new NegativeArraySizeException("The array size is negative.");
        return copyOfRange(original, 0, newLength);
    }

    /*
    public <T> T[] copyOfRange(T[] original, int from, int to) {
        if (from > to)
            throw new IllegalArgumentException("The initial index is after " +
                           "the final index.");
        T[] newArray = (T[]) new Object[to - from];
        if (to > original.length) {
            System.arraycopy(original, from, newArray, 0,
                     original.length - from);
            fill(newArray, original.length, newArray.length, null);
        } else
            System.arraycopy(original, from, newArray, 0, to - from);
        return newArray;
    }
    */

    public Object[] copyOfRange(Object[] original, int from, int to) {
        if (from > to)
            throw new IllegalArgumentException("The initial index is after " +
                           "the final index.");
        Object[] newArray = (Object[]) new Object[to - from];
        if (to > original.length) {
            System.arraycopy(original, from, newArray, 0,
                     original.length - from);
            fill(newArray, original.length, newArray.length, null);
        } else
            System.arraycopy(original, from, newArray, 0, to - from);
        return newArray;
    }

    public static void fill(Object[] a, int fromIndex, int toIndex, Object val) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException();
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = val;
    }



    /*
    public synchronized void addElement(Object obj) {
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;
    }

    public synchronized void removeAllElements() {
        // Let gc do its work
        for (int i = 0; i < k.length; i++) {
            k[i] = null;
        }
        k = null;
    }

    public synchronized Object[] toArray() {
        return Arrays.copyOf(elementData, elementCount);
    }

    */

}
