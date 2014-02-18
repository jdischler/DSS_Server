
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_Detail', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_detail',
    
    id: "DSS_ReportDetail",
    
 //   height: 380,
    width: 500,
    layout: 'vbox',
    title: 'Simulation Detail',
	icon: 'app/images/magnify_icon.png',
    activeTab: 0,

    requires : [
    	'MyApp.view.Report_ValueTypePopup',
		'MyApp.view.Report_DetailHeader',
		'MyApp.view.Report_DetailElement',
		'MyApp.view.Report_GraphPopUp'
    ],
    
 /*   tools:[{
		type: 'help',
		qtip: 'Simulation Results Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],*/
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
	
        Ext.applyIf(me, {
            items: [{
				xtype: 'container', 
				itemId: 'results_container',
				x: 0,
				y: 0,
				padding: '0 0 3 0', // just really need to pad bottom to maintain spacing there
				layout: {
					type: 'vbox'
				},
				items: [{
					xtype: 'report_value_popup'
				},
				{
					itemId: 'result_soc',
					xtype: 'report_detail_item',
					DSS_FieldString: 'soc',
					DSS_Label: 'Soil Carbon',
					DSS_UnitLabel: 'Mg/Yr',
					DSS_GraphTitle: 'Soil Carbon',
					DSS_InfoHTML: 'http://www.epa.gov/airquality/modeling.html',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_nitrous_oxide',
					xtype: 'report_detail_item',
					DSS_FieldString: 'nitrous_oxide',
					DSS_UnitLabel: 'Mg/Yr',
					DSS_Label: 'Nitrous Oxide',
					DSS_GraphTitle: 'Nitrous Oxide Emissions',
					DSS_InfoHTML: 'http://www.epa.gov/airquality/modeling.html',
					DSS_DetailReportContainer: me
				},
				{
					itemId: 'result_water_quality',
					xtype: 'report_detail_item',
					DSS_FieldString: 'water_quality',
					DSS_UnitLabel: 'Kg/Yr',
					DSS_Label: 'Water Quality',
					DSS_GraphTitle: 'Water Quality',
					DSS_InfoHTML: 'http://www.epa.gov/airquality/modeling.html',
					DSS_DetailReportContainer: me
				},
				{
					itemId: 'result_phosphorus_epic',
					xtype: 'report_detail_item',
					DSS_FieldString: 'P_Loss_EPIC',
					//DSS_UnitLabel: 'mg/l',
					DSS_UnitLabel: 'Kg/Yr',
					DSS_Label: 'P Loading',
					DSS_GraphTitle: 'Phosphorus Epic',
					DSS_InfoHTML: 'http://water.epa.gov/scitech/datait/models/index.cfm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_habitat_index',
					xtype: 'report_detail_item',
					DSS_FieldString: 'habitat_index',
					DSS_UnitLabelDelta: '0 to 1',
					DSS_UnitLabelFile: '0 to 1',
					DSS_Label: 'Bird Habitat',
					DSS_GraphTitle: 'Bird Habitat Index',
					DSS_InfoHTML: 'http://www.chjv.org/chjv_forest_bird_hsi_modeling_p.html',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_pest',
					xtype: 'report_detail_item',
					DSS_FieldString: 'pest',
					DSS_UnitLabelDelta: '0 to 1',
					DSS_UnitLabelFile: '0 to 1',
					DSS_Label: 'Biocontrol',
					DSS_GraphTitle: 'Biocontrol / Crop Pest Supression',
					DSS_InfoHTML: 'http://ncp-dev.stanford.edu/~dataportal/invest-releases/documentation/current_release/croppollination.html',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_pollinators',
					xtype: 'report_detail_item',
					DSS_FieldString: 'pollinator',
					DSS_UnitLabelDelta: '0 to 1',
					DSS_UnitLabelFile: '0 to 1',
					DSS_Label: 'Pollinators',
					DSS_GraphTitle: 'Key Pollinators',
					DSS_InfoHTML: 'http://ncp-dev.stanford.edu/~dataportal/invest-releases/documentation/current_release/croppollination.html',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_ethanol',
					xtype: 'report_detail_item',
					DSS_FieldString: 'ethanol',
					DSS_UnitLabel: 'Gl/Yr',
					DSS_Label: 'Gross Biofuel',
					DSS_GraphTitle: 'Biofuel Production',
					DSS_InfoHTML: 'http://www.sciencedirect.com/science/article/pii/S0305750X11000933',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_net_income',
					xtype: 'report_detail_item',
					DSS_FieldString: 'net_income',
					DSS_UnitLabel: '$Million/Yr',
					DSS_Label: 'Net Income',
					DSS_GraphTitle: 'Net Income',
					DSS_InfoHTML: 'http://www.sciencedirect.com/science/article/pii/S0305750X11000933',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_net_energy',
					xtype: 'report_detail_item',
					DSS_FieldString: 'net_energy',
					DSS_UnitLabel: 'TJ/Yr',
					DSS_Label: 'Net Energy',
					DSS_GraphTitle: 'Net Energy',
					DSS_InfoHTML: 'http://www.sciencedirect.com/science/article/pii/S0305750X11000933',
					DSS_DetailReportContainer: me
				}]
			},{
				xtype: 'container',
				id: 'DSS_heatmap_legend',
				x: -1,
				y: 310,
				
				width: 502,
				height: 40,
				layout: {
					type: 'hbox'
				}
			}]
        });
        
        me.callParent(arguments);
    },

    //--------------------------------------------------------------------------
	clearFields: function() {
		
		Ext.getCmp('DSS_SpiderGraphPanel').clearSpiderData(0);// set all fields to zero

		// cycle through all of the detail elements and clear them out...
		var c = this.getComponent('results_container');
		for (var idx = 0; idx < c.items.getCount(); idx++) {
			var comp = c.items.getAt(idx);
			comp.clearFields();
		}
	},
	
    //--------------------------------------------------------------------------
	clearHeatToggles: function(exceptFor) {
	
		// TODO: fixme
		var c = this.getComponent('results_container');
		for (var idx = 0; idx < c.items.getCount(); idx++) {
			var comp = c.items.getAt(idx);
			if (!exceptFor || exceptFor !== comp) {
				comp.clearHeatToggle();
			}
		}
	},
	
    //--------------------------------------------------------------------------
	setWaitFields: function() {
		
		this.clearFields();

		// cycle through all of the detail elements and set the wait on them...
		var c = this.getComponent('results_container');
		for (var idx = 0; idx < c.items.getCount(); idx++) {
			var comp = c.items.getAt(idx);
			comp.setWait();
		}
	},
	
	// valid style values: 'delta', 'file1', 'file2'
    //--------------------------------------------------------------------------
	setDataStyle: function(newStyle) {
		
		var c = this.getComponent('results_container');
		for (var idx = 0; idx < c.items.getCount(); idx++) {
			var comp = c.items.getAt(idx);
			comp.changeDataStyleType(newStyle);
		}
	},

	// valid substyle values: 'quantile', 'equal'
    //--------------------------------------------------------------------------
	changeDataSubStyle: function(subtype) {
		
		var c = this.getComponent('results_container');
		for (var idx = 0; idx < c.items.getCount(); idx++) {
			var comp = c.items.getAt(idx);
			comp.changeDataSubStyle(subtype);
		}
	},

	// valid style values: 'absolute', '%'	
    //--------------------------------------------------------------------------
	setValueStyle: function(newStyle) {
		
		var c = this.getComponent('results_container');
		for (var idx = 0; idx < c.items.getCount(); idx++) {
			var comp = c.items.getAt(idx);
			comp.changeValueStyleType(newStyle);
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
    setData: function(obj)
    {
		var c = this.getComponent('results_container');
		
		if (obj.habitat_index) {
			var base = obj.habitat_index.selection;
//			var base = obj.habitat_index.landscape;
			var val1 = base.file1.sum / base.file1.count;
			var val2 = base.file2.sum / base.file2.count;
			var totalVal = (val2 - val1);
			c.getComponent('result_habitat_index').setData(val1, val2, totalVal, base);
		}
		
		if (obj.soc) {
			var base = obj.soc.selection;
//			var base = obj.soc.landscape;
			// Convert from Kg per Ha to Mg per cell (...*900/(10000*1000))
			var val1 = base.file1.sum * 0.09 / 1000;
			var val2 = base.file2.sum * 0.09 / 1000;
			// This is for 1 year and other process is on server side
			// Convert change from 20 years to 1 year
			var totalVal = ((val2 - val1) / 20);
			//var totalVal = (val2 - val1);
			c.getComponent('result_soc').setData(val1, val2, totalVal, base);
		}	

		if (obj.net_income) {
			var base = obj.net_income.selection;
//			var base = obj.net_income.landscape;
		// Convert from $ per Ha to million $ per cell (...*900/(10000*1000*1000))
			var val1 = base.file1.sum * 0.09 / 1000000;
			var val2 = base.file2.sum * 0.09 / 1000000;
			var totalVal = (val2 - val1);
			c.getComponent('result_net_income').setData(val1, val2, totalVal, base);
		}	

		if (obj.net_energy) {
			var base = obj.net_energy.selection;
//			var base = obj.net_energy.landscape;
			// Convert from MJ per Ha to TJ per cell (...*900/(10000*1000*1000))
			var val1 = base.file1.sum * 0.09 / 1000000;
			var val2 = base.file2.sum * 0.09 / 1000000;
			var totalVal = (val2 - val1);
			c.getComponent('result_net_energy').setData(val1, val2, totalVal, base);
		}	

		if (obj.phosphorus) {
			var base = obj.phosphorus.selection;
//			var base = obj.phosphorus.landscape;
			var val1 = base.file1.sum / base.file1.count;
			var val2 = base.file2.sum / base.file2.count;
			var totalVal = (val2 - val1);
			c.getComponent('result_phosphorus').setData(val1, val2, totalVal, base);
		}
		if (obj.P_Loss_EPIC) {
			var base = obj.P_Loss_EPIC.selection;
//			var base = obj.P_Loss_EPIC.landscape;
			var val1 = base.file1.sum / base.file1.count;
			var val2 = base.file2.sum / base.file2.count;
			var totalVal = (val2 - val1);
			c.getComponent('result_phosphorus_epic').setData(val1, val2, totalVal, base);
		}	

		if (obj.water_quality) {
			var base = obj.water_quality.selection;
//			var base = obj.water_quality.landscape;
			var val1 = base.file1.sum / base.file1.count;
			var val2 = base.file2.sum / base.file2.count;
			var totalVal = (val2 - val1);
			c.getComponent('result_water_quality').setData(val1, val2, totalVal, base);
		}	
    	
    	if (obj.ethanol) {
    		var base = obj.ethanol.selection;
//			var base = obj.ethanol.landscape;
			// Convert from L per Ha to Giga L per cell (...*900/(10000*1000*1000))
			var val1 = base.file1.sum * 0.09 / 1000000;
			var val2 = base.file2.sum * 0.09 / 1000000;
			var totalVal = (val2 - val1);
			c.getComponent('result_ethanol').setData(val1, val2, totalVal, base);
		}
		
    	if (obj.pest) {
    		var base = obj.pest.selection;
//			var base = obj.pest.landscape;
			var val1 = base.file1.sum / base.file1.count;
			var val2 = base.file2.sum / base.file2.count;
			var totalVal = (val2 - val1);
			c.getComponent('result_pest').setData(val1, val2, totalVal, base);
		}	
    	
    	if (obj.pollinator) {
    		var base = obj.pollinator.selection; 
//			var base = obj.pollinator.landscape;
    		var max = base.range.max;
			var val1 = base.file1.sum / (base.file1.count * max);
			var val2 = base.file2.sum / (base.file1.count * max);
			var totalVal = (val2 - val1);
			c.getComponent('result_pollinators').setData(val1, val2, totalVal, base);
		}	
	
    	if (obj.nitrous_oxide) {
    		var base = obj.nitrous_oxide.selection;
//			var base = obj.nitrous_oxide.landscape;
			// Convert from Kg per Ha to Mega per cell (...*900/(10000*1000))
			var val1 = base.file1.sum * 0.09 / 1000;
			var val2 = base.file2.sum * 0.09 / 1000;
			var totalVal = (val2 - val1);
			c.getComponent('result_nitrous_oxide').setData(val1, val2, totalVal, base);
		}	
    }

});

