
// Panel to assist with working with vector layer selection logic, etc.
//
// If needed, we could consider generalizing this to any sort of vector selection.
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Watershed', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_watershed',

    width: 400,
    height: 86,
	DSS_unpressedText: 'Activate Click Selection Tool',
	DSS_pressedText: 'Deactivate Selection Tool',
   
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;

		this.DSS_selectionLayer = new OpenLayers.Layer.Vector("Watershed Features", {
			displayInLayerSwitcher: false, 
			isBaseLayer: false 
		});
		
		globalMap.addLayer(this.DSS_selectionLayer);
		
		this.DSS_watershedSelections = [];

        Ext.applyIf(me, {
            items: [{
				xtype: 'button',
				itemId: 'DSS_watershedClickActivation',
				text: 'Activate Click Selection Tool',
				x: 70,
				y: 12,
				height: 28,
				width: 152,
				tooltip: {
					text: 'Click on a watershed to include it in your query'
				},
				enableToggle: true,
				handler: function(button, evt) {
					var panel = button.up();
					if (button.pressed) {
						// Feature became pressed, so change text to 'turn off' and turn on selection
						button.setText(panel.DSS_pressedText);
						panel.enableClickSelection();
					}
					else {
						button.setText(panel.DSS_unpressedText);
						panel.disableClickSelection();
					}
				}
			},{
				xtype: 'button', 
				text: 'Clear Selection',
				x: 230,
				y: 12,
				height: 28,
				tooltip: {
					text: 'Clear all selected watersheds'
				},
				handler: function(button, evt) {
					var panel = button.up();
					panel.clearSelection();
				}
			},{
            	xtype: 'button',
            	x: 390,
            	y: 4,
            	width: 23,
            	icon: 'app/images/go_icon_small.png',
            	handler: function(self) {
            		me.createOpacityPopup(self);
            	},
            	tooltip: {
            		text: 'Viewable Layer Overlay'
            	}
			},{
            	xtype: 'button',
            	x: 390,
            	y: 30,
            	width: 23,
            	hidden: true,
            	icon: 'app/images/eye_icon.png',
            	handler: function(self) {
            		alert('Query for this layer would be run here...');
            	},
            	tooltip: {
            		text: 'Preview only this criteria selection'
            	}
			}]
        });

        me.callParent(arguments);
        
        me.on('collapse', function(panel) {
			var button = panel.getComponent('DSS_watershedClickActivation');
			button.toggle(false);
			button.setText(panel.DSS_unpressedText);

			panel.clearSelection();
			panel.disableClickSelection();
			panel.DSS_Layer.setVisibility(false);
		});
    },
	
    //--------------------------------------------------------------------------
    clearSelection: function() {
    	
		this.DSS_selectionLayer.removeAllFeatures();
		this.DSS_watershedSelections = [];
	},

    //--------------------------------------------------------------------------
    getSelectionCriteria: function() {
    	
		var queryLayer = { 
			name: this.DSS_QueryTable,
			type: 'indexed',
			matchValues: []
		};
		
		var addedElement = false;
		for (var i = 0; i < this.DSS_watershedSelections.length; i++) {
			// FIXME: BS, minus one because the geoserver gives the index back 1 based vs. zero based
			queryLayer.matchValues.push(parseInt(this.DSS_watershedSelections[i])-1);
			addedElement = true;
		}
        if (!addedElement) {
        	return;
        }
        return queryLayer;
    },

    //--------------------------------------------------------------------------
    setSelectionCriteria: function(jsonQuery) {

    	if (!jsonQuery || !jsonQuery.queryLayers) {
			this.header.getComponent('DSS_ShouldQuery').toggle(false);
    		return;
    	}
    	
		for (var i = 0; i < jsonQuery.queryLayers.length; i++) {
		
			var queryElement = jsonQuery.queryLayers[i];
			
			// in query?
			if (queryElement.name == this.DSS_QueryTable) {
				// yup
				this.header.getComponent('DSS_ShouldQuery').toggle(true);
				
				// start with a clean slate, then select only the ones that need it
				this.clearSelection();
				// get each match value in the query...
				for (var j = 0; j < queryElement.matchValues.length; j++) {
					this.DSS_watershedSelections.push(queryElement.matchValues[j]);
					
					// FIXME: TODO: Need to somehow get the relevant feature vectors!!!??
				}
				return;
        	}
        }
				
		// Nope, mark as not queried
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
    },

	//--------------------------------------------------------------------------
	clickSelection: function(event) {

		var me = this;
		
		console.log(event);
		OpenLayers.Element.addClass(globalMap.viewPortDiv, "olCursorWait");
		var obj = Ext.Ajax.request({
			url: location.href + 'wmsRequest',
			method: 'POST',
			jsonData: {
				layer: 'Vector:Watershed-New',
				x: event.xy.x,
				y: event.xy.y,
				width: globalMap.getSize().w,
				height: globalMap.getSize().h,
				bbox: globalMap.getExtent().toBBOX()
			},
			timeout: 15000, // in milliseconds
			
			success: function(response, opts) {
				
				var gmlParser = new OpenLayers.Format.GML.v3();
				
				var obj = gmlParser.read(response.responseText);
				console.log(obj);
				OpenLayers.Element.removeClass(globalMap.viewPortDiv, "olCursorWait");
				// FIXME: pick standard keypress that works for all platforms??
				//	alt key APPENDS selections...
				if (event.altKey == false) {
					// so if it ISN'T pressed, clear the selection so we can add new stuffs....
					me.clearSelection();
				}
				var add = []; // track the vector features to add....
				for (var i = 0; i < obj.length; i++) {
					var idxs = obj[i].fid.split(".");
					var pos = me.DSS_watershedSelections.indexOf(idxs[1]);
					if (pos < 0) {
						add.push(obj[i]);
						me.DSS_watershedSelections.push(idxs[1]);
					}
				}
				me.DSS_selectionLayer.addFeatures(add);
			},
			
			failure: function(respose, opts) {
				OpenLayers.Element.removeClass(globalMap.viewPortDiv, "olCursorWait");
			}
		});
	},
	
	//--------------------------------------------------------------------------
	enableClickSelection: function() {
		
		var viewport = Ext.getCmp('DSS_MainViewport');
		viewport.activateClickControlWithHandler(this.clickSelection, this);
		// FIXME: probably want to tie this to the watershed layer visibility?
		this.DSS_selectionLayer.setVisibility(true);
		
		// When click selection is on, show the layer...Make sense?
		this.DSS_Layer.setVisibility(true);
		this.DSS_Layer.setOpacity(0.6);		
	},

	//--------------------------------------------------------------------------
	disableClickSelection: function() {
		
		var viewport = Ext.getCmp('DSS_MainViewport');
		
		viewport.deactivateClickControl();
		// FIXME: probably want to tie this to the watershed layer visibility?
		this.DSS_selectionLayer.setVisibility(false);
	},
	
	//--------------------------------------------------------------------------
	resetLayer: function() {
		
		// TODO: RESET everything...
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
		this.clearSelection();
	}
	
});

