	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Management_Fertilizer', {
	extend: 'Ext.container.Container',
	
	height: 80,
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
				itemId: 'DSS_FertAmount',
				x: 0,
				y: 0,
				width: 290,
				fieldLabel: 'Fertilizer',
				labelAlign: 'right',
				labelWidth: 70,
				allowBlank: false,
				columns: 2,
				items: [{
					xtype: 'radiofield',
					boxLabel: 'None',
					checked: true,
					name: 'Amount',
					inputValue: 'None'
				},
				{
					xtype: 'radiofield',
					boxLabel: 'Low',
					name: 'Amount',
					inputValue: 'Low'
				},
				{
					xtype: 'radiofield',
					boxLabel: 'High',
					name: 'Amount',
					inputValue: 'High'
				},
				{
					xtype: 'radiofield',
					disabled: true,
					boxLabel: 'Custom',
					name: 'Amount',
					inputValue: 'Custom'
				}]
			},
			{
				xtype: 'radiogroup',
				itemId: 'DSS_FertType',
				x: 0,
				y: 50,
				width: 290,
				fieldLabel: 'Type',
				labelAlign: 'right',
				labelWidth: 70,
				allowBlank: false,
				columns: 2,
				items: [{
					xtype: 'radiofield',
					boxLabel: 'Manure',
					checked: true,
					name: 'Type',
					inputValue: 'Manure'
				},
				{
					xtype: 'radiofield',
					boxLabel: 'Synthetic',
					name: 'Type',
					inputValue: 'Synthetic'
				}]
			}]
		});
		
		me.callParent(arguments);
		
		this.setFromTransform(this.DSS_Transform);
	},
	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {
		
		if (transform && transform.Fertilizer) {
			var fertAmount = this.getComponent('DSS_FertAmount');
			fertAmount.setValue({'Amount': transform.Fertilizer.Amount});
			
			var fertType = this.getComponent('DSS_FertType');
			fertType.setValue({'Type': transform.Fertilizer.Type});
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var obj = {};
		
		var fertAmount = this.getComponent('DSS_FertAmount');
		obj['Amount'] = fertAmount.getValue()['Amount'];
		
		var fertType = this.getComponent('DSS_FertType');
		obj['Type'] = fertType.getValue()['Type'];
		
		transform['Fertilizer'] = obj;
		
		return '<b>Fertilizer:</b> ' + obj['Amount'] + ', ' + obj['Type'];
	}
	
});

