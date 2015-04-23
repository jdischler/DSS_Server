package util;

import play.*;
import org.apache.commons.mail.*;

//------------------------------------------------------------------------------
public class SendEmail
{
	//--------------------------------------------------------------------------
	public static void to(String recipient, String subject, String txtMsg, String htmlMsg)
	{    
		try {
			HtmlEmail email = new HtmlEmail();
			// Handy option for tracking down mailer problems...
			email.setDebug(true);
			email.setHostName("smtp.glbrc.org");
			email.setSmtpPort(25);
			// NOTE: Use Authenticator and SSL if needed. Our mailer server hides behind the firewall
			//	on the same network as the DSS so authentication is not needed.
		//	email.setAuthenticator(new DefaultAuthenticator("dss@wei.wisc.edu", "dss3m@il!!"));
		//	email.setSSLOnConnect(true);
			email.setFrom("dss@wei.wisc.edu", "No Reply");
			email.setSubject(subject);
			email.setTextMsg(txtMsg);
			email.setHtmlMsg(htmlMsg);
			email.addTo(recipient);
			email.send();
		}
		catch(Exception e) {
			Logger.error(e.toString());
		}
		finally {
			Logger.debug(txtMsg);
		}
	}

	//--------------------------------------------------------------------------
	public static void createValidation(String email, String validationCode) {
	
		String txtMsg = "Your registration at dss.wei.wisc.edu is ready! " + 
			"Please click on the following link (or copy and paste into a browser) to complete registration: " +
			"https://dss.wei.wisc.edu/validate?validationID=" + validationCode + " " +
			"This link will be valid for one hour. " +
			"Note: If this email was sent to you in error, please ignore and the validation will expire with no association to your email.";
			
		String htmlMsg = "<html><h3>Your registration at dss.wei.wisc.edu is ready!</h3>" + 
			"Please click on the following link (or copy and paste into a browser) to complete your registration: " +
			"<a href='https://dss.wei.wisc.edu/validate?validationID=" + validationCode + "'>" + 
			"https://dss.wei.wisc.edu/validate?validationID=" + validationCode + "</a><br /><br />" +
			"This link will be valid for <u>one</u> hour.<br /><br />" + 
			"<i>Note: If this email was sent to you in error, please ignore it and the validation will expire with no association to your email.</i></html>";
			
		SendEmail.to(email, "DSS SmartScape User Verification", txtMsg, htmlMsg);
	}
	
	//--------------------------------------------------------------------------
	public static void createPasswordReset(String email, String validationCode) {
	
		String txtMsg = "We've recieved a request at dss.wei.wisc.edu to change your password! " +
			"Please copy and paste the following reset code and enter it in SmartScape to finish " +
			"your password change: " + validationCode + " This code will be valid for one hour. " +
			"Note: If this email was sent to you in error, please ignore it and the password change request will expire.";
			
		String htmlMsg = "<html><h3>We've recieved a request at dss.wei.wisc.edu to change your password!</h3>" +
			"Please copy and paste the following reset code (minus any surrounding spaces) " + 
			"and enter it in SmartScape to finish your password change: " +
			"<b>" + validationCode + "</b><br /><br />" + 
			"This code will be valid for <u>one</u> hour.<br /><br />" +
			"<i>Note: If this email was sent to you in error, please ignore it and the password change request will automatically expire.</i></html>";
			
		SendEmail.to(email, "DSS SmartScape Request Password Change", txtMsg, htmlMsg);
	}
	
	//--------------------------------------------------------------------------
	public static void createPasswordChangeConfirmation(String email) {
	
		String txtMsg = "Your password at dss.wei.wisc.edu has been changed!"; 
		String htmlMsg = "<html><h3>Your password at dss.wei.wisc.edu has been changed!</h3></html>";
		SendEmail.to(email, "DSS SmartScape Password Change Confirmation", txtMsg, htmlMsg);
	}
}

