
// Toolbar for View / Selection panel

//------------------------------------------------------------------------------
Ext.define('MyApp.view.ViewSelectToolbar', {
		
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.view_select_toolbar',

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
			items: [{
				xtype: 'tbspacer', 
				width: 8
			},
			{	
				xtype: 'button',
				scale: 'small',
				text: 'Expand',
				tooltip: {
					text: 'Expand all query groups',
					showDelay: 100
				},
				border: 1,
				style: {
					borderColor: '#eff',
					borderStyle: 'dotted'
				},
				handler: function(button) {
					button.up().tryExpandAll();
				}
			},
			{	
				xtype: 'button',
				scale: 'small',
				text: 'Expand Queried',
				tooltip: {
					text: 'Expand only queried layers',
					showDelay: 100
				},
				border: 1,
				style: {
					borderColor: '#eff',
					borderStyle: 'dotted'
				},
				handler: function(button) {
					button.up().tryExpandQueried();
				}
			},
			{	
				xtype: 'button',
				scale: 'small',
				text: 'Collapse',
				tooltip: {
					text: 'Collapse all query groups',
					showDelay: 100
				},
				border: 1,
				style: {
					borderColor: '#eff',
					borderStyle: 'dotted'
				},
				handler: function(button) {
					button.up().tryCollapseAll();
				}
			},
			{
				xtype: 'button',
				itemId: 'DSS_queryButton',
				scale: 'small',
				text: 'Run Query',
				iconAlign: 'right',
				tooltip: {
					text: 'Run the current query and show selection results',
					showDelay: 100
				},
				border: 1,
				style: {
					borderColor: '#eff',
					borderStyle: 'dotted'
				},
				handler: function(button, evt, toolEl, owner, tool) {
					button.up().buildQuery();
				}
			}]
        });

        me.callParent(arguments);
    },

    //--------------------------------------------------------------------------
    buildQuery: function() {
    	
       	var requestData = {
    		clientID: 12345, //temp
    		queryLayers: []
    	};

    	var query = false;
    	for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		
    		if (DSS_globalQueryableLayers[i].includeInQuery()) {
    			var queryComp = DSS_globalQueryableLayers[i].setSelectionCriteria();
    			requestData.queryLayers.push(queryComp);
    			query = true;
    		}
    	}
    	
		console.log(requestData);
		if (query) {
			this.submitQuery(requestData);
		}
		else {
			alert("No query built - nothing to query");
		}
    },
    
    //--------------------------------------------------------------------------
    submitQuery: function(queryJson) {
    	
		var button = this.getComponent('DSS_queryButton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);

		var obj = Ext.Ajax.request({
			url: location.href + 'query',
			jsonData: queryJson,
			timeout: 15000, // in milliseconds
			
			success: function(response, opts) {
				
				var obj = JSON.parse(response.responseText);
				console.log("success: ");
				console.log(obj);
				
				// TODO: not 100% sure a delay is needed here? Was added to give server
				//	time to finish writing out file...but if the OK response comes back from the server
				//	...that is AFTER the file write process so the file should be ready?
				// Still, sometimes the file fails to be found if we request the image too fast...
				//	as if the server is still finishing writing it out?
				Ext.defer(function(response) {
					// FIXME: bounds should probably be computed by the server and passed back!!!
					var bounds = new OpenLayers.Bounds(
						-10062652.65061, 5249032.6922889,
						-10062652.65061 + (6150 * 30),
						5249032.6922889 + (4557 * 30)
					);
					var imgTest = new OpenLayers.Layer.Image(
						'Selection',
						obj.url,
						bounds,
						new OpenLayers.Size(2113.0,-2113.0),
						{
							buffer: 0,
							opacity: 1.0,
							isBaseLayer: false,
							displayInLayerSwitcher: false,
							transitionEffect: "resize",
							visibility: true,
							maxResolution: "auto",
							projection: globalMap.getProjectionObject(),
							numZoomLevels: 19
						}
					);
					
					var selectionPanel = Ext.getCmp('CurrentSelectionLayer');
					selectionPanel.setSelectionLayer(imgTest)
					selectionPanel.setNumSelectedPixels(obj.selectedPixels, obj.totalPixels);
			
					var summaryPanel = Ext.getCmp('DSS_ScenarioSummary');
					summaryPanel.expand(true);
					
					button.setIcon(null);
					button.setDisabled(false);
					
				}, 1000, this, [response]);
	
			},
			
			failure: function(respose, opts) {
				button.setIcon(null);
				button.setDisabled(false);
				alert("Query failed, request timed out?");
			}
		});
	},
	
	//--------------------------------------------------------------------------
	tryExpandAll: function() {
		
    	for (var i = 0; i < DSS_globalCollapsibleLayers.length; i++) {
    		
    		var layer = DSS_globalCollapsibleLayers[i];
    		if (layer.DSS_noCollapseTool == false) {
    			layer.expand();
    		}
		}
	},
	
	//--------------------------------------------------------------------------
	tryExpandQueried: function() {
		
    	for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		
    		var layer = DSS_globalQueryableLayers[i];
    		if (layer.includeInQuery()) {
    			layer.expand();
    		}
    		else {
    			layer.collapse();
    		}
		}
	},
	
	//--------------------------------------------------------------------------
	tryCollapseAll: function() {
		
    	for (var i = 0; i < DSS_globalCollapsibleLayers.length; i++) {
    		
    		var layer = DSS_globalCollapsibleLayers[i];
    		if (layer.DSS_noCollapseTool == false) {
    			layer.collapse();
    		}
		}
	}

});