
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_CurrentSelection', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_selection',

    // Unique ID for this layer, thus there should only be one of these ever?
    id: 'CurrentSelectionLayer',
    
    title: 'Current Selection',
    DSS_noCollapseTool: false,
    DSS_noQueryTool: true,
    hideCollapseTool: true,
    collapsed: false,

    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			border: '1px dotted #d0d8e7'
    	}
    },
    layout: 'absolute',
    bodyPadding: '0 0 10 0',

    // Print text standardizing
    DSS_areaText: 'Area Selected: ',
    DSS_areaUnits: ' km\xb2',
    DSS_percText: '% of study area: ',
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        	items: [{
        		xtype: 'label',
        		itemId: 'DSS_selectionArea',
        		text: '',
        		x: 30,
        		y: 10
        	},
        	{
        		xtype: 'label',
        		itemId: 'DSS_selectionPerc',
        		text: '',
        		x: 230,
        		y: 10
        	}]
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

		if (this.isHidden()) {
			this.show();
			// OK, I don't know why this panel doesn't size correctly. Seems wrong
			//	to have to do a slightly delayed "jiggle" of the compents, but...blah...
			Ext.defer(function() {
				Ext.getCmp('DSS_LeftPanel').doComponentLayout();
			}, 5, this);
		}
		else if (this.getCollapsed()) {
			this.expand();
		}
	},
	
    //--------------------------------------------------------------------------
	setNumSelectedPixels: function(pixelCount, totalPixels) {
		
		console.log(pixelCount);
		
		var areaLabel = this.getComponent('DSS_selectionArea');
		var areaPerc = this.getComponent('DSS_selectionPerc');
		
		// 1 pixel is 30x30 meters...then convert to km sqr
		var area = (pixelCount * 30.0 * 30.0) / 1000000.0;
		area = area.toFixed(1);
		
		// Units should be the same (pixels), so can just divide vs. total to get %?
		var totalAreaPerc = (pixelCount / totalPixels) * 100.0;
		totalAreaPerc = totalAreaPerc.toFixed(2);

		areaLabel.setText(this.DSS_areaText + area + this.DSS_areaUnits);
		areaPerc.setText(this.DSS_percText + totalAreaPerc + '%');
	}
    
});
