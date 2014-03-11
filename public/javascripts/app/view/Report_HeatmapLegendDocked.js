
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_HeatmapLegendDocked', {
    extend: 'Ext.container.Container',
    alias: 'widget.docked_heat_legend',

    requires: [
    	'MyApp.view.Legend_HeatmapColorChip',
    	'MyApp.view.Legend_HeatmapColorLabel'
    ],
    
	id: 'DSS_heatmap_legend',
    width: 420,

    // FIXME: Hiding and showing would just be preferable but something goes wrong with the border styling
    //	with hiding and showing? So...keeping it visible but at a height of 1...then sizing up to "show"
    height: 30, //1,
    DSS_OpenedHeight: 30,
	hidden: false,
    dock: 'bottom',
    
    layout: {
    	type: 'hbox',
    	pack: 'end' // right
    },
	padding: 2,
	style: {
		'background-color': '#D3E1F1',
		border: '1px solid #cdd'
	},

    initComponent: function() {
        var me = this;
         
        Ext.applyIf(me, {
            items: [{
				xtype: 'container',
				flex: 1,
				layout: {
					type: 'hbox',
					pack: 'center'
				},
				items: [{
					xtype: 'container',
					layout: {
						type: 'vbox',
						align: 'center'
					},
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
			},
			{
            	xtype: 'tool',
				type: 'down',
				width: 15,
				handler: function() {
					var container = Ext.getCmp('DSS_heatmap_legend');
					container.hide();
            	}
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
		
		if (this.isHidden()) {
			this.show();
		}
		if (this.getHeight() < this.DSS_OpenedHeight) {
			this.setHeight(this.DSS_OpenedHeight);
		}
		
		Ext.resumeLayouts(true);
    }

});

