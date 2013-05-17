
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
			},{
				xtype: 'button',
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
				}
			},{
				xtype: 'label',
				x: 70,
				y: 40,
				text: rangeLabel
			}]
        });

        me.callParent(arguments);
    }

});
