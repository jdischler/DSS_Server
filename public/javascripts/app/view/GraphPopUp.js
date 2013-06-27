Ext.define('MyApp.view.GraphPopUp', {
    extend: 'Ext.window.Window',

    height: 250,
    width: 400,
    title: 'My Window',
    layout: 'fit',

    initComponent: function() {
        var me = this;

        	Ext.define('Habitat_Index', {
	    extend: 'Ext.data.Model',
	    fields: ['Frequency', 'Bin']
	});
	
        this.graphstore = Ext.create('Ext.data.Store', {
	    model: 'Habitat_Index',
	    data: [
	    ]
	});
                    
        Ext.applyIf(me, {
            items: [
                {
                    xtype: 'chart',
                    //height: 250,
                    //width: 400,
                    animate: true,
                    store: this.graphstore,
                    insetPadding: 20,
                    axes: [
                        {
                            title: 'Frequency',
                            type: 'Numeric',
                            position: 'left',
                            fields: ['Frequency']
                        },
                        {
			    title: 'Bin',
			    type: 'Numeric',
			    position: 'bottom',
			    fields: ['Bin']
                        }
                    ],
                    series: [
                        {
			    type: 'line',
			    xField: 'Bin',
			    yField: 'Frequency',
                            smooth: 3
                        }
                    ]
                }
            ]
        });

        me.callParent(arguments);
    },
    
    SetChartData: function(obj)
    {

    	var data1 = obj.Default;
    	//var data2 = obj.Habitat_Index.Transform;
    	var Min = obj.Min;
    	var Max = obj.Max;
    	
	var array = [];
	for (var i = 0; i < data1.length; i++){
		array.push({ Frequency: data1[i], Bin: (Max-Min)/(data1.length) * i + Min });
		//array.push({ Frequency: data2[i], Bin: (Max-Min)/(data.length) * i + Min });
	}
	
	this.graphstore.loadData(array);
    }

});


