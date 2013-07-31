Ext.define('MyApp.view.GraphSpider', {
    extend: 'Ext.window.Window',

    height: 500,
    width: 500,
    title: 'My Window',
    layout: 'fit',

    initComponent: function() {
        var me = this;

		Ext.define('Habitat_Index', {
			extend: 'Ext.data.Model',
			fields: ['Freq_Default', 'Freq_Transform', 'Bin']
		});
	
        this.graphstore = Ext.create('Ext.data.Store', {
			model: 'Habitat_Index',
			//data: [{Freq_Default: 1, Freq_Transform: 4, Bin: "Sunday"}, {Freq_Default: 5, Freq_Transform: 8, Bin: "Sat"}, {Freq_Default: 0, Freq_Transform: 9, Bin: "Mon"}],
			data: []
		});
                    
        Ext.applyIf(me, {
            items: [{
				xtype: 'chart',
				itemId: 'MyGraph_Spider',
				//height: 250,
				//width: 400,
				//animate: true,
				store: this.graphstore,
				insetPadding: 20,
				legend: {
					position: 'top'
			    },
				axes: [{
					title: '', // square kilometers
					type: 'Radial',
					position: 'radial',
					label: {
						display: true
					},
					fields: ['Freq_Default', 'Freq_Transform']
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
					yField: 'Freq_Default',
					showInLegend: true,
					showMarkers: true,
					markerConfig: {
						radius: 5,
						size: 5
					},
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							this.setTitle(store.get('Bin') + ': ' + store.get('Freq_Default'));
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
					yField: 'Freq_Transform',
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
							this.setTitle(store.get('Bin') + ': ' + store.get('Freq_Transform'));
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
    
    SetSpiderData: function(objD, objT)
    {
		var data1 = objD;
		var data2 = objT;
		console.log(data1);
		console.log(data2);
		var Bin1 = ["Habitat Index", "Nitrogen", "Phosphorus", "Crop Pest", "Pollinator", "Biomass"];
		var chart = this.getComponent("MyGraph_Spider");
    	
		var array = [];
		for (var i = 0; i < data1.length; i++)
		{
			array.push({ Freq_Default: data1[i], Freq_Transform: data2[i], Bin: Bin1[i]});
		}
		
		this.graphstore.loadData(array);
    }

});


