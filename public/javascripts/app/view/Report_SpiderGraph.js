
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
			fields: ['Current', 'Scenario', 'Bin', 'Match']
		});
	
        this.graphstore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [ 
					{Bin: 'Nitrogen', Match: 'nitrogen'}, 
					{Bin: 'Phosphorus', Match: 'phosphorus'}, 
					{Bin: 'Bird Index', Match: 'habitat_index'},
					{Bin: 'Biocontrol Index', Match: 'pest'}, 
					{Bin: 'Pollinator Index', Match: 'pollinator'}, 
					{Bin: 'Fuel', Match: 'ethanol'}, 
					{Bin: 'Net Income', Match: 'net_income'}, 
					{Bin: 'Net Energy', Match: 'net_energy'}, 
					{Bin: 'Soil Carbon', Match: 'soc'}, 
					{Bin: 'Nitrous Oxide', Match: 'nitrous_oxide'}]
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
				store: this.graphstore,
				insetPadding: 62,
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
					maximum: 1,
					fields: ['Bin']
				}],
				series: [{
					type: 'radar',
					xField: 'Bin',
					yField: 'Current',
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
					yField: 'Scenario',
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

    	var rec = this.graphstore.findRecord('Match', element);
    	if (rec) {
    		var max = value1;
    		if (value2 > max) {
    			max = value2;
    		}
//			rec.set("Default", value1 / max);
//			rec.set("Transform", value2 / max);
			// FIXME: reversed because we don't know why the data is reversed...blah
			rec.set("Current", value2 / max);
			rec.set("Scenario", value1 / max);
			rec.commit();
    	}
    },
    
    //--------------------------------------------------------------------------
    clearSpiderData: function(defaultValue)
    {
		for (var idx = 0; idx < this.graphstore.count(); idx++)
		{
			var rec = this.graphstore.getAt(idx);
			rec.set("Current", defaultValue);
			rec.set("Scenario", defaultValue);
			rec.commit();
		}
    }

});

