
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderGraph', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_spider',

    height: 500,
    width: 500,
    title: 'Quick Summary',
	icon: 'app/images/graph_icon.png',
    layout: 'fit',
    id: 'DSS_SpiderGraphPanel',

    //------------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

		Ext.define('Spider_Model', {
			extend: 'Ext.data.Model',
			fields: ['Default', 'Transform', 'Bin', 'Match']
		});
	
        this.graphstore = Ext.create('Ext.data.Store', {
			model: 'Spider_Model',
			data: [{Bin: 'Bird Index', Match: 'habitat_index'}, 
					{Bin: 'Nitrogen', Match: 'nitrogen'}, 
					{Bin: 'Phosphorus', Match: 'phosphorus'}, 
					{Bin: 'Biocontrol Index', Match: 'pest'}, 
					{Bin: 'Pollinator Index', Match: 'pollinator'}, 
					{Bin: 'Fuel', Match: 'ethanol'}, 
					{Bin: 'Net Income', Match: 'net_income'}, 
					{Bin: 'Net Energy', Match: 'net_energy'}, 
					{Bin: 'Soil Carbon', Match: 'soc'}, 
					{Bin: 'Nitrous Oxide', Match: 'nitrous_oxide'}]
		});
                    
        Ext.applyIf(me, {
            items: [{
				xtype: 'chart',
				itemId: 'MyGraph_Spider',
				//height: 250,
				//width: 400,
				animate: true,
				store: this.graphstore,
				insetPadding: 60,
				legend: {
					position: 'float',
					x: -55,
					y: -55
			    },
				axes: [
				{
					title: '',
					type: 'Radial',
					position: 'radial',
					maximum: 1,
					label: {
						display: true
					},
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
							this.setTitle(store.get('Bin') + ': ' + store.get('Default'));
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
							this.setTitle(store.get('Bin') + ': ' + store.get('Transform'));
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
			rec.set("Default", value1 / max);
			rec.set("Transform", value2 / max);
			rec.commit();
    	}
    },
    
    //--------------------------------------------------------------------------
    clearSpiderData: function(defaultValue)
    {
		for (var idx = 0; idx < this.graphstore.count(); idx++)
		{
			var rec = this.graphstore.getAt(idx);
			rec.set("Default", defaultValue);
			rec.set("Transform", defaultValue);
			rec.commit();
		}
    }

});

