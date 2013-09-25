
//------------------------------------------------------------------------------
Ext.define('MyApp.view.HeatMapLegend', {
    extend: 'Ext.container.Container',
    alias: 'widget.Unique',

    height: 30,
    width: 25,
    layout: {
        type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
        		
            items: [{
				xtype: 'container',
				x: 5,
				y: 1,
				frame: false,
				height: 19,
				width: 20,
				html: '',
				style: {
					'background-color': '#FFBBBB',//DSS_LegendElementColor,
					border: '1px dotted #BBBBBB'
				}
			},
			{
				xtype: 'label',
				x: 1,
				y: 25,
				text: 1,//DSS_LegendElementType
			}]
        });

        me.callParent(arguments);
    },

});

