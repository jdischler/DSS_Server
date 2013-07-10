// * File: app/view/MainViewport.js

var globalMap;
var imgFormat = 'image/png';
var baseUrl = 'pgis.glbrc.org';
var baseUrl1 = 'pgis1.wei.wisc.edu';
var baseUrl2 = 'pgis2.wei.wisc.edu';
var baseUrl3 = 'pgis3.wei.wisc.edu';
var vectorPath = '/geoserver/Vector/wms';
var rasterPath = '/geoserver/Raster/wms';
var port = '8080';
var DSS_bufferSize = 2; // how many non-visible tiles on either side of visible area to cache?
var DSS_resizeMethod = null; // "resize";
var DSS_LogoPanelHeight = 64;

// boo
var DSS_globalQueryableLayers = [];
var DSS_globalCollapsibleLayers = [];

//------------------------------------------------------------------------------
Ext.define('MyApp.view.MainViewport', {
//------------------------------------------------------------------------------

	extend: 'Ext.container.Viewport',
	requires: [
		'MyApp.view.InfoToolbar',
		'MyApp.view.TransformationTools',
		'MyApp.view.ManagementTools',
		//'MyApp.view.EvaluationTools',
		'MyApp.view.GraphTools',
		'GeoExt.panel.Map',
		'MyApp.view.ScenarioTools',
		'MyApp.view.GlobalScenarioTools',
		'MyApp.view.ReportTools',
		'MyApp.view.LayerPanel_Google',
        'MyApp.view.LayerPanel_Indexed',
        'MyApp.view.LayerPanel_Continuous',
        'MyApp.view.LayerPanel_CurrentSelection',
        'MyApp.view.LayerPanel_Watershed',
        'MyApp.view.LogoPanel',
        'MyApp.view.ViewSelectToolbar',
        'MyApp.view.ScenarioMasterLayout',        
        'MyApp.view.ReportMasterLayout'        
	],
	
	autoScroll: true,
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
			-10062652.65061, 5278060.469521415,// 5249032.6922889,
			-9878152.65061, 5415259.640662575// 5385742.6922889
		);
		var options = {
			controls: [],
			maxExtent: bounds,
			restrictedExtent: bounds.scale(1.25),
			maxResolution: 305.74811309814453,
			projection: projectionType,
			units: 'm'
		};
		
		globalMap = new OpenLayers.Map('map', options)
		var map = globalMap;

		this.doApplyIf(me, map);
		
		me.callParent(arguments);
		this.addMapLayers(map);
		this.addMapControls(map);		
	},

	// Controls wired up to DOM elements need some manner of delay other control
	//	INIT will attempt to find the element which will not exist yet    
	//--------------------------------------------------------------------------
	addControlsNeedingLayout: function() {
		
		globalMap.addControl(new OpenLayers.Control.Scale($('DSS_scale_tag')));
		Ext.getCmp('DSS_scale_tag').updateLayout();
		
		globalMap.zoomTo(1);
		globalMap.pan(1,1); // FIXME: lame workaround for google map zoom level not starting out correctly?
	},
	
	//--------------------------------------------------------------------------
	addMapControls: function(map) {
		
		var layerBrowser = Ext.getCmp('mapLayerPanel');

		map.addControl(new OpenLayers.Control.Navigation({
			documentDrag: true, 
			dragPanOptions: {
				enableKinetic: true,
				kineticInterval: 500
			}
		}));
		
		var overviewMap = new OpenLayers.Control.OverviewMap({minRatio: 32, maxRatio:64, 
			autoPan:true,
			size: {w: 270, h: 120},
			maximized: true
		}); 
		map.addControl(overviewMap);
		var tip = Ext.create('Ext.tip.ToolTip', {
				target: overviewMap.maximizeDiv,
				html: 'Open Overview Map'
		});
		
		map.addControl(new OpenLayers.Control.PanZoomBar({zoomWorldIcon: true}));
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

	// Type is 'raster' or 'vector'	
	//--------------------------------------------------------------------------
	getGeoserverURL: function(urltype, thePort) {
		
		if (urltype == 'raster') {
			return ['http://' + baseUrl + ':' + port + rasterPath,
					'http://' + baseUrl1 + ':' + port + rasterPath,
					'http://' + baseUrl2 + ':' + port + rasterPath,
					'http://' + baseUrl3 + ':' + port + rasterPath 
			];
		}
		else {
			return ['http://' + baseUrl + ':' + port + vectorPath,
					'http://' + baseUrl1 + ':' + port + vectorPath,
					'http://' + baseUrl2 + ':' + port + vectorPath,
					'http://' + baseUrl3 + ':' + port + vectorPath 
			];
		}
	},
	
	// returns
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
				projectionType : true
			}
		};
		
		return res;
	},
	
	//--------------------------------------------------------------------------
	addMapLayers: function(map) {
		
		var layerBrowser = Ext.getCmp('mapLayerPanel');
		
		//------------------------------------------------
		var wmsRivers = new OpenLayers.Layer.WMS("Rivers", 
			this.getGeoserverURL('vector'),
			{ 
				layers: 'Vector:Rivers-B',  
				transparent: true,
				format: imgFormat,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom  
			},
			this.getWMS_Settings(false, 1)
		);
		//------------------------------------------------
		var wmsWatershed = new OpenLayers.Layer.WMS("Watersheds", 
			this.getGeoserverURL('vector'),
			{ 
				layers: 'Vector:Watersheds-C',  
				transparent: true,
				format: imgFormat,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			this.getWMS_Settings(false, 1)
		);
		
		//------------------------------------------------
		var wmsSlope = new OpenLayers.Layer.WMS("Slope", 
			this.getGeoserverURL('raster'),
			{
				layers: 'Raster:Slope-b',
				format: imgFormat,
				transparent: true,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			this.getWMS_Settings(false, 0.5)
		);

		//------------------------------------------------
		var wmsLCC = new OpenLayers.Layer.WMS("lcc", 
			this.getGeoserverURL('raster'),
			{
				layers: 'Raster:LCC',
				format: imgFormat,
				transparent: true,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			this.getWMS_Settings(false, 0.5)
		);
		
		//------------------------------------------------
		var wmsLCS = new OpenLayers.Layer.WMS("lcs",
			this.getGeoserverURL('raster'),
			{
				layers: 'Raster:LCS',
				format: imgFormat,
				transparent: true,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			this.getWMS_Settings(false, 0.5)
		);
		
		//------------------------------------------------
		var wmsSOC = new OpenLayers.Layer.WMS("SOC", 
			this.getGeoserverURL('raster'),
			{
				layers: 'Raster:SOC_I',
				format: imgFormat,
				transparent: true,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			this.getWMS_Settings(false, 0.5)
		);
		
		//------------------------------------------------
		var wmsCDL = new OpenLayers.Layer.WMS("rotation", 
			this.getGeoserverURL('raster'),
			{
				layers: 'Raster:Rotation',
				format: imgFormat,
				transparent: true,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			this.getWMS_Settings(true, 0.5)
		);
		
		//----------------
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
		
		map.addLayers([googTerrain,googHybrid,
			wmsCDL,
			wmsSlope,
			wmsSOC,
			wmsLCC,
			wmsLCS,
			wmsWatershed,
			wmsRivers
			]);
		
		var lpCDL = Ext.create('MyApp.view.LayerPanel_Indexed', {
			title: 'Cropland Data',
			DSS_Layer: wmsCDL,
			minHeight: 90,
			maxHeight: 400,
			DSS_QueryTable: 'rotation',
			collapsed: true
		});
		
		var lpLCC = Ext.create('MyApp.view.LayerPanel_Indexed', {
			title: 'LCC (Land Capability Class)',
			DSS_ShortTitle: 'LCC',
			DSS_AutoSwapTitles: true,
			DSS_Layer: wmsLCC,
			minHeight: 90,
			maxHeight: 400,
			DSS_QueryTable: 'lcc',
			collapsed: true
		});
		
		var lpLCS = Ext.create('MyApp.view.LayerPanel_Indexed', {
			title: 'LCS (Land Capability Subclass)',
			DSS_ShortTitle: 'LCS',
			DSS_AutoSwapTitles: true,
			DSS_Layer: wmsLCC,
			minHeight: 90,
			maxHeight: 400,
			DSS_QueryTable: 'lcs',
			collapsed: true
		});
		
		var lpSlope = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Slope',
			DSS_Layer: wmsSlope,
			DSS_LayerUnit: '\xb0',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 45.5,
			DSS_ValueDefaultGreater: 10,
			DSS_QueryTable: 'slope',
			collapsed: true
		});

		var lpWatershed = Ext.create('MyApp.view.LayerPanel_Watershed', {
			title: 'Watershed',
			DSS_Layer: wmsWatershed,
			DSS_QueryTable: 'watersheds',
			collapsed: true
		});

		var lpRiver = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Distance to River',
			DSS_ShortTitle: 'River',
			DSS_AutoSwapTitles: false,
			DSS_Layer: wmsRivers,
			DSS_LayerUnit: 'm',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 915.5,
			DSS_ValueDefaultLess: 120,
			DSS_QueryTable: 'rivers',
			collapsed: true
		});

		var lpRoad = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Distance to Road',
			DSS_ShortTitle: 'Road',
			DSS_AutoSwapTitles: false,
			DSS_Layer: null,
			DSS_LayerUnit: 'm',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 915.5,
			DSS_ValueDefaultGreater: 1,
			DSS_ValueDefaultLess: 120,
			DSS_QueryTable: 'roads',
			collapsed: true
		});
		
		var lpSOC = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'SOC (Soil Organic Carbon)',
			DSS_ShortTitle: 'SOC',
			DSS_AutoSwapTitles: true,
			DSS_Layer: wmsSOC,
			DSS_LayerUnit: '',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 1300,
			DSS_ValueDefaultLess: 300,
			DSS_QueryTable: 'soc',
			collapsed: true
		});
		
		var lpGoog = Ext.create('MyApp.view.LayerPanel_Google', {
			DSS_LayerSatellite: googTerrain,
			DSS_LayerHybrid: googHybrid
		});

		// Speed up the insertion process a bit by suspending the layout engine until the new
		// 	elements are added...		
		Ext.suspendLayouts();
		var dssLeftPanel = Ext.getCmp('DSS_LeftPanel');
		dssLeftPanel.insert(0,lpGoog);
		dssLeftPanel.insert(0,lpSOC);
		dssLeftPanel.insert(0,lpLCS);
		dssLeftPanel.insert(0,lpLCC);
		dssLeftPanel.insert(0,lpSlope);
		dssLeftPanel.insert(0,lpWatershed);
		dssLeftPanel.insert(0,lpRoad);
		dssLeftPanel.insert(0,lpRiver);
		dssLeftPanel.insert(0,lpCDL);
		Ext.resumeLayouts(true);
		
		// BOO - FIXME
		DSS_globalQueryableLayers.push(lpCDL);
		DSS_globalQueryableLayers.push(lpSlope);
		DSS_globalQueryableLayers.push(lpLCC);
		DSS_globalQueryableLayers.push(lpLCS);
		DSS_globalQueryableLayers.push(lpWatershed);
		DSS_globalQueryableLayers.push(lpRoad);
		DSS_globalQueryableLayers.push(lpRiver);
		DSS_globalQueryableLayers.push(lpSOC);
		
		DSS_globalCollapsibleLayers.push(lpGoog);
		DSS_globalCollapsibleLayers.push(lpSOC);
		DSS_globalCollapsibleLayers.push(lpLCC);
		DSS_globalCollapsibleLayers.push(lpLCS);
		DSS_globalCollapsibleLayers.push(lpSlope);
		DSS_globalCollapsibleLayers.push(lpWatershed);
		DSS_globalCollapsibleLayers.push(lpRoad);
		DSS_globalCollapsibleLayers.push(lpRiver);
		DSS_globalCollapsibleLayers.push(lpCDL);
		
		var lpSel = Ext.create('MyApp.view.LayerPanel_CurrentSelection', {
			hidden: true//,
//			DSS_Layer: wmsSlope // NOTE: dummy layer
		});
		dssLeftPanel.insert(0,lpSel);
