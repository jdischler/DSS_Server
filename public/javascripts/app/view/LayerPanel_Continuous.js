
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Continuous', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_continuous',

    height: 90,
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        var label = '<p style="text-align:right">' + me.title + '</p>';
        
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
				tooltip: 'Greater than',
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
				width: 60,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: 0.5,
				value: me.DSS_ValueDefaultGreater
			},{
				xtype: 'label',
				x: 163,
				y: 14,
				html: me.DSS_LayerUnit,
				width: 60
			},/*{
				xtype: 'button',
				icon: 'app/images/switch_icon.png',
				x: 170,
				y: 10
			},*/{
				xtype: 'button',
				itemId: 'DSS_LessThanTest',
				x: 190,
				y: 10,
				width: 30,
				text: '<=',
				tooltip: 'Less than',
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
				x: 220,
				y: 10,
				width: 60,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: 0.5,
				value: me.DSS_ValueDefaultLess
			},{
				xtype: 'label',
				x: 283,
				y: 14,
				html: me.DSS_LayerUnit,
				width: 60
			},{
				xtype: 'button',
				x: 300,
				y: 10,
				text: 'Set Selection',
				handler: function(me,evt) {
					this.up().buildQuery();
				}
			},{
				xtype: 'label',
				itemId: 'DSS_ValueRange',
				x: 70,
				y: 40,
				text: rangeLabel
			}]
        });

        me.callParent(arguments);
    },

    // TODO: finish!
	//--------------------------------------------------------------------------
    requestLayerRange: function() {

		var queryLayerRequest = { 
			name: this.DSS_QueryTable,
			type: 'layerRange',
		};
    	
		var obj = Ext.Ajax.request({
			url: 'http://localhost:9000/layerRequest',
			jsonData: queryLayerRequest,
			timeout: 2000,
			
			success: function(response, opts) {
				console.log("success: ");
				console.log(response);
				
				var label = this.up().getComponent('DSS_ValueRange');
				
				this.DSS_LayerRangeMin = response.layerMin;
				this.DSS_LayerRangeMax = response.layerMax;
				
				var rangeLabel = 'Range of values: ' + 
        					this.DSS_LayerRangeMin.toFixed(1) + this.DSS_LayerUnit +
        					' to ' + 
        					this.DSS_LayerRangeMax.toFixed(1) + methisDSS_LayerUnit;

				label.setText(rangeLabel);
			},
			
			failure: function(respose, opts) {
				alert("Request Failsauce");
			}
		});
	},

    //--------------------------------------------------------------------------
    setSelection: function() {
    	
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
