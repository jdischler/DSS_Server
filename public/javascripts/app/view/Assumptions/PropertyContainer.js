
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
    addAssumptionElement: function(definition) {
    	
    	var element = Ext.create('MyApp.view.Assumptions.PropertyElement',
    		{DSS_elementDefinition: definition});
    	this.add(element);
    }

});

