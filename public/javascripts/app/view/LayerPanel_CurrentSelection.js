
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_CurrentSelection', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_selection',

    // Unique ID for this layer, thus there should only be one of these ever?
    id: 'CurrentSelectionLayer',
    
    title: 'Current Selection',
    DSS_noCollapseTool: true,
    DSS_noQueryTool: true,
    hideCollapseTool: true,

    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			border: '1px dotted #d0d8e7'
    	}
    },

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    setSelectionLayer: function(selectionLayer) {
		
		// Don't forget to remove the old layer from the map system!
		if (this.DSS_Layer) { 
			globalMap.removeLayer(this.DSS_Layer);
		}
		this.DSS_Layer = selectionLayer;
		globalMap.addLayer(this.DSS_Layer);
		
		// Ensure header controls are updated...
		var headerCheck = this.getHeader().getComponent('DSS_visibilityToggle');
		headerCheck.setValue(true);
		var headerSlider = this.getHeader().getComponent('DSS_opacitySlider');
		this.DSS_Layer.setOpacity(headerSlider.getValue() / 100.0);
		
		this.show();
	}
    
});
