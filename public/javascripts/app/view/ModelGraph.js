Ext.define('MyApp.view.ModelGraph', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.ModelGraph',
    
    id: "Model_Graph",
    
    height: 220,
    width: 500,
        layout: {
        type: 'absolute'
    },
    //bodyPadding: 10,
    title: 'Simulation Detail',
	icon: 'app/images/magnify_icon.png',
    activeTab: 0,

    require : [
    	    'MyApp.view.GraphPopUp',
    	    'MyApp.view.GraphSpider'
    ],
    
    tools:[{
		type: 'help',
		qtip: 'Simulation Results Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],
    
    initComponent: function() {
        var me = this;
	
	Ext.applyIf(me, {
            items: [
			 {
			    itemId: 'value_habitat_index',  
			    xtype: 'textfield',
			    x: 0,
			    y: 10,
			    width: 160,
			    fieldLabel: 'Birds',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_soc',
			    xtype: 'textfield',
			    x: 250,
			    y: 10,
			    width: 160,
			    fieldLabel: 'Soil Carbon',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_nitrogen',
			    xtype: 'textfield',
			    x: 0,
			    y: 40,
			    width: 160,
			    fieldLabel: 'Nitrogen',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_pollinator',
			    xtype: 'textfield',
			    x: 250,
			    y: 40,
			    width: 160,
			    fieldLabel: 'Pollinators',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_phosphorus',
			    xtype: 'textfield',
			    x: 0,
			    y: 70,
			    width: 160,
			    fieldLabel: 'Phosphorus',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_net_energy',
			    xtype: 'textfield',
			    x: 250,
			    y: 70,
			    width: 160,
			    fieldLabel: 'Net Energy',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_pest',
			    xtype: 'textfield',
			    x: 0,
			    y: 100,
			    width: 160,
			    fieldLabel: 'Biocontrol',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_net_income',
			    xtype: 'textfield',
			    x: 250,
			    y: 100,
			    width: 160,
			    fieldLabel: 'Net Income',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_ethanol',
			    xtype: 'textfield',
			    x: 0,
			    y: 130,
			    width: 160,
			    fieldLabel: 'Fuel',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'value_nitrous_oxide',
			    xtype: 'textfield',
			    x: 250,
			    y: 130,
			    width: 160,
			    fieldLabel: 'Nitrous Oxide',
			    labelWidth: 80,
			    labelAlign: 'right'
			}, //-------------------- End of Text Fields
			{
			    itemId: 'graph_habitat_index',
			    xtype: 'button',
			    x: 162,
			    y: 10,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Bird Habitat Index'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_soc',
			    xtype: 'button',
			    x: 412,
			    y: 10,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Soil Carbon'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_nitrogen',
			    xtype: 'button',
			    x: 162,
			    y: 40,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Nitrogen'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_pollinator',
			    xtype: 'button',
			    x: 412,
			    y: 40,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Pollinator'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_phosphorus',
			    xtype: 'button',
			    x: 162,
			    y: 70,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Phosphorus'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_net_energy',
			    xtype: 'button',
			    x: 412,
			    y: 70,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Net Energy'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_pest',
			    xtype: 'button',
			    x: 162,
			    y: 100,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Crop Pest Supression'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_net_income',
			    xtype: 'button',
			    x: 412,
			    y: 100,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Net Income'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_ethanol',
			    xtype: 'button',
			    x: 162,
			    y: 130,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Ethanol'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			},
			{
			    itemId: 'graph_nitrous_oxide',
			    xtype: 'button',
			    x: 412,
			    y: 130,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Nitrous Oxide Emissions'});
					mypopup.show();
					mypopup.SetChartData(self.DSS_data);
			    }
			}, //----------------------- End of Graph buttons
			{
			    itemId: 'heat_habitat_index',
			    xtype: 'button',
			    x: 207,
			    y: 10,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_soc',
			    xtype: 'button',
			    x: 457,
			    y: 10,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_nitrogen',
			    xtype: 'button',
			    x: 207,
			    y: 40,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_pollinator',
			    xtype: 'button',
			    x: 457,
			    y: 40,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_phosphorus',
			    xtype: 'button',
			    x: 207,
			    y: 70,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_net_energy',
			    xtype: 'button',
			    x: 457,
			    y: 70,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_pest',
			    xtype: 'button',
			    x: 207,
			    y: 100,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_net_income',
			    xtype: 'button',
			    x: 457,
			    y: 100,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_ethanol',
			    xtype: 'button',
			    x: 207,
			    y: 130,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			},
			{
			    itemId: 'heat_nitrous_oxide',
			    xtype: 'button',
			    x: 457,
			    y: 130,
			    enableToggle: true,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showHeatmap(self);
			    }
			}, // ----------------- End of Heat Buttons
			{
			    itemId: 'Clear',
			    xtype: 'button',
			    x: 230,
			    y: 160,
			    text: 'Clear Fields',
			    handler: function () {
			    	this.up().clearFields();
			    }
			},
	    ]
        });
        
        me.callParent(arguments);
    },

    //--------------------------------------------------------------------------
	clearFields: function() {
		
		Ext.getCmp('DSS_SpiderGraphPanel').clearSpiderData(0);// set all fields to zero
		
		this.getComponent('value_habitat_index').setValue(null);
		this.getComponent('value_nitrogen').setValue(null);
		this.getComponent('value_phosphorus').setValue(null);
		this.getComponent('value_pest').setValue(null);
		this.getComponent('value_pollinator').setValue(null);
		this.getComponent('value_ethanol').setValue(null);
		this.getComponent('value_net_income').setValue(null);
		this.getComponent('value_net_energy').setValue(null);
		this.getComponent('value_soc').setValue(null);
		this.getComponent('value_nitrous_oxide').setValue(null);
	},
	
    //--------------------------------------------------------------------------
	setWaitFields: function() {
		this.clearFields();

		var spinnerStyle = {"background-image":"url(app/images/spinner_16a.gif)",
			"background-repeat":"no-repeat","background-position":"center center", 
			"padding-left":"16px"};
		
		this.getComponent('value_habitat_index').setFieldStyle(spinnerStyle);
		this.getComponent('value_nitrogen').setFieldStyle(spinnerStyle);
		this.getComponent('value_phosphorus').setFieldStyle(spinnerStyle);
		this.getComponent('value_pest').setFieldStyle(spinnerStyle);
		this.getComponent('value_pollinator').setFieldStyle(spinnerStyle);
		this.getComponent('value_ethanol').setFieldStyle(spinnerStyle);
		this.getComponent('value_net_income').setFieldStyle(spinnerStyle);
		this.getComponent('value_net_energy').setFieldStyle(spinnerStyle);
		this.getComponent('value_soc').setFieldStyle(spinnerStyle);
		this.getComponent('value_nitrous_oxide').setFieldStyle(spinnerStyle);
	},
	
    SetData: function(obj)
    {
    	// Average Fields ----------------------
    	// Habitat Index
    	var hi_1 = obj.Habitat_Index.averageFile1;
    	var hi_2 = obj.Habitat_Index.averageFile2;
    	var Habitat_Text = this.getComponent('Habitat_Index');
    	Habitat_Text.setValue((hi_2 - hi_1).toFixed(4));
    	
    	// Nitrogen
    	var n_1 = obj.Nitrogen.averageFile1;
    	var n_2 = obj.Nitrogen.averageFile2;
    	var n_max = obj.Nitrogen.max;
    	var Nitrogen_Text = this.getComponent('Nitrogen');
    	Nitrogen_Text.setValue((n_2 / n_max - n_1 / n_max).toFixed(4));
    	
    	// Phosphorus
    	var p_1 = obj.Phosphorus.averageFile1;
    	var p_2 = obj.Phosphorus.averageFile2;
    	var Phosphorus_Text = this.getComponent('Phosphorus');
    	Phosphorus_Text.setValue((p_2 - p_1).toFixed(4));
    	
    	// Pest_Suppression
    	var ps_1 = obj.Pest_Suppression.averageFile1;
    	var ps_2 = obj.Pest_Suppression.averageFile2;
    	var Pest_Text = this.getComponent('Pest_Suppression');
    	Pest_Text.setValue((ps_2 - ps_1).toFixed(4));
    	
    	// Pollinator
    	var pol_1 = obj.Pollinator.averageFile1;
    	var pol_2 = obj.Pollinator.averageFile2;
    	var pol_max = obj.Pollinator.max;
    	var Pollinator_Text = this.getComponent('Pollinator');
    	Pollinator_Text.setValue((pol_2 / pol_max - pol_1 / pol_max).toFixed(4));
    	
    	// Ethanol
    	var e_1 = obj.Ethanol.averageFile1;
    	var e_2 = obj.Ethanol.averageFile2;
    	var e_max = obj.Ethanol.max;
    	var Biomass_Text = this.getComponent('Ethanol');
    	Biomass_Text.setValue((e_2 / e_max - e_1 / e_max).toFixed(4));
    	
    	// Net_Income
    	var ni_1 = obj.Net_Income.averageFile1;
    	var ni_2 = obj.Net_Income.averageFile2;
    	var ni_max = obj.Net_Income.max;
    	var Net_Energy_Text = this.getComponent('Net_Income');
    	Net_Energy_Text.setValue((ni_2 / ni_max - ni_1 / ni_max).toFixed(4));
    	
    	// Net_Energy
    	var ne_1 = obj.Net_Energy.averageFile1;
    	var ne_2 = obj.Net_Energy.averageFile2;
    	var ne_max = obj.Net_Energy.max;
    	var Net_Energy_Text = this.getComponent('Net_Energy');
    	Net_Energy_Text.setValue((ne_2 / ne_max - ne_1 / ne_max).toFixed(4));
    	
    	
    	// Graphs ---------------
    	var Habitat_Button = this.getComponent('Graph_Habitat_Index');
    	Habitat_Button.graphdataD = obj.Habitat_Index.histogramFile1;
    	Habitat_Button.graphdataT = obj.Habitat_Index.histogramFile2;
    	this.getComponent('Heat_Habitat_Index').DSS_heatString = 'habitat_index';
    	
    	var Nitrogen_Button = this.getComponent('Graph_Nitrogen');
    	Nitrogen_Button.graphdataD = obj.Nitrogen.histogramFile1;
    	Nitrogen_Button.graphdataT = obj.Nitrogen.histogramFile2;
    	this.getComponent('Heat_Nitrogen').DSS_heatString = 'nitrogen';
    	
    	var Phosphorus_Button = this.getComponent('Graph_Phosphorus');
    	Phosphorus_Button.graphdataD = obj.Phosphorus.histogramFile1;
    	Phosphorus_Button.graphdataT = obj.Phosphorus.histogramFile2;
    	this.getComponent('Heat_Phosphorus').DSS_heatString = 'phosphorus';
    	
    	var Pest_Button = this.getComponent('Graph_Pest_Suppression');
    	Pest_Button.graphdataD = obj.Pest_Suppression.histogramFile1;
    	Pest_Button.graphdataT = obj.Pest_Suppression.histogramFile2;
    	this.getComponent('Heat_Crop_Pest').DSS_heatString = 'pest';
    	
    	var Pollinator_Button = this.getComponent('Graph_Pollinator');
    	Pollinator_Button.graphdataD = obj.Pollinator.histogramFile1;
    	Pollinator_Button.graphdataT = obj.Pollinator.histogramFile2;
    	this.getComponent('Heat_Pollinator').DSS_heatString = 'pollinator';
    	
    	var Biomass_Button = this.getComponent('Graph_Ethanol');
    	Biomass_Button.graphdataD = obj.Ethanol.histogramFile1;
    	Biomass_Button.graphdataT = obj.Ethanol.histogramFile2;
    	this.getComponent('Heat_Ethanol').DSS_heatString = 'ethanol';
    	
    	var Net_Income_Button = this.getComponent('Graph_Net_Income');
    	Net_Income_Button.graphdataD = obj.Net_Income.histogramFile1;
    	Net_Income_Button.graphdataT = obj.Net_Income.histogramFile2;
    	this.getComponent('Heat_Net_Income').DSS_heatString = 'net_income';
    	
    	var Net_Energy_Button = this.getComponent('Graph_Net_Energy');
    	Net_Energy_Button.graphdataD = obj.Net_Energy.histogramFile1;
    	Net_Energy_Button.graphdataT = obj.Net_Energy.histogramFile2;
    	this.getComponent('Heat_Net_Energy').DSS_heatString = 'net_energy';
    	
    	// Spider_Graph
    	var spiderPanel = Ext.getCmp('DSS_SpiderGraphPanel');
    	var array_1 = [hi_1, 
    					n_1 / n_max, 
    					p_1, 
    					ps_1, 
    					pol_1 / pol_max, 
    					e_1 / e_max, 
    					ni_1 / ni_max, 
    					ne_1 / ne_max];
    	var array_2 = [hi_2, 
    					n_2 / n_max, 
    					p_2, 
    					ps_2, 
    					pol_2 / pol_max, 
    					e_2 / e_max, 
    					ni_2 / ni_max, 
    					ne_2 / ne_max];
    	spiderPanel.setSpiderData(array_1, array_2);
    },

    //--------------------------------------------------------------------------
    showHeatmap: function(button) {

		if (button.DSS_Layer) { 
			globalMap.removeLayer(button.DSS_Layer);
			button.DSS_Layer = null;
		}
		else {
			var obj = Ext.Ajax.request({
				url: location.href + 'getHeatmap',
				jsonData: {
					'model': button.DSS_heatString
				},
				timeout: 10 * 60 * 1000, // minutes * seconds * (i.e. converted to) milliseconds
				
				success: function(response, opts) {
					
					var obj= JSON.parse(response.responseText);
					console.log("success: ");
					console.log(obj);
					Ext.defer(function(obj) {		
						var bounds = new OpenLayers.Bounds(
							-10062652.65061, 5278060.469521415,
							-9878152.65061, 5415259.640662575
						);
						var imgTest = new OpenLayers.Layer.Image(
							button.DSS_heatString,
							'app/file/' + obj.heatFile,
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
					}, 1000, this, [obj]);
				},
				
				failure: function(respose, opts) {
					alert("heatmap request failed, request timed out?");
				}
			});
		}
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
    SetData_new: function(obj)
    {
    	var clearSpinnerStyle = {"background-image":"none"};
     	var spiderPanel = Ext.getCmp('DSS_SpiderGraphPanel');

    	if (obj.habitat_index) {
    		var data = obj.habitat_index;
    		var field = 'habitat_index';
			var dat_1 = data.file1.sum / data.file1.count;
			var dat_2 = data.file2.sum / data.file2.count;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}	

    	if (obj.soc) {
    		var data = obj.soc;
    		var field = 'soc';
			var dat_1 = data.file1.sum;
			var dat_2 = data.file2.sum;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}	
		
    	if (obj.ethanol) {
    		var data = obj.ethanol;
    		var field = 'ethanol';
			var dat_1 = data.file1.sum;
			var dat_2 = data.file2.sum;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}
    	
		if (obj.net_income) {
    		var data = obj.net_income;
    		var field = 'net_income';
			var dat_1 = data.file1.sum;
			var dat_2 = data.file2.sum;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}

		if (obj.net_energy) {    	
    		var data = obj.net_energy;
    		var field = 'net_energy';
			var dat_1 = data.file1.sum;
			var dat_2 = data.file2.sum;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}
		
		if (obj.nitrogen) {    	
    		var data = obj.nitrogen;
    		var field = 'nitrogen';
			var dat_1 = data.file1.sum;
			var dat_2 = data.file2.sum;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}
    	
		if (obj.phosphorus) {    	
    		var data = obj.phosphorus;
    		var field = 'phosphorus';
			var dat_1 = data.file1.sum;
			var dat_2 = data.file2.sum;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}
    	
    	if (obj.pest) {
    		var data = obj.pest;
    		var field = 'pest';
			var dat_1 = data.file1.sum / data.file1.count;
			var dat_2 = data.file2.sum / data.file2.count;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}
    	
    	if (obj.pollinator) {
    		var data = obj.pollinator;
    		var field = 'pollinator';
			var dat_1 = data.file1.sum / (data.file1.count * data.max);
			var dat_2 = data.file2.sum / (data.file2.count * data.max);
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}
	
    	if (obj.nitrous_oxide) {
    		var data = obj.nitrous_oxide;
    		var field = 'nitrous_oxide';
			var dat_1 = data.file1.sum;
			var dat_2 = data.file2.sum;
			var value = (dat_2 - dat_1).toFixed(4);
			spiderPanel.setSpiderDataElement(dat_1, dat_2, field);
			this.getComponent('value_' + field).setValue(value).setFieldStyle(clearSpinnerStyle);
			this.getComponent('graph_' + field).DSS_data = data;
			this.getComponent('heat_' + field).DSS_heatString = field;
		}
    }

});

