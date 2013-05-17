
Ext.define('MyApp.store.LayerStore', {
	extend: 'Ext.data.Store',
	
	constructor: function(cfg) {
		var me = this;
		cfg = cfg || {};
		
		me.callParent([Ext.apply({
			storeId: 'layerStore',
			
			data: [{
				name: 'Corn / Soy',
				index: 1,
				check: false
			},{
				name: 'Forest',
				index: 2,
				check: false
			},{
				name: 'Grassland',
				index: 3,
				check: false
			}],
			
			fields: [{
				name: 'name',
				type: 'string'
			},{
				name: 'index',
				type: 'int'
			},{
				name: 'check',
				type: 'boolean'
			}]
			
		}, cfg)]);
	}
});
