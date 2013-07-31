Ext.define('MyApp.view.ModelGraph', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.ModelGraph',
    
    id: "Model_Graph",
    
    height: 300,
    width: 500,
        layout: {
        type: 'absolute'
    },
    //bodyPadding: 10,
    title: 'Simulation Results',
	icon: 'app/images/scenario_icon.png',
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
			    x: 10,
			    y: 10,
			    width: 160,
			    fieldLabel: 'Habitat',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Carbon',
			    xtype: 'textfield',
			    x: 260,
			    y: 10,
			    width: 160,
			    fieldLabel: 'Carbon',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Nitrogen',
			    xtype: 'textfield',
			    x: 10,
			    y: 50,
			    width: 160,
			    fieldLabel: 'Nitrogen',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Pollinator',
			    xtype: 'textfield',
			    x: 260,
			    y: 50,
			    width: 160,
			    fieldLabel: 'Pollinator',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Phosphorus',
			    xtype: 'textfield',
			    x: 10,
			    y: 90,
			    width: 160,
			    fieldLabel: 'Phosphorus',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Net_Energy',
			    xtype: 'textfield',
			    x: 260,
			    y: 90,
			    width: 160,
			    fieldLabel: 'Net Energy',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Crop_Pest',
			    xtype: 'textfield',
			    x: 10,
			    y: 130,
			    width: 160,
			    fieldLabel: 'Crop Pest',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Net_Income',
			    xtype: 'textfield',
			    x: 260,
			    y: 130,
			    width: 160,
			    fieldLabel: 'Net Income',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Biomass',
			    xtype: 'textfield',
			    x: 10,
			    y: 170,
			    width: 160,
			    fieldLabel: 'Biomass',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Drainage',
			    xtype: 'textfield',
			    x: 260,
			    y: 170,
			    width: 160,
			    fieldLabel: 'Drainage',
			    labelWidth: 80,
			    labelAlign: 'right'
			},
			{
			    itemId: 'Graph_Habitat_Index',
			    xtype: 'button',
			    x: 180,
			    y: 10,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Habitat Index'});
				
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Carbon',
			    xtype: 'button',
			    x: 430,
			    y: 10,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Carbon'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Nitrogen',
			    xtype: 'button',
			    x: 180,
			    y: 50,
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
			    x: 430,
			    y: 50,
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
			    x: 180,
			    y: 90,
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
			    x: 430,
			    y: 90,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Net Energy'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Crop_Pest',
			    xtype: 'button',
			    x: 180,
			    y: 130,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Crop Pest'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Net_Income',
			    xtype: 'button',
			    x: 430,
			    y: 130,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Net Income'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Biomass',
			    xtype: 'button',
			    x: 180,
			    y: 170,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Biomass'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Graph_Drainage',
			    xtype: 'button',
			    x: 430,
			    y: 170,
			    text: 'Graph',
			    handler: function (self)
			    {
					var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Drainage'});
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			},
	    		{
			    itemId: 'Spider_Graph',
			    xtype: 'button',
			    x: 100,
			    y: 210,
			    text: 'Spider Graph',
			    handler: function (self)
			    {
					var myspider = Ext.create("MyApp.view.GraphSpider", {title: 'Spider Graph'});
					myspider.show();
					myspider.SetSpiderData(self.graphdataD, self.graphdataT);
			    }
			},
			{
			    itemId: 'Clear',
			    xtype: 'button',
			    x: 350,
			    y: 210,
			    text: 'Clear Text',
			    handler: function ()
			    {
			    	    this.up().getComponent('Habitat_Index').setValue(null);
			    	    this.up().getComponent('Nitrogen').setValue(null);
			    	    this.up().getComponent('Phosphorus').setValue(null);
			    	    this.up().getComponent('Crop_Pest').setValue(null);
			    	    this.up().getComponent('Pollinator').setValue(null);
			    	    this.up().getComponent('Biomass').setValue(null);
			    	    this.up().getComponent('Net_Income').setValue(null);
			    	    this.up().getComponent('Net_Energy').setValue(null);
			    }
			},
	    ]
        });
        	
        me.callParent(arguments);
    },
    
	SetData: function(obj)
    {
    	// Habitat Index
    	var val1 = obj.Default.Habitat_Index.Average_HI;
    	var val2 = obj.Transform.Habitat_Index.Average_HI;
    	var Habitat_Text = this.getComponent('Habitat_Index');
    	Habitat_Text.setValue(val1-val2);
    	
    	// Nitrogen
    	var val3 = obj.Default.Nitrogen.Nitrogen;
    	var val4 = obj.Transform.Nitrogen.Nitrogen;
    	var Nitrogen_Text = this.getComponent('Nitrogen');
    	Nitrogen_Text.setValue(val3-val4);
    	
    	// Phosphorus
    	var val5 = obj.Default.Phosphorus.Phosphorus;
    	var val6 = obj.Transform.Phosphorus.Phosphorus;
    	var Phosphorus_Text = this.getComponent('Phosphorus');
    	Phosphorus_Text.setValue(val5-val6);
    	
    	// Crop Pest
    	var val7 = obj.Default.Pest.Pest;
    	var val8 = obj.Transform.Pest.Pest;
    	var Pest_Text = this.getComponent('Crop_Pest');
    	Pest_Text.setValue(val7-val8);
    	
    	// Pollinator
    	var val9 = obj.Default.Pollinator.Pollinator;
    	var val10 = obj.Transform.Pollinator.Pollinator;
    	var Pollinator_Text = this.getComponent('Pollinator');
    	Pollinator_Text.setValue(val9-val10);
    	
    	// Biomass
    	var val11 = obj.Default.Ethanol.Ethanol;
    	var val12 = obj.Transform.Ethanol.Ethanol;
    	var Biomass_Text = this.getComponent('Biomass');
    	Biomass_Text.setValue(val11-val12);
    	
    	// Net_Income
    	var val13 = obj.Default.Net_Income.Net_Income;
    	var val14 = obj.Transform.Net_Income.Net_Income;
    	var Net_Energy_Text = this.getComponent('Net_Income');
    	Net_Energy_Text.setValue(val13-val14);
    	
    	// Net_Energy
    	var val15 = obj.Default.Net_Energy.Net_Energy;
    	var val16 = obj.Transform.Net_Energy.Net_Energy;
    	var Net_Energy_Text = this.getComponent('Net_Energy');
    	Net_Energy_Text.setValue(val15-val16);
    	
    	
    	
    	
    	
    	// Graph_Habitat_Index
    	var Habitat_Button = this.getComponent('Graph_Habitat_Index');
    	Habitat_Button.graphdataD = obj.Default.Habitat_Index;
    	Habitat_Button.graphdataT = obj.Transform.Habitat_Index;
    	
    	// Graph_Nitrogen
    	var Nitrogen_Button = this.getComponent('Graph_Nitrogen');
    	Nitrogen_Button.graphdataD = obj.Default.Nitrogen;
    	Nitrogen_Button.graphdataT = obj.Transform.Nitrogen;
    	
    	// Graph_Phosphorus
    	var Phosphorus_Button = this.getComponent('Graph_Phosphorus');
    	Phosphorus_Button.graphdataD = obj.Default.Phosphorus;
    	Phosphorus_Button.graphdataT = obj.Transform.Phosphorus;
    	
    	// Graph_Crop_Pest
    	var Pest_Button = this.getComponent('Graph_Crop_Pest');
    	Pest_Button.graphdataD = obj.Default.Pest;
    	Pest_Button.graphdataT = obj.Transform.Pest;
    	
    	// Graph_Pollinator
    	var Pollinator_Button = this.getComponent('Graph_Pollinator');
    	Pollinator_Button.graphdataD = obj.Default.Pollinator;
    	Pollinator_Button.graphdataT = obj.Transform.Pollinator;
    	
    	// Graph_Biomass
    	var Biomass_Button = this.getComponent('Graph_Biomass');
    	Biomass_Button.graphdataD = obj.Default.Ethanol;
    	Biomass_Button.graphdataT = obj.Transform.Ethanol;
    	
    	// Graph_Net_Income
    	var Net_Income_Button = this.getComponent('Graph_Net_Income');
    	Net_Income_Button.graphdataD = obj.Default.Net_Income;
    	Net_Income_Button.graphdataT = obj.Transform.Net_Income;
    	
    	// Graph_Net_Energy
    	var Net_Energy_Button = this.getComponent('Graph_Net_Energy');
    	Net_Energy_Button.graphdataD = obj.Default.Net_Energy;
    	Net_Energy_Button.graphdataT = obj.Transform.Net_Energy;
    	
    	
    	
    	
    	
    	// Spider_Graph
    	var Spider_Button = this.getComponent('Spider_Graph');
    	var array1 = [obj.Default.Habitat_Index.Average_HI, obj.Default.Nitrogen.Nitrogen, obj.Default.Phosphorus.Phosphorus, obj.Default.Pest.Pest, obj.Default.Pollinator.Pollinator, obj.Default.Ethanol.Ethanol, obj.Default.Net_Income.Net_Income, obj.Default.Net_Energy.Net_Energy];
    	var array2 = [obj.Transform.Habitat_Index.Average_HI, obj.Transform.Nitrogen.Nitrogen, obj.Transform.Phosphorus.Phosphorus, obj.Transform.Pest.Pest, obj.Transform.Pollinator.Pollinator, obj.Transform.Ethanol.Ethanol, obj.Transform.Net_Income.Net_Income, obj.Transform.Net_Energy.Net_Energy];
    	Spider_Button.graphdataD = array1;
    	Spider_Button.graphdataT = array2;
    }

});