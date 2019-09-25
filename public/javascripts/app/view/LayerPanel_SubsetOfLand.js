

// TODO: verify that these cell sizes, in 30 meter cell widths generate the specified
//	english unit of area... e.g., does 7x7 30x30 meter cells really equal roughly 10 acres??
//------------------------------------------------------------------------------
var DSS_GridSizes = Ext.create('Ext.data.Store', {
		
	fields: ['size', 'name'],
	data: [
		{ 'size': 7, 'name': '10 Acres'},
		{ 'size': 21, 'name': '100 Acres'},
		{ 'size': 38, 'name': '0.5 sq Miles'}
	]
});

//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_SubsetOfLand', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_subset',

    width: 400,
    height: 66,
    
    DSS_FractionPosition_X: 70,
    DSS_FractionPosition_Y: 6,
    DSS_CellSizePosition_X: 200,
    
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'button', 
				text: 'Select',
				x: me.DSS_FractionPosition_X,
				y: me.DSS_FractionPosition_Y,
				width: 55,
				tooltip: {
					text: 'Select a fraction of the land'
				},
				handler: function(button, evt) {
					button.disable(true);
					button.setText('Reselect');
					
					var res = Math.random();
					res *= 32767.0;
					res = Math.floor(res);
					
					me.DSS_Seed = res;

					var queryButton = Ext.getCmp('DSS_queryButton');
					queryButton.DSS_associatedButton = button;
					queryButton.btnEl.dom.click();
				}
			},{
				xtype: 'numberfield',
				itemId: 'DSS_FractionOfLand',
				x: me.DSS_FractionPosition_X + 55,
				y: me.DSS_FractionPosition_Y,
				width: 40,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 0,
				step: 5,
				value: 50,
				minValue: 1,
				maxValue: 99,
				enableKeyEvents: true,
				listeners: DSS_NumberFieldListener
			},{
				xtype: 'label',
				x: me.DSS_FractionPosition_X + 55 + 45,
				y: me.DSS_FractionPosition_Y + 3,
				text: '%'
			},
			{
				xtype: 'combobox',
				itemId: 'DSS_CellScales',
				x: me.DSS_CellSizePosition_X,
				y: me.DSS_FractionPosition_Y,
				width: 200,
				fieldLabel: 'Approx Scale',
				labelAlign: 'right',
				labelWidth: 90,
				labelPad: 5,
				displayField: 'name',
				forceSelection: true,
				store: DSS_GridSizes,
				valueField: 'size',
				value: 21, // 100 acres?
				listeners: {
					change: function(self) {
						self.disable(true);
						var queryButton = Ext.getCmp('DSS_queryButton');
						queryButton.DSS_associatedButton = self;
						queryButton.btnEl.dom.click();
					}
				}
			}]
        });

        me.callParent(arguments);
    },
	
	//--------------------------------------------------------------------------
	resetLayer: function() {
		
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
	},
	
    //--------------------------------------------------------------------------
    getSelectionCriteria: function() {
    	
    	var scale = 21;
		var combo = this.getComponent('DSS_CellScales');
		if (combo.getValue()) {
			scale = combo.getValue();
		}

		var queryLayer = { 
			name: 'proceduralFraction',
			type: 'fractionalLand',
			fraction: this.getComponent('DSS_FractionOfLand').getValue(),
			gridCellSize: scale,
			seed: this.DSS_Seed
		};
		
        return queryLayer;
    },

    //--------------------------------------------------------------------------
    setSelectionCriteria: function(jsonQuery) {

    	var me = this;
    	if (!jsonQuery || !jsonQuery.queryLayers) {
			me.header.getComponent('DSS_ShouldQuery').toggle(false);
    		return;
    	}
    	
		for (var i = 0; i < jsonQuery.queryLayers.length; i++) {
		
			var queryElement = jsonQuery.queryLayers[i];
			
			// in query?
			if (queryElement && queryElement.name == me.DSS_QueryTable) {
				me.getComponent('DSS_FractionOfLand').setValue(queryElement.fraction);
				var combo = me.getComponent('DSS_CellScales');
				combo.setValue(queryElement.gridCellSize);
				// yup
				me.show();
				me.header.getComponent('DSS_ShouldQuery').toggle(true);
				return;
        	}
        }
				
		// Nope, mark as not queried
		me.hide();
		me.header.getComponent('DSS_ShouldQuery').toggle(false);
    }
	
});

