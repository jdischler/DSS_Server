package util;

import play.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import com.avaje.ebean.Ebean;
import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.libs.*;
import util.Scenario;

import com.fasterxml.jackson.core.*;

import fileHandling.ScenarioLogger;

//import models.Layer_Integer;

//------------------------------------------------------------------------------
public class Global extends GlobalSettings
{
	private static final boolean LOAD_DEFAULT_DATA = true;
	
	// mostly for DEV, production servers are set up to always recompute this data to be safe...
	private static final boolean FORCE_COMPUTE_DEFAULT_DATA = false;
	
	// Timed services to do server maintenance
	ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
	ScheduledFuture scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(
		new Runnable() { 
			public void run() {
				cleanUpPendingRegistrations(1); 
			}
		}, 10, 30, TimeUnit.MINUTES);// initialDelay, period, units);

	//--------------------------------------------------------------------------
	private void cleanUpPendingRegistrations(int shelfLife) { // time in hours
		
		Date date = new Date();
		long expirationDateMS = date.getTime() - shelfLife * 30 * /*60 * */1000; // hour
		Date expDate = new Date(expirationDateMS);

		List<PendingRegistration> prList = PendingRegistration.find.where().
			lt("create_time", expDate).
				findList();
		
		if (prList.size() > 0) {
			Logger.info("+----------------------------------------------+");
			Logger.info(" Pending Registrations: Deleting expired ones");
			for(PendingRegistration pr : prList) {
				Logger.info(" > deleting one for: " + pr.email);
				Ebean.delete(pr);
			}
			Logger.info("+----------------------------------------------+");
		}
	}
			
	// TODO: computed layerData (model runs) cleanup process if needed?		
		// Process
		// 1: for each "client_*" folder
		// 2: get subfolder (0-9) list for the client_* folder
		// 3: check date on this folder and delete it if it's older than 12-ish hours old?

	//--------------------------------------------------------------------------
	@Override
	public void onStart(play.Application app) 
	{
		systemReport(app, "Application has started");
	
		// Add the yaml defined starting users if there are no current users in the db
		List<ClientUser> userList = ClientUser.find
			.findList();
		if (userList.size() <= 0) {
			Map<String,List<Object>> all = (Map<String,List<Object>>)Yaml.load("initial-data.yml");
			Ebean.save(all.get("users"));
		}

		// Create all of the assumptions the server knows about, these will be fed to clients
		GlobalAssumptions.initAssumptions();
		Scenario.initTracking();
		
		// create any computed layers (currently don't have any in here?)
		computeLayers();
		
		cacheLayers();

		ApplyManagementOptions.now(); // Takes in-memory CDL and adds management options to it...
		
		QueuedWriter.beginWatchWriteQueue();
		ScenarioLogger.beginWatchWriteQueue();
		conditionalCreateDefaultModelOutputs();		
		cacheModelDefaults();
		
		systemReport(app, "Data Layers Cached");
	}

	//--------------------------------------------------------------------------
	@Override
	public void onStop(play.Application app) {
		
		Layer_Base.removeAllLayers();
		System.gc();
		systemReport(app, "Application stopped, Garbage Collection call made");
	}
	
	//--------------------------------------------------------------------------
	private void systemReport(play.Application app, String customMessage) {
		
		play.Configuration config = app.configuration();
		String appVersionInfo = config.getString("application.name") + " v" 
				+ config.getString("application.version");
				
		float unitConversion = (1024.0f * 1024.0f); // bytes -> MB
		File apPath = Play.application().path();
		String unitName = "MB";
		
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| " + appVersionInfo);
		Logger.info("| " + customMessage);
		Logger.info("+-------------------------------------------------------+");
		Logger.info("|  Application Path: " + apPath.toString());
		Logger.info("|  Available Processors: " + 
			Integer.toString(Runtime.getRuntime().availableProcessors()));
		Logger.info("|  Total Free Memory: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().freeMemory() / unitConversion)) +
				unitName);
		Logger.info("|  Current Total Memory in Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().totalMemory() / unitConversion)) +
				unitName);
		Logger.info("|  Maximum Memory for Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().maxMemory() / unitConversion)) +
				unitName);

		Logger.info("+-------------------------------------------------------+");
		Logger.info("");
	}
	
	//--------------------------------------------------------------------------
	private void computeLayers() {
		/* // Uncomment if need to recalculate and output slope
		CalculateSlope cs = new CalculateSlope();
		cs.computeSlope();
		*/

		/* // Uncomment if need to recalculate and output crop rotation
		CropRotation cr = new CropRotation();
		cr.computeRotation();
		*/
		
		/* new CalculateCornGrassProduction().run();
		*/
	}
	
