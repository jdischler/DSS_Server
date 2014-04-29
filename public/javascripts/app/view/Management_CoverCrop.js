	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Management_CoverCrop', {
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
				itemId: 'DSS_CoverCrop',
				x: 0,
				y: 0,
				width: 290,
				fieldLabel: 'Cover Crop',
				labelAlign: 'right',
				labelWidth: 70,
				allowBlank: false,
				items: [{
					xtype: 'radiofield',
					boxLabel: 'Yes',
					checked: true,
					name: 'CoverCrop',
					inputValue: 0
				},
				{
					xtype: 'radiofield',
					boxLabel: 'No',
					name: 'CoverCrop',
					inputValue: 1
				}]
			}]
		});
		
		me.callParent(arguments);
		
		this.setFromTransform(this.DSS_Transform);
	},
	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {
		
		if (transform && transform.Options && transform.Options.CoverCrop) {
			var coverCrop = this.getComponent('DSS_CoverCrop');
			coverCrop.setValue({'CoverCrop': !transform.Options.CoverCrop.CoverCrop});
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var obj = {
			CoverCrop: false,
			text: '<b>Cover Crop:</b> '
		};
		
		var tillageType = this.getComponent('DSS_CoverCrop');
		var value = tillageType.getValue()['CoverCrop'];
		console.log(value);
		
		if (value == 0) {
			obj.text += 'Yes';
			obj.CoverCrop = true;
		}
		else if (value == 1) {
			obj.text += 'None';
		}
		
		transform['CoverCrop'] = obj;
		
		return obj;
	}
	
});

