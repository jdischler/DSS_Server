	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Management_Terraced', {
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
				itemId: 'DSS_Terraced',
				x: 0,
				y: 0,
				width: 290,
				fieldLabel: 'Terraced',
				labelAlign: 'right',
				labelWidth: 70,
				allowBlank: false,
				items: [{
					xtype: 'radiofield',
					boxLabel: 'Yes',
					name: 'Terraced',
					inputValue: 0
				},
				{
					xtype: 'radiofield',
					boxLabel: 'No',
					checked: true,
					name: 'Terraced',
					inputValue: 1
				}]
			}]
		});
		
		me.callParent(arguments);
		
		this.setFromTransform(this.DSS_Transform);
	},
	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {
		
		if (transform && transform.Options && transform.Options.Terraced) {
			var terraced = this.getComponent('DSS_Terraced');
			terraced.setValue({'Terraced': !transform.Options.Terraced.Terraced});
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var obj = {
			Terraced: false,
			text: '<b>Terraced:</b> '
		};
		
		var terraced = this.getComponent('DSS_Terraced');
		var value = terraced.getValue()['Terraced'];
		
		if (value == 0) {
			obj.text += 'Yes';
			obj.Terraced = true;
		}
		else if (value == 1) {
			obj.text += 'None';
		}
		
		transform['Terraced'] = obj;
		
		return obj;
	}
	
});

