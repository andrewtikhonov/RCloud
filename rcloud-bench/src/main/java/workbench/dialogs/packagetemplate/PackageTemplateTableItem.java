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
package workbench.dialogs.packagetemplate;

import uk.ac.ebi.rcloud.common.util.ImageLoader;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 04/07/2012
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
public class PackageTemplateTableItem {
    private String name ;
    private String Class ;
    private String Group ;
    private String Dim ;
    private boolean included  = false;

    private static final HashMap<String,ImageIcon> icons = icons() ;

    private static HashMap icons(){
        HashMap<String,ImageIcon> map = new HashMap( ) ;
        map.put( "character"  , new ImageIcon( ImageLoader.load("/views/images/explorer/character.png") ) );
        map.put( "data.frame" , new ImageIcon( ImageLoader.load("/views/images/explorer/data.frame.png"  ) ) );
        map.put( "factor"     , new ImageIcon( ImageLoader.load("/views/images/explorer/factor.png"      ) ) );
        map.put( "function"   , new ImageIcon(ImageLoader.load("/views/images/explorer/function.png"     ) ) );
        map.put( "integer"    , new ImageIcon( ImageLoader.load("/views/images/explorer/integer.png"     ) ) );
        map.put( "list"       , new ImageIcon( ImageLoader.load("/views/images/explorer/list.png"        ) ) );
        map.put( "logical"    , new ImageIcon( ImageLoader.load("/views/images/explorer/logical.png"     ) ) );
        map.put( "matrix"     , new ImageIcon( ImageLoader.load("/views/images/explorer/matrix.png"      ) ) );
        map.put( "numeric"    , new ImageIcon( ImageLoader.load("/views/images/explorer/numeric.png"     ) ) );
        return map ;
    }

    public PackageTemplateTableItem( String name, String Class, String group, String dim ){
        this.name  = name ;
        this.Dim   = dim ;
        this.Group = group ;
        this.Class = Class ;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    public boolean isIncluded() {
        return included;
    }

    public String getName() {
        return name;
    }

    public String getDim() {
        return Dim;
    }

    public String getRClass() {
        return Class;
    }

    public String getGroup() {
        return Group;
    }

    public ImageIcon getIcon() {
        if( icons.containsKey( Class ) ) return icons.get( Class ) ;
        return null ;
    }

    public Object getValueAt(int index) {

        if( index == 0 ) return isIncluded() ;
        if( index == 1 ) return getIcon() ;
        if( index == 2 ) return getName() ;
        if( index == 3 ) return getDim() ;
        if( index == 4 ) return getGroup() ;

        return null ;
    }

}
