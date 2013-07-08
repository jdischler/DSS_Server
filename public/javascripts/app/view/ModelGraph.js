Ext.define('MyApp.view.ModelGraph', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.ModelGraph',
    
    id: "Model_Graph",
    
    height: 200,
    width: 500,
        layout: {
        type: 'absolute'
    },
    //bodyPadding: 10,
    title: 'Simulation Results',
	icon: 'app/images/scenario_icon.png',
    activeTab: 0,

    require : [
    	    'MyApp.view.GraphPopUp'
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
					var mypopup = Ext.create("MyApp.view.GraphPopUp");
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
					var mypopup = Ext.create("MyApp.view.GraphPopUp");
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
					var mypopup = Ext.create("MyApp.view.GraphPopUp");
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
					var mypopup = Ext.create("MyApp.view.GraphPopUp");
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
					var mypopup = Ext.create("MyApp.view.GraphPopUp");
					mypopup.show();
					mypopup.SetChartData(self.graphdataD, self.graphdataT);
			    }
			}]
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
    	
    	var val3 = obj.Default.Nitrogen.Nitrogen;
    	var val4 = obj.Transform.Nitrogen.Nitrogen;
    	var Nitrogen_Text = this.getComponent('Nitrogen');
    	Nitrogen_Text.setValue(val3-val4);
    	
    	var val5 = obj.Default.Phosphorus.Phosphorus;
    	var val6 = obj.Transform.Phosphorus.Phosphorus;
    	var Phosphorus_Text = this.getComponent('Phosphorus');
    	Phosphorus_Text.setValue(val5-val6);
    	
    	var Habitat_Button = this.getComponent('Graph_Habitat_Index');
    	Habitat_Button.graphdataD = obj.Default.Habitat_Index;
    	Habitat_Button.graphdataT = obj.Transform.Habitat_Index;
    	
    	var Nitrogen_Button = this.getComponent('Graph_Nitrogen');
    	Nitrogen_Button.graphdataD = obj.Default.Nitrogen;
    	Nitrogen_Button.graphdataT = obj.Transform.Nitrogen;
    	
    	var Phosphorus_Button = this.getComponent('Graph_Phosphorus');
    	Phosphorus_Button.graphdataD = obj.Default.Phosphorus;
    	Phosphorus_Button.graphdataT = obj.Transform.Phosphorus;
    }

});