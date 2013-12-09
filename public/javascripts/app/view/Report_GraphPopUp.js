Ext.define('MyApp.view.Report_GraphPopUp', {
    extend: 'Ext.window.Window',

    height: 320,
    width: 400,
    title: 'My Window',
	icon: 'app/images/graph_icon.png',
    layout: 'fit',

    initComponent: function() {
        var me = this;

		Ext.define('Habitat_Index', {
			extend: 'Ext.data.Model',
			fields: ['Current', 'Scenario', 'Bin']
		});
	
        this.graphstore = Ext.create('Ext.data.Store', {
			model: 'Habitat_Index',
			data: []
		});
                    
        Ext.applyIf(me, {
            items: [{
				xtype: 'chart',
				itemId: 'MyGraph',
				//height: 250,
				//width: 400,
				//animate: true,
				store: this.graphstore,
				insetPadding: 20,
				legend: {
					position: 'top'
			    },
				axes: [{
					title: 'km\xb2', // square kilometers
					type: 'Numeric',
					position: 'left',
					fields: ['Current', 'Scenario']
				},
				{
					title: 'Value',
					type: 'Numeric',
					position: 'bottom',
					fields: ['Bin']
				}],
				series: [{
					type: 'line',
					xField: 'Bin',
					yField: 'Current',
					smooth: 3,
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							var areaUnits = ' km\xb2'; // km2
							var freq = 'Area: ' + store.get('Current').toFixed(2) + areaUnits;
							var bin = 'Value: ' + store.get('Bin').toFixed(3);

							this.setTitle(freq + '<br />' + bin);
						}
					}
				},
				{
					type: 'line',
					xField: 'Bin',
					yField: 'Scenario',
					smooth: 3,
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							var areaUnits = ' km\xb2'; // km2
							var freq = 'Area: ' + store.get('Scenario').toFixed(2) + areaUnits;
							var bin = 'Value: ' + store.get('Bin').toFixed(3);

							this.setTitle(freq + '<br />' + bin);
						}
					}
				}]
			}]
        });

        me.callParent(arguments);
    },
    
    SetChartData: function(data)
    {
		var data1 = data.file1.graph;
		var data2 = data.file2.graph;
		var min = data.range.min;
		var max = data.range.max;
		
		var chart = this.getComponent("MyGraph");
		chart.axes.items[1].maximum = max;
		chart.axes.items[1].minimum = min;
		
		var array = [];
		for (var i = 0; i < data1.length; i++)
		{
			array.push({ 	Current: data1[i] * 900 / 1000000, 
							Scenario: data2[i] * 900 / 1000000, 
							Bin: (max-min)/(data1.length) * i + min });
		}
		
		this.graphstore.loadData(array);
    }

});


