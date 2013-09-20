Ext.define('MyApp.view.Report_SpiderGraph', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.spiderpanel',

    height: 500,
    width: 500,
    title: 'Quick Summary',
	icon: 'app/images/graph_icon.png',
    layout: 'fit',
    id: 'DSS_SpiderGraphPanel',

    initComponent: function() {
        var me = this;

		Ext.define('Habitat_Index', {
			extend: 'Ext.data.Model',
			fields: ['Default', 'Transform', 'Bin']
		});
	
        this.graphstore = Ext.create('Ext.data.Store', {
			model: 'Habitat_Index',
			//data: [{Freq_Default: 1, Freq_Transform: 4, Bin: "Sunday"}, {Freq_Default: 5, Freq_Transform: 8, Bin: "Sat"}, {Freq_Default: 0, Freq_Transform: 9, Bin: "Mon"}],
			data: [{Bin: "Bird Index"}, {Bin: "Nitrogen"}, {Bin: "Phosphorus"}, 
					{Bin: "Biocontrol Index"}, {Bin: "Pollinator Index"}, {Bin: "Fuel"}, 
					{Bin: "Net Income"}, {Bin: "Net Energy"}, {Bin: "Soil Carbon"} , {Bin: "Nitrous Oxide"}]
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
				axes: [{
					title: '', // square kilometers
					type: 'Radial',
					position: 'radial',
					label: {
						display: true
					},
					fields: ['Default', 'Transform']
				},
				{
					title: '',
					type: 'Radial',
					position: 'radial',
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
					fill: 'none'
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
					fill: 'none'
					}
				}]
			}]
        });

        me.callParent(arguments);
    },
    
    setSpiderData: function(objD, objT)
    {
		var data1 = objD;
		var data2 = objT;
		console.log(data1);
		console.log(data2);
		var Bin1 = ["Bird Index", "Nitrogen", "Phosphorus", 
					"Biocontrol Index", "Pollinator Index", "Fuel", 
					"Net Income", "Net Energy", "Soil Carbon", "Nitrous Oxide"];
		var chart = this.getComponent("MyGraph_Spider");
    	
		var array = [];
		for (var i = 0; i < data1.length; i++)
		{
			array.push({ Default: data1[i], Transform: data2[i], Bin: Bin1[i]});
		}
		
		this.graphstore.loadData(array);
    }

});


