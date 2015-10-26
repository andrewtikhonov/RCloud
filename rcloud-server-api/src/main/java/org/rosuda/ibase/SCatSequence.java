//
//  SCatSequence.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jun 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase;

import java.io.Serializable;

public interface SCatSequence extends NotifierInterface, Serializable {

    public void setNotifyVarOnChange(boolean noc);

    public Object getOwner();

    public int size();

    /** returns the category ID at position pos */
    public int catAtPos(int id);

    /** returns the position of the category id */
    public int posOfCat(int id);

    public void reset();

    public boolean swapCatsAtPositions(int p1, int p2);

    public boolean swapCats(int c1, int c2);

    public boolean moveCatAtPosTo(int p1, int p2);

    public String toString();

}
