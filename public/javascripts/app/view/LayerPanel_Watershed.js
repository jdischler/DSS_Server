
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
				text: me.DSS_unpressedText,
				x: 70,
				y: 12,
				height: 28,
				width: 152,
				tooltip: {
					text: 'Click on a watershed to include it in your query'
				},
				enableToggle: true,
				handler: function(button, evt) {
					if (button.pressed) {
						// Feature became pressed, so change text to 'turn off' and turn on selection
//						button.setText(me.DSS_pressedText);
						me.tryEnableClickSelection();
					}
					else {
						me.tryDisableClickSelection();
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
					panel.triggerRequery(button, true);
				}
			},{
				// FIXME: this feature causes problems with the click selection tool
				//	until it can be fixed, causes less confusion to toggle this off...
            	xtype: 'button',
            	x: 390,
            	y: 4,
            	width: 23,
            	hidden: true,
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
        me.prepareGetFeatureProtocol();
    },

	//--------------------------------------------------------------------------
    onCollapse: function(panel) {
		this.tryDisableClickSelection();
    },

	//--------------------------------------------------------------------------
	onExpand: function(panel) {
		this.tryEnableClickSelection();
	},
	
    //--------------------------------------------------------------------------
    triggerRequery: function(localButton, force) {
    	
		if (force || this.DSS_watershedSelections.length > 0) {		
			// let the query button managed enabling us...
			if (localButton) {
				localButton.disable(true);
			}
			
			var queryButton = Ext.getCmp('DSS_queryButton');
			queryButton.DSS_associatedButton = localButton;
			queryButton.btnEl.dom.click();
		}
    },
    
	//--------------------------------------------------------------------------
	resetLayer: function() {
		
		// TODO: RESET everything...
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
		this.clearSelection();
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
			queryLayer.matchValues.push(parseInt(this.DSS_watershedSelections[i]));
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
    		console.log('Saying to hide watershed!');
			this.header.getComponent('DSS_ShouldQuery').toggle(false);
    		return;
    	}
    	
		for (var i = 0; i < jsonQuery.queryLayers.length; i++) {
		
			var queryElement = jsonQuery.queryLayers[i];
			
			// in query?
			if (queryElement && queryElement.name == this.DSS_QueryTable) {
				// yup
				this.show();
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
//		console.log('Saying to hide watershed!');
//		this.header.getComponent('DSS_ShouldQuery').toggle(false);
//		this.hide();
    },

	//--------------------------------------------------------------------------
    prepareGetFeatureProtocol: function() {
    	
		var protocol = OpenLayers.Protocol.WFS.fromWMSLayer(this.DSS_Layer);
        
        // BLAH, route through the proxy...Not sure why this doesn't automatically work?
        var urls = protocol.url.slice(0);
        urls[0] = location.href + "openLayersProxy?" + urls[0]; 
        protocol.url = urls;
        protocol.options.url = urls;
        
        this.DSS_protocol = protocol;
    },

	//--------------------------------------------------------------------------
	tryEnableClickSelection: function() {

		console.log('WaterShed::tryEnableClickSelection');
		var button = this.getComponent('DSS_watershedClickActivation');
		if (!button.pressed) {
//			button.btnEl.dom.click();
			button.toggle(true, true);
		}
			button.setText(this.DSS_pressedText);
			this.enableClickSelection();
	//	}
	},
	
	//--------------------------------------------------------------------------
	enableClickSelection: function() {
		
		console.log('WaterShed::enableClickSelection');
		var viewport = Ext.getCmp('DSS_MainViewport');
		viewport.activateClickControlWithHandler(this.clickSelection, this.unClickSelection, 
			this.DSS_protocol, this);
		
		// FIXME: probably want to tie this to the watershed layer visibility?
		this.DSS_selectionLayer.setVisibility(true);
		
		// When click selection is on, show the layer...Make sense?
		this.DSS_Layer.setVisibility(true);
		this.DSS_Layer.setOpacity(0.6);		
	},

	//--------------------------------------------------------------------------
	tryDisableClickSelection: function() {

		console.log('WaterShed::tryDisableClickSelection');
		var button = this.getComponent('DSS_watershedClickActivation');
		if (button.pressed) {
//			button.btnEl.dom.click();
			button.toggle(false, true);
		}
			button.setText(this.DSS_unpressedText);
			this.disableClickSelection();
		//}
	},
	
	//--------------------------------------------------------------------------
	disableClickSelection: function() {
		
		console.log('WaterShed::disableClickSelection');
		var viewport = Ext.getCmp('DSS_MainViewport');
		viewport.deactivateClickControl();
		// FIXME: probably want to tie this to the watershed layer visibility?
		this.DSS_Layer.setVisibility(false);
		this.DSS_selectionLayer.setVisibility(false);
	},
	
	//--------------------------------------------------------------------------
	clickSelection: function(event) {

		console.log('WaterShed::clickSelection');
		var feature = event.feature;
		var idxs = feature.fid.split(".");
		// FIXME: BS, minus one because the geoserver gives the index back 1 based vs. zero based
		var realIdx = idxs[1] - 1;
		var pos = this.DSS_watershedSelections.indexOf(realIdx);
		if (pos < 0) {
			this.DSS_selectionLayer.addFeatures(feature);
			this.DSS_watershedSelections.push(realIdx);
		}
		else if (event.object.modifiers.toggle == true) {
			this.DSS_watershedSelections.splice(pos,1);
			var featureObj = this.DSS_selectionLayer.getFeatureBy('fid',feature.fid);
			this.DSS_selectionLayer.removeFeatures(featureObj);
		}
	},

	//--------------------------------------------------------------------------
	unClickSelection: function(event) {

		console.log('WaterShed::unClickSelection');
		var feature = event.feature;
		var idxs = feature.fid.split(".");
		// FIXME: BS, minus one because the geoserver gives the index back 1 based vs. zero based
		var realIdx = idxs[1] - 1;
		var pos = this.DSS_watershedSelections.indexOf(realIdx);
		if (pos >= 0) {
			this.DSS_watershedSelections.splice(pos,1);
			var featureObj = this.DSS_selectionLayer.getFeatureBy('fid', feature.fid);
			this.DSS_selectionLayer.removeFeatures(featureObj);
		}
	}
		
});

