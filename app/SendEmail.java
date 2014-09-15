package util;

import play.*;
import org.apache.commons.mail.*;

//------------------------------------------------------------------------------
public class SendEmail
{
	//--------------------------------------------------------------------------
	public static void to(String recipient)
	{    
		try {
			Email email = new SimpleEmail();
			email.setHostName("smtpauth.wiscmail.wisc.edu");
			email.setSmtpPort(25);
			email.setAuthenticator(new DefaultAuthenticator("dss@wei.wisc.edu", "dss3m@il!!"));
			email.setSSLOnConnect(true);
			email.setFrom("dss@wei.wisc.edu");
			email.setSubject("DSS SmartScape User Verification");
			email.setMsg("This is a test mail ... :-)");
			email.addTo(recipient);
			email.send();
		}
		catch(Exception e) {
			e.toString();
		}
	}
}
