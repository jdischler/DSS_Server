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
			    width: 150,
			    fieldLabel: 'Habitat',
			    labelWidth: 80
			},
			{
			    itemId: 'Carbon',
			    xtype: 'textfield',
			    x: 260,
			    y: 10,
			    width: 150,
			    fieldLabel: 'Carbon',
			    labelWidth: 80
			},
			{
			    itemId: 'Nitrogen',
			    xtype: 'textfield',
			    x: 10,
			    y: 50,
			    width: 150,
			    fieldLabel: 'Nitrogen',
			    labelWidth: 80
			},
			{
			    itemId: 'Pollinator',
			    xtype: 'textfield',
			    x: 260,
			    y: 50,
			    width: 150,
			    fieldLabel: 'Pollinator',
			    labelWidth: 80
			},
			{
			    itemId: 'Phosphorus',
			    xtype: 'textfield',
			    x: 10,
			    y: 100,
			    width: 150,
			    fieldLabel: 'Phosphorus',
			    labelWidth: 80
			},
			{
			    itemId: 'Net_Energy',
			    xtype: 'textfield',
			    x: 260,
			    y: 100,
			    width: 150,
			    fieldLabel: 'Net Energy',
			    labelWidth: 80
			},
			{
		            itemId: 'Crop_Pest',
			    xtype: 'textfield',
			    x: 10,
			    y: 150,
			    width: 150,
			    fieldLabel: 'Crop Pest',
			    labelWidth: 80
			},
			{
			    itemId: 'Net_Income',
			    xtype: 'textfield',
			    x: 260,
			    y: 150,
			    width: 150,
			    fieldLabel: 'Net Income',
			    labelWidth: 80
			},
			{
			    itemId: 'Graph_Habitat_Index',
			    xtype: 'button',
			    x: 170,
			    y: 10,
			    text: 'Graph',
			    handler: function (self)
			    {
			    	    var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Habitat Index'});
			    	
			    	    mypopup.show();
			    	    mypopup.SetChartData(self.graphdata);
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
			    	    mypopup.SetChartData(self.graphdata);
			    }
			},
			{
			    itemId: 'Graph_Nitrogen',
			    xtype: 'button',
			    x: 170,
			    y: 50,
			    text: 'Graph',
			    handler: function (self)
			    {
			    	    var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Nitrogen'});
			    	    mypopup.show();
			    	    mypopup.SetChartData(self.graphdata);
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
			    	    mypopup.SetChartData(self.graphdata);
			    }
			},
			{
			    itemId: 'Graph_Phosphorus',
			    xtype: 'button',
			    x: 170,
			    y: 100,
			    text: 'Graph',
			    handler: function (self)
			    {
			    	    var mypopup = Ext.create("MyApp.view.GraphPopUp", {title: 'Phosphorus'});
			    	    mypopup.show();
			    	    mypopup.SetChartData(self.graphdata);
			    }
			},
			{
			    itemId: 'Graph_Net_Energy',
			    xtype: 'button',
			    x: 430,
			    y: 100,
			    text: 'Graph',
			    handler: function (self)
			    {
			    	    var mypopup = Ext.create("MyApp.view.GraphPopUp");
			    	    mypopup.show();
			    	    mypopup.SetChartData(self.graphdata);
			    }
			},
			{
			    itemId: 'Graph_Crop_Pest',
			    xtype: 'button',
			    x: 170,
			    y: 150,
			    text: 'Graph',
			    handler: function (self)
			    {
			    	    var mypopup = Ext.create("MyApp.view.GraphPopUp");
			    	    mypopup.show();
			    	    mypopup.SetChartData(self.graphdata);
			    }
			},
			{
			    itemId: 'Graph_Net_Income',
			    xtype: 'button',
			    x: 430,
			    y: 150,
			    text: 'Graph',
			    handler: function (self)
			    {
			    	    var mypopup = Ext.create("MyApp.view.GraphPopUp");
			    	    mypopup.show();
			    	    mypopup.SetChartData(self.graphdata);
			    }
			},
                //}
            ]
        });
        	
        me.callParent(arguments);
    },
    
        SetData: function(obj)
    {
    	// Habitat Index
    	var val1 = obj.Habitat_Index.Average_HI;
    	var Habitat_Text = this.getComponent('Habitat_Index');
    	Habitat_Text.setValue(val1);
    	
    	var val2 = obj.Nitrogen.Nitrogen_T;
    	var Nitrogen_Text = this.getComponent('Nitrogen');
    	Nitrogen_Text.setValue(val2);
    	
    	var val3 = obj.Phosphorus.Phosphorus_T;
    	var Phosphorus_Text = this.getComponent('Phosphorus');
    	Phosphorus_Text.setValue(val3);
    	
    	var Habitat_Button = this.getComponent('Graph_Habitat_Index');
    	Habitat_Button.graphdata = obj.Habitat_Index;
    	
    	var Nitrogen_Button = this.getComponent('Graph_Nitrogen');
    	Nitrogen_Button.graphdata = obj.Nitrogen;
    	
    	var Phosphorus_Button = this.getComponent('Graph_Phosphorus');
    	Phosphorus_Button.graphdata = obj.Phosphorus;
    }

});