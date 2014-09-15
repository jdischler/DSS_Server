
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Google', {
    extend: 'Ext.panel.Panel',

    requires: [
		'MyApp.view.LayerPanel_CurrentSelection',
    ],

    title: 'Map Options',
	icon: 'app/images/active_block_icon.png',
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
            	height: 25,
            	items: [{
					xtype: 'label',
					x: 38,
					y: -4,
					html: '<p style="text-align:right">Base Map:</p>',
					width: 70
				},		
				{
					xtype: 'radiofield',
					x: 128,
					y: 5,
					name: 'google',
					boxLabel: 'Google Hybrid',
					checked: true,
					value: true,
					DSS_LayerValue:	this.DSS_LayerHybrid,
					handler: function(checkbox, checked) {
						// Uh, why reversed value check?
						if (!checked) {
							globalMap.setBaseLayer(this.DSS_LayerValue);
						}
					}
				},
				{
					xtype: 'radiofield',
					x: 245,
					y: 5,
					name: 'google',
					boxLabel: 'Google Satellite',
					DSS_LayerValue: this.DSS_LayerSatellite,
					handler: function(checkbox, checked) {
						// Uh, why reversed value check?
						if (!checked) {
							globalMap.setBaseLayer(this.DSS_LayerValue);
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
