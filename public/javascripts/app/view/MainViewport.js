// * File: app/view/MainViewport.js

var globalMap;
var DSS_ImgFormat = 'image/png';
var DSS_bufferSize = 2; // how many non-visible tiles on either side of visible area to cache?
var DSS_resizeMethod = 'null';//null; // can be: null, "resize", or ”map-resize”

// The pgis123 variants are all mapped to pgis at the server level. From the client POV,
//	they appear as different URLs which allows the client to make multiple simultaneous
//	requests and potentially get results back faster.
var DSS_GeoServerURLS = [
	'http://pgis.glbrc.org'];/*,
	'pgis1.wei.wisc.edu:8080',
	'pgis2.wei.wisc.edu:8080',
	'pgis3.wei.wisc.edu:8080'];*/

var DSS_VectorPath = '/geoserver/Vector/wms';
var DSS_RasterPath = '/geoserver/Raster/wms';

// boo
var DSS_globalQueryableLayers = [];
var DSS_globalCollapsibleLayers = [];
var DSS_AssumptionsDefaults = null; // DON'T modify these - they come from the server...
var DSS_AssumptionsAdjustable = null; // A copy!
var DSS_currentModelRunID = 0;//

var DSS_LogoPanelHeight = 70;

var G_ClickControl = {};

