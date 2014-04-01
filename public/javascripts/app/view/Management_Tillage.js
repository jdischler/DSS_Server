	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Management_Tillage', {
	extend: 'Ext.container.Container',
	
	height: 35,
	width: 290,
	layout: {
		type: 'absolute'
	},

	disabled: true,

	//--------------------------------------------------------------------------
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'radiogroup',
				itemId: 'DSS_Tillage',
				x: 0,
				y: 0,
				width: 290,
				fieldLabel: 'Tillage',
				labelAlign: 'right',
				labelWidth: 70,
				allowBlank: false,
				items: [{
					xtype: 'radiofield',
					boxLabel: 'Conventional',
					checked: true,
					name: 'Tillage',
					inputValue: 'Conventional'
				},
				{
					xtype: 'radiofield',
					boxLabel: 'No-Till',
					name: 'Tillage',
					inputValue: 'No-Till'
				}]
			}]
		});
		
		me.callParent(arguments);
		
		this.setFromTransform(this.DSS_Transform);
	},
	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {
		
		if (transform && transform.Tillage) {
			var tillageType = this.getComponent('DSS_Tillage');
			tillageType.setValue({'Tillage': transform.Tillage});
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var tillageType = this.getComponent('DSS_Tillage');
		
		transform['Tillage'] = tillageType.getValue()['Tillage'];
		
		return '<b>Tillage:</b> ' + transform['Tillage'];
	}
	
});

