
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Indexed', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_indexed',

    requires: [
        'MyApp.view.LegendTitle',
        'MyApp.view.LegendElement'
    ],

    width: 400,
    bodyPadding: 10,
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'legendtitle',
				x: 70,
				y: 10
			},
			{
				xtype: 'label',
				x: 0,
				y: 37,
				html: '<p style="text-align:right">Legend</p>',
				width: 60
			},			
			{
				xtype: 'container',
				itemId: 'legendcontainer',
				x: 70,
				y: 31,
				style: {
					border: '1px solid #c0c0c0'
				},
				width: 220,
				layout: {
//					align: 'stretch',
					type: 'vbox'
				}
			},
			{
				xtype: 'button',
				x: 300,
				y: 35,
				text: 'Set Selection',
				handler: function() {
					this.up().setSelection();
				}
			}]
        });

        me.callParent(arguments);
        
        var cont = me.getComponent('legendcontainer');
        for (var i = 0; i < me.DSS_LegendElements.length; i++) {
        	// add index for every other colouring
        	me.DSS_LegendElements[i].DSS_LegendElementIndex = i-1;
        	var element = Ext.create('MyApp.view.LegendElement', 
        		me.DSS_LegendElements[i]);
        	cont.insert(i, element);
        }
    },

    //--------------------------------------------------------------------------
    setSelection: function() {
    	
    	var requestData = {
    		clientID: 12345, //temp
    		queryLayers: []
    	};
		
		var queryLayer = { 
			name: this.DSS_QueryTable,
			type: 'indexed',
			matchValues: []
		};
		
        var cont = this.getComponent('legendcontainer');
        for (var i = 0; i < cont.items.length; i++) {
        	var item = cont.items.items[i];
        	if (item.elementIsChecked()) {
        		var queryIdx = item.getElementQueryIndex();
        		queryLayer.matchValues.push(queryIdx);
        	}
        }
        requestData.queryLayers.push(queryLayer);
        console.log(requestData);
        this.submitQuery(requestData);
    }
});
