
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LegendTitle', {
    extend: 'Ext.container.Container',
    alias: 'widget.legendtitle',

    height: 21,
    width: 178,
    layout: {
        type: 'absolute'
    },

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            style: {
                'background-color': '#f0f0f0',
                border: '1px solid #c0c0c0'
            },
            items: [{
				xtype: 'label',
				x: 0,
				y: 1,
				width: 30,
				style: {
					'text-align': 'center',
//					'font-weight': 'bold',
					'color': '#789'
				},
				text: 'Key'
			},
			{
				xtype: 'label',
				x: 31,
				y: 1,
				width: 45,
				style: {
					'text-align': 'left',
//					'font-weight': 'bold',
					'color': '#789'
				},
				text: 'Type'
			},
			{
				xtype: 'label',
				x: 125,
				y: 1,
				width: 50,
				style: {
					'text-align': 'center',
//					'font-weight': 'bold',
					'color': '#789'
				},
				text: 'Query'
			}]
        });

        me.callParent(arguments);
    }

});

