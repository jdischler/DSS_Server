	
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Management_Fertilizer', {
	extend: 'Ext.container.Container',
	
	height: 75,
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
				itemId: 'DSS_FertType',
				x: 0,
				y: 0,
				width: 290,
				fieldLabel: 'Fertilizer',
				labelAlign: 'right',
				labelWidth: 70,
				allowBlank: false,
				columns: 1,
				items: [{
					xtype: 'radiofield',
					boxLabel: 'None',
					name: 'Type',
					inputValue: 0,
					handler: function(radio, checked) {
						if (checked) {
							me.getComponent('DSS_FallSpread').setDisabled(true);
						}
					}
				},{
					xtype: 'radiofield',
					boxLabel: 'Manure',
					name: 'Type',
					inputValue: 1,
					handler: function(radio, checked) {
						if (checked) {
							me.getComponent('DSS_FallSpread').setDisabled(false);
						}
					}
				},
				{
					xtype: 'radiofield',
					boxLabel: 'Synthetic',
					name: 'Type',
					checked: true,
					inputValue: 2,
					handler: function(radio, checked) {
						if (checked) {
							me.getComponent('DSS_FallSpread').setDisabled(true);
						}
					}
				}]
			},
			{
				xtype: 'checkbox',
				itemId: 'DSS_FallSpread',
				x: 165,
				y: 21,
				fieldLabel: 'Fall spread?',
				labelSeparator: '',
				labelWidth: 70,
				disabled: true
			}]
		});
		
		me.callParent(arguments);
		
		this.setFromTransform(this.DSS_Transform);
	},

	
	//--------------------------------------------------------------------------
	setFromTransform: function(transform) {
		
		if (transform && transform.Options && transform.Options.Fertilizer) {
			var fertType = this.getComponent('DSS_FertType');
			var value = 0;
			if (transform.Options.Fertilizer.Fertilizer) {
				if (transform.Options.Fertilizer.FertilizerManure) {
					value = 1;
				}
				else {
					value = 2;
				}
			}
			fertType.setValue({'Type': value});
			
			if (value == 1) {// manure 
				this.getComponent('DSS_FallSpread').setDisabled(false);
				this.getComponent('DSS_FallSpread').setValue(transform.Options.Fertilizer.FertilizerFallSpread);
			}
		}
	},
	
	//--------------------------------------------------------------------------
	collectChanges: function(transform) {
		
		var obj = {
			Fertilizer: false,
			FertilizerManure: false,
			FertilizerFallSpread: false,
			text: '<b>Fertilizer:</b> '
		};
		
		var fertType = this.getComponent('DSS_FertType');
		var value = fertType.getValue()['Type'];
//		console.log(value);
		
		if (value == 0) {
			obj.text += 'None';
		}
		else
		{
			obj.Fertilizer = true;
			if (value == 1) {
				obj.FertilizerManure = true;
				if (this.getComponent('DSS_FallSpread').getValue()) {
					obj.FertilizerFallSpread = true;
					obj.text += 'Fall Spread Manure';
				}
				else {
					obj.text += 'Manure';
				}
			}
			else if (value == 2) {
				obj.text += 'Synthetic';
			}
		}
			
		transform['Fertilizer'] = obj;
		
		return obj;
	}
	
});

