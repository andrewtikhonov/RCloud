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

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Oct 12, 2009
 * Time: 4:32:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CompletionInterface {
    // provide completion result
    public CompletionResult provideResult();

    // provide completion pattern
    public String providePattern();

    // accept
    public void acceptResult(String s, int offset);

    // partially complete
    public void makeAddition(String s);

    // check whether completion can be invoked
    public boolean canShow();

    // callback handlers
    public void handlePopupShowed();
    public void handlePopupClosed();
}
