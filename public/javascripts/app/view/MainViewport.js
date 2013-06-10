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
var DSS_LogoPanelHeight = 64;

// boo
var DSS_globalQueryableLayers = [];

//------------------------------------------------------------------------------
Ext.define('MyApp.view.MainViewport', {
//------------------------------------------------------------------------------

	extend: 'Ext.container.Viewport',
	requires: [
		'MyApp.view.InfoToolbar',
		'MyApp.view.TransformationTools',
		'MyApp.view.ManagementTools',
		'MyApp.view.EvaluationTools',
		'MyApp.view.GraphTools',
		'GeoExt.panel.Map',
		'MyApp.view.ScenarioTools',
		'MyApp.view.GlobalScenarioTools',
		'MyApp.view.ReportTools',
		'MyApp.view.LayerPanel_Google',
        'MyApp.view.LayerPanel_Indexed',
        'MyApp.view.LayerPanel_Continuous',
        'MyApp.view.LayerPanel_CurrentSelection'
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
		
		map.addLayers([googTerrain,googHybrid,
			wmsCDL,
			wmsSlope,
			wmsRivers
			]);
		
		var lpCDL = Ext.create('MyApp.view.LayerPanel_Indexed', {
			DSS_Layer: wmsCDL,
			title: 'Cropland Data',
			minHeight: 90,
			maxHeight: 400,
//			icon: 'app/images/layers_icon.png',
			DSS_LegendElements: [{
				DSS_LegendElementType: 'Corn and Beans', // 1,255,85,0,Corn and beans
				DSS_LegendElementColor: '#ff5500',
				DSS_Index: 1
			},{
				DSS_LegendElementType: 'Small Grains', //2,255,102,0,Small grains
				DSS_LegendElementColor: '#ff6600',
				DSS_Index: 2
			},{
				DSS_LegendElementType: 'Vegetables', //3,255,132,0,255,Vegetables
				DSS_LegendElementColor: '#ff8400',
				DSS_Index: 3
			},{
				DSS_LegendElementType: 'Tree Crops', //4,255,204,0,255,Tree crops
				DSS_LegendElementColor: '#ffcc00',
				DSS_Index: 4
			},{
				DSS_LegendElementType: 'Other Crops', //5,255,255,0,255,Other crops
				DSS_LegendElementColor: '#ffff00',
				DSS_Index: 5
			},{
				DSS_LegendElementType: 'Grass and forage', //6,0,85,0,255,Grass and forage
				DSS_LegendElementColor: '#005500',
				DSS_Index: 6
			},{
				DSS_LegendElementType: 'Woodlands', //7,85,0,0,255,Woodland
				DSS_LegendElementColor: '#550000',
				DSS_Index: 7
			},{
				DSS_LegendElementType: 'Wetland', //8,0,0,104,255,Wetland
				DSS_LegendElementColor: '#000068',
				DSS_Index: 8
			},{
				DSS_LegendElementType: 'Open Water', //9,0,128,255,255,Open water
				DSS_LegendElementColor: '#0080ff',
				DSS_Index: 9
			},{
				DSS_LegendElementType: 'Suburbs', //10,104,104,104,255,Suburbs
				DSS_LegendElementColor: '#686868',
				DSS_Index: 10
			},{
				DSS_LegendElementType: 'Urban', //11,49,49,49,255,Urban
				DSS_LegendElementColor: '#313131',
				DSS_Index: 11
			},{
				DSS_LegendElementType: 'Barren', //12,0,0,0,255,Barren
				DSS_LegendElementColor: '#000000',
				DSS_Index: 12
			}],
			
			DSS_QueryTable: 'cdl',
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

		var lpRiver = Ext.create('MyApp.view.LayerPanel_Continuous', {
			title: 'River',
			DSS_Layer: wmsRivers,
			DSS_LayerUnit: 'm',
			DSS_LayerRangeMin: 0,
			DSS_LayerRangeMax: 915.5,
			DSS_ValueDefaultLess: 120,
			DSS_QueryTable: 'river',
			collapsed: true
		});
		
		var lpGoog = Ext.create('MyApp.view.LayerPanel_Google', {
			DSS_LayerSatellite: googTerrain,
			DSS_LayerHybrid: googHybrid
		});
		
		Ext.getCmp('DSS_LeftPanel').insert(0,lpGoog);
		Ext.getCmp('DSS_LeftPanel').insert(0,lpSlope);
		Ext.getCmp('DSS_LeftPanel').insert(0,lpRiver);
		Ext.getCmp('DSS_LeftPanel').insert(0,lpCDL);
		
		// BOO
		DSS_globalQueryableLayers.push(lpCDL);
		DSS_globalQueryableLayers.push(lpSlope);
		
		var lpSel = Ext.create('MyApp.view.LayerPanel_CurrentSelection', {
			hidden: true,
			DSS_Layer: wmsSlope // NOTE: dummy layer
		});
		Ext.getCmp('DSS_LeftPanel').insert(0,lpSel);
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
					tools: [{
						type: 'up',
						tooltip: 'Show/Hide Logo',
						handler: function() {
							var panel = Ext.getCmp('DSS_LogoPanel');
							if (this.type == 'up') {
								panel.setSize(undefined, 0);
/*								panel.animate({
										to: { height: 0
										},
										duration: 100
								});*/
								this.setType('down');
							}
							else {
								panel.setSize(undefined, DSS_LogoPanelHeight);
/*								panel.animate({
										to: { height: DSS_LogoPanelHeight
										},
										duration: 100
								});
								*/
								this.setType('up');
							}
							panel = Ext.getCmp('DSS_ScenarioPanel');
							panel.setSize(undefined,300);//doComponentLayout();
							
						}
					}]
				}],
				dockedItems: [{
					xtype: 'panel',
					id: 'DSS_LogoPanel',
					frame: false,
					layout: {
						type: 'hbox',
						pack: 'center',
						align: 'stretch'
					},
					header: false,
					dock: 'top',
					collapsible: true,
					animCollapse: false,
					collapsed: false,
					height: DSS_LogoPanelHeight,
					bodyStyle: 'background-color:rgb(220,230,240)',
					items: [{
						xtype: 'image',
//						x: 0,
//						y: 0,
						width: 356,
						src: 'app/images/dss_logo.png',
						autoEl: {
							tag: 'a',
							href: 'http://www.glbrc.org'
						}
					},
					{
						xtype: 'image',
						flex: 1,
						src: 'app/images/globe_icon.png',
						autoEl: {
							tag: 'a',
							href: 'http://www.glbrc.org'
						}
					},
					{
						xtype: 'image',
						flex: 1,
						src: 'app/images/globe_icon.png',
						autoEl: {
							tag: 'a',
							href: 'http://www.glbrc.org'
						}
					}]
				},
				{
					xtype: 'infotoolbar',
					dock: 'bottom'
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
						}]
					}]
					,
					dockedItems: [{
						xtype: 'panel',
						id: 'DSS_ScenarioPanel',
						frameHeader: false,
						border: false,
						autoScroll: true,

						dock: 'bottom',
						height: 300,
						layout: {
							type: 'accordion',
							animate: false,
							multi: true
						},
						dockedItems: [{
							xtype: 'panel',
							dock: 'top',
							title: 'Scenario Setup / Tools',
							icon: 'app/images/magnify_icon.png'
						}],
						items: [{
						// NOTE: Hidden Panel to allow all visible items to collapse.
							xtype: 'panel',
							hidden: true,
							collapsed: false
						},
						{
							xtype: 'transformationtools',
							collapsed: true
						},
						{
							xtype: 'globalscenariotools',
							collapsed: true
						},
						{
							xtype: 'scenariotools',
							collapsed: true
						}]
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
//					animCollapse: false,
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

