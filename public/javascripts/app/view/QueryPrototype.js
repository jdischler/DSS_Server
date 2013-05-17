
Ext.define('MyApp.view.QueryPrototype', {
	extend: 'Ext.window.Window',
	
	height: 300,
	width: 500,
	layout: {
		type: 'absolute'
	},
	title: 'Scenario Creator',
	titleAlign: 'center',
	
	
	initComponent: function() {
		var me = this;
		
		Ext.applyIf(me, {
			items: [{
				xtype: 'textfield',
				x: 10,
				y: 20,
				width: 420,
				fieldLabel: '',
				emptyText: 'Loading...'
			},
			{
				xtype: 'label',
				x: 40,
				y: 20,
				id: 'loadingLabel',
				width: 320,
				text: 'Loading...'
			}]
		});
		
		me.callParent(arguments);
	}

});
