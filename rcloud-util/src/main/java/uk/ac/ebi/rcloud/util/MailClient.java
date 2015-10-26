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
package uk.ac.ebi.rcloud.util;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: andrew
 * Date: Jun 19, 2009
 * Time: 3:40:54 PM
 * To change this template use File | Settings | File Templates.
 */

public class MailClient {

	private boolean    _debug = false;
	private Properties _props = null;

	private void initprops() { _props = System.getProperties(); }

	public MailClient()              { initprops(); _props.put( "mail.smtp.host", "smtp.ebi.ac.uk" ); }
	public MailClient(String server) { initprops(); _props.put( "mail.smtp.host", server ); }


	public void sendMail(String from, String to[], String subject, String body, String[] attachments) throws Exception {
		int i;

		// get a mail session
		Session session = Session.getDefaultInstance( _props, null );
		session.setDebug(_debug);

		// define a new message
		Message msg = new MimeMessage( session );
		msg.setFrom(new InternetAddress( from ));

		InternetAddress[] addressTo = new InternetAddress[to.length];
		for(i = 0; i < to.length; i++) {
			addressTo[i] = new InternetAddress( to[i] );
		}
		msg.addRecipients( Message.RecipientType.TO, addressTo );

		// msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		msg.setSubject( subject );

		// create a message body part
		MimeBodyPart msgBody = new MimeBodyPart();

		//msgBody.setText(body);
		msgBody.setContent( body, "text/html" );

		// multipart for attachments (just in case)
		Multipart mp = new MimeMultipart();

		// add the body part
		mp.addBodyPart( msgBody );


		for(i = 0; i < attachments.length; i++) {
            
			String fname = attachments[i];
			MimeBodyPart attBodyPart = new MimeBodyPart();

			//use a JAF FileDataSource as it does MIME type detection
			DataSource source = new FileDataSource( fname );
			attBodyPart.setDataHandler(new DataHandler( source ));

			// set the name of the file
			attBodyPart.setFileName( source.getName() );

			// add the attachment
			mp.addBodyPart( attBodyPart );
		}

		msg.setContent( mp );

		Transport.send( msg );

	}
}

