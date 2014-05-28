	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Management_Contour', {
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
				itemId: 'DSS_Contour',
				x: 0,
				y: 0,
				width: 290,
				fieldLabel: 'Contour',
				labelAlign: 'right',
				labelWidth: 70,
				allowBlank: false,
				items: [{
					xtype: 'radiofield',
					boxLabel: 'Yes',
					name: 'Contour',
					inputValue: 0
				},
				{
					xtype: 'radiofield',
					boxLabel: 'No',
					checked: true,
					name: 'Contour',
					inputValue: 1
				}]
			}]
		});
		
		me.callParent(arguments);
		
		this.setFromTransform(this.DSS_Transform);
	},
	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {
		
		if (transform && transform.Options && transform.Options.Contour) {
			var contour = this.getComponent('DSS_Contour');
			contour.setValue({'Contour': !transform.Options.Contour.Contour});
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var obj = {
			Contour: false,
			text: '<b>Contour:</b> '
		};
		
		var contour = this.getComponent('DSS_Contour');
		var value = contour.getValue()['Contour'];
		
		if (value == 0) {
			obj.text += 'Yes';
			obj.Contour = true;
		}
		else if (value == 1) {
			obj.text += 'None';
		}
		
		transform['Contour'] = obj;
		
		return obj;
	}
	
});

