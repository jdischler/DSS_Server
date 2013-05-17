
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Common', {
    extend: 'Ext.panel.Panel',

    layout: {
        type: 'absolute'
    },
    
	icon: 'app/images/layers_icon.png',
    titleCollapse: false,
    floatable: false,
    bodyStyle: {"background-color": "#f8faff"},

	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) { 
			
			var chk = Ext.create('Ext.form.field.Checkbox',
			{
				padding: '0 5 0 2',
				checked: c.DSS_Layer.getVisibility()
			});
			var el = c.header.insert(1,chk);
			chk.on({
				'dirtychange': function(me) {
					if (me.getValue() == true) {
						me.DSS_associatedOpacitySlider.show();
					}
					else
					{
						me.DSS_associatedOpacitySlider.hide();
					}
					c.DSS_Layer.setVisibility(me.getValue());
				},
				scope: c
			});
			
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 20
			});
			el = c.header.insert(3,spc);
			
			var slider = Ext.create('Ext.slider.Single',
			{
				width: 190,
				padding: '0 20 0 10',
				value: 50,
				minValue: 1,
				maxValue: 100,
				increment: 1,
				fieldLabel: 'Opacity',
				labelWidth: 45,
				hidden: !c.DSS_Layer.getVisibility(),
				listeners: {
					change: function(slider, newvalue) {
						c.adjustOpacity(slider);
					},
					scope: c
				}
			});
			
			el = c.header.insert(4, slider);
			chk.DSS_associatedOpacitySlider = slider;
		}
	},

    //--------------------------------------------------------------------------
    adjustOpacity: function(slider) {
    	
    	var value = slider.getValue() / 100.0;

		if (value < 0.01) value = 0.01;
		else if (value > 0.9999) value = 0.99999; // blugh, value of 1 is more transparent than 0.99??
		
    	this.DSS_Layer.setOpacity(value);
    },
    
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    submitQuery: function(queryJson) {
    	
		var obj = Ext.Ajax.request({
//    		url: 'http://dss.wei.wisc.edu:9000/query',
			url: 'http://localhost:9000/query',
			jsonData: queryJson,
			timeout: 2000,
			
			success: function(response, opts) {
				console.log("success: ");
				console.log(response);
				
				Ext.defer(function(response) {
					var bounds = new OpenLayers.Bounds(
						-10035269.3627204, 5259982.9002571,
						-9882534.26873933, 5386224.15842662
					);
					var imgTest = new OpenLayers.Layer.Image(
						'Test',
						response.responseText,
						bounds,
						new OpenLayers.Size(2113.0,-2113.0),
						{
							buffer: 0,
							opacity: 0.5,
							isBaseLayer: false,
							displayInLayerSwitcher: false,
							transitionEffect: "resize",
							visibility: true,
							maxResolution: "auto",
							projection: globalMap.getProjectionObject(),
							numZoomLevels: 16
						}
					);
					var layerBrowser = Ext.getCmp('mapLayerPanel');
					layerBrowser.addLayer(imgTest, 'Geophysical', 'app/images/raster.png',
							'Test layer of images!!');
					globalMap.addLayer(imgTest);
			
				}, 1000, this, [response]);
	
			},
			
			failure: function(respose, opts) {
				alert("Query Failsauce");
			}
		});
	}

});
