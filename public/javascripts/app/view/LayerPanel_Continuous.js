
//------------------------------------------------------------------------------
Ext.define('MyApp.view.LayerPanel_Continuous', {
    extend: 'MyApp.view.LayerPanel_Common',
    alias: 'widget.layer_continuous',

    height: 86,
    
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        var label = '<p style="text-align:right">' + (me.DSS_ShortTitle ? me.DSS_ShortTitle : me.title) + '</p>';
        var rangeLabel = 'Range of values: ' + 
        					me.DSS_LayerRangeMin.toFixed(1) + me.DSS_LayerUnit +
        					' to ' + 
        					me.DSS_LayerRangeMax.toFixed(1) + me.DSS_LayerUnit;
        					
        if (!me.DSS_DefaultLessThanTest) {
        	me.DSS_DefaultLessThanTest = '<=';
        }
        if (!me.DSS_DefaultGreaterThanTest) {
        	me.DSS_DefaultGreaterThanTest = '>=';
        }
        				
        Ext.applyIf(me, {
            items: [{
				xtype: 'label',
				x: 0,
				y: -2,
				html: label,
				width: 60
			},{
				xtype: 'button',
				itemId: 'DSS_GreaterThanTest',
				x: 70,
				y: 6,
				width: 30,
				text: me.DSS_DefaultGreaterThanTest, //'>=',
				tooltip: {
					text: 'Toggle greater than/equal'
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
				y: 6,
				width: 64,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: me.DSS_ValueStep,
				value: me.DSS_ValueDefaultGreater,
				minValue: 0
			},{
				xtype: 'button',
				x: 164,
				y: 6,
				width: 18,
				text: 'c',
				tooltip: {
					text: 'Clear this text field'
				},
				handler: function(me,evt) {
					me.up().getComponent('DSS_GreaterThanValue').setValue('');
				}
			},{
				xtype: 'label',
				x: 187,
				y: 10,
				html: me.DSS_LayerUnit,
				width: 60
			},{
				xtype: 'button',
				icon: 'app/images/switch_icon.png',
				tooltip: {
					text: 'Swap values'
				},
				x: 211,
				y: 6,
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
				x: 249,
				y: 6,
				width: 30,
				text: me.DSS_DefaultLessThanTest, // '<='
				tooltip: {
					text: 'Toggle less than/equal'
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
				x: 279,
				y: 6,
				width: 64,
				hideEmptyLabel: false,
				hideLabel: true,
				decimalPrecision: 1,
				step: me.DSS_ValueStep,
				value: me.DSS_ValueDefaultLess,
				minValue: 0
			},{
				xtype: 'button',
				x: 343,
				y: 6,
				width: 18,
				text: 'c',
				tooltip: {
					text: 'Clear this text field'
				},
				handler: function(me,evt) {
					me.up().getComponent('DSS_LessThanValue').setValue('');
				}
			},{
				xtype: 'label',
				x: 367,
				y: 10,
				html: me.DSS_LayerUnit,
				width: 60
			},{
				xtype: 'label',
				itemId: 'DSS_ValueRange',
				x: 70,
				y: 36,
				text: rangeLabel,
				style: {
					color: '#888'
				}
			},{
            	xtype: 'button',
            	x: 390,
            	y: 4,
            	width: 23,
            	icon: 'app/images/go_icon_small.png',
            	handler: function(self) {
            		me.createOpacityPopup(self);
            	},
            	tooltip: {
            		text: 'Viewable Layer Overlay'
            	}
			},{
            	xtype: 'button',
            	x: 390,
            	y: 30,
            	width: 23,
            	hidden: true,
            	icon: 'app/images/eye_icon.png',
            	handler: function(self) {
            		alert('Query for this layer would be run here...');
            	},
            	tooltip: {
            		text: 'Preview only this criteria selection'
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
				
				// TODO: keep this check? Or should the server be sending a fail message?		
				if (response.responseText != '') {
					// Note: old versions of IE may not support Json.parse...
					var obj = JSON.parse(response.responseText);
	
					var label = container.getComponent('DSS_ValueRange');
					
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
				}
			},
			
			failure: function(response, opts) {
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
    getSelectionCriteria: function() {
    	
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
    },

    //--------------------------------------------------------------------------
    setSelectionCriteria: function(jsonQuery) {

    	if (!jsonQuery || !jsonQuery.queryLayers) {
			this.header.getComponent('DSS_ShouldQuery').toggle(false);
			this.getComponent('DSS_GreaterThanTest').setText(this.DSS_DefaultGreaterThanTest);
			this.getComponent('DSS_GreaterThanValue').setValue(this.DSS_ValueDefaultGreater);
			this.getComponent('DSS_LessThanTest').setText(this.DSS_DefaultLessThanTest);
			this.getComponent('DSS_LessThanValue').setValue(this.DSS_ValueDefaultLess);
    		
    		return;
    	}
    	
		for (var i = 0; i < jsonQuery.queryLayers.length; i++) {
		
			var queryElement = jsonQuery.queryLayers[i];
			
			// in query?
			if (queryElement.name == this.DSS_QueryTable) {
				// yup
				this.header.getComponent('DSS_ShouldQuery').toggle(true);
				var gtrTest = this.getComponent('DSS_GreaterThanTest');
				var gtrValue = this.getComponent('DSS_GreaterThanValue');
				var lessTest = this.getComponent('DSS_LessThanTest');
				var lessValue = this.getComponent('DSS_LessThanValue');
		
				lessTest.setText(queryElement.lessThanTest);
				gtrTest.setText(queryElement.greaterThanTest);
				lessValue.setValue(queryElement.lessThanValue);
				gtrValue.setValue(queryElement.greaterThanValue);
				return;
			}
		}
		
		// Nope, mark as not queried
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
    },
    
    // turns layer off and resets it to the defaults....
    //--------------------------------------------------------------------------
    resetLayer: function() {
    	
		this.header.getComponent('DSS_ShouldQuery').toggle(false);
		var gtrTest = this.getComponent('DSS_GreaterThanTest');
		var gtrValue = this.getComponent('DSS_GreaterThanValue');
		var lessTest = this.getComponent('DSS_LessThanTest');
		var lessValue = this.getComponent('DSS_LessThanValue');

		lessTest.setText(this.DSS_DefaultLessThanTest);
		gtrTest.setText(this.DSS_DefaultGreaterThanTest);
		lessValue.setValue(this.DSS_ValueDefaultLess);
		gtrValue.setValue(this.DSS_ValueDefaultGreater);
    }
    
});

