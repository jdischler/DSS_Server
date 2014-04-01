
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_HeatmapLegendPopUp', {
    extend: 'Ext.window.Window',

    requires: [
    	'MyApp.view.Legend_HeatmapColorChip',
    	'MyApp.view.Legend_HeatmapColorLabel'
    ],
    
    // TODO: FIXME: client body is too small if there's a scroll bar in it...
    //	Maybe size window based on the client body size needs vs. just blindly setting
    //	a window height?
    height: 65,
    width: 420,
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    resizable: false,
    closeAction: 'hide',
    layout: {
    	type: 'hbox',
    	pack: 'center'
    },
//	overflowX: 'scroll',
	hidden: true,
	DSS_everShown: false,

	id: 'DSS_heatmap_legend',

    initComponent: function() {
        var me = this;
                    
        Ext.applyIf(me, {
            items: [{
				xtype: 'container',
				layout: {
					type: 'vbox',
					align: 'center'
				},
				padding: 2,
				items: [{
					id: 'DSS_heatmapLegendColorKey',
					xtype: 'container',
					height: 16,
					layout: 'hbox',
					items: []
				},{
					id: 'DSS_heatmapLegendValue',
					xtype: 'container',
					height: 18,
					layout: 'hbox',
					items: []
				}]
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    clearKeys: function(suspendLayouts) {
    	
    	if (suspendLayouts) {
    		Ext.suspendLayouts();
    	}
    	
    	Ext.getCmp('DSS_heatmapLegendColorKey').removeAll();
    	Ext.getCmp('DSS_heatmapLegendValue').removeAll();
    	
    	if (suspendLayouts) {
    		Ext.resumeLayouts(true);
    	}
    },
    
    //--------------------------------------------------------------------------
    setKeys: function(title, keyFromServer) {

		Ext.suspendLayouts();
		
		this.setTitle(title);
		this.clearKeys(false); // don't suspend in the helper 
		
    	var colorKey = Ext.getCmp('DSS_heatmapLegendColorKey');
    	var labelKey = Ext.getCmp('DSS_heatmapLegendValue');

    	for (var idx = 0; idx < keyFromServer.palette.length; idx++) {
			var obj = {
				DSS_ElementColor: keyFromServer.palette[idx],
				DSS_ElementValue: keyFromServer.values[idx],
			};
			
			var element = Ext.create('MyApp.view.Legend_HeatmapColorChip', obj);
			colorKey.add(element);

			element = Ext.create('MyApp.view.Legend_HeatmapColorLabel', obj);
			labelKey.add(element);
			
			// Always try to add one more label....
			if (idx == keyFromServer.palette.length - 1) {
				element = Ext.create('MyApp.view.Legend_HeatmapColorLabel', {
						DSS_ElementValue: keyFromServer.values[idx+1]
				});
				labelKey.add(element);
			}
		}
		
		Ext.resumeLayouts(true);
		
		if (!this.DSS_everShown) {
			var mapPanel = Ext.get('DSS_map_panel');
			
			this.showAt(mapPanel.getX() + (mapPanel.getWidth() - this.width) / 2,
				mapPanel.getY() + (mapPanel.getHeight() - 16) - this.height);
		}
		else if (this.isHidden()) {
			this.show();
		}
		
		this.DSS_everShown = true;
    }

});

