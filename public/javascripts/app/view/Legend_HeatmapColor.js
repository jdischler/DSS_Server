
/*	xtype: 'container',
	id: 'DSS_heatmap_legend',
	x: -1,
	y: 310,
	
	width: 502,
	height: 40,
	layout: {
		type: 'hbox'
	}
*/

//------------------------------------------------------------------------------
Ext.define('MyApp.view.Legend_HeatmapColor', {
    extend: 'Ext.container.Container',
    alias: 'widget.heatmapcolor',

    width: 82,
    height: 40,
    layout: {
        type: 'absolute'
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        var valueFixing = 3;
        var value = Math.abs(me.DSS_ElementValue);
        
        if (value <= 1) valueFixing++;
        else if (value >= 1000) valueFixing--;
        else if (value >= 10000) valueFixing--;
        
        Ext.applyIf(me, {
            items: [{
            	xtype: 'container',
				x: 35,
				y: 0,
				frame: false,
				height: 23,
				width: 80,
				html: '',
				style: {
					'background-color': me.DSS_ElementColor,
					border: '1px dotted #BBBBBB'
				}
			},{
				xtype: 'label',
				x: 0,
				y: 24,
				width: 75,
				style: {
					'text-align': 'center'
				},
				text: (me.DSS_ElementValue == 0) ? '0.0' : 
						(me.DSS_ElementValue ? me.DSS_ElementValue.toFixed(valueFixing) : me.DSS_ElementValue)
			}]
        });

        me.callParent(arguments);
        
        if (typeof me.DSS_ElementValueLast !== 'undefined') {
			var valueFixing = 3;
			var value = Math.abs(me.DSS_ElementValueLast);
			
			if (value <= 1) valueFixing++;
			else if (value >= 1000) valueFixing--;
			else if (value >= 10000) valueFixing--;
        	var lbl = Ext.create('Ext.form.Label', {
        		x: 75,
        		y: 24,
				width: 75,
				style: {
					'text-align': 'center'
				},
        		text: (me.DSS_ElementValueLast == 0) ? '0.0' : 
						(me.DSS_ElementValueLast ? me.DSS_ElementValueLast.toFixed(valueFixing) : me.DSS_ElementValueLast)
				});
			me.add(lbl);
        	
        }
    }

});

