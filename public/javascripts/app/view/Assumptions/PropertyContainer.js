
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Assumptions.PropertyContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.propertycontainer',

    requires: [
        'MyApp.view.Assumptions.PropertyElement'
    ],

    layout: {
        type: 'vbox'
    },

	//--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        	items: [{}]        
		});

        me.callParent(arguments);
    },
    
    // {"Category","VariableName","DisplayName","DefaultValue"}
	//--------------------------------------------------------------------------
    addAssumptionElement: function(key, definition) {
    	
    	var element = Ext.create('MyApp.view.Assumptions.PropertyElement',
    		{DSS_variableKey: key, DSS_elementDefinition: definition});
    	this.add(element);
    },
    
	//--------------------------------------------------------------------------
    applyValue: function(assumptionsObject) {
    	
		for (var idx = 0; idx < this.items.getCount(); idx++) {
			var comp = this.items.getAt(idx);
			
			// check if safe to call this function....
			if (typeof(comp.applyValue) !== 'undefined' && typeof(comp.applyValue) === 'function') {
				comp.applyValue(assumptionsObject);
			}
		}
    }

});

