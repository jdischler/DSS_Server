
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Assumptions.PropertyWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.propertywindow',

    requires: [
        'MyApp.view.Assumptions.PropertyContainer'
    ],

    height: 503,
    minHeight: 400,
    width: 300,
    closable: false,
    title: 'Global Assumptions',
    constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
    modal: true,

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
    	
        // create panels for each unique category...
        for (var categoryName in assumptionsObject) {
        	var category = assumptionsObject[categoryName];
        	var panel = Ext.create('MyApp.view.Assumptions.PropertyContainer', {
        		title: category.CategoryName,
				icon: 'app/images/' + category.CategoryIcon
        	});
        	this.getComponent('DSS_AssumptionCategories').add(panel);
        	
			// Now push the variables into the correct panel....
			for (var propertyName in category) {
				var property = category[propertyName];
				if (typeof property === "object") {
					panel.addAssumptionElement(propertyName, property);
				}
			}
        }
        
    },
    
    // Recursively travel through the objects to get all the values back off the controls...
    //--------------------------------------------------------------------------
    scrapeValues: function(assumptionsObject) {
    	
    	var results = [];
    	var cats = this.getComponent('DSS_AssumptionCategories');
		for (var idx = 0; idx < cats.items.getCount(); idx++) {
			var comp = cats.items.getAt(idx);

			// check to see if it's safe to call this as a function...			
			if (typeof(comp.getValues) !== 'undefined' && typeof(comp.getValues) === 'function') {
				var result = comp.getValues();
				results = results.concat(result);
			}
		}
		
		// Not terribly efficient...update any variable instances found in the array to 
		//	the source object...
		for (var i = 0; i < results.length; i++) {
			var propertyToFind = results[i].key;
			
			for (var categoryName in assumptionsObject) {
				var category = assumptionsObject[categoryName];
				for (var propertyName in category) {
					if (propertyName == propertyToFind) {
						category[propertyName].DefaultValue = results[i].value;
					}
				}
			}
		}
    }

});

