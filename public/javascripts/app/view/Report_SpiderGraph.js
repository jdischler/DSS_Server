
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
			fields: ['Default', 'Transform', 'Bin', 'Match', 'IntermDefault', 'IntermTransform']
		});
	
        this.graphDetailStore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [ 
					{Bin: 'Phosphorus', Match: 'water_quality'}, 
					{Bin: 'Soil Carbon', Match: 'soc'}, 
					{Bin: 'Nitrous Oxide', Match: 'nitrous_oxide'},
					{Bin: 'Pollinators', Match: 'pollinator'}, 
					{Bin: 'Biocontrol', Match: 'pest'}, 
					{Bin: 'Bird Habitat', Match: 'habitat_index'},
					{Bin: 'Net Income', Match: 'net_income'}, 
					{Bin: 'Gross Biofuel', Match: 'ethanol'}, 
					{Bin: 'Net Energy', Match: 'net_energy'},
					{Bin: 'Phosphorus EPIC', Match: 'P_Loss_EPIC'}]
		});
        this.graphCombinedStore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [ 
					{Bin: 'Erosion', Match: 'erosion'}, 
					{Bin: 'Emissions', Match: 'emissions'}, 
					{Bin: 'Biodiversity', Match: 'biodiversity'},
					{Bin: 'Economic', Match: 'economic'},
					{Bin: 'Energy', Match: 'energy'}]
		});
                    
        Ext.applyIf(me, {
            items: [{
            	xtype: 'report_spider_header'
            },{
				xtype: 'report_spiderObject',
				hidden: true,
				id: 'DSS_CombinedSpiderGraph',
				store: this.graphCombinedStore
			},{
				xtype: 'report_spiderObject',
				id: 'DSS_DetailSpiderGraph',
				store: this.graphDetailStore
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
		var result1 = value2 / max * 100;
		var result2 = value1 / max * 100;
    	if (rec) {
			// FIXME: reversed because we don't know why the data is reversed...blah
			rec.set("Default", result1);
			rec.set("Transform", result2);
			rec.commit();
    	}
    	
    	// calculate combined spider data - have to figure out which things go to which
    	var newmatch='';
    	var divisor = 1;
		if (element =='P_Loss_EPIC') {
			newmatch = 'erosion';
			divisor = 2;
		}
		else if (element =='water_quality') {
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
			// FIXME: reversed because we don't know why the data is reversed...blah
			var intermediate1 = rec.get('IntermDefault') + result1;
			var intermediate2 = rec.get('IntermTransform') + result2;
			rec.set('IntermDefault', intermediate1);
			rec.set('IntermTransform', intermediate2);
			
			intermediate1 = intermediate1 / divisor;
			intermediate2 = intermediate2 / divisor;
			
			var max = intermediate1;
			if (intermediate2 > max) {
				max = intermediate2;
			}
			result1 = intermediate2 / max * 100;
			result2 = intermediate1 / max * 100;
  			
			rec.set('Default', result1);
			rec.set('Transform', result2);
			
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
			rec.set("Default", defaultValue);
			rec.set("Transform", defaultValue);
			rec.commit();
		}
		for (var idx = 0; idx < this.graphCombinedStore.count(); idx++)
		{
			var rec = this.graphCombinedStore.getAt(idx);
			rec.set("Default", defaultValue);
			rec.set("Transform", defaultValue);
			rec.set('IntermDefault', 0);
			rec.set('IntermTransform', 0);
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

