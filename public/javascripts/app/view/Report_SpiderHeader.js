
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_SpiderHeader', {
    extend: 'Ext.container.Container',
    alias: 'widget.report_spider_header',

    hidden: true,
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
            	xtype: 'container',
            	id: 'DSS_spiderHeaderDisplay',
            	layout: 'absolute',
            	height: 24,
            	items: [{
					xtype: 'label',
					x: 60,
					y: 4,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Value Type:',
					width: 65
            	},{
					xtype: 'label',
					id: 'DSS_spiderValueTypeDisplay',
					disabledCls: 'dss-disabled-label',
					x: 130,
					y: 4,
					text: 'Normalized'
				},{
					xtype: 'label',
					x: 260,
					y: 4,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Graph:',
					width: 30
            	},{
					xtype: 'label',
					id: 'DSS_spiderGraphType',
					x: 305,
					y: 4,
					text: 'Detail'
            	},{
            		xtype: 'tool',
            		x: 460,
            		y: 4,
            		type: 'down',
 					tooltip: {
						text: 'Show/Hide display options for this report section.'
					},
					handler: function() {
            			Ext.suspendLayouts();
            			Ext.getCmp('DSS_spiderHeaderDisplay').hide();
            			Ext.getCmp('DSS_spiderHeaderChange').show();
            			Ext.resumeLayouts(true);
            		}
            	}]
            },
            {
				xtype: 'container',
            	id: 'DSS_spiderHeaderChange',
				layout: 'absolute',
				height: 50,
				hidden: true,
				items: [{
					xtype: 'radiogroup',
					disabled: true,
					x: 60,
					y: 0,
					fieldLabel: 'Value Type',
					labelWidth: 62,
					labelAlign: 'right',
					width: 200,
					vertical: true,
					columns: 1,
					labelPad: 5,
					labelAlign: 'left',
					labelStyle: 'color:#777;',
					items: [{
						boxLabel: 'Normalized',
						name: 'graphValue',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
							//	Ext.getCmp('DSS_ReportDetail').setValueStyle('absolute');
								Ext.getCmp('DSS_spiderValueTypeDisplay').setText('Normalized');
							}
						}
					},{
						boxLabel: 'Absolute',
						name: 'graphValue',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
							//	Ext.getCmp('DSS_ReportDetail').setValueStyle('%');
								Ext.getCmp('DSS_spiderValueTypeDisplay').setText('Absolute');
							}
						}
					}]
				},{
					xtype: 'radiogroup',
					x: 260,
					y: 0,
					fieldLabel: 'Graph',
					labelWidth: 40,
					width: 190,
					vertical: true,
					columns: 1,
					labelStyle: 'color:#777;',
					items: [{
						boxLabel: 'Detail',
						name: 'graphStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_SpiderGraphPanel').setSpiderDetailType('detail');
								Ext.getCmp('DSS_spiderGraphType').setText('Detail');
							}
						}
					},{
						boxLabel: 'Simplified',
						name: 'graphStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_SpiderGraphPanel').setSpiderDetailType('combined');
								Ext.getCmp('DSS_spiderGraphType').setText('Simplified');
							}
						}
					}]
				},{
            		xtype: 'tool',
            		x: 460,
            		y: 4,
            		type: 'up',
					tooltip: {
						text: 'Show/Hide display options for this report section.'
					},
            		handler: function() {
            			Ext.suspendLayouts();
            			Ext.getCmp('DSS_spiderHeaderChange').hide();
            			Ext.getCmp('DSS_spiderHeaderDisplay').show();
            			Ext.resumeLayouts(true);
            		}
            	}]
			}]
	    });
        
        me.callParent(arguments);
    }
    
});

