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
			fields: ['Freq_Default', 'Freq_Transform', 'Bin']
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
					fields: ['Freq_Default', 'Freq_Transform']
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
					yField: 'Freq_Default',
					smooth: 3,
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							var areaUnits = ' km\xb2'; // km2
							var freq = 'Area: ' + store.get('Freq_Default').toFixed(2) + areaUnits;
							var bin = 'Value: ' + store.get('Bin').toFixed(3);

							this.setTitle(freq + '<br />' + bin);
						}
					}
				},
				{
					type: 'line',
					xField: 'Bin',
					yField: 'Freq_Transform',
					smooth: 3,
					tips: {
						trackMouse: true,
						width: 120,
						height: 40,
						renderer: function(store, item) {
							var areaUnits = ' km\xb2'; // km2
							var freq = 'Area: ' + store.get('Freq_Transform').toFixed(2) + areaUnits;
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
		var data1 = data.file1.histogram;
		var data2 = data.file2.histogram;
		var min = data.min;
		var max = data.max;
		
		var chart = this.getComponent("MyGraph");
		chart.axes.items[1].maximum = max;
		chart.axes.items[1].minimum = min;
		
		var array = [];
		for (var i = 0; i < data1.length; i++)
		{
			array.push({ 	Freq_Default: data1[i]*900/1000000, 
							Freq_Transform: data2[i]*900/1000000, 
							Bin: (max-min)/(data1.length) * i + min });
		}
		
		this.graphstore.loadData(array);
    }

});


