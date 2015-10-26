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
package workbench.completion;

import uk.ac.ebi.rcloud.common.util.ImageLoader;

import javax.swing.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Feb 26, 2010
 * Time: 12:01:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompletionItem {
    public String value = null;
    public String valueClass = null;

    public CompletionItem(){
    }

    public CompletionItem(String value, String valueClass){
        this.value = value;
        this.valueClass = valueClass;
    }

    public String getValue() {
        return value;
    }

    public String getValueClass() {
        return valueClass;
    }


    public void setValue(String value) {
        this.value = value;
    }

    public void setValueClass(String valueClass) {
        this.valueClass = valueClass;
    }

    public ImageIcon getIcon() {
        if(iconMap.containsKey(valueClass)) return iconMap.get(valueClass);
        return null ;
    }

    public String getSuffix() {
        if(suffixMap.containsKey(valueClass)) return suffixMap.get(valueClass);
        return "";
    }

    private static HashMap<String, ImageIcon> iconMap = createIconMap();
    private static HashMap<String, String> suffixMap = createSuffixMap();

    private static HashMap<String, ImageIcon> createIconMap(){

        HashMap<String, ImageIcon> map = new HashMap<String, ImageIcon>() ;

        map.put("character", new ImageIcon( ImageLoader.load("/views/images/completion/character.png")));
        map.put("data.frame", new ImageIcon( ImageLoader.load("/views/images/completion/data.frame.png")));
        map.put("factor", new ImageIcon( ImageLoader.load("/views/images/completion/factor.png")));
        map.put("function", new ImageIcon(ImageLoader.load("/views/images/completion/function.png")));
        map.put("integer", new ImageIcon( ImageLoader.load("/views/images/completion/integer.png")));
        map.put("list", new ImageIcon( ImageLoader.load("/views/images/completion/list.png")));
        map.put("logical", new ImageIcon( ImageLoader.load("/views/images/completion/logical.png")));
        map.put("matrix", new ImageIcon( ImageLoader.load("/views/images/completion/matrix.png")));
        map.put("numeric", new ImageIcon( ImageLoader.load("/views/images/completion/numeric.png")));
        //map.put("command", new ImageIcon( ImageLoader.load("/views/images/completion/media-playback-start.png")));

        return map ;
    }

    private static HashMap<String, String> createSuffixMap(){

        HashMap<String, String> map = new HashMap<String, String>() ;

        map.put("character", "");
        map.put("data.frame", "");
        map.put("factor", "");
        map.put("function", "()");
        map.put("integer", "");
        map.put("list", "[]");
        map.put("logical", "");
        map.put("matrix", "[]");
        map.put("numeric", "");

        return map ;
    }
}
