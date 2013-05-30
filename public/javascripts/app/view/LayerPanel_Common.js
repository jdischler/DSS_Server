
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
    buildQuery: function() {
    	
       	var requestData = {
    		clientID: 12345, //temp
    		queryLayers: []
    	};

    	var query = false;
    	for (var i = 0; i < DSS_globalQueryableLayers.length; i++) {
    		
    		if (!DSS_globalQueryableLayers[i].collapsed) {
    			var queryComp = DSS_globalQueryableLayers[i].setSelection();
    			console.log(queryComp);
    			requestData.queryLayers.push(queryComp);
    			query = true;
    		}
    	}
    	
		console.log(requestData);
		if (query) {
			this.submitQuery(requestData);
		}
		else {
			alert("No query built - nothing to query");
		}
    },
    
    //--------------------------------------------------------------------------
    submitQuery: function(queryJson) {
    	
    	
		var button = this.getComponent('selectionbutton');
		button.setIcon('app/images/spinner_16a.gif');
		button.setDisabled(true);

		var obj = Ext.Ajax.request({
			url: location.href + 'query',
			jsonData: queryJson,
			timeout: 15000, // in milliseconds
			
			success: function(response, opts) {
				console.log("success: ");
				console.log(response);
				
				// TODO: not 100% sure a delay is needed here? Was added to give server
				//	time to finish writing out file...but if the OK response comes back from the server
				//	...that is AFTER the file write process so the file should be ready?
				// Still, sometimes the file fails to be found if we request the image too fast...
				//	as if the server is still finishing writing it out?
				Ext.defer(function(response) {
					var bounds = new OpenLayers.Bounds(
						-10067785.16592, 5246156.162177,
						-10067785.16592 + (4709 * 40.261055644652),
						5246156.162177 + (3868 * 40.261055644652)
//						-10035269.3627204, 5259982.9002571,
//						-9882534.26873933, 5386224.15842662
					);
					var imgTest = new OpenLayers.Layer.Image(
						'Selection',
						response.responseText,
						bounds,
						new OpenLayers.Size(2113.0,-2113.0),
						{
							buffer: 0,
							opacity: 1.0,
							isBaseLayer: false,
							displayInLayerSwitcher: false,
							transitionEffect: "resize",
							visibility: true,
							maxResolution: "auto",
							projection: globalMap.getProjectionObject(),
							numZoomLevels: 19
						}
					);
					var layerBrowser = Ext.getCmp('mapLayerPanel');
					layerBrowser.addLayer(imgTest, 'Selection', 'app/images/raster.png',
							'Selection');
					globalMap.addLayer(imgTest);
			
					button.setIcon(null);
					button.setDisabled(false);
					
				}, 1000, this, [response]);
	
			},
			
			failure: function(respose, opts) {
				button.setIcon(null);
				button.setDisabled(false);
				alert("Query failed, request timed out?");
			}
		});
	}

});
