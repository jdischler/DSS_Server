package util;

import play.*;
import java.sql.Timestamp;
import javax.persistence.*;
import com.avaje.ebean.Ebean;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="account")
public class ClientUser {
	@Id
	public String email;
	public String password;
	public String organization;
	public String passwordSalt;
	public boolean admin;
	public int accessFlags;
	
	// Requires Java 7 for binary literals...Please use ALL CAPS for names to reduce chance of
	//	grid panel data collisions with the client admin panel. :/
	public enum ACCESS {
		CRP			(0b00001),
		AG_LANDS	(0b00010);
		
		public int value;
		private ACCESS(int value) { this.value = value; }
		public boolean matches(int testValue) {
			return (this.value & testValue) > 0;
		}
		public int addOption(int currentMask) {
			return currentMask | this.value;
		}
		
		public static ACCESS getEnumForString(String enumName) {
			for (ClientUser.ACCESS e : ClientUser.ACCESS.values()) {
				if (e.name().equals(enumName)) {
					return e;
				}
			}
			return null;
		}
	};
	
	public static int getMaskForAccessOptions(ACCESS ... options) {
		int mask = 0;
		for (int i = 0; i < options.length; i++) {
			mask = options[i].addOption(mask);
		}
		return mask;
	}
	
	public ClientUser(String email, String organization, String password, String pwdSalt){
		this.email = email;
		this.organization = organization;
		this.password = password;
		this.passwordSalt = pwdSalt;
		this.admin = false;
		this.accessFlags = 0;
		Ebean.save(this);
	}
	
	public static Finder<String,ClientUser> find = new Finder<String, ClientUser>(
			String.class, ClientUser.class
			);
	
	public static ClientUser authenticate(String email, String password) {
		return find.where().eq("email",  email)
				.eq("password", password).findUnique();
	}
	
	public int getAccessFlags() {
		 return this.accessFlags; 
	}
	
	public void updateAccessFlags(int accessFlags) {
		Logger.info("Updating access settings for user: " + email);
		this.accessFlags = accessFlags;
		Ebean.save(this);
	}
}
