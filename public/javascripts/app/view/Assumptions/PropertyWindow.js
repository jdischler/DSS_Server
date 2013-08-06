
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Assumptions.PropertyWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.propertywindow',

    requires: [
        'MyApp.view.Assumptions.PropertyContainer'
    ],

    height: 503,
    width: 300,
    closable: false,
    title: 'Global Assumptions',

	dockedItems: [{
		xtype: 'toolbar',
		dock: 'bottom',
		items: [{
			xtype: 'button',
			icon: 'app/images/new_icon.png',
			scale: 'medium',
			disabled: true,
			text: 'Defaults'
		},
		{
			xtype: 'button',
			icon: 'app/images/save_icon.png',
			scale: 'medium',
			disabled: true,
			text: 'Save'
		},
		{
			xtype: 'button',
			icon: 'app/images/load_icon.png',
			scale: 'medium',
			disabled: true,
			text: 'Load'
		},
		{
			xtype: 'tbspacer', 
			width: 5
		},
		{
			xtype: 'button',
			icon: 'app/images/go_icon.png',
			scale: 'medium',
			text: 'Close',
			handler: function(self) {
				self.up(). 	// go up to toolbar level (from the button level)
					up().	// go up to the window level that the toolbar is in
					doClose(); 
			}
		}]
	}],
 
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	itemId: 'DSS_AssumptionCategories',
				xtype: 'panel',
				autoScroll: true,
				layout: {
					type: 'accordion',
					animate: false,
					multi: true,
					titleCollapse: false
				},
				items: [{
					xtype: 'panel',
					hidden: true
				}]
			}]
        });

        me.callParent(arguments);
        
        if (DSS_AssumptionsDefaults && DSS_AssumptionsDefaults.Assumptions) {
        	Ext.suspendLayouts();
        	this.populateAssumptions(DSS_AssumptionsDefaults.Assumptions);
			Ext.resumeLayouts(true);
        }
    },
    
    //--------------------------------------------------------------------------
    populateAssumptions: function(assumptionsArray) {

/*      // Each array element should have these fields....
		node.put("Category", category);
		node.put("Icon", icon);
		node.put("VariableName", variableName);
		node.put("DisplayName", displayName);
		node.put("DefaultValue", defaultValue);
*/
		// first find unique categories....
        var categories = {};
		for (var idx = 0; idx < assumptionsArray.length; idx++) {
			categories[assumptionsArray[idx].Category] = assumptionsArray[idx].Icon; 
		}
        
        // create panels for each unique category...
        var categoryPanels = {};
        for (var property in categories) {
        	var panel = Ext.create('MyApp.view.Assumptions.PropertyContainer', {
        		title: property,
				icon: 'app/images/' + categories[property]
        	});
        	categoryPanels[property] = panel;
        	this.getComponent('DSS_AssumptionCategories').add(panel);
        }
        
        // Now finally push the variables into the correct panel....
		for (var idx = 0; idx < assumptionsArray.length; idx++) {
        	categoryPanels[assumptionsArray[idx].Category].addAssumptionElement(assumptionsArray[idx]);
        }
    }

});

