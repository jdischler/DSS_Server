
Ext.define('MyApp.view.LegendPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.legendpanel',

    requires: [
        'MyApp.view.LegendTitle',
        'MyApp.view.LegendElement'
    ],

    width: 400,
    bodyPadding: 10,
    layout: {
        type: 'absolute'
    },
    
    titleCollapse: false,
    floatable: false,
    bodyStyle: {"background-color": "#f8faff"},
    
	listeners: {
		afterrender: function(c) { 
			
			console.log('After render CDL!');
			var chk = Ext.create('Ext.form.field.Checkbox',
			{
				padding: '0 5 0 2',
				checked: c.DSS_Layer.getVisibility()
			});
			var el = c.header.insert(1,chk);
			chk.on({
				'dirtychange': function(me) {
					if (me.getValue() == true) {
						me.DSS_associatedOpacitySlider.show();
					}
					else
					{
						me.DSS_associatedOpacitySlider.hide();
					}
					c.DSS_Layer.setVisibility(me.getValue());
				},
				scope: c
			});
			
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 20
			});
			el = c.header.insert(3,spc);
			
			var slider = Ext.create('Ext.slider.Single',
			{
				width: 190,
				padding: '0 20 0 10',
				value: 50,
				minValue: 1,
				maxValue: 100,
				increment: 1,
				fieldLabel: 'Opacity',
				labelWidth: 45,
				hidden: !c.DSS_Layer.getVisibility(),
				listeners: {
					change: function(slider, newvalue) {
						c.adjustOpacity(slider);
					},
					scope: c
				}
			});
			
			el = c.header.insert(4, slider);
			chk.DSS_associatedOpacitySlider = slider;
		}
	},

    //--------------------------------------------------------------------------
    adjustOpacity: function(slider) {
    	
    	var value = slider.getValue() / 100.0;

		if (value < 0.01) value = 0.01;
		else if (value > 0.9999) value = 0.99999; // blugh, value of 1 is more transparent than 0.99??
		
    	this.DSS_Layer.setOpacity(value);
    },
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'legendtitle',
				x: 70,
				y: 10
			},
			{
				xtype: 'label',
				x: 0,
				y: 37,
				html: '<p style="text-align:right">Legend</p>',
				width: 60
			},			
			{
				xtype: 'container',
				itemId: 'legendcontainer',
				x: 70,
				y: 31,
				style: {
					border: '1px solid #c0c0c0'
				},
				width: 220,
				layout: {
//					align: 'stretch',
					type: 'vbox'
				}
			},
			{
				xtype: 'button',
				x: 300,
				y: 35,
				text: 'Set Selection'
			}]
        });

        me.callParent(arguments);
        
        var cont = me.getComponent('legendcontainer');
        for (var i = 0; i < me.DSS_LegendElements.length; i++) {
        	// add index for every other colouring
        	me.DSS_LegendElements[i].DSS_LegendElementIndex = i-1;
        	var element = Ext.create('MyApp.view.LegendElement', 
        		me.DSS_LegendElements[i]);
        	cont.insert(i, element);
        }
    }

});
