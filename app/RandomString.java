package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//------------------------------------------------------------------------------
public class RandomString {

	public static final String get(int numCharacters) {
	
		StringBuilder result = new StringBuilder(numCharacters);
		Random rand = new Random();
		for (int i=0; i < numCharacters; i++) {	
			int value = rand.nextInt(26) + 65; // 26 characters, A-Z, A starts at 65 ASCII
			result.append((char)value);
		}
		
		return result.toString();
	}
}

