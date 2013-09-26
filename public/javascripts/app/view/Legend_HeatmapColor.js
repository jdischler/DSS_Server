
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
				x: 2,
				y: 1,
				frame: false,
				height: 40,
				width: 56,
				html: '',
				style: {
					'background-color': me.DSS_ElementColor,
					border: '1px dotted #BBBBBB'
				}
			},{
				xtype: 'label',
				x: 5,
				y: 20,
				text: me.DSS_ElementValue.toFixed(3)
			}]
        });

        me.callParent(arguments);
    }

});

