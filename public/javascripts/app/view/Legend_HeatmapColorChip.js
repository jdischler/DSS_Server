
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Legend_HeatmapColorChip', {
    extend: 'Ext.container.Container',
    alias: 'widget.heatmapcolorchip',

    width: 50,
    height: 16,
	
	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        this.style = {		
        	border: '1px dotted #BBBBBB',
        	'background-color': me.DSS_ElementColor
        };

		me.callParent(arguments);
	}
	
});

