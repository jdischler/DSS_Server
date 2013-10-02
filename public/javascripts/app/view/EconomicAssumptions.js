
//------------------------------------------------------------------------------
Ext.define('MyApp.view.EconomicAssumptions', {
		
    extend: 'Ext.container.Container',
    alias: 'widget.economicassumptions',

    layout: {
        type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'numberfield',
				x: 0,
				y: 10,
				width: 160,
				fieldLabel: 'Corn Price',
				labelAlign: 'right',
				labelPad: 5,
				value: 360.00
			},
			{
				xtype: 'numberfield',
				x: 0,
				y: 40,
				width: 160,
				fieldLabel: 'Soy Price',
				labelAlign: 'right',
				labelPad: 5,
				value: 260.00
			},
			{
				xtype: 'numberfield',
				x: 0,
				y: 70,
				width: 160,
				fieldLabel: 'Alfalfa Price',
				labelAlign: 'right',
				labelPad: 5,
				value: 160.00
			},
			{
				xtype: 'numberfield',
				x: 170,
				y: 70,
				width: 160,
				fieldLabel: 'Switchgrass Price',
				labelAlign: 'right',
				labelPad: 5,
				value: 160.00
			},
			{
				xtype: 'numberfield',
				x: 170,
				y: 40,
				width: 160,
				fieldLabel: 'Ethanol Price',
				labelAlign: 'right',
				labelPad: 5,
				value: 320.00
			},
			{
				xtype: 'numberfield',
				x: 170,
				y: 10,
				width: 160,
				fieldLabel: 'Other Price',
				labelAlign: 'right',
				labelPad: 5,
				value: 60.00
			}]
        });

        me.callParent(arguments);
    }

});
