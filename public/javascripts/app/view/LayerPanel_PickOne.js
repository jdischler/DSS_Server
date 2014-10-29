
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_PickOne', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_option',

//    width: 400,
//   bodyPadding: '0 0 5 0', // just really need to pad bottom to maintain spacing there
    
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;

        // Fill in the stuff we take care of for the user....
        for (var i = 0; i < this.DSS_RadioOptions.length; i++) {
        	var option = this.DSS_RadioOptions[i];
        	option['xtype'] = 'radiofield';
        	option['name'] = this.DSS_UniqueRadioGroupName;
        }
        
        Ext.applyIf(me, {
            items: [{            	
            	xtype: 'radiogroup',
				itemId: 'DSS_radioGroup',
				x: this.DSS_AtX,
				y: 6,
				width: 290,
				labelAlign: 'right',
				labelWidth: 70,
				labelPad: 20,
				fieldLabel: this.DSS_Label,
				allowBlank: false,
				columns: this.DSS_RadioColumns,
				border: false,
				items: this.DSS_RadioOptions
			}]
        });

        me.callParent(arguments);
    },

	// Parameters that come from an indexed or discreet set of values, e.g. val in (n1, n2, n3) 
	//	{field: (table column in database as string), 
	//		valueData: (an item value) 
	//	}
    //--------------------------------------------------------------------------
    getSelectionCriteria: function() {
    	
		var queryLayer = { 
			name: this.DSS_QueryTable,
			type: 'indexed',
			matchValues: []
		};
		
		var option = this.getComponent('DSS_radioGroup').getValue()[this.DSS_UniqueRadioGroupName];
		queryLayer.matchValues.push(option);
        return queryLayer;
    },

    //--------------------------------------------------------------------------
    setSelectionCriteria: function(jsonQuery) {
    	
    	if (!jsonQuery || !jsonQuery.queryLayers) {
			this.header.getComponent('DSS_ShouldQuery').toggle(false);
    		return;
    	}
    	
		for (var i = 0; i < jsonQuery.queryLayers.length; i++) {
		
			var queryElement = jsonQuery.queryLayers[i];
			
			// in query?
			if (queryElement && queryElement.name == this.DSS_QueryTable) {
				// yup
				this.show();
				this.header.getComponent('DSS_ShouldQuery').toggle(true);
				
				var option = jsonQuery[i].parameterData.valueData;
				var val = {};
				val[this.DSS_UniqueRadioGroupName] = option;
				this.getComponent('DSS_radioGroup').setValue(val);

				return;
			}
		}
		
		// Nope, mark as not queried
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
		this.hide();
    },
    
    //--------------------------------------------------------------------------    
	resetLayer: function() {
    	
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
    }
    
});

