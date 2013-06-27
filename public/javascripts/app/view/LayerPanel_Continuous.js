
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Continuous', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_continuous',

    height: 90,
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        var label = '<p style="text-align:right">' + (me.DSS_ShortTitle ? me.DSS_ShortTitle : me.title) + '</p>';
        var rangeLabel = 'Range of values: ' + 
        					me.DSS_LayerRangeMin.toFixed(1) + me.DSS_LayerUnit +
        					' to ' + 
        					me.DSS_LayerRangeMax.toFixed(1) + me.DSS_LayerUnit;
        				
        Ext.applyIf(me, {
            items: [{
				xtype: 'label',
				x: 0,
				y: 14,
				html: label,
				width: 60
			},{
				xtype: 'button',
				itemId: 'DSS_GreaterThanTest',
				x: 70,
				y: 10,
				width: 30,
				text: '>=',
				tooltip: {
					text: 'Toggle greater than/equal',
					showDelay: 100
				},
				handler: function(me,evt) {
					if (me.text == '>=') {
						me.setText('>');
					}
					else {
						me.setText('>=');
					}
				}
			},{
				xtype: 'numberfield',
				itemId: 'DSS_GreaterThanValue',
				x: 100,
				y: 10,
				width: 54,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: 0.5,
				value: me.DSS_ValueDefaultGreater
			},{
				xtype: 'button',
				x: 154,
				y: 10,
				width: 18,
				text: 'c',
				tooltip: {
					text: 'Clear this text field',
					showDelay: 100
				},
				handler: function(me,evt) {
					me.up().getComponent('DSS_GreaterThanValue').setValue('');
				}
			},{
				xtype: 'label',
				x: 177,
				y: 14,
				html: me.DSS_LayerUnit,
				width: 60
			},{
				xtype: 'button',
				icon: 'app/images/switch_icon.png',
				tooltip: {
					text: 'Swap values',
					showDelay: 100
				},
				x: 197,
				y: 10,
				handler: function(me,evt) {
					var gtrValue = me.up().getComponent('DSS_GreaterThanValue');
					var lessValue = me.up().getComponent('DSS_LessThanValue');
					var temp = gtrValue.getValue();
					gtrValue.setValue(lessValue.getValue());
					lessValue.setValue(temp);
				}
			},{
				xtype: 'button',
				itemId: 'DSS_LessThanTest',
				x: 235,
				y: 10,
				width: 30,
				text: '<=',
				tooltip: {
					text: 'Toggle less than/equal',
					showDelay: 100
				},
				handler: function(me,evt) {
					if (me.text == '<=') {
						me.setText('<');
					}
					else {
						me.setText('<=');
					}
				}
			},{
				xtype: 'numberfield',
				itemId: 'DSS_LessThanValue',
				x: 265,
				y: 10,
				width: 54,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: 0.5,
				value: me.DSS_ValueDefaultLess
			},{
				xtype: 'button',
				x: 319,
				y: 10,
				width: 18,
				text: 'c',
				tooltip: {
					text: 'Clear this text field',
					showDelay: 100
				},
				handler: function(me,evt) {
					me.up().getComponent('DSS_LessThanValue').setValue('');
				}
			},{
				xtype: 'label',
				x: 343,
				y: 14,
				html: me.DSS_LayerUnit,
				width: 60
			},/*{
				xtype: 'button',
				itemId: 'selectionbutton',
				iconAlign: 'right',
				x: 300,
				y: 10,
				text: 'Set Selection',
				handler: function(me,evt) {
					this.up().buildQuery();
				}
			},*/{
				xtype: 'label',
				itemId: 'DSS_ValueRange',
				x: 70,
				y: 40,
				text: rangeLabel,
				style: {
					color: '#888'
				}
			}]
        });

        this.DSS_RequestTryCount = 0;
        this.requestLayerRange(this);
        
        me.callParent(arguments);
    },

	//--------------------------------------------------------------------------
    requestLayerRange: function(container) {

		var queryLayerRequest = { 
			name: container.DSS_QueryTable,
			type: 'layerRange',
		};
    	
		var obj = Ext.Ajax.request({
			url: location.href + 'layerParmRequest',
			jsonData: queryLayerRequest,
			timeout: 10000,
			scope: container,
			
			success: function(response, opts) {
				
				var label = container.getComponent('DSS_ValueRange');
				
				// Note: old versions of IE may not support Json.parse...
				var obj = JSON.parse(response.responseText);
				
				if (obj.length == 0 || obj.layerMin == null || obj.layerMax == null) {
					console.log("layer request object return was null?");
					return;
				}
				container.DSS_LayerRangeMin = obj.layerMin;
				container.DSS_LayerRangeMax = obj.layerMax;
				
				var rangeLabel = 'Range of values: ' + 
        					obj.layerMin.toFixed(1) + container.DSS_LayerUnit +
        					' to ' + 
        					obj.layerMax.toFixed(1) + container.DSS_LayerUnit;

				label.setText(rangeLabel);
			},
			
			failure: function(respose, opts) {
				console.log('layer request failed');
				if (this.DSS_RequestTryCount < 5) {
					console.log('trying again');
					this.DSS_RequestTryCount++;
					this.requestLayerRange(this);
				}
				else {
					console.log('giving up');
				}
			}
		});
	},

    //--------------------------------------------------------------------------
    setSelectionCriteria: function() {
    	
		var queryLayer = { 
			name: this.DSS_QueryTable,
			type: 'continuous'
		};
		
		var gtrTest = this.getComponent('DSS_GreaterThanTest');
		var gtrValue = this.getComponent('DSS_GreaterThanValue');
		var lessTest = this.getComponent('DSS_LessThanTest');
		var lessValue = this.getComponent('DSS_LessThanValue');

		queryLayer.lessThanTest = lessTest.text;
		queryLayer.greaterThanTest = gtrTest.text;
		queryLayer.lessThanValue = lessValue.getValue();
		queryLayer.greaterThanValue = gtrValue.getValue();
		
		return queryLayer;		
    }


});
