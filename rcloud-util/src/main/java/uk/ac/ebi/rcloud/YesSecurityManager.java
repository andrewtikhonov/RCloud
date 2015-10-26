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
package uk.ac.ebi.rcloud;

import java.security.Permission;
import java.io.FileDescriptor;
import java.net.InetAddress;

/**
 * Created by IntelliJ IDEA.
 * User: ostolop
 * Date: Jul 6, 2009
 * Time: 11:17:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class YesSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission permission) {
    }

    @Override
    public void checkPermission(Permission permission, Object o) {
    }

    @Override
    public void checkCreateClassLoader() {
    }

    @Override
    public void checkAccess(Thread thread) {
    }

    @Override
    public void checkAccess(ThreadGroup threadGroup) {
    }

    @Override
    public void checkExit(int i) {
    }

    @Override
    public void checkExec(String s) {
    }

    @Override
    public void checkLink(String s) {
    }

    @Override
    public void checkRead(FileDescriptor fileDescriptor) {
    }

    @Override
    public void checkRead(String s) {
    }

    @Override
    public void checkRead(String s, Object o) {
    }

    @Override
    public void checkWrite(FileDescriptor fileDescriptor) {
    }

    @Override
    public void checkWrite(String s) {
    }

    @Override
    public void checkDelete(String s) {
    }

    @Override
    public void checkConnect(String s, int i) {
    }

    @Override
    public void checkConnect(String s, int i, Object o) {
    }

    @Override
    public void checkListen(int i) {
    }

    @Override
    public void checkAccept(String s, int i) {
    }

    @Override
    public void checkMulticast(InetAddress inetAddress) {
    }

    @Override
    public void checkPropertiesAccess() {
    }

    @Override
    public void checkPropertyAccess(String s) {
    }

    @Override
    public boolean checkTopLevelWindow(Object o) {
        return true;
    }

    @Override
    public void checkPrintJobAccess() {
    }

    @Override
    public void checkSystemClipboardAccess() {
    }

    @Override
    public void checkAwtEventQueueAccess() {
    }

    @Override
    public void checkPackageAccess(String s) {
    }

    @Override
    public void checkPackageDefinition(String s) {
    }

    @Override
    public void checkSetFactory() {
    }

    @Override
    public void checkMemberAccess(Class<?> aClass, int i) {
    }

    @Override
    public void checkSecurityAccess(String s) {
    }
}
