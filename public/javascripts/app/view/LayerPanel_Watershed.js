
// Panel to assist with working with vector layer selection logic, etc.
//
// If needed, we could consider generalizing this to any sort of vector selection.
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Watershed', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_watershed',

    width: 400,
    height: 66,
//    bodyPadding: '0 0 3 0', // just really need to pad bottom to maintain spacing there
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
				text: 'Activate Click Selection Tool',
				x: 35,
				y: 6,
				height: 20,
				tooltip: {
					text: 'Click on a watershed to include it in your query',
					showDelay: 100
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
				x: 190,
				y: 6,
				height: 20,
				tooltip: {
					text: 'Clear all selected watersehds',
					showDelay: 100
				},
				handler: function(button, evt) {
					var panel = button.up();
					panel.clearSelection();
				}
			}]
        });

        me.callParent(arguments);
    },
	
    //--------------------------------------------------------------------------
    clearSelection: function() {
    	
		this.DSS_selectionLayer.removeAllFeatures();
		this.DSS_watershedSelections = [];
	},

    //--------------------------------------------------------------------------
    setSelectionCriteria: function() {
    	
		var queryLayer = { 
			name: this.DSS_QueryTable,
			type: 'indexed',
			matchValues: []
		};
		
		var addedElement = false;
		for (var i = 0; i < this.DSS_watershedSelections.length; i++) {
			queryLayer.matchValues.push(this.DSS_watershedSelections[i]);
			addedElement = true;
		}
        if (!addedElement) {
        	return;
        }
        return queryLayer;
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
				layer: 'Vector:Watersheds-C',
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
		console.log(viewport);
		viewport.activateClickControlWithHandler(this.clickSelection, this);
		// FIXME: probably want to tie this to the watershed layer visibility?
		this.DSS_selectionLayer.setVisibility(true);
	},

	//--------------------------------------------------------------------------
	disableClickSelection: function() {
		
		var viewport = Ext.getCmp('DSS_MainViewport');
		console.log(viewport);
		
		viewport.deactivateClickControl();
		// FIXME: probably want to tie this to the watershed layer visibility?
		this.DSS_selectionLayer.setVisibility(false);
	}
	
});

