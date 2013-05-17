Ext.define('MyApp.view.Slope_Panel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.slope_panel',

    height: 90,
    layout: {
        type: 'absolute'
    },
    collapsible: true,
    manageHeight: false,
	icon: 'app/images/layers_icon.png',
//    title: '<input type="checkbox" /> Slope',
    title: 'Slope',
    titleCollapse: false,
    bodyStyle: {"background-color": "#f8faff"},
    
    //--------------------------------------------------------------------------
	listeners: {
		afterrender: function(c) { 
			
			console.log('After render Slope!');
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
				xtype: 'label',
				x: 0,
				y: 14,
				html: '<p style="text-align:right">Slope</p>',
				width: 60
			},
			{
				xtype: 'button',
				x: 70,
				y: 10,
				width: 30,
				text: '>=',
				tooltip: 'Greater than',
				handler: function(me,evt) {
					if (me.text == '>=') {
						me.setText('>');
					}
					else {
						me.setText('>=');
					}
				}
			},
			{
				xtype: 'numberfield',
				x: 100,
				y: 10,
				width: 70,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: 0.5,
				value: 10
			},
			{
				xtype: 'button',
				x: 190,
				y: 10,
				width: 30,
				text: '<=',
				tooltip: 'Less than',
				handler: function(me,evt) {
					if (me.text == '<=') {
						me.setText('<');
					}
					else {
						me.setText('<=');
					}
				}
			},
			{
				xtype: 'numberfield',
				x: 220,
				y: 10,
				width: 70,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: 0.5
			},
			{
				xtype: 'button',
				x: 300,
				y: 10,
				text: 'Set Selection',
				handler: function(me,evt) {
				}
			},
			{
				xtype: 'label',
				x: 70,
				y: 40,
				text: 'Range of values: 0.0 to 45.5'
			}]
        });

        me.titleCollapes = false;
        me.callParent(arguments);
    }

});