//------------------------------------------------------------------------------
Ext.define('MyApp.view.MainViewport', {
//------------------------------------------------------------------------------

	extend: 'Ext.container.Viewport',
	requires: [
		'MyApp.view.InfoToolbar',
		'GeoExt.panel.Map',
		'MyApp.view.LayerPanel_Google',
        'MyApp.view.LayerPanel_Indexed',
        'MyApp.view.LayerPanel_Continuous',
        'MyApp.view.LayerPanel_Watershed',
        'MyApp.view.LogoPanel',
        'MyApp.view.ViewSelectToolbar',
		'MyApp.view.Scenario_Layout',
        'MyApp.view.Report_MasterLayout'
	],
	
	//autoScroll: true,
	layout: {
		type: 'fit'
	},
	id: 'DSS_MainViewport',
	
	listeners: {
		// Some controls need the layout to be done before being wired in...
		afterrender: function(c) {
			this.addControlsNeedingLayout();
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		
		OpenLayers.IMAGE_RELOAD_ATTEMPTS = 5;
		// make OL compute scale according to WMS spec
		OpenLayers.DOTS_PER_INCH = 25.4 / 0.28;

		var me = this;
		var projectionType = "EPSG:3857";
		var bounds = new OpenLayers.Bounds(
			-10062652.65061, 5278060.469521415,
			-9878152.65061, 5415259.640662575
		);
		function zoomEvent(event) {
			console.log('Zoom Event object...');
			console.log(event);
		}
		
		var options = {
			controls: [],
			maxExtent: bounds,
			restrictedExtent: bounds.scale(1.25),
			maxResolution: 305.74811309814453,
			projection: projectionType,
			units: 'm',
			eventListeners: { "zoomstart": zoomEvent}
		};
		
		globalMap = new OpenLayers.Map('map', options)
		var map = globalMap;

		this.doApplyIf(me, map);
		
		me.callParent(arguments);
		
		this.addGoogleLayers(map);
		this.addMapLayers(map);
		this.addMapControls(map);	
		this.getAssumptions();
		this.checkAssignClientID();
	},

	//--------------------------------------------------------------------------
	checkAssignClientID: function(tryCount) {
		
		if (!tryCount) tryCount = 0;
		
		var me = this;
		var res = Ext.util.Cookies.get('DSS_clientID');
		if (!res) {
			console.log(' No cookies for you! (but that isnt always a problem...)');
			//--------
			var obj = Ext.Ajax.request({
				url: location.href + 'getClientID',
				method: 'POST',
				timeout: 10 * 1000, // seconds * (i.e. converted to) milliseconds
				
				success: function(response, opts) {
					var obj = JSON.parse(response.responseText);
					console.log("getClientID success: ");
					console.log(obj);
					Ext.util.Cookies.set('DSS_clientID', obj.DSS_clientID);
					Ext.util.Cookies.set('DSS_nextSaveID', 0); // start off with folder zero (0)
					
					var res = Ext.util.Cookies.get('DSS_clientID');
					if (res) {
						console.log(' Cookie is: ');
						console.log(res);
					}
					else {
						console.log(' Boo, still no cookie for u!!');
						Ext.util.Cookies.set('DSS_clientID', 'Fixme_NoID');
					}
					return;
				},
				
				failure: function(respose, opts) {
					tryCount++;
					if (tryCount < 6) {
						me.CheckAssignClientID(tryCount);
					}
					else {
						alert("GetClientID call failed, request timed out?");
					}
				}
			});
		}
	},
	
	// Controls wired up to DOM elements need some manner of delay other control
	//	INIT will attempt to find the element which will not exist yet    
	//--------------------------------------------------------------------------
	addControlsNeedingLayout: function() {
		
//		globalMap.zoomToMaxExtent();
		globalMap.zoomTo(1);
		globalMap.pan(1,1); // FIXME: lame workaround for google map zoom level not starting out correctly?
	},
	
	//--------------------------------------------------------------------------
	addMapControls: function(map) {
		
		map.addControl(new OpenLayers.Control.Navigation({
			documentDrag: true, 
			dragPanOptions: {
				enableKinetic: true,
				kineticInterval: 500
			}
		}));
		
		map.addControl(new OpenLayers.Control.Zoom());
		map.addControl(new OpenLayers.Control.ArgParser());
		
		var scaleLine = new OpenLayers.Control.ScaleLine({maxWidth: 200,         					
			lineSymbolizer: {
				strokeColor: "#bbb",
				strokeWidth: 1,
				strokeOpacity: 0.7
			},
			labelSymbolizer: {
				fontColor: '#bbb',
				labelXOffset: "0",
				labelYOffset: "2",
				labelOutlineColor: "black",
				labelOutlineWidth: 4
			}
		}); 
		map.addControl(scaleLine);
	},

	// endpath would typically be DSS_RasterPath or DSS_VectorPath ( <- note, those are global vars)	
	//--------------------------------------------------------------------------
	getGeoserverURLs: function(endpath) {
		
		// create a copy of the URL array...and append to that...
		var urls = DSS_GeoServerURLS.slice(0);
		for (var index = 0; index < urls.length; ++index) {
			urls[index] += endpath;
		}

		return urls;
	},

	//--------------------------------------------------------------------------
	getLayerSettings: function(map, layerName) {

		var	res = {
			layers: layerName,
			format: DSS_ImgFormat,
			transparent: true,
			tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
		};
		
		return res;
	},
	
	//--------------------------------------------------------------------------
	getWMS_Settings: function(visible, opacity) {
		
		var res =  
		{ 
			buffer: DSS_bufferSize,
			displayOutsideMapExtent: false,
			isBaseLayer: false,
			displayInLayerSwitcher: false,
			opacity: opacity,
			transitionEffect: DSS_resizeMethod,
			visibility: visible,
			yx : {
				'EPSG:3857' : false
			}
		};
		
		return res;
	},
	
	//--------------------------------------------------------------------------
	addGoogleLayers: function(map) {
		
		var googTerrain = new OpenLayers.Layer.Google(
			"Google Terrain",
			{
				type: google.maps.MapTypeId.TERRAIN, minZoomLevel: 9, maxZoomLevel: 15
			});
		var googHybrid = new OpenLayers.Layer.Google(
			"Google Hybrid",
			{
				type: google.maps.MapTypeId.HYBRID, minZoomLevel: 9, maxZoomLevel: 15
			});
		
		map.addLayers([googTerrain,googHybrid]);
		
		var lpGoog = Ext.create('MyApp.view.LayerPanel_Google', {
			DSS_LayerSatellite: googTerrain,
			DSS_LayerHybrid: googHybrid,
			dock: 'bottom'
		});
		
		var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
		dssLeftPanel.up().addDocked(lpGoog);
	},
	
	//--------------------------------------------------------------------------
	addMapLayers: function(map) {
	
		//-- CDL -----------------------------------------------
		/*var wmsCDL = new OpenLayers.Layer.WMS('cdl_2012', 
			this.getGeoserverURLs(DSS_RasterPath),
			this.getLayerSettings(map, 'Raster:CDL_2012'),
			this.getWMS_Settings(false, 0.5)
		);*/
		
		//-- Rivers ----------------------------------------------
		/*var wmsRivers = new OpenLayers.Layer.WMS("Rivers", 
			this.getGeoserverURLs(DSS_VectorPath),
			this.getLayerSettings(map, 'Vector:Rivers-B'),
			this.getWMS_Settings(false, 1)
		);*/

		//-- Slope ----------------------------------------------
		/*var wmsSlope = new OpenLayers.Layer.WMS("Slope", 
			this.getGeoserverURLs(DSS_RasterPath),
			this.getLayerSettings(map, 'Raster:Slope-b'),
			this.getWMS_Settings(false, 0.5)
		);*/

		//-- Watersheds ----------------------------------------------
		var wmsWatershed = new OpenLayers.Layer.WMS("Watersheds", 
			this.getGeoserverURLs(DSS_VectorPath),
			this.getLayerSettings(map, 'Vector:Watershed-New'),
			this.getWMS_Settings(false, 1)
		);
		
		//-- Ag Lands ----------------------------------------------
		var wmsAg_Lands = new OpenLayers.Layer.WMS("Ag Lands", 
			this.getGeoserverURLs(DSS_VectorPath),
			this.getLayerSettings(map, 'Vector:Ag_Lands'),
			this.getWMS_Settings(false, 1)
		);
		
		//-- LCC ----------------------------------------------
		/*var wmsLCC = new OpenLayers.Layer.WMS("lcc", 
			this.getGeoserverURLs(DSS_RasterPath),
			this.getLayerSettings(map, 'Raster:LCC'),
			this.getWMS_Settings(false, 0.5)
		);*/
		
		//-- LCS ----------------------------------------------
		/*var wmsLCS = new OpenLayers.Layer.WMS("lcs",
			this.getGeoserverURLs(DSS_RasterPath),
			this.getLayerSettings(map, 'Raster:LCS'),
			this.getWMS_Settings(false, 0.5)
		);*/
		
		map.addLayers([
			//wmsCDL,
			//wmsSlope,
			//wmsRivers,
			wmsWatershed,
			wmsAg_Lands,
			//wmsLCC,
			//wmsLCS
			]);
		
		var lpCDL = Ext.create('MyApp.view.LayerPanel_Indexed', {
			title: 'Landcover',
			DSS_Description: 'Match land by landcover, example: select rowcrops such as corn or soybeans',
			//DSS_Layer: wmsCDL,
			minHeight: 90,
			maxHeight: 400,
			DSS_QueryTable: 'cdl_2012',
			collapsed: true
		});
		
		// Slope is greater than or equal to 10.2 degrees and less than or equal to 20.3 degrees
		var lpSlope = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Slope',
			DSS_Description: 'Match land by range of slope, example: select land on steep slopes',
			//DSS_Layer: wmsSlope,
			DSS_LayerUnit: '%',//'\xb0',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 45.5,
			DSS_ValueDefaultGreater: 5,
			DSS_ValueStep: 1,
			DSS_QueryTable: 'slope',
			collapsed: true
		});

		var lpRiver = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Distance to Stream',
			DSS_Description: 'Match land by distance to water, example: select land close to streams',
			DSS_ShortTitle: 'Stream',
			DSS_AutoSwapTitles: false,
			//DSS_Layer: wmsRivers,
			DSS_LayerUnit: 'ft',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 17220,
			DSS_ValueDefaultLess: 100,
			DSS_ValueStep: 30,
			DSS_QueryTable: 'rivers',
			collapsed: true,
			DSS_MetricToEnglish: function(unitsIn) {
				if (!unitsIn) return unitsIn;
				return unitsIn * 3.28084;// go from meters to feet
			},
			DSS_EnglishToMetric: function(unitsIn) {
				if (!unitsIn) return unitsIn;
				return unitsIn * 0.3048; // go from ft to meters
			}
		});

		var lpWatershed = Ext.create('MyApp.view.LayerPanel_Watershed', {
			title: 'Watershed',
			DSS_Description: 'Match land by watershed, example: select land in specific watersheds',
			DSS_Layer: wmsWatershed,
			DSS_QueryTable: 'watersheds',
			collapsed: true
		});

		var lpAg_Lands = Ext.create('MyApp.view.LayerPanel_Watershed', {
			title: 'Ag_Lands',
			DSS_Description: 'Match land by Ag_Lands, example: select land in specific Ag_Lands',
			DSS_Layer: wmsAg_Lands,
			DSS_QueryTable: 'Ag_Lands',
			collapsed: true
		});
		
		var lpLCC = Ext.create('MyApp.view.LayerPanel_Indexed', {
			title: 'Land Capability Class',
			DSS_Description: 'Match land by capability, example: select poor quality crop land',
			DSS_ShortTitle: 'LCC',
			DSS_AutoSwapTitles: true,
			//DSS_Layer: wmsLCC,
			minHeight: 90,
			maxHeight: 400,
			DSS_QueryTable: 'lcc',
			collapsed: true
		});
		
		var lpLCS = Ext.create('MyApp.view.LayerPanel_Indexed', {
			title: 'Land Capability Subclass',
			DSS_Description: 'Match land by capability subclass, example: select soils that are prone to saturation',
			DSS_ShortTitle: 'LCS',
			DSS_AutoSwapTitles: true,
			//DSS_Layer: wmsLCS,
			minHeight: 90,
			maxHeight: 400,
			DSS_QueryTable: 'lcs',
			collapsed: true
		});
		
		var lpPublicLand = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Distance to Public Land',
			DSS_Description: 'Match public lands, example: select lands that are adjacent to public lands',
			DSS_ShortTitle: 'Public Land',
			DSS_AutoSwapTitles: false,
		//	DSS_Layer: wmsRivers,//fix
			DSS_LayerUnit: 'mi',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 9,
			DSS_ValueDefaultLess: 1,
			DSS_ValueStep: 0.25,
			DSS_QueryTable: 'public_land',
			collapsed: true,
			DSS_MetricToEnglish: function(unitsIn) {
				if (!unitsIn) return unitsIn;
				return unitsIn * 0.000621371;// go from meters to miles
			},
			DSS_EnglishToMetric: function(unitsIn) {
				if (!unitsIn) return unitsIn;
				return unitsIn * 1609.34; // go from miles to meters
			}
		});
		
		var lpDairy = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Density of Dairies',
			DSS_Description: 'Match land by dairy density, example: select lands with a high density of dairy farms',
			DSS_ShortTitle: 'Per mi&#178;',
			DSS_AutoSwapTitles: false,
		//	DSS_Layer: wmsRivers,//fix
			DSS_LayerUnit: '',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 8,
			DSS_ValueDefaultGreater: 1,
			DSS_ValueStep: 1,
			DSS_QueryTable: 'dairy',
			collapsed: true
		});
			
		var lpGrid = Ext.create('MyApp.view.LayerPanel_SubsetOfLand', {
			title: 'Subset of Land',
			DSS_Description: 'Match land by percentage, example: select a random subset of land to simulate less-than-100% practice adoption rates',
			DSS_shortTitle: 'Subset',
//			DSS_Layer: DSS_GridLayer,
			DSS_QueryTable: 'box_selection',
			collapsed: true
		});
		
		// Speed up the insertion process a bit by suspending the layout engine until the new
		// 	elements are added...		
		Ext.suspendLayouts();
		var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
		dssLeftPanel.add(lpCDL);
		dssLeftPanel.add(lpRiver);
		dssLeftPanel.add(lpSlope);
		dssLeftPanel.add(lpWatershed);
		dssLeftPanel.add(lpAg_Lands);
		dssLeftPanel.add(lpLCC);
		dssLeftPanel.add(lpLCS);
		dssLeftPanel.add(lpPublicLand);
		dssLeftPanel.add(lpDairy);
		dssLeftPanel.add(lpGrid);
		Ext.resumeLayouts(true);
		
		// BOO - FIXME
		DSS_globalQueryableLayers.push(lpCDL);
		DSS_globalQueryableLayers.push(lpSlope);
		DSS_globalQueryableLayers.push(lpRiver);
		DSS_globalQueryableLayers.push(lpLCC);
		DSS_globalQueryableLayers.push(lpLCS);
		DSS_globalQueryableLayers.push(lpWatershed);
		DSS_globalQueryableLayers.push(lpAg_Lands);
		DSS_globalQueryableLayers.push(lpPublicLand);
		DSS_globalQueryableLayers.push(lpDairy);
		DSS_globalQueryableLayers.push(lpGrid);
		
		DSS_globalCollapsibleLayers.push(lpCDL);
		DSS_globalCollapsibleLayers.push(lpSlope);
		DSS_globalCollapsibleLayers.push(lpRiver);
		DSS_globalCollapsibleLayers.push(lpLCC);
		DSS_globalCollapsibleLayers.push(lpLCS);
		DSS_globalCollapsibleLayers.push(lpWatershed);
		DSS_globalCollapsibleLayers.push(lpAg_Lands);
		DSS_globalCollapsibleLayers.push(lpPublicLand);
		DSS_globalCollapsibleLayers.push(lpDairy);
		DSS_globalCollapsibleLayers.push(lpGrid);
		
		this.addFeatureClickControl(map);
	},
	
	// Used by vector selection layers (e.g., Watershed)
	//--------------------------------------------------------------------------
	activateClickControlWithHandler: function(clickHandler, unClickHandler, protocol, owner) {
		
		Ext.getCmp('DSS_selection_toolbar').setDisabled(false);
		Ext.getCmp('DSS_single_select').toggle(true);
		
		console.log('mainViewport::activateClickControlWithHandler');
		// turn off old handler when activating a new one...
		if (this.DSS_clickFeatureHandler) {
			this.DSS_clickFeatureHandler.scope.tryDisableClickSelection();
		}
		
		console.log('mainViewport::settingstuffs');
		this.DSS_clickFeatureHandler = {
			handler: clickHandler,
			scope: owner
		};
		this.DSS_unClickFeatureHandler = {
			handler: unClickHandler,
			scope: owner
		};
		G_ClickControl.protocol = protocol;
		G_ClickControl.activate();
		G_ClickControl.handlers.box.deactivate();
		
		OpenLayers.Element.addClass(globalMap.viewPortDiv, "olCursorHand");
	},
	
	//--------------------------------------------------------------------------
	deactivateClickControl: function() {
	
		console.log('Called into deactivateClickControl');
		
		this.DSS_clickFeatureHandler = null;
		this.DSS_unClickFeatureHandler = null;
		
		G_ClickControl.deactivate();
		OpenLayers.Element.removeClass(globalMap.viewPortDiv, "olCursorHand");
		Ext.getCmp('DSS_selection_toolbar').setDisabled(true);
	},
	
	// Bah, the OpenLayers click handler doesn't seem terribly configurable with
	//	switching out a handler call back with a different scope, etc...
	//	Prefer to just have the given Layer Panel manage it...so define the
	//	click callback handler to go here for consistency sake...then route to 
	//	the correct Layer Panel handler that was configured in: this.activateClickControlWithHandler()
	//--------------------------------------------------------------------------
	onClick: function(evt) {
		
		this.DSS_clickFeatureHandler.handler.call(
			this.DSS_clickFeatureHandler.scope,
			evt);
	},
	
	//--------------------------------------------------------------------------
	onUnClick: function(evt) {
		
		this.DSS_unClickFeatureHandler.handler.call(
			this.DSS_unClickFeatureHandler.scope,
			evt);
	},
	
	//--------------------------------------------------------------------------
	addFeatureClickControl: function(map) {
		
		var me = this;

		G_ClickControl = new OpenLayers.Control.GetFeature({
		//                protocol: G_protocol,
			box: true,
			multipleKey: "shiftKey",
			toggleKey: Ext.isMac ? 'altKey' : 'ctrlKey'
		});
		G_ClickControl.events.register("featureselected", this, function(e) {
				me.onClick(e);
		});
		G_ClickControl.events.register("featureunselected", this, function(e) {
				me.onUnClick(e);
		});
		map.addControl(G_ClickControl);
	},
	
	//--------------------------------------------------------------------------
    doApplyIf: function(me, map) {
    	
        Ext.applyIf(me, {
			items: [{
				xtype: 'panel',
				minHeight: 400,
				minWidth: 800,
				autoScroll: true,
				layout: {
					type: 'fit'
				},
				items: [{
					xtype: 'gx_mappanel',
					id: 'DSS_map_panel',
					title: 'Landscape Map',
					icon: 'app/images/globe_icon.png',
					map: map,
					border: 0,
					center: '12,51',
					zoom: 6,
					stateId: 'mappanel',
					dockedItems: [{
						xtype: 'toolbar',
						id: 'DSS_selection_toolbar',
						dock: 'top',
						disabled: true,
						items: [{
							xtype: 'button',
							id: 'DSS_single_select',
							text: 'Single Selection',
							pressed: true,
							toggleGroup: 'selectionGroup',
							allowDepress: false,
							icon: 'app/images/single_point_icon.png',
							toggleHandler: function(button, pressed) {
								if (pressed) {
									console.log('Single select toggleHandler called!!');
									G_ClickControl.deactivate();
									G_ClickControl.activate();
									G_ClickControl.handlers.box.deactivate();
								}
							}
						},{
							xtype: 'button',
							text: 'Multi-Selection',
							toggleGroup: 'selectionGroup',
							allowDepress: false,
							icon: 'app/images/box_select_icon.png',
							toggleHandler: function(button, pressed) {
								if (pressed) {
									console.log('Multi select toggleHandler called!!');
									G_ClickControl.deactivate();
									G_ClickControl.handlers.box.activate();
									G_ClickControl.activate();
								}
							}
						}]
					}],
					tools: [{
						type: 'down',
						tooltip: 'Show/Hide Logo and Meta',
						handler: function() {
							var panel = Ext.getCmp('DSS_LogoPanel');
							if (this.type == 'up') {
								panel.setSize(undefined, 0);
								this.setType('down');
							}
							else {
								panel.setSize(undefined, DSS_LogoPanelHeight);
								this.setType('up');
							}
							panel = Ext.getCmp('DSS_ScenarioSummary');
							panel.setSize(undefined,250);//doComponentLayout();
						}
					}]
				}],
				dockedItems: [{
					xtype: 'logo_panel', // docked top
				},
				{
					xtype: 'panel',
					dock: 'left',
					width: 450,
					autoScroll: true,
					collapseDirection: 'left',
//					animCollapse: false,
					collapsible: true,
					header: {
						style: {
							'background-color': '#95b0db !important'
						},
						icon: 'app/images/magnify_icon.png',
					},
					manageHeight: false,
					title: 'Step 1: Select Land to Transform',
					DSS_NamedQuery: 'Untitled',
					listeners: {
						collapse: function(p, eOpts) { 
							p.DSS_SetTitle(p.DSS_NamedQuery, true);
						},
						beforeexpand: function(p, animated, eOpts) {
							p.DSS_SetTitle(p.DSS_NamedQuery, false);
						},
					},

					DSS_SetTitle: function(queryName, collapsed) {
						this.DSS_NamedQuery = queryName;
						if (queryName == 'Double Click to Set Custom Name') {
							queryName = 'Unnamed Transform';
						}
						queryName = ' - "' + queryName + '"'; 
						if (collapsed) {
							this.setTitle('Step 1: Select Land to Transform / Scenario Tools' + queryName);
						}
						else {
							this.setTitle('Step 1: Select Land to Transform' + queryName);
						}
					},
					layout: {
						fill: false,
						autoWidth: false
					},
					
					items: [{
						xtype: 'container',
						id: 'DSS_LeftPanel',
						layout: {
							type: 'accordion',
						//	animate: false,
							multi: true,
							titleCollapse: false
						},
						items: [{
						// NOTE: Hidden Panel to allow all visible items to collapse.
							xtype: 'panel',
							hidden: true,
							collapsed: false
						}] // NOTE: other panels are dynamically added to this list...
					}],
					dockedItems: [{
						xtype: 'view_select_toolbar' // docked top left
					},
					{
						xtype: 'scenario_layout' // docked bottom
					}]
				},
				{
					xtype: 'report_master_layout', // docked right
					id: 'DSS_report_panel',
				}]
			}]
        });
    },
    
	//--------------------------------------------------------------------------
    getAssumptions: function() {
    	
		var obj = Ext.Ajax.request({
			url: location.href + 'getAssumptions',
			method: 'POST',
			timeout: 10 * 1000, // seconds * (i.e. converted to) milliseconds
			
			success: function(response, opts) {
				
				var obj = JSON.parse(response.responseText);
				DSS_AssumptionsDefaults = obj;
				
				//console.log(obj);
				
				// MAKE a COPY vs just setting the pointers, which does nothing to make a copy
				//	like we really need...
				DSS_AssumptionsAdjustable = JSON.parse(JSON.stringify(DSS_AssumptionsDefaults));
			},
			
			failure: function(respose, opts) {
				alert("GetAssumptions call failed, request timed out?");
			}
		});
	}
});

