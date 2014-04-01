
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
            	xtype: 'container',
            	id: 'DSS_comparisonHeaderDisplay',
            	layout: 'absolute',
            	height: 24,
            	items: [{
					xtype: 'label',
					x: 30,
					y: 4,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Comparison Type:',
					width: 105
            	},{
					xtype: 'label',
					id: 'DSS_comparisonTypeDisplay',
					x: 131,
					y: 4,
					text: 'Selected Cells',
					width: 100
				},{
					xtype: 'label',
					x: 270,
					y: 4,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Normalize By:',
					width: 80
            	},{
					xtype: 'label',
					id: 'DSS_normalizeTypeDisplay',
					x: 348,
					y: 4,
					text: 'None'
            	},{
            		xtype: 'tool',
            		x: 460,
            		y: 4,
            		type: 'down',
            		handler: function() {
            			Ext.suspendLayouts();
            			Ext.getCmp('DSS_comparisonHeaderDisplay').hide();
            			Ext.getCmp('DSS_comparisonHeaderChange').show();
            			Ext.resumeLayouts(true);
            		}
            	}]
            },
            {
				xtype: 'container',
            	id: 'DSS_comparisonHeaderChange',
				layout: 'absolute',
				height: 70,
				hidden: true,
				items: [{
					xtype: 'radiogroup',
					id: 'DSS_ComparisonStyleRadioGroup',
					x: 30,
					y: 0,
					fieldLabel: 'Comparison Type',
					labelWidth: 98,
					labelAlign: 'right',
					width: 220,
					vertical: true,
					columns: 1,
					labelPad: 5,
					labelAlign: 'left',
					labelStyle: 'color:#777;',
					items: [{
						boxLabel: 'Selected Cells',
						name: 'compareStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setValueStyle('absolute');
								Ext.getCmp('DSS_comparisonTypeDisplay').setText('Selected Cells');
							}
						}
					},{
						boxLabel: 'All Cells',
						name: 'compareStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setValueStyle('%');
								Ext.getCmp('DSS_comparisonTypeDisplay').setText('All Cells');
							}
						}
					}]
				},{
					xtype: 'radiogroup',
					x: 270,
					y: 0,
					fieldLabel: 'Normalize By',
					labelWidth: 75,
					width: 190,
					vertical: true,
					columns: 1,
					labelStyle: 'color:#777;',
					items: [{
						boxLabel: 'None',
						name: 'normalizeStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('delta');
								Ext.getCmp('DSS_normalizeTypeDisplay').setText('None');
							}
						}
					},{
						boxLabel: 'Area',
						name: 'normalizeStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('file1');
								Ext.getCmp('DSS_normalizeTypeDisplay').setText('Area');
							}
						}
					},{
						boxLabel: '&#916; Income',
						name: 'normalizeStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('file2');
								Ext.getCmp('DSS_dataTypeDisplay').setText('&#916; Income');
							}
						}
					}]
				},{
            		xtype: 'tool',
            		x: 460,
            		y: 4,
            		type: 'up',
            		handler: function() {
            			Ext.suspendLayouts();
            			Ext.getCmp('DSS_comparisonHeaderChange').hide();
            			Ext.getCmp('DSS_comparisonHeaderDisplay').show();
            			Ext.resumeLayouts(true);
            		}
            	}]
			}]
	    });
        
        me.callParent(arguments);
    }

});

