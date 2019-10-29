
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_GraphPopUp', {
    extend: 'Ext.window.Window',

    height: 320,
    width: 400,
    title: 'My Window',
	icon: 'app/images/graph_icon.png',
    layout: 'fit',
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    maximizable: true,

    initComponent: function() {
        var me = this;

		Ext.define('Habitat_Index', {
			extend: 'Ext.data.Model',
			fields: ['Baseline', 'Scenario', 'Delta', 'Bin']
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
					fields: ['Baseline', 'Scenario']
				}/*,{
					title: 'km\xb2', // square kilometers
					type: 'Numeric',
					position: 'right',
					fields: ['Delta']
				}*/,
				{
					title: 'Value',
					type: 'Numeric',
					position: 'bottom',
					fields: ['Bin']
				}],
				series: [{
					type: 'line',
					xField: 'Bin',
					yField: 'Baseline',
					smooth: 3,
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							var areaUnits = ' km\xb2'; // km2
							var freq = 'Area: ' + store.get('Baseline').toFixed(2) + areaUnits;
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
				}/*,{
					type: 'line',
					xField: 'Bin',
					yField: 'Delta',
					smooth: 3,
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							var areaUnits = ' km\xb2'; // km2
							var freq = 'Area: ' + store.get('Delta').toFixed(2) + areaUnits;
							var bin = 'Value: ' + store.get('Bin').toFixed(3);

							this.setTitle(freq + '<br />' + bin);
						}
					}
				}*/]
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
			var cr = data1[i] * 900 / 1000000;
			var sc = data2[i] * 900 / 1000000;
			
			array.push({ 	Baseline: cr, 
							Scenario: sc,
							Delta: sc - cr,
							Bin: (max-min)/(data1.length) * i + min });
		}
		
		this.graphstore.loadData(array);
    }

});


