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
package workbench.report;

import uk.ac.ebi.rcloud.common.components.JOptionPaneExt;
import workbench.actions.wb.SendBugReportAction;
import workbench.runtime.RuntimeEnvironment;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: 26/07/2011
 * Time: 12:14
 * To change this template use File | Settings | File Templates.
 */
public class BugReport {


    public static boolean reportAProblem (Component c, String message) {
        return reportAProblem(c, "Problem", message);
    }

    public static boolean reportAProblem (Component c, String title, String message) {
        String[] options = { "Report This", "Cancel" };

        int reply = JOptionPaneExt.showOptionDialog(c,
                message,
                title,
                JOptionPaneExt.DEFAULT_OPTION,
                JOptionPaneExt.QUESTION_MESSAGE,
                null, options, options[0]);

        return (reply == 0);

    }

    public static String stackTraceToString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer().toString();
    }

    public static void showReportDialog(Component c, RuntimeEnvironment runtime, String title, String message) {
        SendBugReportAction.showReportDialog(c, runtime, title, message);
    }

}
