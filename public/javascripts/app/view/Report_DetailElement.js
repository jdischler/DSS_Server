
// Values used by this WIDGET object - should be provided on the configured object...    
// DSS_Label - the label for the value field
// DSS_UnitLabel - the label that describes what units the value field are in
// DSS_GraphTitle - the title for the popup graph window
// DSS_GraphData - the server passed JSON that contains the data bins for the graphes
// DSS_FieldString - the name passed to the server heatmap function, spider graphs, etc.

//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_DetailElement', {
    extend: 'Ext.container.Container',
    alias: 'widget.report_detail_item',

    requires : [
    	'MyApp.view.Report_GraphPopUp',
    	'MyApp.view.Info_PopUp_HTML',
    	'MyApp.view.Report_CalculatorPopUp'
    ],
    
    width: 500,
    height: 28,
	layout: {
        type: 'absolute'
    },
	
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
	
        // set's this as the default, ie, where is the data source? Delta? File1? File2?
        me.DSS_FieldDataType = 'delta'; 
        // set's this as the default, ie, how is the value displayed? Absolute? %?
        me.DSS_FieldValueType = 'absolute';
        me.DSS_UnformatedValue = 0;
        me.DSS_SubStyleType = 'quantile';
        
        if (!me.DSS_calculators) {
        	me.DSS_calculators = false;
        }
        
        Ext.applyIf(me, {
            items: [
            {
			    itemId: 'DSS_ValueField',  
			    xtype: 'textfield',
			    x: 5,
			    y: 5,
			    width: 260,
			    fieldLabel: me.DSS_Label,
			    fieldStyle: 'text-align: right;',
			    labelWidth: 120,
			    labelAlign: 'right',
				labelSeparator: '',
			    readOnly: true,
				value: '0'
			},{
				xtype: 'label',
				itemId: 'DSS_UnitsLabel',
				x: 272,
				y: 9,
				text: me.DSS_UnitLabelDelta ? me.DSS_UnitLabelDelta : me.DSS_UnitLabel,
				style: {
					color: '#888'
				}
			},{
			    itemId: 'calculator_button',
			    xtype: 'button',
			    x: 335,
			    y: 3,
			    width: 30,
			    disabled: !me.DSS_calculators,
			    padding: '3 0 3 6',
			    icon: 'app/images/calculator_16.png',
			    tooltip: {
			    	text: 'CONVERT this value to other units, such as dollar amounts or metric'
			    },
			    handler: function (self) {
					var mypopup = Ext.create("MyApp.view.Report_CalculatorPopUp", {
						title: me.DSS_GraphTitle,
						DSS_formattedValue: me.getComponent('DSS_ValueField').getValue(),
						DSS_initialValue: me.getComponent('DSS_ValueField').DSS_UnformatedValue,
						DSS_Label: me.DSS_Label,
						DSS_UnitLabel: me.DSS_UnitLabel,
						DSS_calculators: me.DSS_calculators // array of calulators to add...
					});
					mypopup.show();
			    }
			},{
			},{
			    itemId: 'graph_button',
			    xtype: 'button',
			    x: 370,
			    y: 3,
			    width: 30,
			    padding: '3 0 3 6',
			    icon: 'app/images/graph_icon.png',
			    tooltip: {
			    	text: 'GRAPH the results, showing a histogram of this result set'
			    },
			    handler: function (self) {
					var mypopup = Ext.create("MyApp.view.Report_GraphPopUp", {title: me.DSS_GraphTitle});
					mypopup.show();
					mypopup.SetChartData(me.DSS_GraphData);
			    }
			},{
			    itemId: 'heat_delta_button',
			    xtype: 'button',
			    x: 405,
			    y: 3,
			    width: 30,
			    enableToggle: true,
			    padding: '3 0 3 6',
			    icon: 'app/images/map_small_icon.png',
			    tooltip: {
			    	text: 'MAP the results, showing a heatmap overlay of this result set'
			    },
			    handler: function(self) {
			    	me.showHeatmap(self, me.DSS_FieldDataType, me.DSS_SubStyleType);
			    },
			    DSS_DetailReportContainer: me.DSS_DetailReportContainer // pass the parent linkage along...
			},{
			    itemId: 'information_button',
			    xtype: 'button',
			    disabled: false,
			    x: 440,
			    y: 3,
			    width: 30,
			    padding: '3 0 3 6',
			    icon: 'app/images/question_mark_16.png',
			    tooltip: {
			    	text: 'HELP, view information about this model result'
			    },
			    handler: function(self) {
			    	var mypopup = Ext.create('MyApp.view.Info_PopUp_HTML', {
			    		title: me.DSS_GraphTitle, 
			    		DSS_InfoHTML: me.DSS_InfoHTML});
					mypopup.show();
			    }
			}
	    ]});
        
        me.callParent(arguments);
    },

    //--------------------------------------------------------------------------
	clearFields: function() {
		
		this.getComponent('DSS_ValueField').setValue(null);
	},
	
    //--------------------------------------------------------------------------
	clearHeatToggle: function() {
		
		var button = this.getComponent('heat_delta_button');
		this.hideHeatmap(button);
    },

    //--------------------------------------------------------------------------
	setWait: function() {
		var spinnerStyle = {"background-image":"url(app/images/spinner_16a.gif)",
			"background-repeat":"no-repeat","background-position":"center center", 
			"padding-left":"16px"};
		
		this.getComponent('DSS_ValueField').setFieldStyle(spinnerStyle);
		this.getComponent('graph_button').disable();
		this.getComponent('heat_delta_button').disable();
	},
	
    //--------------------------------------------------------------------------
	clearWait: function() {
		
		var clearSpinnerStyle = {"background-image":"none"};

		this.getComponent('DSS_ValueField').setFieldStyle(clearSpinnerStyle);
		this.getComponent('graph_button').enable();
		this.getComponent('heat_delta_button').enable();
	},
	
    //--------------------------------------------------------------------------
	createHeatmapLegend: function(serverData) {
		
		var legendObject = Ext.getCmp('DSS_heatmap_legend');
		
		legendObject.setKeys(this.DSS_GraphTitle, serverData);
	},
	
    //--------------------------------------------------------------------------
    setValueField: function() {
    	
    	if (!this.DSS_FieldData) {
    		return;
    	}
    	
    	var res = null;
    	var unitsLabel = this.DSS_UnitLabelFile ? this.DSS_UnitLabelFile : this.DSS_UnitLabel;
    	
    	if (this.DSS_FieldDataType == 'file1') {
    		res = this.DSS_FieldData.val1; // NOTE: set up in setData
    	}
    	else if (this.DSS_FieldDataType == 'file2') {
    		res = this.DSS_FieldData.val2; // NOTE: set up in setData
    	}
    	else { // type is 'delta'
			if (this.DSS_FieldValueType == 'absolute') {
				res = this.DSS_FieldData.total; // NOTE: set up in setData
				unitsLabel = this.DSS_UnitLabelDelta ? this.DSS_UnitLabelDelta : this.DSS_UnitLabel;
			}
			else { // type is '%'
				res = (this.DSS_FieldData.val2 - this.DSS_FieldData.val1) * 100 / this.DSS_FieldData.val1;
				unitsLabel = '%';
			}
    	}
    	
		this.getComponent('DSS_ValueField').DSS_UnformatedValue = res;

		// workaround comma formatting issue for negative numbers		
		var isNegative = false;
		if (res < 0) {
			isNegative = true;
			res = Math.abs(res);
		}
    	if (res >= 10000) {
    		res = Ext.util.Format.number(res, '0,000');
    	}
    	else if (res >= 1000) {
    		res = Ext.util.Format.number(res, '0,000.0');
    	}
    	else if (res > 100) {
    		res = Ext.util.Format.number(res, '0.00');
    	}
    	else {
    		res = Ext.util.Format.number(res, '0.000');
    	}
    	if (isNegative) {
			res = '-' + res;
    	}
		this.getComponent('DSS_ValueField').setValue(res);
		this.getComponent('DSS_UnitsLabel').setText(unitsLabel);
    },
  
    // valid style types: 'file1', 'file2', 'delta'
    //--------------------------------------------------------------------------
    changeDataStyleType: function(newType) {
    	
    	this.DSS_FieldDataType = newType;
    	this.setValueField();
    },
    
    // valid substyle: 'equal', 'quantile'
    //--------------------------------------------------------------------------
    changeDataSubStyle: function(substyle) {
    	
    	this.DSS_SubStyleType = substyle;
    },
    
    // valid style types: 'absolute', '%'
    //--------------------------------------------------------------------------
	changeValueStyleType: function(newType) {
		
		this.DSS_FieldValueType = newType;
    	this.setValueField();
    },

    //--------------------------------------------------------------------------
	// OBJ Data comes in with this format
	// obj.*model_name*	// where model name is something like 'habitat_index', 'soc', 'nitrogen', etc.
	//		.file1		// right now, Default, but could be any model run when arbitrary model compares are supported
	//			.sum
	//			.count
	//			.min
	//			.max
	//		.file2		// right now, Transform, but could be any model run later...
	//			.sum
	//			.count
	//			.min
	//			.max
    //--------------------------------------------------------------------------
    setData: function(val1, val2, totalVal, data) // send in something like: obj.habitat_index
    {
    	this.DSS_FieldData = {val1: val1, val2: val2, total: totalVal};
    	
		this.clearWait();
		Ext.getCmp('DSS_SpiderGraphPanel').setSpiderDataElement(val1, val2, this.DSS_FieldString);
		this.setValueField();
		this.DSS_GraphData = data;
    },
    
    //--------------------------------------------------------------------------
    hideHeatmap: function(onButton) {
    	
		if (onButton.DSS_Layer) { 
			globalMap.removeLayer(onButton.DSS_Layer);
			onButton.DSS_Layer = null;
			onButton.toggle(false);
		}
    },
    
    // type can be: 
	//	delta - shows change between file1 and file2
	//	file1 - shows file1 as an absolute map
	//	file2 - shows file2 as an absolute map
	// subtype can be:
	//	equal - equal interval
	//	quantile - quantiled..
    //--------------------------------------------------------------------------
    showHeatmap: function(button, type, subtype) {

//		var spinnerStyle = {"background-image":"url(app/images/spinner_16a.gif)",
//			"background-repeat":"no-repeat","background-position":"center center", 
//			"padding-left":"0px"};
			
    	var me = this;
		if (button.DSS_Layer) { 
			this.hideHeatmap(button);
		}
		else {
			var scCombo1 = Ext.getCmp('DSS_ScenarioCompareCombo_1').getValue();	
			var scCombo2 = Ext.getCmp('DSS_ScenarioCompareCombo_2').getValue();
			
			// TODO: validate scCombo1 & 2? Should be numbers in the range of -1 to 9
			
			var clientID = '1234';
			var clientID_cookie = Ext.util.Cookies.get('DSS_clientID');
			if (clientID_cookie) {
				clientID = clientID_cookie;
			}
			else {
				console.log('WARNING: no client id cookie was found...');
			}
			
			var button = this.getComponent('heat_delta_button');
			button.setIcon('app/images/spinner_16a.gif');
			button.setDisabled(true);
			
			var obj = Ext.Ajax.request({
				url: location.href + 'getHeatmap',
				jsonData: {
					model: me.DSS_FieldString,
					clientID: clientID,
					compare1ID: scCombo1,//-1, // default
					compare2ID: scCombo2,//DSS_currentModelRunID,
					type: type,
					subtype: subtype
				},
				timeout: 1 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
				
				success: function(response, opts) {
					var res = JSON.parse(response.responseText);
					console.log('success: ');
					console.log(res);
					
					// Send server data for colors/values to the legend creation code...
					me.createHeatmapLegend(res);
					me.tryCreateHeatmapLayer(res, 0);
				},
				
				failure: function(respose, opts) {
					alert('heatmap request failed, request timed out?');
					button.setIcon('app/images/map_small_icon.png');
					button.setDisabled(false);
					button.toggle(false);
				}
			});
		}
	},

    //--------------------------------------------------------------------------
    tryCreateHeatmapLayer: function(json, tryCount) {
    	
		var me = this;
		var button = this.getComponent('heat_delta_button');
		
		console.log('Doing a try create heatmap layer');
		
		// waits a small amount of time...then checks to see if they image could load...
		Ext.defer(function() {
			var tester = new Image();
			
			// Set up a SUCCESS handler...
			//----------------------------
			tester.onload = function() {
				var bounds = new OpenLayers.Bounds(
					-10062652.65061, 5278060.469521415,
					-9878152.65061, 5415259.640662575
				);
				var imgTest = new OpenLayers.Layer.Image(
					button.DSS_heatString,
					json.heatFile,
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
				
				if (button.DSS_Layer) { 
					globalMap.removeLayer(button.DSS_Layer);
				}
				button.DSS_Layer = imgTest;
				globalMap.addLayer(button.DSS_Layer);
				button.DSS_Layer.setOpacity(0.9);
				
				button.setIcon('app/images/map_small_icon.png');
				button.setDisabled(false);
				// call up to the button owner and tell it to toggle all of the
				//	others off (except myself if I'm toggled)...
				button.DSS_DetailReportContainer.clearHeatToggles(button.up());
				
			};
			// Set up a failure handler...
			//-----------------------
			tester.onerror = function() {
				tryCount++;
				if (tryCount < 20) {
					me.tryCreateHeatmapLayer(json, tryCount);
				}
				else {
					console.log(' Image not ready yet...and lets give up...');
					button.setIcon('app/images/map_small_icon.png');
					button.setDisabled(false);
					button.toggle(false);
				}
			};
			
			tester.src = json.heatFile;
		
		}, 200 + tryCount * 100, this);
	}
	
});

