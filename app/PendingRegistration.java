package util;

import javax.persistence.*;
import play.db.ebean.Model;
import com.avaje.ebean.Ebean;
import java.util.Date;


@Entity
public class PendingRegistration {
	@Id
	public String email;
	public String password;
	public String organization;
	public String validationCode;
	public String password_salt;
    Date create_time;
    
	public PendingRegistration(String email, String password, String organization, String validationCode, String salt){
		this.email = email;
		this.password = password;
		this.organization = organization;
		this.validationCode = validationCode;
		this.password_salt = salt; 
		create_time = new Date();
		Ebean.save(this);
	}
	
	public static Model.Finder<Integer, PendingRegistration> find = new Model.Finder<>(Integer.class, PendingRegistration.class);
}
