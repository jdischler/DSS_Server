
//------------------------------------------------------------------------------
var DSS_TransformTypes = Ext.create('Ext.data.Store', {
		
	fields: ['index', 'name', 'controls'],
	data: [
		{ 'index': 1, 'name': 'Corn', 	'controls': ['Tillage','Fertilizer'] },
		{ 'index': 16, 'name': 'Soy', 	'controls': ['Tillage','Fertilizer'] },
		{ 'index': 17, 'name': 'Alfalfa', 'controls': ['Tillage','Fertilizer'] },
//		{ 'index': 3, 'name': 'Veggies', 			'controls': ['Tillage','Fertilizer'] },
		{ 'index': 6, 'name': 'Grass', 	'controls': ['Fertilizer'] },
		{ 'index': 10, 'name': 'Suburban' }
//		{ 'index': 7,'name': 'Woodland' }
	]
});

//------------------------------------------------------------------------------
Ext.define('MyApp.view.TransformPopup', {
    extend: 'Ext.window.Window',

    requires: [
    	'MyApp.view.Management_Tillage',
    	'MyApp.view.Management_Fertilizer',
    ],
    
    height: 310,
    width: 330,
    layout: {
        type: 'absolute'
    },
	modal: true,
    resizable: false,
    // should use Ok/Apply or cancel buttons...	OR...fix the bugs with closing via [X] or Esc
    closable: false,
	icon: 'app/images/layers_icon.png',
    title: 'Transform and Management Options',

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'label',
				x: 10,
				y: 10,
				style: 'font-size: 120%',
				text: 'New Landcover'
			},
			{
				xtype: 'label',
				x: 10,
				y: 80,
				style: 'font-size: 120%',
				text: 'Management Options'
			},
			{
				xtype: 'container',
				x: 10,
				y: 10,
				height: 20,
				style: 'border-width: 3px; border-style: none none groove none; border-color: #cef;',
				width: 170
			},
			{
				xtype: 'container',
				x: 10,
				y: 80,
				height: 20,
				style: 'border-width: 3px; border-style: none none groove none; border-color: #cef;',
				width: 170
			},
			{
				xtype: 'combobox',
				itemId: 'DSS_transformTypes',
				x: 20,
				y: 40,
				width: 165,
				fieldLabel: 'Type',
				labelAlign: 'right',
				labelWidth: 30,
				labelPad: 5,
				displayField: 'name',
				forceSelection: true,
				store: DSS_TransformTypes,
				valueField: 'index',
				value: 1, // continuous corn?
				listeners: {
					select: {
						fn: function(combo, record, eOpts) {
							
							me.displayCorrectManagementOptions();
						}
					}
				}
			},
			{
				xtype: 'container',
				itemId: 'DSS_managementContainer',
				x: 10,
				y: 110,
				height: 130,
//				style: 'background: #fff',
				width: 290
			},
			{
				xtype: 'button',
				x: 180,
				y: 235,
				scale: 'medium',
				text: 'Cancel',	
				handler: function(self) {
					// back up to the window level and call the window close...
					self.up().closeWindow(false); // don't save 
				}
			},
			{
				xtype: 'button',
				x: 240,
				y: 235,
				scale: 'medium',
				text: 'Ok / Apply',						
				handler: function(self) {
					// back up to the window level and call the window close...
					self.up().closeWindow(true); // save 
				}
			}]
        });

        me.callParent(arguments);
        
		var combo = this.getComponent('DSS_transformTypes');
		if (this.DSS_Transform && this.DSS_Transform.Type) {
			combo.setValue(this.DSS_Transform.Type);
		}

        this.displayCorrectManagementOptions();
    },
    
    //--------------------------------------------------------------------------
    closeWindow: function(applyChanges) {
    	
    	if (applyChanges) {
    		if (this.DSS_Transform == null) {
    			this.DSS_Transform = {};
    		}
    		var combo = this.getComponent('DSS_transformTypes');
    		this.DSS_Transform.Type = combo.getValue();
    		this.DSS_Transform.Text = 'To ' + combo.getRawValue();
    		this.DSS_Transform.Management = '<b><i>Management Options:</i></b></br>';
    		
    		var managementOptions = '';
    		var container = this.getComponent('DSS_managementContainer');
    		var len = container.items.length;
    		for (var idx = 0; idx < len; idx++) {
    			var child = container.items.items[idx];
    			managementOptions += child.collectChanges(this.DSS_Transform);
    			if (idx < len - 1) {
    				managementOptions += '</br>';
    			}
    		}
    		if (managementOptions == '') {
    			managementOptions = 'None';
    		}
    		this.DSS_Transform.Management += managementOptions;
//    		console.log(this.DSS_Transform.Management);
    	}
    	else {
    		this.DSS_Transform = null;
    	}
    	this.doClose()
    },
    
    //--------------------------------------------------------------------------
    displayCorrectManagementOptions: function() {
    	    	
		var container = this.getComponent('DSS_managementContainer');
		container.removeAll(true);
		
		var combo = this.getComponent('DSS_transformTypes');
		if (combo.getValue()) {
			var record = combo.findRecord('index', combo.getValue());
			if (record) {
				var controls = record.data.controls;
				if (controls) {
					for (var idx = 0; idx < controls.length; idx++) {
						var newManagement = Ext.create('MyApp.view.Management_' + controls[idx], 
							{DSS_Transform: this.DSS_Transform}); // pass the transform in so they can modify
						if (newManagement) {
							container.add(newManagement);
						}
					}
				}
			}
		}
    }

});

