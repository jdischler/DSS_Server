
var store = Ext.create('Ext.data.TreeStore', {
            
	root: {
		expanded: true
	}
});
/*		children: [
		/*{
			text: 'Land Coverage',
			expanded: true,
			children: [{
				text: 'CDL',
				leaf: true,
				checked: false,
				icon: 'app/images/raster.png',
				qtip: 'Activate a raster overlay of land usage'
			}]
		},
		{
			text: 'Geophysical',
			expanded: true,
			children: [{
				text: 'Digital Elevation',
				leaf: true,
				checked: false,
				icon: 'app/images/raster.png',
				qtip: 'Activate a raster overlay of elevation'
			},
			{	text: 'Slope',
				leaf: true,
				checked: false,
				icon: 'app/images/raster.png',
				qtip: 'Activate a raster overlay of calculated terrain slope'
			},
			{	text: 'Rivers',
				leaf: true,
				checked: false,
				icon: 'app/images/vector.png',
				qtip: 'Activate a vector overlay of all rivers'
			},
			{ 	text: 'Roads',
				leaf: true,
				checked: false,
				icon: 'app/images/vector.png',
				qtip: 'Activate a vector overlay of all roads'
			},
			{	text: 'Public Land',
				leaf: true,
				checked: false,
				icon: 'app/images/vector.png',
				qtip: 'Activate a vector overlay of publicly owned lands'
			},
			{	text: 'Dane County',
				leaf: true,
				checked: false,
				icon: 'app/images/vector.png',
				qtip: 'Activate a vector overlay of Dane County'
			}]
		},
		{
			text: 'Biophysical',
			expanded: false,
			children: [{
				text: 'TODO : Soil',
				leaf: true,
				checked: false
			}]
		},
		{
			text: 'Economic',
			expanded: false,
			children: [{
				text: 'TODO : Marginal Land',
				leaf: true,
				checked: false
			}]
		},
		{
			text: 'Tools',
			expanded: true,
			children: [{
				text: 'Longitude/Latitude Grid',
				leaf: true,
				checked: false,
				icon: 'app/images/grid.png',
				qtip: 'Activate the world lon/lat grid overlay'
			},
			{
				text: 'Scale',
				leaf: true,
				checked: true,
				icon: 'app/images/tool.png',
				qtip: 'Activate the world scale bar overlay'
			},
			{
				text: 'Mini-Map',
				leaf: true,
				checked: true,
				icon: 'app/images/tool.png',
				qtip: 'Activate the world mini-map overlay'
			}]
		}]
	}
});
*/
Ext.define('MyApp.view.LayerPanel', {
    extend: 'Ext.tree.Panel',
    alias: 'widget.layerPanel',

    id: 'mapLayerPanel',
    title: 'View Landscape',
	icon: 'app/images/layers_icon.png',

    store: store,
    rootVisible: false,
    minHeight: 200,
    maxHeight: 500,
    
    cls: 'my-header',
    
    tools:[{
		type: 'help',
		qtip: 'Layer Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],

    //--------------------------------------------------------------------------
    listeners: {
    	checkchange: function(node, checked, eOpts) {
    		
    		if (typeof node.DSS_LayerInfo != 'undefined') {
    			if (node.DSS_LayerInfo.type == 'Layer') {
    				node.DSS_LayerInfo.layer.setVisibility(checked);
    				if (checked) {
    					this.populateSlider(node.DSS_LayerInfo);
    				}
    				else {
    					this.clearSlider();
    				}
    			}
    		}
    	},
    	itemclick: function(me, record, item, index, e, eOpts) {
    		
    		if (typeof record.DSS_LayerInfo != 'undefined') {
    			if (record.DSS_LayerInfo.type == 'Layer') {
    				this.populateSlider(record.DSS_LayerInfo);
    			}
    		}
    	}
    },
    
    //--------------------------------------------------------------------------
    populateSlider: function(layerInfo) {
    	
    	var nameDisplay = Ext.getCmp('DSS_LayerSliderText');
    	var slider = Ext.getCmp('DSS_LayerSlider');
    	
    	nameDisplay.setValue(layerInfo.layer.name);
    	
    	slider.enable();
    	slider.DSS_Layer = layerInfo.layer;
    	slider.setValue(layerInfo.layer.opacity * 100);
    },
    
    //--------------------------------------------------------------------------
    clearSlider: function() {
    	
    	var nameDisplay = Ext.getCmp('DSS_LayerSliderText');
    	var slider = Ext.getCmp('DSS_LayerSlider');
    	
    	nameDisplay.setValue('-');
    	slider.disable();
    },
    
    //--------------------------------------------------------------------------
    adjustOpacity: function(slider) {
    	
    	var value = slider.getValue() / 100.0;

		if (value < 0) value = 0;
		else if (value > 0.9999) value = 0.99999; // blugh, value of 1 is more transparent than 0.99??
		
    	slider.DSS_Layer.setOpacity(value);
    	
    	if (value < 0.001)
    	{
    		if (slider.DSS_Layer.visibility) {
    			slider.DSS_Layer.setVisibility(false);
    		}
    	}
    	else {
    		if (!slider.DSS_Layer.visibility) {
    			slider.DSS_Layer.setVisibility(true);
    		}
    	}
    },
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
        
        Ext.applyIf(me, {
            viewConfig: {
            },
            dockedItems: [{
				xtype: 'toolbar',
				dock: 'bottom',
				items: [{
					xtype: 'container',
					height: 35,
//					maxHeight: 40,
//					minHeight: 40,
					width: 400,
					layout: {
						type: 'absolute'
					},
					items: [{
						id: 'DSS_LayerSliderText',
						xtype: 'displayfield',
						fieldLabel: 'Layer',
						labelWidth: 35,
						value: '-',
						x: 10,
						y: 8
					},
					{
						id: 'DSS_LayerSlider',
						xtype: 'slider',
						x: 175,
						y: 8,
						width: 200,
						fieldLabel: 'Opacity',
						labelWidth: 60,
						value: 50,
						keyIncrement: 10,
						listeners: {
							change: function(slider, newvalue) {
								this.adjustOpacity(slider);
							},/*
							drag: function(slider, e, eOpts) {
								this.adjustOpacity(slider);
							},
							dragend: function(slider, e, eOpts) {
								this.adjustOpacity(slider);
							},
							specialkey: function(me, e, eOpts) {
								console.log(me);
								this.adjustOpacity(me);
							},*/
							scope: this
						}
					}]
				}]
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    addLayer: function(layer, groupName, icon, qtip) {
    	
    	var dataStore = this.getStore();
    	var root = dataStore.getRootNode();
    	var group = root.findChild('text', groupName, true);
    	
    	if (!group || typeof group == 'undefined') {
    		group = root.createNode({
    			text: groupName,
    			expanded: true
    		});
    		root.appendChild(group);
    	}
    	
    	var childLayer = root.createNode({
    		text: layer.name,
    		checked: layer.visibility,
    		leaf: true,
    		icon: icon,
    		qtip: qtip
    	});
    	childLayer.DSS_LayerInfo = {
    		type: 'Layer',
    		layer: layer
    	};
    	group.appendChild(childLayer);
    	
    	if (!this.DSS_FirstAdded || typeof this.DSS_FirstAdded == 'undefined') {
    		this.DSS_FirstAdded = true;
    		this.populateSlider(childLayer.DSS_LayerInfo);
    	}
    }

});
