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

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jan 27, 2010
 * Time: 3:11:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class JPanelAlphaBase extends JPanel {
    public float alpha = 1.0f;

    public void setAlpha(float alpha){
        this.alpha = alpha;

        if (isShowing()) {
            repaint();
        }
    }

    public float getAlpha(){
        return alpha;
    }
}
