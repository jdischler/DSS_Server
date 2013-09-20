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
			    itemId: 'Habitat_Index',  
			    xtype: 'textfield',
			    x: 0,
			    y: 10,
			    width: 160,
			    fieldLabel: 'Birds',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Soil_Carbon',
			    xtype: 'textfield',
			    x: 250,
			    y: 10,
			    width: 160,
			    fieldLabel: 'Soil Carbon',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Nitrogen',
			    xtype: 'textfield',
			    x: 0,
			    y: 40,
			    width: 160,
			    fieldLabel: 'Nitrogen',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Pollinator',
			    xtype: 'textfield',
			    x: 250,
			    y: 40,
			    width: 160,
			    fieldLabel: 'Pollinators',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Phosphorus',
			    xtype: 'textfield',
			    x: 0,
			    y: 70,
			    width: 160,
			    fieldLabel: 'Phosphorus',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Net_Energy',
			    xtype: 'textfield',
			    x: 250,
			    y: 70,
			    width: 160,
			    fieldLabel: 'Net Energy',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Pest_Suppression',
			    xtype: 'textfield',
			    x: 0,
			    y: 100,
			    width: 160,
			    fieldLabel: 'Biocontrol',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Net_Income',
			    xtype: 'textfield',
			    x: 250,
			    y: 100,
			    width: 160,
			    fieldLabel: 'Net Income',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Ethanol',
			    xtype: 'textfield',
			    x: 0,
			    y: 130,
			    width: 160,
			    fieldLabel: 'Fuel',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Nitrous_Oxide_Emissions',
			    xtype: 'textfield',
			    x: 250,
			    y: 130,
			    width: 160,
			    fieldLabel: 'Nitrous Oxide',
			    labelWidth: 80,
			    labelAlign: 'right'
			}, //-------------------- End of Text Fields
			{
			    itemId: 'Graph_Habitat_Index',
			    xtype: 'button',
			    x: 162,
			    y: 10,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Bird Habitat Index'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Soil_Carbon',
			    xtype: 'button',
			    x: 412,
			    y: 10,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Soil Carbon'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Nitrogen',
			    xtype: 'button',
			    x: 162,
			    y: 40,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Nitrogen'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Pollinator',
			    xtype: 'button',
			    x: 412,
			    y: 40,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Pollinator'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Phosphorus',
			    xtype: 'button',
			    x: 162,
			    y: 70,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Phosphorus'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Net_Energy',
			    xtype: 'button',
			    x: 412,
			    y: 70,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Net Energy'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Pest_Suppression',
			    xtype: 'button',
			    x: 162,
			    y: 100,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Crop Pest Supression'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Net_Income',
			    xtype: 'button',
			    x: 412,
			    y: 100,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Net Income'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Ethanol',
			    xtype: 'button',
			    x: 162,
			    y: 130,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Ethanol'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Nitrous_Oxide_Emissions',
			    xtype: 'button',
			    x: 412,
			    y: 130,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Nitrous Oxide Emissions'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			}, //----------------------- End of Graph buttons
			{
			    itemId: 'Heat_Habitat_Index',
			    xtype: 'button',
			    x: 207,
			    y: 10,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showFakeHeatmap(self,'Heat','app/file/heat.png');
			    }
			},
			{
			    itemId: 'Heat_Carbon',
			    xtype: 'button',
			    x: 457,
			    y: 10,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			},
			{
			    itemId: 'Heat_Nitrogen',
			    xtype: 'button',
			    x: 207,
			    y: 40,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    },
			    handler: function(self) {
			    	self.up().showFakeHeatmap(self,'Heat2','app/file/heat_2.png');
			    }
			},
			{
			    itemId: 'Heat_Pollinator',
			    xtype: 'button',
			    x: 457,
			    y: 40,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			},
			{
			    itemId: 'Heat_Phosphorus',
			    xtype: 'button',
			    x: 207,
			    y: 70,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			},
			{
			    itemId: 'Heat_Net_Energy',
			    xtype: 'button',
			    x: 457,
			    y: 70,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			},
			{
			    itemId: 'Heat_Crop_Pest',
			    xtype: 'button',
			    x: 207,
			    y: 100,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			},
			{
			    itemId: 'Heat_Net_Income',
			    xtype: 'button',
			    x: 457,
			    y: 100,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			},
			{
			    itemId: 'Heat_Biomass',
			    xtype: 'button',
			    x: 207,
			    y: 130,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			},
			{
			    itemId: 'Heat_Drainage',
			    xtype: 'button',
			    x: 457,
			    y: 130,
			    text: 'Heat',
			    tooltip: {
			    	text: 'View a heatmap overlay',
			    	showDelay: 100
			    }
			}, // ----------------- End of Heat Buttons
			{
			    itemId: 'Clear',
			    xtype: 'button',
			    x: 230,
			    y: 160,
			    text: 'Clear Fields',
			    handler: function ()
			    {
			    	    this.up().getComponent('Habitat_Index').setValue(null);
			    	    this.up().getComponent('Nitrogen').setValue(null);
			    	    this.up().getComponent('Phosphorus').setValue(null);
			    	    this.up().getComponent('Pest_Suppression').setValue(null);
			    	    this.up().getComponent('Pollinator').setValue(null);
			    	    this.up().getComponent('Biomass').setValue(null);
			    	    this.up().getComponent('Net_Income').setValue(null);
			    	    this.up().getComponent('Net_Energy').setValue(null);
			    	    this.up().getComponent('Soil_Carbon').setValue(null);
			    	    this.up().getComponent('Nitrous_Oxide_Emissions').setValue(null);
			    }
			},
	    ]
        });
        
        me.callParent(arguments);
    },
    
    SetData: function(obj)
    {
    	// Habitat Index
    	var count1 = obj.Transform.Habitat_Index.Count;
    	var val1 = obj.Default.Habitat_Index.Sum / count1;
    	var val2 = obj.Transform.Habitat_Index.Sum / count1;
    	//var Max1 = this.Max(obj.Default.Habitat_Index.Max, obj.Transform.Habitat_Index.Max);
    	//var val1 = obj.Habitat_Index_D.Sum_HI;
    	//var val2 = obj.Habitat_Index_T.Sum_HI;
    	var Habitat_Text = this.getComponent('Habitat_Index');
    	Habitat_Text.setValue((val2 - val1).toFixed(4));
    	
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
    	Phosphorus_Text.setValue((val6 - val5).toFixed(4));
    	
    	// Pest_Suppression
    	var count4 = obj.Transform.Pest_Suppression.Count;
    	var val7 = obj.Default.Pest_Suppression.Sum / count4;
    	var val8 = obj.Transform.Pest_Suppression.Sum / count4;
    	//var Max4 = this.Max(obj.Default.Pest.Max, obj.Transform.Phosphorus.Max);
    	//var val7 = obj.Pest_Suppression_D.Pest;
    	//var val8 = obj.Pest_Suppression_T.Pest;
    	var Pest_Text = this.getComponent('Pest_Suppression');
    	Pest_Text.setValue((val8 - val7).toFixed(4));
    	
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
    	
    	
    	// Graph_Habitat_Index
    	var Habitat_Button = this.getComponent('Graph_Habitat_Index');
    	Habitat_Button.graphdataD = obj.Default.Habitat_Index;
    	Habitat_Button.graphdataT = obj.Transform.Habitat_Index;
    	//Habitat_Button.graphdataD = obj.Habitat_Index_D;
    	//Habitat_Button.graphdataT = obj.Habitat_Index_T;
    	
    	// Graph_Nitrogen
    	var Nitrogen_Button = this.getComponent('Graph_Nitrogen');
    	Nitrogen_Button.graphdataD = obj.Default.Nitrogen;
    	Nitrogen_Button.graphdataT = obj.Transform.Nitrogen;
    	//Nitrogen_Button.graphdataD = obj.Nitrogen_D;
    	//Nitrogen_Button.graphdataT = obj.Nitrogen_T;
    	
    	// Graph_Phosphorus
    	var Phosphorus_Button = this.getComponent('Graph_Phosphorus');
    	Phosphorus_Button.graphdataD = obj.Default.Phosphorus;
    	Phosphorus_Button.graphdataT = obj.Transform.Phosphorus;
    	//Phosphorus_Button.graphdataD = obj.Phosphorus_D;
    	//Phosphorus_Button.graphdataT = obj.Phosphorus_T;
    	
    	// Graph_Pest_Suppression
    	var Pest_Button = this.getComponent('Graph_Pest_Suppression');
    	Pest_Button.graphdataD = obj.Default.Pest_Suppression;
    	Pest_Button.graphdataT = obj.Transform.Pest_Suppression;
    	//Pest_Button.graphdataD = obj.Pest_Suppression_D;
    	//Pest_Button.graphdataT = obj.Pest_Suppression_T;
    	
    	// Graph_Pollinator
    	var Pollinator_Button = this.getComponent('Graph_Pollinator');
    	Pollinator_Button.graphdataD = obj.Default.Pollinator;
    	Pollinator_Button.graphdataT = obj.Transform.Pollinator;
    	//Pollinator_Button.graphdataD = obj.Pollinator_D;
    	//Pollinator_Button.graphdataT = obj.Pollinator_T;
    	
    	// Graph_Ethanol
    	var Biomass_Button = this.getComponent('Graph_Ethanol');
    	Biomass_Button.graphdataD = obj.Default.Ethanol;
    	Biomass_Button.graphdataT = obj.Transform.Ethanol;
    	//Biomass_Button.graphdataD = obj.Ethanol_D;
    	//Biomass_Button.graphdataT = obj.Ethanol_T;
    	
    	// Graph_Net_Income
    	var Net_Income_Button = this.getComponent('Graph_Net_Income');
    	Net_Income_Button.graphdataD = obj.Default.Net_Income;
    	Net_Income_Button.graphdataT = obj.Transform.Net_Income;
    	//Net_Income_Button.graphdataD = obj.Net_Income_D;
    	//Net_Income_Button.graphdataT = obj.Net_Income_T;
    	
    	// Graph_Net_Energy
    	var Net_Energy_Button = this.getComponent('Graph_Net_Energy');
    	Net_Energy_Button.graphdataD = obj.Default.Net_Energy;
    	Net_Energy_Button.graphdataT = obj.Transform.Net_Energy;
    	//Net_Energy_Button.graphdataD = obj.Net_Energy_D;
    	//Net_Energy_Button.graphdataT = obj.Net_Energy_T;
    	
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

    Max: function (a1, a2)
    {
		if (a1 >= a2)
		{
			return a1;
		}
		else 
		{
			return a2;
		}
    },
    
    //--------------------------------------------------------------------------
    showFakeHeatmap: function(button, layerName, imagePath) {

		if (button.DSS_Layer) { 
			globalMap.removeLayer(button.DSS_Layer);
			button.DSS_Layer = null;
		}
		else {
			var bounds = new OpenLayers.Bounds(
				-10062652.65061, 5278060.469521415,
				-9878152.65061, 5415259.640662575
			);
			var imgTest = new OpenLayers.Layer.Image(
				layerName,
				imagePath,
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
		}
    }

});
