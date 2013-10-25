
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderHeader', {
    extend: 'Ext.container.Container',
    alias: 'widget.report_spider_header',

    height: 28,
    width: 500,
        layout: {
        type: 'absolute'
    },
	style: {
		'background-color': '#f1f4f6',
		border: '1px solid #edf0f0'
	},

    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;
	
        Ext.applyIf(me, {
            items: [{
				xtype: 'radiogroup',
				x: 100,
				y: 2,
				fieldLabel: 'Value Display',
				labelWidth: 82,
				width: 280,
				labelPad: 5,
				labelAlign: 'left',
				labelStyle: 'color:#777;',
				items: [{
					boxLabel: 'Absolute',
					name: 'valueStyle',
					checked: true,
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setValueStyle('absolute');
						}
					}
				},{
					boxLabel: 'Normalized',
					name: 'valueStyle',
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setValueStyle('%');
						}
					}
				}]
			}]
	    });
        
        me.callParent(arguments);
    }
    
});