	//--------------------------------------------------------------------------
	private void cacheLayers() 
	{
		PerformanceTimer timer = new PerformanceTimer();
		Layer_Base layer;
		try {
			Logger.info(" ... Server starting layer load ... ");
			
			// Queryable layers...though some of these are also used by model computations..
			layer = new Layer_ProceduralFraction(); layer.init();// really has no data...init may not also be needed?
			layer = new Layer_Integer("cdl_2012"); layer.init();
			layer = new Layer_Float("slope"); layer.init();
			layer = new Layer_Float("rivers"); layer.init();
			layer = new Layer_Integer("watersheds", Layer_Integer.EType.ERaw); layer.init();
			layer = new Layer_Integer("lcc"); layer.init();
			layer = new Layer_Integer("lcs"); layer.init();
			layer = new Layer_Float("dairy"); layer.init();
			layer = new Layer_Float("public_land"); layer.init();
			
			// Layers for model computation
			layer = new Layer_Float("cec"); layer.init();
			layer = new Layer_Float("depth"); layer.init();
			layer = new Layer_Float("silt"); layer.init();
			layer = new Layer_Float("soc"); layer.init();
			/*layer = new Layer_Float("texture"); layer.init();
			layer = new Layer_Float("om_soc"); layer.init();
			layer = new Layer_Float("drainage"); layer.init();
			layer = new Layer_Float("ph"); layer.init();
			*/
			// The above were converted into a single composite lookup
			layer = new Layer_Float("n2o_composite"); layer.init();
			layer = new Layer_Float("ls"); layer.init();
			layer = new Layer_Float("rainfall_erosivity"); layer.init();
			layer = new Layer_Float("soil_erodibility"); layer.init();
			
			// Epic computed data...
			layer = new Layer_Float("alfa_p"); layer.init();
			layer = new Layer_Float("corn_p"); layer.init();
			layer = new Layer_Float("soy_p"); layer.init();
			layer = new Layer_Float("grass_p"); layer.init();
			
			// Ag_Lands - RESTRICTED
		/*	layer = new Layer_Integer("ag_lands", Layer_Integer.EType.ERaw); layer.init();
			layer.setAccessRestrictions(ClientUser.getMaskForAccessOptions(ClientUser.ACCESS.AG_LANDS));
			// CRP - RESTRICTED
			layer = new Layer_Integer("crp", Layer_Integer.EType.ERaw); // don't do fancy shift/match tricks...there are only two values possible here...
			((Layer_Integer)layer).setNoDataConversion(0);// work around a data issue - conversion -9999 to zeros
			layer.init();
			layer.setAccessRestrictions(ClientUser.getMaskForAccessOptions(ClientUser.ACCESS.CRP));
*/
		}
		catch (Exception e) {
			Logger.error(e.toString());
		}
		
		Logger.debug(" -Time to cache all layers (s): " + timer.stringSeconds(2));
	}
	
	//--------------------------------------------------------------------------
	private void cacheModelDefaults() {
		
		if (LOAD_DEFAULT_DATA) {
			PerformanceTimer timer = new PerformanceTimer();
			Logger.info(" ... Server is going to load MODEL DEFAULT files ...");
			Layer_Base layer;
			try {
				layer = new Layer_Float("default/net_income"); layer.init();
				layer = new Layer_Float("default/net_energy"); layer.init();
				layer = new Layer_Float("default/ethanol"); layer.init();
				layer = new Layer_Float("default/habitat_index"); layer.init();
				//layer = new Layer_Float("default/water_quality"); layer.init();
				layer = new Layer_Float("default/p_loss_epic"); layer.init();
				layer = new Layer_Float("default/pest"); layer.init();
				layer = new Layer_Float("default/pollinator"); layer.init();
				layer = new Layer_Float("default/nitrous_oxide"); layer.init();
				layer = new Layer_Float("default/soil_loss"); layer.init();
				layer = new Layer_Float("default/soc"); layer.init();
			}
			catch (Exception e) {
				Logger.error(e.toString());
			}
			Logger.debug(" -Time to cache all model defaults (s): " + timer.stringSeconds(2));
		}
		else {
			Logger.info(" ... The Server is skipping loading MODEL DEFAULT files ...");
		}
	}

