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

import java.io.Serializable;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jul 27, 2009
 * Time: 3:52:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileNode implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final char separatorChar = '/';
    public static final String separator = "" + separatorChar;

    public  int     ec = 0;
    private int     s = 0;
    private String  n;
    private long    l;
    private long    m;
    private boolean d;

    private Object[] k = null;
    private Object   a = null;

    public FileNode(File file) {
        n = file.getPath();
        l = file.length();
        m = file.lastModified();
        d = file.isDirectory();
    }

    public FileNode(String name, long length, long modified, boolean directory) {
        n = name;
        l = length;
        m = modified;
        d = directory;
    }

    public FileNode(File file, FileNode ancestor) {
        n = file.getName();
        l = file.length();
        m = file.lastModified();
        d = file.isDirectory();
        a = ancestor;
    }

    public Object[] getChildren() {
        return k;
    }

    public void setChildrenNodes(File[] list) {
        k = new Object[list.length];

        if (list.length > 0) {
            for(int i=0;i<list.length;i++) {
                k[i] = new FileNode(list[i], this);
            }
            ec = list.length;
        }
    }

    public void setChildren(Object[] children, int childcount) {
        k = new Object[childcount];

        if (children.length > 0) {
            for(int i=0;i<children.length;i++) {
                k[i] = children[i];
                ((FileNode) k[i]).setAncestor(this);
            }
        }

        ec = childcount;
    }

    public String toString() {
        return getName();
    }

    /* getters
    */

    public String getName() {
        if (a != null) {
            return n;
        } else {
            return n.substring(n.lastIndexOf(FileNode.separator) + 1);
        }
    }
    
    public String getPath() {
        if (a != null) {
            return ((FileNode)a).getPath() + FileNode.separator + n;
        } else {
            return n;
        }
    }

    public long length() {
        return l;
    }

    public long lastModified() {
        return m;
    }

    public boolean isDirectory() {
        return d;
    }

    public Object getAncestor() {
        return a;
    }

    /* setters
    */
    public void setLength(long l) {
        this.l = l;
    }

    public void setLastModified(long m) {
        this.m = m;
    }

    public void setIsDirectory(boolean d) {
        this.d = d;
    }

    public void setAncestor(FileNode ancestor) {
        this.a = ancestor;
    }

    /* sqn related stuff
    */

    public int getSQN() {
        return s;
    }

    public void setSQN(int sqn) {
        this.s = sqn;
    }

    /* utiliuty
    */

    public boolean isSameFile(FileNode node) {
        return (this.getPath().equals(node.getPath()));
    }

    public boolean isUpToDate(FileNode node) {
        return (this.length()       == node.length() &&
                this.lastModified() == node.lastModified());
    }

    public void update(FileNode node) {
        this.setLength(node.length());
        this.setLastModified(node.lastModified());
    }

    /* children interface
    */
    public Object getChild(int index) {
        return k[index];
    }

    public void addChildAt(int index, Object element) {
        insertElementAt(element, index);
    }

    public synchronized void addChild(Object element) {
        ensureCapacityHelper(ec + 1);
        k[ec++] = element;
    }

    public synchronized void removeChildAt(int index) {
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
    

    public int indexOfChild(Object o) {
        return indexOf(o, 0);
    }

    public int getChildCount() {
        return ec;
    }

    // private
    private synchronized boolean add(Object obj) {
        ensureCapacityHelper(ec + 1);
        k[ec++] = obj;
        return true;
    }

    private synchronized void insertElementAt(Object obj, int index) {
        if (index > ec) {
            throw new ArrayIndexOutOfBoundsException(index + " > " + ec);
        }
        ensureCapacityHelper(ec + 1);
        System.arraycopy(k, index, k, index + 1, ec - index);
        k[index] = obj;
        ec++;
    }

    private synchronized int indexOf(Object o, int index) {
        if (o == null) {
            for (int i = index ; i < ec ; i++)
                if (k[i]==null)
                    return i;
        } else {
            for (int i = index ; i < ec ; i++)
                if (o.equals(k[i]))
                    return i;
        }
        return -1;
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

}
