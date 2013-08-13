Ext.define('MyApp.view.GraphPopUp', {
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
    
    SetChartData: function(objD, objT)
    {
		var data1 = objD.Result;
		var data2 = objT.Result;
		var Min = objD.Min;
		var Max = objD.Max;
		
		var chart = this.getComponent("MyGraph");
		chart.axes.items[1].maximum = Max;
		chart.axes.items[1].minimum = Min;
		
		var array = [];
		for (var i = 0; i < data1.length; i++)
		{
			array.push({ Freq_Default: data1[i]*900/1000000, Freq_Transform: data2[i]*900/1000000, Bin: (Max-Min)/(data1.length) * i + Min });
		}
		
		this.graphstore.loadData(array);
    }

});


