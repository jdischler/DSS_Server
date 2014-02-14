
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderGraph', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_spider',

	requires: [
		'MyApp.view.Report_SpiderHeader'
	],
	
    height: 450,
    width: 500,
    title: 'Quick Summary',
	icon: 'app/images/fast_icon.png',
    layout: 'absolute',
    id: 'DSS_SpiderGraphPanel',

    //------------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

		Ext.define('Spider_Model', {
			extend: 'Ext.data.Model',
			fields: ['Default', 'Transform', 'Bin', 'Match', 'IntermDefault', 'IntermTransform']
		});
	
        this.graphdetailstore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [ 
					{Bin: 'Phosphorus EPIC', Match: 'P_Loss_EPIC'}, 
					{Bin: 'Phosphorus', Match: 'water_quality'}, 
					{Bin: 'Bird Index', Match: 'habitat_index'},
					{Bin: 'Biocontrol Index', Match: 'pest'}, 
					{Bin: 'Pollinator Index', Match: 'pollinator'}, 
					{Bin: 'Fuel', Match: 'ethanol'}, 
					{Bin: 'Net Income', Match: 'net_income'}, 
					{Bin: 'Net Energy', Match: 'net_energy'}, 
					{Bin: 'Soil Carbon', Match: 'soc'}, 
					{Bin: 'Nitrous Oxide', Match: 'nitrous_oxide'}]
		});
        this.graphshortstore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [ 
					{Bin: 'Surface Water', Match: 'surface_water'}, 
					{Bin: 'Emissions', Match: 'emissions'}, 
					{Bin: 'Ecosystem', Match: 'ecosystem'},
					{Bin: 'Economic', Match: 'economic'}]
		});
                    
        Ext.applyIf(me, {
            items: [/*{
            	xtype: 'report_spider_header',
            	x: 0,
            	y: 0
            	
            },*/{
				xtype: 'chart',
				itemId: 'MyGraph_Spider',
				x: -4,
				y: -40,
				height: 500,
				width: 500,
				animate: true,
//				store: this.graphdetailstore,
				store: this.graphshortstore,
				insetPadding: 60,
				legend: {
					position: 'float',
					x: -53,
					y: -18
			    },
				axes: [
				{
					title: '',
					type: 'Radial',
					position: 'radial',
					maximum: 100,
					fields: ['Bin']
				}],
				series: [{
					type: 'radar',
					xField: 'Bin',
					yField: 'Default',
					showInLegend: true,
					showMarkers: true,
					markerConfig: {
						radius: 3,
						size: 3
					},
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							this.setTitle(store.get('Bin') + ': ' + store.get('Current'));
						}
					},
					style: {
						'stroke-width': 2,
						'fill-opacity': 0.1,
						'stroke-opacity': 1
					}
				},
				{
					type: 'radar',
					xField: 'Bin',
					yField: 'Transform',
					showInLegend: true,
					showMarkers: true,
					markerConfig: {
						radius: 3,
						size: 3
					},
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							this.setTitle(store.get('Bin') + ': ' + store.get('Scenario'));
						}
					},
					style: {
						'stroke-width': 2,
						'fill-opacity': 0.1,
						'stroke-opacity': 1
					}
				}]
			}]
        });

        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
    setSpiderDataElement: function(value1, value2, element) {

    	// Fill in detailed spider data
    	var rec = this.graphdetailstore.findRecord('Match', element);
		var max = value1;
		if (value2 > max) {
			max = value2;
		}
		var result1 = value2 / max * 100;
		var result2 = value1 / max * 100;
    	if (rec) {
//			rec.set("Default", value1 / max);
//			rec.set("Transform", value2 / max);
			// FIXME: reversed because we don't know why the data is reversed...blah
			rec.set("Default", result1);
			rec.set("Transform", result2);
			rec.commit();
    	}
    	
    	// calculate combined spider data - have to figure out which things go to which
    	var newmatch='';
    	var divisor = 1;
		if (element =='P_Loss_EPIC') {
			newmatch = 'surface_water';
			divisor = 2;
		}
		else if (element =='water_quality') {
			newmatch = 'surface_water';
			divisor = 2;
		}
		else if (element =='habitat_index') {
			newmatch = 'ecosystem';
			divisor = 3;
		}
		else if (element =='pest') {
			newmatch = 'ecosystem';
			divisor = 3;
		}
		else if (element =='pollinator') {
			newmatch = 'ecosystem';
			divisor = 3;
		}
		else if (element =='ethanol') {
			newmatch = 'economic';
			divisor = 3;
		}
		else if (element =='net_income') {
			newmatch = 'economic';
			divisor = 3;
		}
		else if (element =='net_energy') {
			newmatch = 'economic';
			divisor = 3;
		}
		else if (element =='soc') {
			newmatch = 'emissions';
			divisor = 2;
		}
		else if (element =='nitrous_oxide') {
			newmatch = 'emissions';
			divisor = 2;
		}
		
		var rec = this.graphshortstore.findRecord('Match', newmatch);
		if (rec) {
//			rec.set("Default", value1 / max);
//			rec.set("Transform", value2 / max);
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
		for (var idx = 0; idx < this.graphdetailstore.count(); idx++)
		{
			var rec = this.graphdetailstore.getAt(idx);
			rec.set("Default", defaultValue);
			rec.set("Transform", defaultValue);
			rec.commit();
		}
		for (var idx = 0; idx < this.graphshortstore.count(); idx++)
		{
			var rec = this.graphshortstore.getAt(idx);
			rec.set("Default", defaultValue);
			rec.set("Transform", defaultValue);
			rec.set('IntermDefault', 0);
			rec.set('IntermTransform', 0);
			rec.commit();
		}
    }

});

