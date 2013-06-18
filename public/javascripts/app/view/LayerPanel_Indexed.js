
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Indexed', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_indexed',

    requires: [
        'MyApp.view.LegendElement',
        'MyApp.view.LegendTitle'
    ],

    width: 400,
    bodyPadding: '0 0 3 0', // just really need to pad bottom to maintain spacing there
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	xtype: 'legendtitle',
            	x: 30,
            	y: 3
            },
            {
            	xtype: 'legendtitle',
            	x: 210,
            	y: 3
            },
            {
				xtype: 'container',
				itemId: 'legendcontainer',
				x: 30,
				y: 24,
				style: {
//					border: '1px solid #c0c0c0'
					border: '1px solid #f0f0f0'
				},
				width: 358,
				layout: {
//					align: 'stretch',
//					type: 'vbox'
					type: 'column'
				}
			}/*,
			{
				xtype: 'button',
				itemId: 'selectionbutton',
				iconAlign: 'right',
				x: 300,
				y: 35,
				text: 'Set Selection',
				handler: function() {
					this.up().buildQuery();
				}
			},
			{
				xtype: 'button',
				x: 300,
				y: 60,
				text: 'Clear Checks',
				handler: function() {
					this.up().clearChecks();
				}
			}*/]
        });

        me.callParent(arguments);
        
/*        var cont = me.getComponent('legendcontainer');
        for (var i = 0; i < me.DSS_LegendElements.length; i++) {
        	// add index for every other colouring
        	me.DSS_LegendElements[i].DSS_LegendElementIndex = i-1;
        	var element = Ext.create('MyApp.view.LegendElement_New', 
        		me.DSS_LegendElements[i]);
        	cont.insert(i, element);
        }*/
        this.DSS_RequestTryCount = 0;
        this.requestLayerRange(this);
    },

	//--------------------------------------------------------------------------
    requestLayerRange: function(container) {

		var queryLayerRequest = { 
			name: container.DSS_QueryTable,
			type: 'colorKey',
		};
    	
		var obj = Ext.Ajax.request({
			url: 'http://localhost:9000/layerParmRequest',
			jsonData: queryLayerRequest,
			timeout: 10000,
			scope: container,
			
			success: function(response, opts) {
				
				// Note: old versions of IE may not support Json.parse...
				var obj = JSON.parse(response.responseText);
				
				if (obj.length == 0) {
					console.log("layer request object return was null?");
					return;
				}
				var cont = this.getComponent('legendcontainer');
				for (var i = 0; i < obj.length; i++) {
					// add index for every other colouring
					var element = Ext.create('MyApp.view.LegendElement_New', 
						obj[i]);
					cont.insert(i, element);
				}
			},
			
			failure: function(respose, opts) {
				console.log('layer request failed');
				if (this.DSS_RequestTryCount < 5) {
					console.log('trying again');
					this.DSS_RequestTryCount++;
					this.requestLayerRange(this);
				}
				else {
					console.log('giving up');
				}
			}
		});
	},

    //--------------------------------------------------------------------------
    setSelectionCriteria: function() {
    	
		var queryLayer = { 
			name: this.DSS_QueryTable,
			type: 'indexed',
			matchValues: []
		};
		
		var addedElement = false;
        var cont = this.getComponent('legendcontainer');
        for (var i = 0; i < cont.items.length; i++) {
        	var item = cont.items.items[i];
        	if (item.elementIsChecked()) {
        		addedElement = true;
        		var queryIdx = item.getElementQueryIndex();
        		queryLayer.matchValues.push(queryIdx);
        	}
        }
        
        if (!addedElement) {
        	return;
        }
        return queryLayer;
    }
    
});
