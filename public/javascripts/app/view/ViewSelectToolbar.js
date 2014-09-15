
// Toolbar for View / Selection panel

var DSS_DoExpandQueried = true;
var DSS_ViewSelectToolbar = null;

//------------------------------------------------------------------------------
Ext.define('MyApp.view.ViewSelectToolbar', {
		
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.view_select_toolbar',

	style: {
    	'background-color': '#ADC5B5'
    },

    requires: [
    	'MyApp.view.AddCriteriaPopup'
    ],
    	
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        DSS_ViewSelectToolbar = this;
        
        Ext.applyIf(me, {
			items: [{
				xtype: 'button',
				scale: 'medium',
				text: 'Add Criteria to Land Selection',
				icon: 'app/images/add_icon.png',
				tooltip: {
					text: "View a list of criteria that can be added to refine what land you wish to transform"
				},
				border: 1,
				handler: function(button) {
					button.setIcon('app/images/24_drop_icon.png');
					button.up().tryShowCriteriaLayers(button);
				}
			},
			{
				xtype: 'tbspacer', 
				width: 131
			},
			{
				xtype: 'button',
				id: 'DSS_queryButton',
				hidden: true,
				scale: 'medium',
				text: 'Preview Selection',
				icon: 'app/images/eye_icon.png',
				iconAlign: 'right',
				tooltip: {
					text: 'Show the combined selection results for the specified criteria'
				},
				border: 1,
				handler: function(button, evt, toolEl, owner, tool) {
					var panel = button.up();
					var query = panel.buildQuery();
					if (query) {
						panel.submitQuery(query);
					}
					else {
						alert("No query built - nothing to query");
					}
				}
			}]
        });

        me.callParent(arguments);
    },

	// Takes the json query that goes to the server and sets up each of the query
	//	layer panels to the settings that match the query
	//--------------------------------------------------------------------------
	setUpSelectionFromQuery: function(queryJson) {
		
		for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
		
			DSS_globalQueryableLayers[i].setSelectionCriteria(queryJson);
		}
		if (DSS_DoExpandQueried) {
			this.tryExpandQueried();
		}
	},
    
    //--------------------------------------------------------------------------
    buildQuery: function() {
    	
       	var requestData = {
    		clientID: 12345, // FIXME: temp
    		queryLayers: []
    	};

    	var query = false;
    	for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		
    		if (DSS_globalQueryableLayers[i].includeInQuery()) {
    			var queryComp = DSS_globalQueryableLayers[i].getSelectionCriteria();
				requestData.queryLayers.push(queryComp);
    			query = true;
    		}
    	}
    	
		if (query) {
			return requestData;
		}
		else {
			return null;
		}
    },
    
    //--------------------------------------------------------------------------
    submitQuery: function(queryJson) {
    	
    	var me = this;
		var button = Ext.getCmp('DSS_queryButton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);

		var obj = Ext.Ajax.request({
			url: location.href + 'query',
			jsonData: queryJson,
			timeout: 25000, // in milliseconds
			
			success: function(response, opts) {
				
				var obj = JSON.parse(response.responseText);
//				console.log("success: ");
//				console.log(obj);

				me.tryCreateSelectionLayer(obj, 0);			
			},
			
			failure: function(respose, opts) {
				button.setIcon('app/images/eye_icon.png');
				button.setDisabled(false);
				if (button.DSS_associatedButton) {
					button.DSS_associatedButton.setDisabled(false);
				}
				alert("Query failed, request timed out?");
			}
		});
	},
	
	// NOTE: This got kinda messy...basically the problem is that the server could signal back
	//	that the selection image has been created but the file system might still be saving it at that point??
	// Just guessing...but the file doesn't always appear to be ready when it ought to be.
	//	so I delay the attempt to use it...and even try to validate that it could be used
	//	before actually doing so...hope this fixes the problem. It's possible this could even be
	//	simplified...?
	//--------------------------------------------------------------------------
	tryCreateSelectionLayer: function(json, tryCount) {
		
    	var me = this;
		var button = Ext.getCmp('DSS_queryButton');
		
		console.log('Doing a try create selection layer');
		
		// waits a small amount of time...then checks to see if they image could load...
		Ext.defer(function() {
				
			var tester = new Image();
			
			// Set up a SUCCESS handler...
			//----------------------------
			tester.onload = function() {
				// FIXME: bounds should probably be computed by the server and passed back!!!
				var bounds = new OpenLayers.Bounds(
					-10062652.65061, 5278060.469521415,
					-9878152.65061, 5415259.640662575
				);
				var imgTest = new OpenLayers.Layer.Image(
					'Selection',
					json.url,
					bounds,
					new OpenLayers.Size(2113.0,-2113.0),
					{
						buffer: 0,
						opacity: 0.5,
						isBaseLayer: false,
						displayInLayerSwitcher: false,
						transitionEffect: "resize",
						visibility: true,
						maxResolution: "auto",
						projection: globalMap.getProjectionObject(),
						numZoomLevels: 19
					}
				);
				
				var selectionPanel = Ext.getCmp('DSS_CurrentSelectionLayer');
				selectionPanel.setSelectionLayer(imgTest)
				selectionPanel.setNumSelectedPixels(json.selectedPixels, json.totalPixels);
		
				var summaryPanel = Ext.getCmp('DSS_ScenarioSummary');
				summaryPanel.expand(true);
				
				button.setIcon('app/images/eye_icon.png');
				button.setDisabled(false);
				if (button.DSS_associatedButton) {
					button.DSS_associatedButton.setDisabled(false);
				}
			};
			// Set up a failure handler...
			//-----------------------
			tester.onerror = function() {
				tryCount++;
				if (tryCount < 10) {
					me.tryCreateSelectionLayer(json, tryCount);
				}
				else {
					console.log(' Image not ready yet...and lets give up...');
					button.setIcon('app/images/eye_icon.png');
					button.setDisabled(false);
					if (button.DSS_associatedButton) {
						button.DSS_associatedButton.setDisabled(false);
					}
				}
			};
			
			tester.src = json.url;
			
		}, 50 + tryCount * 100, this);
	},
	
	//--------------------------------------------------------------------------
	resetAllLayers: function() {
		
		Ext.suspendLayouts();
    	for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		
    		var layer = DSS_globalQueryableLayers[i];
			layer.resetLayer();
			layer.hide();
		}
		// Hide the selection layer...
		var selContainer = Ext.getCmp('DSS_CurrentSelectionLayer');
		if (selContainer.DSS_Layer) {
			selContainer.DSS_Layer.setVisibility(false);
		}
		selContainer.setHeight(0);
		
		Ext.resumeLayouts(true);
	},
	
	//--------------------------------------------------------------------------
	tryExpandAll: function() {
		
		// NOTE: Each layer expand causes a layout calculation...much more efficient
		//	to disable the layout engine, make all of the changes, then do the final layout...
		Ext.suspendLayouts();
    	for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		
    		var layer = DSS_globalQueryableLayers[i];
			layer.expand();
			layer.show();
		}
		Ext.resumeLayouts(true);
	},
	
	//--------------------------------------------------------------------------
	tryExpandQueried: function() {
		
		// NOTE: Each layer expand causes a layout calculation...much more efficient
		//	to disable the layout engine, make all of the changes, then do the final layout...
		Ext.suspendLayouts();
    	for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		
    		var layer = DSS_globalQueryableLayers[i];
    		if (layer.includeInQuery()) {
    			layer.expand();
    			layer.show();
    		}
    		else {
    			layer.collapse();
    			layer.hide();
    		}
		}
		Ext.resumeLayouts(true);
	},
	
	//--------------------------------------------------------------------------
	tryCollapseAll: function() {
		
		// NOTE: Each layer expand causes a layout calculation...much more efficient
		//	to disable the layout engine, make all of the changes, then do the final layout...
		Ext.suspendLayouts();
    	for (var i = 0; i < DSS_globalCollapsibleLayers.length; i++) {
    		
    		var layer = DSS_globalCollapsibleLayers[i];
    		if (layer.DSS_noCollapseTool == false) {
    			layer.collapse();
    			layer.hide();
    		}
		}
		Ext.resumeLayouts(true);
	},
	
	//--------------------------------------------------------------------------
	tryShowCriteriaLayers: function(button) {

		var window = Ext.create('MyApp.view.AddCriteriaPopup').showBy(button.getEl(), "tl-bl?", [4,2]);
	}

});

