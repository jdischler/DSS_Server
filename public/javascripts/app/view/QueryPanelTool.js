Ext.define('MyApp.view.QueryPanelTool', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.querypanel',

    layout: {
        type: 'absolute'
    },
    height: 160,
    title: 'Select Landscape Characteristics',
	icon: 'app/images/query_layer_icon.png',

    tools:[{
		type: 'help',
		qtip: 'Query Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],
    
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'button',
                    x: 180,
                    y: 80,
                    scale: 'medium',
                    text: 'Add to Query',
                    handler: function() {
                    	me.fakeAddToQuery(Ext.getCmp('DSS_CDL').getValue(), 
                    		Ext.getCmp('DSS_slope_gtr').getValue(),
                    		Ext.getCmp('DSS_slope_less').getValue()
                    		);
                    }
                },
                {
                    xtype: 'numberfield',
                    id: 'DSS_CDL',
                    x: 20,
                    y: 10,
                    width: 120,
                    fieldLabel: 'CDL ID',
                    labelWidth: 60,
                    value: 1,
                    hideLabel: false
                },
                {
                    xtype: 'numberfield',
                    id: 'DSS_slope_gtr',
                    x: 20,
                    y: 40,
                    width: 120,
                    fieldLabel: 'Slope: >',
                    labelWidth: 60,
                    hideLabel: false
                },
                {
                    xtype: 'numberfield',
                    id: 'DSS_slope_less',
                    x: 180,
                    y: 40,
                    width: 80,
                    fieldLabel: '<',
                    labelWidth: 10,
                    hideLabel: false
                }//,
/*                {
                    xtype: 'radiogroup',
                    x: 0,
                    y: 5,
                    height: 30,
                    fieldLabel: 'Range',
                    labelAlign: 'right',
                    labelWidth: 50,
                    columns: 1,
                    vertical: true,
                    items: [
                        {
                            xtype: 'radiofield',
                            boxLabel: 'Is equal to:'
                        },
                        {
                            xtype: 'radiofield',
                            boxLabel: 'Is not equal to:'
                        },
                        {
                            xtype: 'radiofield',
                            boxLabel: 'Is less than:'
                        },
                        {
                            xtype: 'radiofield',
                            boxLabel: 'Is within:'
                        },
                        {
                            xtype: 'radiofield',
                            boxLabel: 'Is greater than:'
                        }
                    ]
                },
                {
                    xtype: 'numberfield',
                    x: 180,
                    y: 10,
                    width: 70,
                    fieldLabel: 'Label',
                    hideLabel: true
                },
                {
                    xtype: 'numberfield',
                    x: 260,
                    y: 10,
                    width: 70,
                    fieldLabel: 'Label',
                    hideLabel: true
                }*/
            ]
        });

        me.callParent(arguments);
    },
    
    fakeAddToQuery: function(CDL_id, slope_gtr, slope_less) {
    	
    	var requestData = {};
    	
    	console.log(CDL_id);
    	console.log(slope_gtr);
    	console.log(slope_less);
    	
    	if (CDL_id != null) {
    		requestData.cdl_value = CDL_id;
    	}
    	
    	if (slope_gtr != null || slope_less != null) {
    		var slope = {};
    		if (slope_gtr != null) {
    			slope.greater = slope_gtr;
    		}
    		if (slope_less != null) {
    			slope.less = slope_less;
    		}
    		requestData.slope = slope;
    	}
    	
    	console.log(requestData);
    	
    	var obj = Ext.Ajax.request({
//    		url: 'http://dss.wei.wisc.edu:9000/query',
    		url: 'http://localhost:9000/query',
    		jsonData: requestData,
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
						response.responseText,//'http://dss.wei.wisc.edu:9000/app/file/test2.png',
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
