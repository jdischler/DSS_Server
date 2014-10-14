
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_Detail', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_detail',
    
    id: "DSS_ReportDetail",
    
    width: 500,
 //   layout: 'vbox',
    title: 'Simulation Detail',
	icon: 'app/images/magnify_icon.png',

    requires : [
    	'MyApp.view.Report_ValueTypePopup',
		'MyApp.view.Report_DetailHeader',
		'MyApp.view.Report_DetailElement',
		'MyApp.view.Report_GraphPopUp',
        'MyApp.view.Report_HeatmapLegendPopUp'
    ],
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
	
        // Common conversion tranform definitions...
        var ton_to_megagram = {
			DSS_HiddenConversion: true,
			DSS_ConversionLabel: '<i>conversion to megagrams</i>',
			DSS_ConversionFactor: '0.90718474', // TODO: AMIN Validate
			DSS_ResultsPreUnits: '',
			DSS_ResultsPostUnits: 'mg/yr'
		};
		
        Ext.applyIf(me, {
            items: [{
				xtype: 'container', 
				itemId: 'results_container',
				padding: '0 0 3 0', // just really need to pad bottom to maintain spacing there
				/*layout: { 
					type: 'vbox'
				},*/
				items: [{
					xtype: 'report_value_popup'
				},
				{
					itemId: 'result_net_income',
					xtype: 'report_detail_item',
					DSS_FieldString: 'net_income',
					DSS_UnitLabel: '$Million/Yr',
					DSS_Label: 'Net Income',
					DSS_GraphTitle: 'Net Income',
					DSS_InfoHTML: 'help/yield_ethanol_net_energy_net_income.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_ethanol',
					xtype: 'report_detail_item',
					DSS_FieldString: 'ethanol',
					DSS_UnitLabel: 'Gal/Yr',
					//DSS_UnitLabel: 'Gl/Yr',
					DSS_Label: 'Gross Biofuel',
					DSS_calculators: [{
						DSS_HiddenConversion: true,
						DSS_ConversionLabel: '<i>conversion to liters</i>',
						DSS_ConversionFactor: '3.78541', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '',
						DSS_ResultsPostUnits: 'l/yr'
					},{
						DSS_HiddenConversion: true,
						DSS_ConversionLabel: '<i>conversion to kiloliters</i>',
						DSS_ConversionFactor: '0.00378541178', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '',
						DSS_ResultsPostUnits: 'kl/yr'
					},{
						DSS_ConversionLabel: '<b>x</b>  price per Gal',
						DSS_ConversionFactor: '2.00', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '$',
						DSS_ResultsPostUnits: ''
					}],
					DSS_GraphTitle: 'Biofuel Production',
					DSS_InfoHTML: 'help/yield_ethanol_net_energy_net_income.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_net_energy',
					xtype: 'report_detail_item',
					DSS_FieldString: 'net_energy',
					//DSS_UnitLabel: 'TJ/Yr',
					DSS_UnitLabel: 'MBtu/Yr',
					DSS_Label: 'Net Energy',
					DSS_calculators: [{
						DSS_HiddenConversion: true,
						DSS_ConversionLabel: '<i>conversion to megajoules</i>', // TODO: AMIN validate - use Tera, Giga, Mega?
						DSS_ConversionFactor: '1.1', // TODO: AMIN Validate - use Tera, Giga, Mega?
						DSS_ResultsPreUnits: '',
						DSS_ResultsPostUnits: 'mj/yr' // TODO: Amin - use Tera, Giga, Mega?
					},{
						DSS_ConversionLabel: '<b>x</b>  price per mbtu',
						DSS_ConversionFactor: '4.00', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '$',
						DSS_ResultsPostUnits: ''
					}],
					DSS_GraphTitle: 'Net Energy',
					DSS_InfoHTML: 'help/yield_ethanol_net_energy_net_income.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_phosphorus_epic',
					xtype: 'report_detail_item',
					DSS_FieldString: 'p_loss_epic',
					//DSS_UnitLabel: 'Kg/Yr',
					//DSS_UnitLabel: 'ton/Yr',
					DSS_UnitLabel: 'lb/Yr',
					DSS_Label: 'Phosphorus',
					DSS_calculators: [{
						DSS_HiddenConversion: true,
						DSS_ConversionLabel: '<i>conversion to short tons</i>',
						DSS_ConversionFactor: '0.0005', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '',
						DSS_ResultsPostUnits: 'ton/yr'
					},{
						DSS_HiddenConversion: true,
						DSS_ConversionLabel: '<i>conversion to kilograms</i>',
						DSS_ConversionFactor: '0.453592', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '',
						DSS_ResultsPostUnits: 'kg/yr'
					},{
						DSS_ConversionLabel: '<b>x</b>  price per lb',
						DSS_ConversionFactor: '20.00', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '$',
						DSS_ResultsPostUnits: ''
					}],
					DSS_GraphTitle: 'Phosphorus Epic',
					DSS_InfoHTML: 'help/phosphorous.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_soil_loss',
					xtype: 'report_detail_item',
					DSS_FieldString: 'soil_loss',
					//DSS_UnitLabel: 'Mg/Yr',
					DSS_UnitLabel: 'ton/Yr',
					DSS_Label: 'Soil Loss',
					DSS_calculators: [ton_to_megagram],
					DSS_GraphTitle: 'Soil Loss',
					DSS_InfoHTML: 'help/soil_loss.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_soc',
					xtype: 'report_detail_item',
					DSS_FieldString: 'soc',
					DSS_Label: 'Soil Carbon',
					//DSS_UnitLabel: 'Mg/Yr',
					DSS_UnitLabel: 'ton/Yr',
					DSS_calculators: [ton_to_megagram,
					{
						DSS_ConversionLabel: '<b>x</b>  price per lb C',
						DSS_ConversionFactor: '0.40', // TODO: AMIN Validate
						DSS_ExtraFactor: 298,			// TODO: AMIN Validate
						DSS_ResultsPreUnits: '$',
						DSS_ResultsPostUnits: ''
					}],
					DSS_GraphTitle: 'Soil Carbon',
					DSS_InfoHTML: 'help/soil_carbon.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_nitrous_oxide',
					xtype: 'report_detail_item',
					DSS_FieldString: 'nitrous_oxide',
					//DSS_UnitLabel: 'Mg/Yr',
					DSS_UnitLabel: 'ton/Yr',
					DSS_Label: 'Nitrous Oxide',
					DSS_calculators: [ton_to_megagram,
					{
						DSS_HiddenConversion: true,
						DSS_ConversionLabel: '<i>conversion to CO<sub>2</sub> equiv (GMW)</i>',
						DSS_ConversionFactor: '298', // TODO: AMIN Validate
						DSS_ResultsPreUnits: '',
						DSS_ResultsPostUnits: 'CO<sub>2</sub> equiv'
					},{
						DSS_ConversionLabel: '<b>x</b>  price per lb C',
						DSS_ConversionFactor: '0.40', // TODO: AMIN Validate
						DSS_ExtraFactor: 298,	// TODO: AMIN Validate
						DSS_ResultsPreUnits: '$',
						DSS_ResultsPostUnits: ''
					}],
					DSS_GraphTitle: 'Nitrous Oxide Emissions',
					DSS_InfoHTML: 'help/nitrous_oxide_emission.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_pollinators',
					xtype: 'report_detail_item',
					DSS_FieldString: 'pollinator',
					DSS_UnitLabelDelta: '0 to 1',
					DSS_UnitLabelFile: '0 to 1',
					DSS_Label: 'Pollinators',
					DSS_GraphTitle: 'Pollinators',
					DSS_InfoHTML: 'help/pollinator.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_pest',
					xtype: 'report_detail_item',
					DSS_FieldString: 'pest',
					DSS_UnitLabelDelta: '0 to 1',
					DSS_UnitLabelFile: '0 to 1',
					DSS_Label: 'Biocontrol',
					DSS_GraphTitle: 'Biocontrol / Crop Pest Supression',
					DSS_InfoHTML: 'help/pest_suppression.htm',
					DSS_DetailReportContainer: me
				},{
					itemId: 'result_habitat_index',
					xtype: 'report_detail_item',
					DSS_FieldString: 'habitat_index',
					DSS_UnitLabelDelta: '0 to 1',
					DSS_UnitLabelFile: '0 to 1',
					DSS_Label: 'Bird Habitat',
					DSS_GraphTitle: 'Bird Habitat Index',
					DSS_InfoHTML: 'help/biodiversity.htm',
					DSS_DetailReportContainer: me
				}]
			}]
        });
        
        me.callParent(arguments);
        
    	// Create a hidden popup for our legend...
		Ext.create("MyApp.view.Report_HeatmapLegendPopUp");//.show();
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
	
	// TODO: Just trying to cut down on crappy duplicated code...ie, having display strings
	//	littered everywhere. This idea (of things being config'd in one place) needs
	//	to go a ton further, really...
    //--------------------------------------------------------------------------
	getDisplayLabelForKey: function(keyString) {
		
		var c = this.getComponent('results_container');
		for (var idx = 0; idx < c.items.getCount(); idx++) {
			var comp = c.items.getAt(idx);
			if (comp.DSS_FieldString && comp.DSS_FieldString == keyString) {
				return comp.DSS_GraphTitle;
			}
		}
		
		return null;
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

	// valid comparison types: 'selected', 'all' as in, Selected Pixels only, or All pixels (entire landscape)
    //--------------------------------------------------------------------------
	setComparisonType: function(newType) {
		
		// FIXME: TODO
		// Need to switch existing field values, etc here!!!!!
	},
	
	// valid comparison types: 'none', 'area', or 'income'
    //--------------------------------------------------------------------------
	setNormalizeComparison: function(newNormalizeType) {
		
		// FIXME: TODO
		// Need to switch existing normalize style values, etc here!!!!!
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
//			Calculate average habitat index per cell
			var val1 = base.file1.sum / base.file1.count;
			var val2 = base.file2.sum / base.file2.count;
			var totalVal = val2 - val1;
			c.getComponent('result_habitat_index').setData(val1, val2, totalVal, base);
		}
		
		if (obj.soc) {
			var base = obj.soc.selection;
//			var base = obj.soc.landscape;
			// Convert from Mg per Ha to Mg per cell (...*900/10000)
			//var val1 = base.file1.sum * 0.09;
			//var val2 = base.file2.sum * 0.09;
			/// Convert from Mg per Ha to short tons per acre (...*0.44597329)
			//var val1 = base.file1.sum * 0.44597329;
			//var val2 = base.file2.sum * 0.44597329;
			/// Convert Mg to short tons
			var val1 = base.file1.sum * 1.102;
			var val2 = base.file2.sum * 1.102;
			// This is for 1 year and other process is on server side
			// Mg
			//var val1 = base.file1.sum;
			//var val2 = base.file2.sum;
			var totalVal = val2 - val1;
			//var totalVal = (val2 - val1);
			c.getComponent('result_soc').setData(val1, val2, totalVal, base);
		}	

		if (obj.net_income) {
			var base = obj.net_income.selection;
//			var base = obj.net_income.landscape;
		// Convert from $ per Ha to million $ per cell (...*900/(10000*1000*1000))
			//var val1 = base.file1.sum * 0.09 / 1000000;
			//var val2 = base.file2.sum * 0.09 / 1000000;
			// Convert $ to million $
			var val1 = base.file1.sum / 1000000;
			var val2 = base.file2.sum / 1000000;
			var totalVal = val2 - val1;
			c.getComponent('result_net_income').setData(val1, val2, totalVal, base);
		}	

		if (obj.net_energy) {
			var base = obj.net_energy.selection;
//			var base = obj.net_energy.landscape;
			// Convert from MJ per Ha to TJ per cell (...*900/(10000*1000*1000*2.4710))
			//var val1 = base.file1.sum * 0.09 / 1000000;
			//var val2 = base.file2.sum * 0.09 / 1000000;
			// MJ per cell to TJ per cell
			//var val1 = base.file1.sum / 1000000;
			//var val2 = base.file2.sum / 1000000;
			// Convert MJ to Million btu
			var val1 = base.file1.sum * 947.817 / 1000000;
			var val2 = base.file2.sum * 947.817 / 1000000;
			var totalVal = val2 - val1;
			c.getComponent('result_net_energy').setData(val1, val2, totalVal, base);
		}	

		/*if (obj.phosphorus) {
			var base = obj.phosphorus.selection;
//			var base = obj.phosphorus.landscape;
			// Units are Mg per Ha
			//var val1 = base.file1.sum / base.file1.count;
			//var val2 = base.file2.sum / base.file2.count;
			// Kg
			//var val1 = base.file1.sum;
			//var val2 = base.file2.sum;
			// Convert Mg to Short tons then to lb
			var val1 = base.file1.sum * 1.102 * 2000;
			var val2 = base.file2.sum * 1.102 * 2000;
			var totalVal = val2 - val1;
			c.getComponent('result_phosphorus').setData(val1, val2, totalVal, base);
		}*/
		if (obj.p_loss_epic) {
			var base = obj.p_loss_epic.selection;
//			var base = obj.P_Loss_EPIC.landscape;
			// Kg
			//var val1 = base.file1.sum;
			//var val2 = base.file2.sum;
			//var val1 = base.file1.sum / base.file1.count;
			//var val2 = base.file2.sum / base.file2.count;
			// Convert Mg per Ha to Short tons per acre
			//val1 = val1 * 0.44597329;
			//val2 = val2 * 0.44597329;
			// Convert Mg to Short tons per Year
			var val1 = base.file1.sum * 1.102 * 2000;
			var val2 = base.file2.sum * 1.102 * 2000;
			var totalVal = val2 - val1;
			c.getComponent('result_phosphorus_epic').setData(val1, val2, totalVal, base);
		}	
		/*if (obj.water_quality) {
			var base = obj.water_quality.selection;
//			var base = obj.water_quality.landscape;
			var val1 = base.file1.sum;
			var val2 = base.file2.sum;
			// Convert Mg to Short tons per per Year
			val1 = val1 * 1.102;
			val2 = val2 * 1.102;
			var totalVal = (val2 - val1);
			c.getComponent('result_water_quality').setData(val1, val2, totalVal, base);
		}*/
		if (obj.soil_loss) {
			var base = obj.soil_loss.selection;
//			var base = obj.water_quality.landscape;
			//Tonns per cell
			//var val1 = base.file1.sum / base.file1.count;
			//var val2 = base.file2.sum / base.file2.count;
			//Mg per cell per year
			//var val1 = base.file1.sum;
			//var val2 = base.file2.sum;
			// Convert Mg to Short tons per year
			var val1 = base.file1.sum * 1.102;
			var val2 = base.file2.sum * 1.102;
			var totalVal = val2 - val1;
			c.getComponent('result_soil_loss').setData(val1, val2, totalVal, base);
		}	
    	
    	if (obj.ethanol) {
    		var base = obj.ethanol.selection;
//			var base = obj.ethanol.landscape;
			// Convert from L per Ha to Giga L per cell (...*900/(10000*1000*1000))
			//var val1 = base.file1.sum * 0.09 / 1000000;
			//var val2 = base.file2.sum * 0.09 / 1000000;
			// L per cell to GL per cell
			//var val1 = base.file1.sum / 1000000000;
			//var val2 = base.file2.sum / 1000000000;
			// Convert L to Gallon per year 
			var val1 = base.file1.sum * 0.264172;
			var val2 = base.file2.sum * 0.264172;
			var totalVal = val2 - val1;
			c.getComponent('result_ethanol').setData(val1, val2, totalVal, base);
		}
		
    	if (obj.pest) {
    		var base = obj.pest.selection;
//			var base = obj.pest.landscape;
//			Calculate average index using entire landscape
			var val1 = base.file1.sum / base.file1.count;
			var val2 = base.file2.sum / base.file2.count;
			var totalVal = val2 - val1;
			c.getComponent('result_pest').setData(val1, val2, totalVal, base);
		}	
    	
    	if (obj.pollinator) {
    		var base = obj.pollinator.selection; 
//			var base = obj.pollinator.landscape;
    		var max = base.range.max;
//			Calculate average index using entire landscape
			var val1 = base.file1.sum / (base.file1.count * max);
			var val2 = base.file2.sum / (base.file2.count * max);
			var totalVal = val2 - val1;
			c.getComponent('result_pollinators').setData(val1, val2, totalVal, base);
		}	

    	if (obj.nitrous_oxide) {
    		var base = obj.nitrous_oxide.selection;
			//var base = obj.nitrous_oxide.landscape;
			// Convert from Kg per Ha to Mg per cell (...*900/(10000*1000))
			//var val1 = base.file1.sum * 0.09 / 1000;
			//var val2 = base.file2.sum * 0.09 / 1000;
			// Mg
			//var val1 = base.file1.sum;
			//var val2 = base.file2.sum;
			// Convert Mg to short tons per year
			var val1 = base.file1.sum * 1.102;
			var val2 = base.file2.sum * 1.102;
			// Convert Mega per cell to Short Tons per cell
			//val1 = val1 * 1.1023;
			//val2 = val2 * 1.1023;
			var totalVal = val2 - val1;
			c.getComponent('result_nitrous_oxide').setData(val1, val2, totalVal, base);
		}	
    }

});
