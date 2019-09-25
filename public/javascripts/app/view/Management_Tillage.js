	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Management_Tillage', {
	extend: 'Ext.container.Container',
	
	height: 30,
	width: 290,
	layout: {
		type: 'absolute'
	},

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
					inputValue: 0
				},
				{
					xtype: 'radiofield',
					boxLabel: 'No-Till',
					name: 'Tillage',
					inputValue: 1
				}]
			}]
		});
		
		me.callParent(arguments);
		
		this.setFromTransform(this.DSS_Transform);
	},
	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {

		if (transform && transform.Options && transform.Options.Tillage) {
			var tillageType = this.getComponent('DSS_Tillage');
			tillageType.setValue({'Tillage': !transform.Options.Tillage.Tillage});
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var obj = {
			Tillage: false,
			text: '<b>Tillage:</b> '
		};
		
		var tillageType = this.getComponent('DSS_Tillage');
		var value = tillageType.getValue()['Tillage'];
//		console.log(value);
		
		if (value == 0) {
			obj.text += 'Conventional';
			obj.Tillage = true;
		}
		else if (value == 1) {
			obj.text += 'No-Till';
		}
		
		transform['Tillage'] = obj;
		
		return obj;
	}
	
});

