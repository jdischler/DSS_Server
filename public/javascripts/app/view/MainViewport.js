// * File: app/view/MainViewport.js

var globalMap;
var bufferSize = 2; // how many non-visible tiles on either side of visible area to cache?
var imgFormat = 'image/png';
var baseUrl = 'pgis.glbrc.org';
var baseUrl1 = 'pgis1.wei.wisc.edu';
var baseUrl2 = 'pgis2.wei.wisc.edu';
var baseUrl3 = 'pgis3.wei.wisc.edu';
var path = "/geoserver/DSS-Vector-UTM/wms";
var port = '8080';
var resizeMethod = null; // "resize";

//------------------------------------------------------------------------------
Ext.define('MyApp.view.MainViewport', {
//------------------------------------------------------------------------------

	extend: 'Ext.container.Viewport',
	requires: [
		'MyApp.view.InfoToolbar',
		'MyApp.view.MainToolbar',
		'MyApp.view.ViewTools',
		'MyApp.view.SelectionTools',
		'MyApp.view.TransformationTools',
		'MyApp.view.ManagementTools',
		'MyApp.view.EvaluationTools',
		'MyApp.view.GraphTools',
		'GeoExt.panel.Map',
		'MyApp.view.LayerPanel',
		'MyApp.view.ScenarioTools',
		'MyApp.view.GlobalScenarioTools',
		'MyApp.view.QueryPanelTool',
		'MyApp.view.ReportTools',
        'MyApp.view.LayerPanel_Indexed',
        'MyApp.view.LayerPanel_Continuous'
	],
	
	autoScroll: true,
	layout: {
		type: 'fit'
	},

	//--------------------------------------------------------------------------
	initComponent: function() {
		
		var me = this;
		var projectionType = "EPSG:3857";
		var bounds = new OpenLayers.Bounds(
                    -10035269.3627204, 5259982.9002571,
                    -9882534.26873933, 5386224.15842662
                );
		var options = {
			controls: [],
			maxExtent: bounds,
			restrictedExtent: bounds.scale(1.5),
			maxResolution: 596.6214608635564,
			projection: "EPSG:3857",
			units: 'm'
		};
		
		globalMap = new OpenLayers.Map('map', options)
		var map = globalMap;

		this.doApplyIf(me, map);
		
		me.callParent(arguments);
		this.addMapLayers(map, baseUrl, port, path);
		this.addMapControls(map);		
		map.zoomTo(1);
	},

	// Controls wired up to DOM elements need some manner of delay other control
	//	INIT will attempt to find the element which will not exist yet    
	//--------------------------------------------------------------------------
	wireInDelayedControls: function(delay) {
		
		Ext.defer(function() {
			globalMap.addControl(new OpenLayers.Control.Scale($('DSS_scale_tag')));
			
			Ext.getCmp('DSS_scale_tag').updateLayout();
			
		}, delay, this);
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
		
		var layerSwitcher =new OpenLayers.Control.LayerSwitcher(); 
		map.addControl(layerSwitcher);
		
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
		
		// FIXME: maybe just use afterRender listener event?			
		this.wireInDelayedControls(2000);
	},
				
	//--------------------------------------------------------------------------
	addMapLayers: function(map, baseUrl, port, path) {
		
		var layerBrowser = Ext.getCmp('mapLayerPanel');
		
		//----------------
		var wmsWatersheds = new OpenLayers.Layer.WMS("Watersheds", 
			["http://" + baseUrl + ":" + port + path,
			"http://" + baseUrl1 + ":" + port + path,
			"http://" + baseUrl2 + ":" + port + path,
			"http://" + baseUrl3 + ":" + port + path],
			{
				layers: 'DSS-Vector:watersheds',
				transparent: true,
				format: imgFormat
			},
			{ 
				displayOutsideMapExtent: false,
				opacity: 0.5,
				isBaseLayer: false,
				displayInLayerSwitcher: false,
				transitionEffect: resizeMethod,
				buffer: bufferSize,
				visibility: false
			});
		
		//----------------
		var wmsRivers = new OpenLayers.Layer.WMS("Rivers", 
			["http://" + baseUrl + ":" + port + path,
			"http://" + baseUrl1 + ":" + port + path,
			"http://" + baseUrl2 + ":" + port + path,
			"http://" + baseUrl3 + ":" + port + path],
			{ 
				layers: 'DSS-Vector:rivers',  
				transparent: "true",
				format: imgFormat  
			},
			{ 
				displayOutsideMapExtent: false,
				isBaseLayer: false,
				displayInLayerSwitcher: false,
				opacity: 0.3,
				transitionEffect: resizeMethod,
				visibility: false
			});
		
		//----------------
		var wmsSlope = new OpenLayers.Layer.WMS("Slope", 
			["http://" + baseUrl + ":" + port + "/geoserver/DSS-Raster-UTM/wms",
			"http://" + baseUrl1 + ":" + port + "/geoserver/DSS-Raster-UTM/wms",
			"http://" + baseUrl2 + ":" + port + "/geoserver/DSS-Raster-UTM/wms",
			"http://" + baseUrl3 + ":" + port + "/geoserver/DSS-Raster-UTM/wms"],
			{
				layers: 'DSS-Raster:Slope',
				format: imgFormat,
				transparent: true,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			{
				buffer: bufferSize,
				displayOutsideMaxExtent: false,
				isBaseLayer: false,
				displayInLayerSwitcher: false,
				opacity: 0.5,
				transitionEffect: resizeMethod,
				visibility: false,
				yx : {
					projectionType : true
				}
			});

		//----------------
		var wmsCDL = new OpenLayers.Layer.WMS("CDL", 
			["http://" + baseUrl + ":" + port + "/geoserver/DSS-Raster-UTM/wms",
			"http://" + baseUrl1 + ":" + port + "/geoserver/DSS-Raster-UTM/wms",
			"http://" + baseUrl2 + ":" + port + "/geoserver/DSS-Raster-UTM/wms",
			"http://" + baseUrl3 + ":" + port + "/geoserver/DSS-Raster-UTM/wms"],
			{
				layers: 'DSS-Raster:CDL',
				format: imgFormat,
				transparent: true,
				tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom
			},
			{
				buffer: bufferSize,
				displayOutsideMaxExtent: false,
				opacity: 0.5,
				isBaseLayer: false,
				displayInLayerSwitcher: false,
				transitionEffect: resizeMethod,
				yx : {
					projectionType : true
				}
			});
		
		//----------------
		var googTerrain = new OpenLayers.Layer.Google(
			"Google Terrain",
			{
				type: google.maps.MapTypeId.TERRAIN, maxZoomLevel: 20, minZoomLevel: 9
			});
		var googHybrid = new OpenLayers.Layer.Google(
			"Google Hybrid",
			{
				type: google.maps.MapTypeId.HYBRID, maxZoomLevel: 20, minZoomLevel: 9
			});
		
		//----------------
		layerBrowser.addLayer(wmsCDL, 'Land Coverage','app/images/raster.png',
				'Activate a raster overlay of land usage');
		layerBrowser.addLayer(wmsSlope, 'Geophysical','app/images/raster.png',
				'Activate a raster overlay of calculated terrain slope');
		layerBrowser.addLayer(wmsRivers, 'Geophysical','app/images/vector.png',
				'Activate a vector overlay of all rivers');
		layerBrowser.addLayer(wmsWatersheds, 'Geophysical','app/images/vector.png',
				'Activate a vector overlay of the watersheds');
	
		map.addLayers([googTerrain,googHybrid,
			wmsCDL,
			wmsSlope,
			wmsWatersheds,
			wmsRivers
			]);
		
		var lpCDL = Ext.create('MyApp.view.LayerPanel_Indexed', {
			DSS_Layer: wmsCDL,
			title: 'Cropland Data',
			minHeight: 90,
			maxHeight: 400,
			icon: 'app/images/layers_icon.png',
			DSS_LegendElements: [{
				DSS_LegendElementType: 'Corn',
				DSS_LegendElementColor: '#ffdd00',
				DSS_Index: 1
			},
			{
				DSS_LegendElementType: 'Soy',
				DSS_LegendElementColor: '#009900',
				DSS_Index: 2
			},
			{
				DSS_LegendElementType: 'Water',
				DSS_LegendElementColor: '#5599ff',
				DSS_Index: 141
			},
			{
				DSS_LegendElementType: 'Trees',
				DSS_LegendElementColor: '#99ffdd',
				DSS_Index: 181
			},
			{
				DSS_LegendElementType: 'Urban',
				DSS_LegendElementColor: '#cccccc',
				DSS_Index: 196
			},
			{
				DSS_LegendElementType: 'Area 51',
				DSS_LegendElementColor: '#cc0000',
				DSS_Index: 6
			},
			{
				DSS_LegendElementType: 'Cancer Research',
				DSS_LegendElementColor: '#ffaaff',
				DSS_Index: 10
			},
			{
				DSS_LegendElementType: 'Taters',
				DSS_LegendElementColor: '#774400',
				DSS_Index: 11
			}],
			
			DSS_QueryTable: 'cdl'
		});
		
		var lpSlope = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'Slope',
			DSS_Layer: wmsSlope,
			DSS_LayerUnit: '\xb0',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 45.5,
			DSS_ValueDefaultGreater: 10,
			DSS_QueryTable: 'slope'
		});

		var lpRiver = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'River',
			DSS_Layer: wmsRivers,
			DSS_LayerUnit: 'm',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 915.5,
			DSS_ValueDefaultLess: 120,
			DSS_QueryTable: 'river'
		});
		
		Ext.getCmp('DSS_LeftPanel').insert(0,lpSlope);
		Ext.getCmp('DSS_LeftPanel').insert(0,lpRiver);
		Ext.getCmp('DSS_LeftPanel').insert(0,lpCDL);
		
		
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
					title: 'Landscape Viewer',
					icon: 'app/images/globe_icon.png',
					map: map,
					border: 0,
					center: '12,51',
					zoom: 6,
					stateId: 'mappanel',
				}],
				dockedItems: [{
					xtype: 'panel',
					frame: false,
					layout: {
						type: 'absolute'
					},
//					title: 'GLBRC Decision Support Tool v0.1',
//					titleAlign: 'center',
					header: false,
					dock: 'top',
					collapsible: true,
					animCollapse: false,
					collapsed: false,
					height: 64,
					bodyStyle: 'background-color:rgb(220,230,240)',
					items: [{
						xtype: 'image',
						x: 0,
						y: 0,
						src: 'app/images/dss_logo.png'
					}]
				},
				{
					xtype: 'infotoolbar',
					dock: 'bottom'
				},
				{
					xtype: 'panel',
					id: 'DSS_LeftPanel',
					dock: 'left',
					width: 400,
					autoScroll: true,
					layout: {
						fill: false,
						autoWidth: false,
						type: 'accordion',
						animate: false,
						multi: true
					},
					collapseDirection: 'left',
					collapsible: true,
					frameHeader: false,
					manageHeight: false,
					title: '',

					listeners: {
						collapse: function(p, eOpts) { 
							p.setTitle('Query / Scenario Tools');
						},
						beforeexpand: function(p, animated, eOpts) {
							p.setTitle('');
						},
					},
					items: [/*{
						xtype: 'legendpanel'
					},
					{
						xtype: 'slope_panel'
					},*/
					{
						xtype: 'layerPanel'
					},
/*					{
						xtype: 'querypanel'
					},*/
					{
						xtype: 'scenariotools',
						collapsed: true
					},
					{
						xtype: 'transformationtools',
						collapsed: true
					},
					{
						xtype: 'globalscenariotools',
						collapsed: true
					}]
				},
				{
					xtype: 'panel',
					dock: 'right',
					width: 400,
					autoScroll: true,
					layout: {
						fill: false,
						autoWidth: false,
						type: 'accordion',
						animate: false,
						multi: true
					},
					collapseDirection: 'right',
					collapsible: true,
					collapsed: true,
					items: [{
						xtype: 'evaluationtools'
					},
					{
						xtype: 'reporttools',
						collapsed: true
					}]
				}]
			}]
        });
    }

});
