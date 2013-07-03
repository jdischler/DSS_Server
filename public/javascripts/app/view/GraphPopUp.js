Ext.define('MyApp.view.GraphPopUp', {
    extend: 'Ext.window.Window',

    height: 320,
    width: 400,
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
                        legend: {
				position: 'top'
			    },
			    tips:
			    {
			    	    trackMouse: true,
			    	    width: 80,
			    	    height:50,
			    	    renderer: function(store, item)
			    	    {
			    	    	    this.setTitle(store.get('Bin') + '<br />' + store.get('Freq_Default'))
			    	    }
			    },
                    axes: [
                        {
                            title: 'Frequency',
                            type: 'Numeric',
                            position: 'left',
                            fields: ['Freq_Default', 'Freq_Transform']
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
			    yField: 'Freq_Default',
                            smooth: 3
                        },
                        {
			    type: 'line',
			    xField: 'Bin',
			    yField: 'Freq_Transform',
                            smooth: 3
                        }
                    ]
                }
            ]
        });

        me.callParent(arguments);
    },
    
    SetChartData: function(objD, objT)
    {

    	var data1 = objD.Result;
    	var data2 = objT.Result;
    	//var data2 = obj.Habitat_Index.Transform;
    	var Min = objD.Min;
    	var Max = objD.Max;
    	
	var array = [];
	for (var i = 0; i < data1.length; i++)
	{
		array.push({ Freq_Default: data1[i]*900/1000000, Freq_Transform: data2[i]*900/1000000, Bin: (Max-Min)/(data1.length) * i + Min });
	}
	
	this.graphstore.loadData(array);
    }

});


