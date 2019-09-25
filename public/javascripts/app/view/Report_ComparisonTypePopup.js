
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_ComparisonTypePopup.js', {
    extend: 'Ext.container.Container',
    alias: 'widget.report_comparison_popup',

    width: 500,
	layout: 'vbox',
	style: {
		'background-color': '#f1f4f6',
		border: '1px solid #dde0e0'
	},

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
	
        Ext.applyIf(me, {
            items: [{
				xtype: 'radiogroup',
				id: 'DSS_ComparisonStyleRadioGroup',
				fieldLabel: 'Comparison Scope',
				labelWidth: 122,
				labelAlign: 'right',
				width: 440,
				vertical: true,
				columns: 2,
				labelPad: 5,
				labelStyle: 'color:#777;',
				items: [{
					boxLabel: 'Entire Landscape',
					checked: true,
					name: 'compareStyle',
					padding: '0 0 -5 0',
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setComparisonType('landscape');
						}
					}
				},{
					boxLabel: 'Only Modified Landscape',
					name: 'compareStyle',
					padding: '0 0 -5 0',
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setComparisonType('selection');
						}
					}
				}]
			}]
	    });
        
        me.callParent(arguments);
    }

});

