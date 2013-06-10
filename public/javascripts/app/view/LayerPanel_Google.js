
// TODO: base this off of the LayerPanel_Common class?
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Google', {
    extend: 'Ext.panel.Panel',

    height: 65,
    layout: {
        type: 'absolute'
    },
    title: 'Base Map',

    hideCollapseTool: true,
    bodyStyle: {'background-color': '#fafcff'},
    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			border: '1px dotted #d0d8e7'
    	}
    },

	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) { 
			
			// Place the collapse/expand tool at the front...
			var tool = Ext.create('Ext.panel.Tool', {
				type: (this.collapsed ? 'plus' : 'minus'),
				tooltip: {
					text: 'Show/Hide Base Map Options',
					showDelay: 100
				},
				toolOwner: c,
				handler: function(evt, toolEl, owner, tool) {
					console.log('Clicked panel tool');
					if (tool.type == 'plus') {
						owner.expand();
						tool.setType('minus');
					} else {
						owner.collapse();
						tool.setType('plus');
					}
				}
			});
			c.header.insert(0,tool);

			// Added to space it out and match the other checks...never used?
			var chk = Ext.create('Ext.form.field.Checkbox',
			{
				padding: '0 5 0 4',
				checked: false,
				fieldStyle: 'position: relative; top: -3px;',
				disabled: true
			});
			var el = c.header.insert(1,chk);
			
			// and one at the end to space out the collapse tool
			spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 10
			});
			c.header.add(spc);
		}
	},
    
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
            items: [{
				xtype: 'label',
				x: 0,
				y: 8,
				html: '<p style="text-align:right">Show</p>',
				width: 60
			},		
			{
				xtype: 'radiofield',
				x: 78,
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
				x: 185,
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
        });
        
        me.callParent(arguments);
    }
    
});
