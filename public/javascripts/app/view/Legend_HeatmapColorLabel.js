
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Legend_HeatmapColorLabel', {
    extend: 'Ext.form.Label',
    alias: 'widget.heatmapcolorlabel',

    width: 50,
	style: {
		'text-align': 'center',
		'font-size': '80%'
	},

    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        var valueFixing = 3;
        var value = Math.abs(me.DSS_ElementValue);
        
        if (value <= 1) valueFixing++;
        else if (value >= 1000) valueFixing--;
        else if (value >= 10000) valueFixing--;
        else if (value >= 100000) valueFixing--;
        
        this.text = (me.DSS_ElementValue == 0) ? '0.0' : 
						(me.DSS_ElementValue ? me.DSS_ElementValue.toFixed(valueFixing) : me.DSS_ElementValue);

        me.callParent(arguments);
    }

});

