
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
			text: 'Restore Defaults',
			handler: function(self) {
				var win = self.up(). 	// go up to toolbar level (from the button level)
								up();	// go up to the window level that the toolbar is in

				// MAKE a COPY vs just setting the pointers, which does nothing to make a copy
				//	like we really need...
				Ext.suspendLayouts();
				DSS_AssumptionsAdjustable = JSON.parse(JSON.stringify(DSS_AssumptionsDefaults));
				win.getComponent('DSS_AssumptionCategories').removeAll(true); // destroy everything in it...
				win.populateAssumptions(DSS_AssumptionsAdjustable.Assumptions);
				Ext.resumeLayouts(true);
			}
		},
		{
			xtype: 'tbspacer', 
			width: 7
		},
		{
			xtype: 'button',
			icon: 'app/images/save_icon.png',
			scale: 'medium',
			text: 'Save & Exit',
			handler: function(self) {
				var me = self.up(). 	// go up to toolbar level (from the button level)
								up();	// go up to the window level that the toolbar is in
 
				me.scrapeValues(DSS_AssumptionsAdjustable.Assumptions);
				me.doClose(); 
			}
		},
		{
			xtype: 'button',
			icon: 'app/images/go_icon.png',
			scale: 'medium',
			text: 'Exit',
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
        
        if (DSS_AssumptionsAdjustable && DSS_AssumptionsAdjustable.Assumptions) {
        	Ext.suspendLayouts();
        	this.populateAssumptions(DSS_AssumptionsAdjustable.Assumptions);
        	Ext.resumeLayouts(true);
       }
    },
    
    // Each array element should have these fields....
	//	node.put("Category", category);
	//	node.put("Icon", icon);
	//	node.put("VariableName", variableName);
	//	node.put("DisplayName", displayName);
	//	node.put("DefaultValue", defaultValue);
    //--------------------------------------------------------------------------
    populateAssumptions: function(assumptionsObject) {
    	
		// first find unique categories....
        var categories = {};
		for (var key in assumptionsObject) {
			categories[assumptionsObject[key].Category] = assumptionsObject[key].Icon; 
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
		for (var key in assumptionsObject) {
        	categoryPanels[assumptionsObject[key].Category].addAssumptionElement(key, assumptionsObject[key]);
        }
    },
    
    // Recursively travel through the objects to get all the values back off the controls...
    //--------------------------------------------------------------------------
    scrapeValues: function(assumptionsObject) {
    	
    	var cats = this.getComponent('DSS_AssumptionCategories');
		for (var idx = 0; idx < cats.items.getCount(); idx++) {
			var comp = cats.items.getAt(idx);

			// check to see if it's safe to call this as a function...			
			if (typeof(comp.applyValue) !== 'undefined' && typeof(comp.applyValue) === 'function') {
				comp.applyValue(assumptionsObject);
			}
		}
    }

});