//		DSS_globalCollapsibleLayers.push(lpSel);
		
		this.addFeatureClickControl(map);
	},
	
	// Used by vector selection layers (e.g., Watershed)
	//--------------------------------------------------------------------------
	activateClickControlWithHandler: function(handler, scope) {
		
		this.DSS_clickFeatureHandler = {
			handler: handler,
			scope: scope
		};
		this.DSS_clickControl.activate();
		OpenLayers.Element.addClass(globalMap.viewPortDiv, "olCursorHand");
	},
	
	//--------------------------------------------------------------------------
	deactivateClickControl: function() {
		
		this.DSS_clickControl.deactivate();
		OpenLayers.Element.removeClass(globalMap.viewPortDiv, "olCursorHand");
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
	addFeatureClickControl: function(map) {
		
		var me = this;
		
		// NOTE: found this in OL code samples. I guess it just creates a click control
		//	class from the OpenLayer.Control class? ie, it adds a custom click handler?
		OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {                
			defaultHandlerOptions: {
				'single': true,
				'double': false,
				'pixelTolerance': 0,
				'stopSingle': false,
				'stopDouble': false
			},

			initialize: function(options) {
				this.handlerOptions = OpenLayers.Util.extend(
					{}, this.defaultHandlerOptions
				);
				OpenLayers.Control.prototype.initialize.apply(
					this, arguments
				); 
				this.handler = new OpenLayers.Handler.Click(
					me, {
						'click': me.onClick
					}, this.handlerOptions
				);
			}
		});
		
		this.DSS_clickControl = new OpenLayers.Control.Click();
		map.addControl(this.DSS_clickControl);
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
					title: 'Landscape Viewer',
					icon: 'app/images/globe_icon.png',
					map: map,
					border: 0,
					center: '12,51',
					zoom: 6,
					stateId: 'mappanel',
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
							panel = Ext.getCmp('DSS_ScenarioPanel');
							panel.setSize(undefined,300);//doComponentLayout();
							
						}
					}]
				}],
				dockedItems: [{
					xtype: 'logo_panel', // docked top
				},
				{
					xtype: 'infotoolbar' // docked bottom
					, hidden: true
				},
				{
					xtype: 'panel',
					dock: 'left',
					width: 400,
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
					title: 'View / Select',

					listeners: {
						collapse: function(p, eOpts) { 
							p.setTitle('View / Select / Scenario Tools');
						},
						beforeexpand: function(p, animated, eOpts) {
							p.setTitle('View / Select');
						},
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
							animate: false,
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
						xtype: 'scenario_master_layout' // docked bottom left
					}]
				},
				{
					xtype: 'report_master_layout' // docked right
				}]
			}]
        });
    }

});

