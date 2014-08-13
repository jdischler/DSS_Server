
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderGraph', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_spider',

	requires: [
		'MyApp.view.Report_SpiderHeader',
		'MyApp.view.Report_SpiderGraphObject'
	],
	
//   height: 420,
    width: 500,
    title: 'Quick Summary',
	icon: 'app/images/fast_icon.png',
    layout: 'vbox',
    id: 'DSS_SpiderGraphPanel',

    //------------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

		Ext.define('Spider_Model', {
			extend: 'Ext.data.Model',
			fields: ['Current', 'Scenario', 'Bin', 'Match', 'IntermCurrent', 'IntermScenario']
		});
	
        me.graphDetailStore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [ 
				{Bin: 'Net Income', Match: 'net_income'}, 
				{Bin: 'Gross Biofuel', Match: 'ethanol'}, 
				{Bin: 'Net Energy', Match: 'net_energy'},
				{Bin: 'Phosphorus', Match: 'P_Loss_EPIC'},
				{Bin: 'Soil Loss', Match: 'soil_loss'}, 
				{Bin: 'Soil Carbon', Match: 'soc'}, 
				{Bin: 'Nitrous Oxide', Match: 'nitrous_oxide'},
				{Bin: 'Pollinators', Match: 'pollinator'}, 
				{Bin: 'Biocontrol', Match: 'pest'}, 
				{Bin: 'Bird Habitat', Match: 'habitat_index'}]
		});
        me.graphCombinedStore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [ 
				{Bin: 'Economic', Match: 'economic'},
				{Bin: 'Energy', Match: 'energy'},
				{Bin: 'Erosion', Match: 'erosion'}, 
				{Bin: 'Emissions', Match: 'emissions'}, 
				{Bin: 'Biodiversity', Match: 'biodiversity'}]
		});
                    
        Ext.applyIf(me, {
            items: [{
            	xtype: 'report_spider_header'
            },{
				xtype: 'report_spiderObject',
				hidden: true,
				id: 'DSS_CombinedSpiderGraph',
				store: me.graphCombinedStore
			},{
				xtype: 'report_spiderObject',
				id: 'DSS_DetailSpiderGraph',
				store: me.graphDetailStore,
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    setSpiderDataElement: function(value1, value2, element) {

    	// Fill in detailed spider data
    	var rec = this.graphDetailStore.findRecord('Match', element);
		var max = value1;
		if (value2 > max) {
			max = value2;
		}
		
		// Fix negative value problem
		if (value1 < 0 & value2 < 0){
			value1 = -value1;
			value2 = -value2;
			
			var max = value1;
			if (value2 > max) {
				max = value2;
			}
		}
		else if (value1 < 0){
			max = value2 - 2 * value1;
			value1 = -value1;
			value2 = max;
		}
		else if (value2 < 0){
			max = value1 - 2 * value2;
			value2 = -value2;
			value1 = max;
		}
		

		
		// FIXME: reversed because we don't know why the data is reversed...blah
		var result1 = value2 / max * 100;
		var result2 = value1 / max * 100;
		
    	if (rec) {
			rec.set("Current", result1);
			rec.set("Scenario", result2);
			rec.commit();
    	}
    	
    	// calculate combined spider data - have to figure out which things go to which
    	var newmatch='';
    	var divisor = 1;
		if (element =='P_Loss_EPIC') {
			newmatch = 'erosion';
			divisor = 2;
		}
		else if (element =='soil_loss') {
			newmatch = 'erosion';
			divisor = 2;
		}
		else if (element =='habitat_index') {
			newmatch = 'biodiversity';
			divisor = 3;
		}
		else if (element =='pest') {
			newmatch = 'biodiversity';
			divisor = 3;
		}
		else if (element =='pollinator') {
			newmatch = 'biodiversity';
			divisor = 3;
		}
		else if (element =='net_income') {
			newmatch = 'economic';
			divisor = 1;
		}
		else if (element =='ethanol') {
			newmatch = 'energy';
			divisor = 2;
		}
		else if (element =='net_energy') {
			newmatch = 'energy';
			divisor = 2;
		}
		else if (element =='soc') {
			newmatch = 'emissions';
			divisor = 2;
		}
		else if (element =='nitrous_oxide') {
			newmatch = 'emissions';
			divisor = 2;
		}
		
		var rec = this.graphCombinedStore.findRecord('Match', newmatch);
		if (rec) {
			var intermediate1 = rec.get('IntermCurrent') + result1;
			var intermediate2 = rec.get('IntermScenario') + result2;
			rec.set('IntermCurrent', intermediate1);
			rec.set('IntermScenario', intermediate2);
			
			intermediate1 = intermediate1 / divisor;
			intermediate2 = intermediate2 / divisor;
			
			var max = intermediate1;
			if (intermediate2 > max) {
				max = intermediate2;
			}
			result1 = intermediate1 / max * 100;
			result2 = intermediate2 / max * 100;
  			
			rec.set('Current', result1);
			rec.set('Scenario', result2);
			
			rec.commit();
    	}

    },
    
    //--------------------------------------------------------------------------
    clearSpiderData: function(defaultValue)
    {
    	// Clear the detailed spider
		for (var idx = 0; idx < this.graphDetailStore.count(); idx++)
		{
			var rec = this.graphDetailStore.getAt(idx);
			rec.set("Current", defaultValue);
			rec.set("Scenario", defaultValue);
			rec.commit();
		}
		for (var idx = 0; idx < this.graphCombinedStore.count(); idx++)
		{
			var rec = this.graphCombinedStore.getAt(idx);
			rec.set("Current", defaultValue);
			rec.set("Scenario", defaultValue);
			rec.set('IntermCurrent', 0);
			rec.set('IntermScenario', 0);
			rec.commit();
		}
    },

	// Where type is a string, valid values: 'detail' or 'combined'    
    //--------------------------------------------------------------------------
    setSpiderDetailType: function(type) {
    	
    	var showControl = Ext.getCmp('DSS_CombinedSpiderGraph');
    	var hideControl = Ext.getCmp('DSS_DetailSpiderGraph'); 
    	if (type == 'detail') {
    		// swap values...
    		var temp = showControl;
    		showControl = hideControl;
    		hideControl = temp;
    	}
    	
		Ext.suspendLayouts();
		showControl.show();
		// FIXME: does this fix the problem of the data points not always being updated correctly on a hidden graph???
 		showControl.redraw();
		hideControl.hide();
		Ext.resumeLayouts(true);

    }
    
});

