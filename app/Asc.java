package util;

import play.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

// WIP: work in progress...using the Scanner class seems to make this DOG slow, boo


// Sample usage:
//		Asc file = new Asc("./layerData/slope.asc");
//		file.load();
//
// NOTE: only reads header and attempts to (slowly) determine whether the file
//	could be read as int or float. Could maybe have the determination step be an
//	optiona and you can just instead specify a data type to load the file as?

//------------------------------------------------------------------------------
class Asc
{
	//--------------------------------------------------------------------------
	protected class AscHeader
	{
		int mWidth;
		int mHeight;
		double mCornerX;
		double mCornerY;
		double mCellSize;
		int mNoData;
		
		//----------------------------------------------------------------------
		protected void log() {
			Logger.info("  " + mWidth);
			Logger.info("  " + mHeight);
			Logger.info("  " + mCornerX);
			Logger.info("  " + mCornerY);
			Logger.info("  " + mCellSize);
			Logger.info("  " + mNoData);
		}
	}
	
	//--------------------------------------------------------------------------
	protected enum DeterminedType {
		INT,
		FLOAT
	};
	
	private AscHeader mAscHeader;
	private String mPath;
	
	//--------------------------------------------------------------------------
	public Asc(String path) {
		mAscHeader = new AscHeader();
		mPath = path;
	}

	// NOTE: only reads header and attempts to (slowly) determine whether the file
	//	could be read as int or float. Could maybe have the determination step be an
	//	optiona and you can just instead specify a data type to load the file as?
	//--------------------------------------------------------------------------
	public void load() {
		
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| " + mPath);
		Logger.info("+-------------------------------------------------------+");

		Scanner scanner = null;		
		try {
			scanner = new Scanner(
							new BufferedReader(
								new FileReader(mPath)));
			readHeader(scanner);
			DeterminedType type = determineDatatype(scanner);
			
			if (type == DeterminedType.INT) {
				Logger.info("  >> file can be read in int format");
			}
			else {
				Logger.info("  >> file must be read in float format");
			}
		}		
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
	
	//--------------------------------------------------------------------------
	private void readHeader(Scanner scanner) throws Exception {
		
		try {
			String value;
			// ncols
			if (scanner.hasNext()) {
				value = scanner.next();
				if (scanner.hasNextInt()) {
					mAscHeader.mWidth = scanner.nextInt();
				}
			}
			
			// nrows
			if (scanner.hasNext()) {
				value = scanner.next();
				if (scanner.hasNextInt()) {
					mAscHeader.mHeight = scanner.nextInt();
				}
			}
			
			// xll corner
			if (scanner.hasNext()) {
				value = scanner.next();
				if (scanner.hasNextDouble()) {
					mAscHeader.mCornerX = scanner.nextDouble();
				}
			}
			
			// yll corner
			if (scanner.hasNext()) {
				value = scanner.next();
				if (scanner.hasNextDouble()) {
					mAscHeader.mCornerY = scanner.nextDouble();
				}
			}
			
			// cell size
			if (scanner.hasNext()) {
				value = scanner.next();
				if (scanner.hasNextDouble()) {
					mAscHeader.mCellSize = scanner.nextDouble();
				}
			}

			// No data value
			if (scanner.hasNext()) {
				value = scanner.next();
				if (scanner.hasNextInt()) {
					mAscHeader.mNoData = scanner.nextInt();
				}
			}
			
			// Output header info to debug log
			mAscHeader.log();
			
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	private DeterminedType determineDatatype(Scanner scanner) {
		
		DeterminedType type = DeterminedType.INT;
		
		Pattern decimal = Pattern.compile("^-?(?:[0-9]+|[0-9]*\\.[0-9]+)$");
		
		while (scanner.hasNext()) {
			if (scanner.hasNext(decimal)) {
				return DeterminedType.FLOAT;
			}
			
			scanner.next();
		}
		
		return type;
	}
}