	// TODO: potentially check for individual model files vs. just the directory?
	//--------------------------------------------------------------------------
	private void conditionalCreateDefaultModelOutputs() {
		
		// Check for Default Scenario files...to replace them, you need to delete the whole
		//	DEFAULT folder otherwise they will not be recalculated with how this is coded
		//	The default folder also cannot exist for first generation (even if empty...)
		File Output = new File("./layerData/default");
		if(!Output.exists() || Play.isProd() || FORCE_COMPUTE_DEFAULT_DATA) {

			if (FORCE_COMPUTE_DEFAULT_DATA) {
				Logger.info("Forcing Defaults to be recalculated!");
			}
			else if (Play.isProd()) {
				Logger.info("Server starting in production mode - recalculating Default scenario folder!");
			}
			else {
				Logger.info("Default scenario folder does not exist, creating it and default model files!");
			}
			
			// Rotation
			Layer_Base layer = Layer_Base.getLayer("cdl_2012");
			int width = layer.getWidth();
			int height = layer.getHeight();
			
			Scenario scenario = new Scenario(null); // user can be null
			scenario.mNewRotation = layer.getIntData();
			scenario.mSelection = new Selection(width, height);
			scenario.mAssumptions = new GlobalAssumptions();
			scenario.mOutputDir = "default";
			
			List<ModelResult> results;
			results = new Model_HabitatIndex().run(scenario);
			QueuedWriter.queueResults(results);

			results = new Model_EthanolNetEnergyIncome().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_PollinatorPestSuppression().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_NitrousOxideEmissions().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_SoilCarbon().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_P_LossEpic().run(scenario);
			QueuedWriter.queueResults(results);

			results = new Model_Soil_Loss().run(scenario);
			QueuedWriter.queueResults(results);
			// NOTE: SOC for the default is not in the model run because it is not a computed data layer like others...
			
			// wait for write queue to dump out the defaults...
			while(QueuedWriter.doesWriteQueueHaveFiles()) {
				Logger.info("Waiting for defaults to be written by the QueuedWriter");
				try {
					Thread.sleep(4000);
				}
				catch(Exception e) {
					// blah, java exception handling...
				}
			}
		}
	}
	
	// converts all .DSS files in a given folder to ASC. E.g. convert...("./layerData/default");
	//-----------------------------------------------------------
	private void convertFolderBinariesToASC(String folderPath, String destPath) {

		File folder = new File(folderPath);
		if (!folder.isDirectory()) {
			Logger.info(folderPath + " is not a directory!!");
			return;
		}
		
		// get all files in that directory...
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				
				File sourceBinary = listOfFiles[i];
				// extract off just the name of that file in the directory...
				String destFile = sourceBinary.getName();
				// make sure it has a .dss it in...
				if (destFile.indexOf(".dss") > 0) {
					// make dest dir if needed
					File destDir = new File(destPath);
					if (!destDir.exists()) {
						destDir.mkdirs();
					}
					// now create the new final path....with the new .asc file extension...
					destFile = destPath + destFile.replace(".dss", ".asc");
					
					Binary_Reader fileReader = new Binary_Reader(sourceBinary);
					if (fileReader.readHeader()) {
						
						PrintWriter ascOut = null;
						int width = fileReader.getWidth(), height = fileReader.getHeight();
						try 
						{
							ascOut = new PrintWriter(new BufferedWriter(new FileWriter(destFile)));
							ascOut.println("ncols         " + Integer.toString(width));
							ascOut.println("nrows         " + Integer.toString(height));
							ascOut.println("xllcorner     -10062652.65061");
							ascOut.println("yllcorner     5249032.6922889");
							ascOut.println("cellsize      30");
							ascOut.println("NODATA_value  -9999");
						} 
						catch (Exception err) 
						{
							Logger.warn(err.toString());
						}
						
						if (ascOut != null) {	
							String stringNoData = Integer.toString(-9999);

							for (int y = 0; y < height; y++) {
								ByteBuffer buff = fileReader.readLine();
								StringBuffer ascLine = new StringBuffer();

								if (buff != null) {
									for (int x=0; x < width; x++) {
										float data = buff.getFloat(x * 4); // blah, 4 = size of float, ie 32bit
										if (data > -9999.0f) {
											ascLine.append(Float.toString(data));
										}
										else {
											ascLine.append(stringNoData);
										}
										if (x != width - 1) {
											ascLine.append(" ");
										}
									}
								}
								ascOut.println(ascLine.toString());
							}
							fileReader.close();
							try {
								ascOut.close();
							}
							catch (Exception err) {
								Logger.warn(err.toString());
							}
						}
					}
					else {
						Logger.warn("File read problem with file: " + sourceBinary.toString());
					}
				}
				Logger.info(destFile);
			}
		}
	}
}

