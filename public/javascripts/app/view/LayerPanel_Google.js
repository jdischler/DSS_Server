
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Google', {
    extend: 'Ext.panel.Panel',

    requires: [
		'MyApp.view.LayerPanel_CurrentSelection',
    ],

    title: 'Map Options & Selection Statistics',
//	icon: 'app/images/active_block_icon.png',
    hideCollapseTool: true,
	hidden: false,
	
    bodyPadding: '0 0 10 0',
    bodyStyle: {'background-color': '#fafcff'},
    header: {
    	cls: 'x-accordion-hd',
    	style: {
    		'background-image': 'none',
    		'background-color': '#cdd7df !important',
			'border-width': '2px',
			'border-style': 'solid none solid none',
			'border-color': '#c4d0e0'
    	}
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
            items: [{
            	xtype: 'container',
            	layout: 'absolute',
            	height: 18,
            	items: [{
					xtype: 'label',
					x: 35,
					y: -6,
					html: '<p style="text-align:right">Base Map:</p>',
					width: 95
				},{
					xtype: 'radiofield',
					x: 150,
					y: 3,
					name: 'google',
					boxLabel: 'Simple',
					handler: function(checkbox, checked) {
						if (checked) {
							globalMap.setBaseLayer(me.DSS_LayerSimpleRoads);
						}
					}
				},{
					xtype: 'radiofield',
					x: 225,
					y: 3,
					name: 'google',
					boxLabel: 'Terrain',
					checked: true,
					value: true,
					handler: function(checkbox, checked) {
						if (checked) {
							globalMap.setBaseLayer(me.DSS_LayerTerrain);
						}
					}
				},{
					xtype: 'radiofield',
					x: 302,
					y: 3,
					name: 'google',
					boxLabel: 'Satellite',
					handler: function(checkbox, checked) {
						if (checked) {
							globalMap.setBaseLayer(me.DSS_LayerSatellite);
						}
					}
				}]
			},
			{
				xtype: 'layer_selection'
			}]
        });
        
        me.callParent(arguments);
    }
    
});
