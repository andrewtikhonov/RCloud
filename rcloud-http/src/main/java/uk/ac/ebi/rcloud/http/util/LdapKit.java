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
package uk.ac.ebi.rcloud.http.util;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by andrew on 12/08/15.
 */
public class LdapKit {

    private static final String LDAP_URL                = "ldap.auth.url";
    private static final String LDAP_BASE               = "ldap.auth.base";

    private static final String ldapBase = readPropertyWithDefault(LDAP_BASE, "dc=emif,dc=ebi,dc=dev");
    private static final String ldapUrl  = readPropertyWithDefault(LDAP_URL, "ldap://193.62.52.17:40389");

    public static String readPropertyWithDefault(String name, String defaultValue) {
        String propValue = System.getProperty(name);
        return propValue == null || "".equals(propValue) ? (defaultValue) : propValue;
    }

    /*
    public static void main(String[] args) throws NamingException {
        TraditionalPersonDaoImpl i = new TraditionalPersonDaoImpl();
        //List l = i.getAllPersonNames();

        List l = i.getPersonByUid("andrew22");

        for (Object o : l) {
            System.out.println(o.toString());
        }

        boolean rcloud_svc_authenticated = i.authenticateUserAccount("rcloud-svc", "rV2F7&dT");
        boolean rcloud_user_authenticated = i.authenticateUserAccount("rcloud-user", "D&8uxFJn");

        System.out.println("rcloud_svc_authenticated  = " + rcloud_svc_authenticated);
        System.out.println("rcloud_user_authenticated = " + rcloud_user_authenticated);
    }
    */

    public static class TraditionalPersonDaoImpl {

        private static final String ldapUsername = "admin";
        private static final String ldapPassword = "ldapPassword";

        public Hashtable initAdminnvironment() {
            return initEnvironment(ldapUsername, ldapPassword, true);
        }

        public Hashtable initEnvironment(String username, String password) {
            return initEnvironment(username, password, false);
        }

        public Hashtable initEnvironment(String username, String password, boolean admin) {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY,  "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL,             ldapUrl + "/" + ldapBase);


            env.put(Context.SECURITY_AUTHENTICATION, "simple");

            if(username != null) {
                if (admin) {
                    env.put(Context.SECURITY_PRINCIPAL, "cn=" + username + "," + ldapBase);
                } else {
                    env.put(Context.SECURITY_PRINCIPAL, "uid=" + username + ",ou=People," + ldapBase);
                }
            }
            if(password != null) {
                env.put(Context.SECURITY_CREDENTIALS, password);
            }

            return env;
        }


        public List<Attributes> getPersonByUid(String uid) {

            DirContext ctx;
            try {
                ctx = new InitialDirContext(initAdminnvironment());
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }

            LinkedList list = new LinkedList();
            NamingEnumeration results = null;
            try {
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                results = ctx.search("", "(&(objectclass=inetOrgPerson)(uid=" + uid + "))", controls);

                while (results.hasMore()) {
                    SearchResult searchResult = (SearchResult) results.next();
                    Attributes attributes = searchResult.getAttributes();
                    list.add(attributes);
                }

            } catch (NameNotFoundException e) {
                // The base context was not found.
                // Just clean up and exit.
            } catch (NamingException e) {
                throw new RuntimeException(e);
            } finally {
                if (results != null) {
                    try {
                        results.close();
                    } catch (Exception e) {
                        // Never mind this.
                    }
                }
                if (ctx != null) {
                    try {
                        ctx.close();
                    } catch (Exception e) {
                        // Never mind this.
                    }
                }
            }
            return list;
        }

        public boolean authenticateUserAccount(String username, String password) {
            DirContext ctx;
            try {
                ctx = new InitialDirContext(initEnvironment(username, password));
            } catch (NamingException e) {
                return false;
            }

            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
                return true;
            } else {
                return false;
            }
        }
    }

}
