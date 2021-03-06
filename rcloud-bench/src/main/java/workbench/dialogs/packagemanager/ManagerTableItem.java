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
package workbench.dialogs.packagemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Nov 12, 2009
 * Time: 4:38:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManagerTableItem implements Cloneable {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private boolean loaded;
    private String name;
    private String description;
    private ManagerTableItemNotifier notifier;

    public ManagerTableItem(boolean loaded, String name, String description,
                            ManagerTableItemNotifier notifier) {

        this.loaded = loaded;
        this.name = name;
        this.description = description;
        this.notifier = notifier;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        try {
            ManagerTableItem before = (ManagerTableItem) this.clone();
            this.loaded = loaded;
            notifyItemUpdated(before, this);
        } catch (CloneNotSupportedException ex) {
            log.error("Error!", ex);
        }
    }

    public void setNotifier(ManagerTableItemNotifier notifier) {
        this.notifier = notifier;
    }

    private void notifyItemUpdated(ManagerTableItem before, ManagerTableItem after) {
        notifier.itemUpdated(before, after);
    }

}
