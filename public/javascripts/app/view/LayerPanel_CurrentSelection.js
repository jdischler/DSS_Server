
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_CurrentSelection', {
	extend: 'Ext.container.Container',
    alias: 'widget.layer_selection',

    // Unique ID for this layer, thus there should only be one of these ever?
    id: 'DSS_CurrentSelectionLayer',
    
    DSS_desiredHeight: 60,
	height: 0,

    layout: 'absolute',
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
        		x: 33,
        		y: 18
        	},
        	{
        		xtype: 'label',
        		itemId: 'DSS_selectionPerc',
        		text: '',
        		x: 230,
        		y: 18
        	},{
        		xtype: 'button',
				itemId: 'DSS_showSelectionButon',
        		text: 'Hide Selection',
        		enableToggle: true,
        		pressed: true,
        		x: 45,
        		y: 41,
        		width: 90,
        		handler: function(self) {
					var slider = me.getComponent('DSS_opacitySlider');
					if (self.pressed) {
						slider.show();
						self.setText('Hide Selection');
					}
					else {
						slider.hide();
						self.setText('Show Selection');
					}
					me.DSS_Layer.setVisibility(self.pressed);	
        		}
        	},{
				xtype: 'slider',
				itemId: 'DSS_opacitySlider',
				width: 210,
				x: 133,
				y: 30,
				padding: 10,
				value: 100,
				minValue: 0,
				maxValue: 100,
				increment: 10,
				fieldLabel: 'Selection Opacity',
				labelWidth: 105,
				listeners: {
					change: function(slider, newvalue) {
						me.adjustOpacity(slider);
					},
					scope: me
				}
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    adjustOpacity: function(slider) {
    	
    	var value = slider.getValue() / 100.0;

		if (value < 0.01) value = 0.01;
		else if (value > 0.9999) value = 0.99999; // blugh, value of 1 is more transparent than 0.99??
		
		if (this.DSS_Layer) {
			this.DSS_Layer.setOpacity(value);
		}
    },
    
   //--------------------------------------------------------------------------
    setSelectionLayer: function(selectionLayer) {
		
		// Don't forget to remove the old layer from the map system!
		if (this.DSS_Layer) { 
			globalMap.removeLayer(this.DSS_Layer);
		}
		this.DSS_Layer = selectionLayer;
		globalMap.addLayer(this.DSS_Layer);
		
		this.getComponent('DSS_showSelectionButon').toggle(true);
		var slider = this.getComponent('DSS_opacitySlider');
		if (slider.getValue() < 10) {
			slider.setValue(10);
		}
		slider.show();
		this.DSS_Layer.setOpacity(slider.getValue() / 100.0);

		if (this.getHeight() < this.DSS_desiredHeight) {
			this.setSize(undefined, this.DSS_desiredHeight);
			// OK, I don't know why this panel doesn't size correctly. Seems wrong
			//	to have to do a slightly delayed "jiggle" of the compents, but...blah...
			Ext.defer(function() {
				Ext.getCmp('DSS_LeftPanel').doComponentLayout();
			}, 5, this);
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
		totalAreaPerc = totalAreaPerc.toFixed(3);

		areaLabel.setText(this.DSS_areaText + area + this.DSS_areaUnits);
		areaPerc.setText(this.DSS_percText + totalAreaPerc + '%');
	}
    
});

