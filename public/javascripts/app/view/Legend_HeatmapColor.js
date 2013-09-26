
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Legend_HeatmapColor', {
    extend: 'Ext.container.Container',
    alias: 'widget.heatmapcolor',

    width: 60,
    height: 40,
    layout: {
        type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
            items: [{
            	xtype: 'container',
				x: 20,
				y: 0,
				frame: false,
				height: 20,
				width: 55,
				html: '',
				style: {
					'background-color': me.DSS_ElementColor,
					border: '1px dotted #BBBBBB'
				}
			},{
				xtype: 'label',
				x: 0,
				y: 25,
				text: me.DSS_ElementValue.toFixed(2)
			}]
        });

        me.callParent(arguments);
    }

});

