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
    	var count1 = obj.Transform.Habitat_Index.Count;
    	var val1 = obj.Default.Habitat_Index.Sum / count1;
    	var val2 = obj.Transform.Habitat_Index.Sum / count1;
    	//var Max1 = this.Max(obj.Default.Habitat_Index.Max, obj.Transform.Habitat_Index.Max);
    	//var val1 = obj.Habitat_Index_D.Sum_HI;
    	//var val2 = obj.Habitat_Index_T.Sum_HI;
    	var Habitat_Text = this.getComponent('Habitat_Index');
    	Habitat_Text.setValue((hi_2 - hi_1).toFixed(4));
    	
    	// Nitrogen
    	var val3 = obj.Default.Nitrogen.Sum;
    	var val4 = obj.Transform.Nitrogen.Sum;
    	var count2 = obj.Transform.Nitrogen.Count;
    	var Max2 = this.Max(obj.Default.Nitrogen.Max, obj.Transform.Nitrogen.Max);
    	//var val3 = obj.Nitrogen_D.Nitrogen;
    	//var val4 = obj.Nitrogen_T.Nitrogen;
    	var Nitrogen_Text = this.getComponent('Nitrogen');
    	//Nitrogen_Text.setValue((val4 / Max2 - val3 / Max2).toFixed(4));
    	Nitrogen_Text.setValue((val4 - val3).toFixed(4));
    	
    	// Phosphorus
    	var val5 = obj.Default.Phosphorus.Sum;
    	var val6 = obj.Transform.Phosphorus.Sum;
    	var count3 = obj.Transform.Phosphorus.Count;
    	//var Max3 = this.Max(obj.Default.Phosphorus.Max, obj.Transform.Phosphorus.Max);
    	//var val5 = obj.Phosphorus_D.Phosphorus;
    	//var val6 = obj.Phosphorus_T.Phosphorus;
    	var Phosphorus_Text = this.getComponent('Phosphorus');
    	Phosphorus_Text.setValue((p_2 - p_1).toFixed(4));
    	
    	// Pest_Suppression
    	var count4 = obj.Transform.Pest_Suppression.Count;
    	var val7 = obj.Default.Pest_Suppression.Sum / count4;
    	var val8 = obj.Transform.Pest_Suppression.Sum / count4;
    	//var Max4 = this.Max(obj.Default.Pest.Max, obj.Transform.Phosphorus.Max);
    	//var val7 = obj.Pest_Suppression_D.Pest;
    	//var val8 = obj.Pest_Suppression_T.Pest;
    	var Pest_Text = this.getComponent('Pest_Suppression');
    	Pest_Text.setValue((ps_2 - ps_1).toFixed(4));
    	
    	// Pollinator
    	var count5 = obj.Transform.Pollinator.Count;
    	var Max5 = this.Max(obj.Default.Pollinator.Max, obj.Transform.Pollinator.Max);
    	var val9 = obj.Default.Pollinator.Sum / (count5 * Max5);
    	var val10 = obj.Transform.Pollinator.Sum / (count5 * Max5);
    	//var val9  = obj.Pollinator_D.Pollinator;
    	//var val10 = obj.Pollinator_T.Pollinator;
    	var Pollinator_Text = this.getComponent('Pollinator');
    	//Pollinator_Text.setValue((val10 / Max5 - val9 / Max5).toFixed(4));
    	Pollinator_Text.setValue((val10 - val9).toFixed(4));
    	
    	// Ethanol
    	var val11 = obj.Default.Ethanol.Sum;
    	var val12 = obj.Transform.Ethanol.Sum;
    	var count6 = obj.Transform.Ethanol.Count;
    	var Max6 = this.Max(obj.Default.Ethanol.Max, obj.Transform.Ethanol.Max);
    	//var val11 = obj.Ethanol_D.Ethanol;
    	//var val12 = obj.Ethanol_T.Ethanol;
    	var Biomass_Text = this.getComponent('Ethanol');
    	//Biomass_Text.setValue((val12 / Max6 - val11 / Max6).toFixed(4));
    	Biomass_Text.setValue((val12 - val11).toFixed(4));
    	
    	// Net_Income
    	var val13 = obj.Default.Net_Income.Sum;
    	var val14 = obj.Transform.Net_Income.Sum;
    	var count7 = obj.Transform.Net_Income.Count;
    	var Max7 = this.Max(obj.Default.Net_Income.Max, obj.Transform.Net_Income.Max);
    	//var val13 = obj.Net_Income_D.Net_Income;
    	//var val14 = obj.Net_Income_T.Net_Income;
    	var Net_Income_Text = this.getComponent('Net_Income');
    	//Net_Energy_Text.setValue((val14 / Max7 - val13 / Max7).toFixed(4));
    	Net_Income_Text.setValue((val14 - val13).toFixed(4));
    	
    	// Net_Energy
    	var val15 = obj.Default.Net_Energy.Sum;
    	var val16 = obj.Transform.Net_Energy.Sum;
    	var count8 = obj.Transform.Net_Energy.Count;
    	var Max8 = this.Max(obj.Default.Net_Energy.Max, obj.Transform.Net_Energy.Max);
    	//var val15 = obj.Net_Energy_D.Net_Energy;
    	//var val16 = obj.Net_Energy_T.Net_Energy;
    	var Net_Energy_Text = this.getComponent('Net_Energy');
    	//Net_Energy_Text.setValue((val16 / Max8 - val15 / Max8).toFixed(4));
    	Net_Energy_Text.setValue((val16 - val15).toFixed(4));
    	
    	// Soil_Carbon
    	var val17 = obj.Default.Soil_Carbon.Sum;
    	var val18 = obj.Transform.Soil_Carbon.Sum;
    	var count9 = obj.Transform.Soil_Carbon.Count;
    	var Max9 = this.Max(obj.Default.Soil_Carbon.Max, obj.Transform.Soil_Carbon.Max);
    	//var val15 = obj.Net_Energy_D.Net_Energy;
    	//var val16 = obj.Net_Energy_T.Net_Energy;
    	var Soil_Carbon_Text = this.getComponent('Soil_Carbon');
    	//Soin_Carbon_Text.setValue((val17 / Max9 - val18 / Max9).toFixed(4));
    	Soil_Carbon_Text.setValue((val17 - val18).toFixed(4));
    	
    	// Nitrous_Oxide_Emissions
    	var val19 = obj.Default.Nitrous_Oxide_Emissions.Sum;
    	var val20 = obj.Transform.Nitrous_Oxide_Emissions.Sum;
    	var count10 = obj.Default.Nitrous_Oxide_Emissions.Count;
    	var Max10 = this.Max(obj.Default.Nitrous_Oxide_Emissions.Max, obj.Transform.Nitrous_Oxide_Emissions.Max);
    	//var val15 = obj.Net_Energy_D.Net_Energy;
    	//var val16 = obj.Net_Energy_T.Net_Energy;
    	var Nitrous_Oxide_Emissions = this.getComponent('Nitrous_Oxide_Emissions');
    	//Nitrous_Oxide_Emissions.setValue((val19 / Max10 - val20 / Max10).toFixed(4));
    	Nitrous_Oxide_Emissions.setValue((val19 - val20).toFixed(4));
    	
    	
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
    	
    	// Graph_Soil_Carbon
    	var Soil_Carbon_Button = this.getComponent('Graph_Soil_Carbon');
    	Soil_Carbon_Button.graphdataD = obj.Default.Soil_Carbon;
    	Soil_Carbon_Button.graphdataT = obj.Transform.Soil_Carbon;
    	//Net_Energy_Button.graphdataD = obj.Net_Energy_D;
    	//Net_Energy_Button.graphdataT = obj.Net_Energy_T;
    	
    	// Graph_Nitrous_Oxide_Emissions
    	var Nitrous_Oxide_Emissions_Button = this.getComponent('Graph_Nitrous_Oxide_Emissions');
    	Nitrous_Oxide_Emissions_Button.graphdataD = obj.Default.Nitrous_Oxide_Emissions;
    	Nitrous_Oxide_Emissions_Button.graphdataT = obj.Transform.Nitrous_Oxide_Emissions;
    	//Net_Energy_Button.graphdataD = obj.Net_Energy_D;
    	//Net_Energy_Button.graphdataT = obj.Net_Energy_T;
    	
    	// Spider_Graph
    	var spiderPanel = Ext.getCmp('DSS_SpiderGraphPanel');
    	// var max_bird
    	// var min_bird
    	// var max_nitrogen
    	// var min_nitrogen
    	// var max_phosphorus
    	// var min_phosphorus
    	// var max_biocontrol
    	// var min_biocontrol
    	// var max_pollinator
    	// var min_pollinator
    	// var max_fuel
    	// var min_fuel
    	// var max_pollinator
    	// var min_pollinator
    	// var max_pollinator
    	// var min_pollinator    	
    	
    	var arrayDef   = [val1 / this.Max(val1, val2), val3 / this.Max(val3, val4), val5 / this.Max(val5, val6), val7 / this.Max(val7, val8),  val9 / this.Max(val9, val10), val11 / this.Max(val11, val12), val13 / this.Max(val13, val14), val15 / this.Max(val15, val16), val17 / this.Max(val17, val18),  val19 / this.Max(val19, val20)];
    	var arrayTrans = [val2 / this.Max(val1, val2), val4 / this.Max(val3, val4), val6 / this.Max(val5, val6), val8 / this.Max(val7, val8), val10 / this.Max(val9, val10), val12 / this.Max(val11, val12), val14 / this.Max(val13, val14), val16 / this.Max(val15, val16), val18 / this.Max(val17, val18),  val20 / this.Max(val19, val20)];
    	//var arrayDef   = [val1, val3 / Max2, val5, val7,  val9 / Max5, val11 / Max6, val13 / Max7, val15 / Max8, val17 / Max9, val19 / Max10];
    	//var arrayTrans = [val2, val4 / Max2, val6, val8, val10 / Max5, val12 / Max6, val14 / Max7, val16 / Max8, val18 / Max9, val20 / Max10];
    	//var arrayDef   = [val1, val3, val5, val7, val9,  val11, val13, val15];
    	//var arrayTrans = [val2, val4, val6, val8, val10, val12, val14, val16];
    	//var arrayDef   = [val1/Max1, val3/Max2, val5/Max3, val7/Max4,  val9/Max5, val11/Max6, val13/Max7, val15/Max8];
    	//var arrayTrans = [val2/Max1, val4/Max2, val6/Max3, val8/Max4, val10/Max5, val12/Max6, val14/Max7, val16/Max8];
    	//var arrayDef = [1, 1, 1, 1, 1, 1, 1, 1];
    	//var arrayTrans = [(val2-val1)/(val1+val2), (val4-val3)/(val3+val4), (val6-val5)/(val5+val6), (val8-val7)/(val7+val8), (val10-val9)/(val9+val10), (val12-val11)/(val11+val12), (val14-val13)/(val13+val4), (val16-val15)/(val15+val16)];
    	spiderPanel.setSpiderData(arrayDef, arrayTrans);
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

