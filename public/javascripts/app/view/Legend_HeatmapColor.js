
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Legend_HeatmapColor', {
    extend: 'Ext.container.Container',
    alias: 'widget.heatmapcolor',

    width: 65,
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
				width: 62,
				html: '',
				style: {
					'background-color': me.DSS_ElementColor,
					border: '1px dotted #BBBBBB'
				}
			},{
				xtype: 'label',
				x: 4,
				y: 25,
				text: (me.DSS_ElementValue == 0) ? '0.0' : 
						(me.DSS_ElementValue ? me.DSS_ElementValue.toFixed(3) : me.DSS_ElementValue)
			}]
        });

        me.callParent(arguments);
        
        if (typeof me.DSS_ElementValueLast !== 'undefined') {
        	var lbl = Ext.create('Ext.form.Label', {
        		x: 60,
        		y: 25,
        		text: (me.DSS_ElementValueLast == 0) ? '0.0' : 
						(me.DSS_ElementValueLast ? me.DSS_ElementValueLast.toFixed(3) : me.DSS_ElementValueLast)
				});
			me.add(lbl);
        	
        }
    }

});

