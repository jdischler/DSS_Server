package util;

import play.*;
import org.apache.commons.mail.*;

//------------------------------------------------------------------------------
public class SendEmail
{
	//--------------------------------------------------------------------------
	public static void to(String recipient, String subject, String message)
	{    
		try {
			Email email = new SimpleEmail();
			email.setHostName("smtpauth.wiscmail.wisc.edu");
			email.setSmtpPort(25);
			email.setAuthenticator(new DefaultAuthenticator("dss@wei.wisc.edu", "dss3m@il!!"));
			email.setSSLOnConnect(true);
			email.setFrom("dss@wei.wisc.edu");
			email.setSubject(subject);
			email.setMsg(message);
			email.addTo(recipient);
			email.send();
		}
		catch(Exception e) {
			Logger.error(e.toString());
		}
		finally {
			Logger.info(message);
		}
	}

	//--------------------------------------------------------------------------
	public static void createValidation(String email, String validationCode) {
	
		String msg = "Your registration at dss.wei.wisc.edu is ready! Please click on the following link (or copy and paste into a browser) to complete registration: https:dss.wei.wisc.edu/validate?validationID=" + validationCode + " This link will be valid for one hour. If this email was sent to you in error, please ignore and the validation will expire with no association to your email.";
		SendEmail.to(email, "DSS SmartScape User Verification", msg);
	}
	
	//--------------------------------------------------------------------------
	public static void createPasswordReset(String email, String validationCode) {
	
		String msg = "We've recieved a request at dss.wei.wisc.edu to change your password! Please copy and paste the following reset code and enter it in SmartScape to finish your password change: " + validationCode + " This code will be valid for one hour. If this email was sent to you in error, please ignore and the password change request will expire."; 
		SendEmail.to(email, "DSS SmartScape Request Password Change", msg);
	}
	
	//--------------------------------------------------------------------------
	public static void createPasswordChangeConfirmation(String email) {
	
		String msg = "Your password at dss.wei.wisc.edu has been changed!"; 
		SendEmail.to(email, "DSS SmartScape Password Change Confirmation", msg);
	}
}

