
//------------------------------------------------------------------------------
var DSS_TransformTypes = Ext.create('Ext.data.Store', {
		
	fields: ['index', 'name', 'controls'],
	data: [
		{ 'index': 1, 'name': 'Corn', 	'controls': ['Fertilizer','Tillage','CoverCrop','Contour','Terraced'] },
		{ 'index': 16, 'name': 'Soy', 	'controls': ['Fertilizer','Tillage','CoverCrop','Contour','Terraced'] },
		{ 'index': 17, 'name': 'Alfalfa', 'controls': ['Fertilizer','Tillage','Contour','Terraced'] },
		{ 'index': 6, 'name': 'Grass', 	'controls': ['Fertilizer','Contour','Terraced'] }
	]
});

//------------------------------------------------------------------------------
Ext.define('MyApp.view.TransformPopup', {
    extend: 'Ext.window.Window',

    requires: [
    	'MyApp.view.Management_Tillage',
    	'MyApp.view.Management_Fertilizer',
    	'MyApp.view.Management_Terraced',
    	'MyApp.view.Management_Contour',
    	'MyApp.view.Management_CoverCrop'
    ],
    
    height: 380,
    width: 330,
    layout: {
        type: 'absolute'
    },
	modal: true,
    resizable: false,
	constrainHeader: true, // keep the header from being dragged out of the app body...otherwise may not be able to close it!
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
				y: 305,
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
				y: 305,
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
		if (this.DSS_TransformIn && this.DSS_TransformIn.LandUse) {
			combo.setValue(this.DSS_TransformIn.LandUse);
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
    		this.DSS_Transform.Config = {LandUse: combo.getValue(), Options: {}};
    		this.DSS_Transform.Text = 'To ' + combo.getRawValue();
    		this.DSS_Transform.Management = '<b><i>Management Options:</i></b></br>';
    		
    		var managementOptionsText = '';
    		var container = this.getComponent('DSS_managementContainer');
    		var len = container.items.length;
    		for (var idx = 0; idx < len; idx++) {
    			var child = container.items.items[idx];
    			var managementOptions = child.collectChanges(this.DSS_Transform.Config.Options);
    			managementOptionsText += managementOptions.text;
    			if (idx < len - 1) {
    				managementOptionsText += '</br>';
    			}
    		}
    		if (managementOptionsText == '') {
    			managementOptionsText = 'None';
    		}
    		this.DSS_Transform.Management += managementOptionsText;
//    		console.log(this.DSS_Transform.Management);
    	}
    	else {
    		this.DSS_Transform = null;
    	}
    	console.log(this.DSS_Transform);
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
							{DSS_Transform: this.DSS_TransformIn}); // pass the transform in so they can modify
						if (newManagement) {
							container.add(newManagement);
						}
					}
				}
			}
		}
    }

});

