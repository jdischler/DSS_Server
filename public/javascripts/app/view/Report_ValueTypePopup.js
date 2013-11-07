
// Values used by this WIDGET object - should be provided on the configured object...    
// DSS_Label - the label for the value field
// DSS_UnitLabel - the label that describes what units the value field are in
// DSS_GraphTitle - the title for the popup graph window
// DSS_GraphData - the server passed JSON that contains the data bins for the graphes
// DSS_FieldString - the name passed to the server heatmap function, spider graphs, etc.

//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_ValueTypePopup', {
    extend: 'Ext.container.Container',
    alias: 'widget.report_value_popup',

//    height: 95,
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
            	id: 'DSS_reportHeaderDisplay',
            	layout: 'absolute',
            	height: 24,
            	items: [{
					xtype: 'label',
					x: 20,
					y: 6,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Value Type:',
					width: 65
            	},{
					xtype: 'label',
					id: 'DSS_valueTypeDisplay',
					disabledCls: 'dss-disabled-label',
					x: 90,
					y: 6,
					text: 'Absolute'
				},{
					xtype: 'label',
					x: 190,
					y: 6,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Data:',
					width: 30
            	},{
					xtype: 'label',
					id: 'DSS_dataTypeDisplay',
					x: 225,
					y: 6,
					text: 'Delta'
            	},{
					xtype: 'label',
					x: 300,
					y: 6,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Heatmap:',
					width: 50
            	},{
					xtype: 'label',
					id: 'DSS_heatmapTypeDisplay',
					x: 355,
					y: 6,
					width: 110,
					text: 'Quantile'
            	},{
            		xtype: 'tool',
            		x: 460,
            		y: 4,
            		type: 'down',
            		handler: function() {
            			Ext.suspendLayouts();
            			Ext.getCmp('DSS_reportHeaderDisplay').hide();
            			Ext.getCmp('DSS_reportHeaderChange').show();
            			Ext.resumeLayouts(true);
            		}
            	}]
            },
            {
				xtype: 'container',
            	id: 'DSS_reportHeaderChange',
				layout: 'absolute',
				height: 70,
				hidden: true,
				items: [{
					xtype: 'radiogroup',
					id: 'DSS_ValueStyleRadioGroup',
					x: 20,
					y: 2,
					fieldLabel: 'Value Type',
					labelWidth: 65,
					labelAlign: 'right',
					width: 200,
					vertical: true,
					columns: 1,
					labelPad: 5,
					labelAlign: 'left',
					labelStyle: 'color:#777;',
					items: [{
						boxLabel: 'Absolute',
						name: 'valueStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setValueStyle('absolute');
								Ext.getCmp('DSS_valueTypeDisplay').setText('Absolute');
							}
						}
					},{
						boxLabel: '%',
						name: 'valueStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setValueStyle('%');
								Ext.getCmp('DSS_valueTypeDisplay').setText('%');
							}
						}
					},{
						boxLabel: '$',
						name: 'valueStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setValueStyle('$');
								Ext.getCmp('DSS_valueTypeDisplay').setText('$');
							}
						}
					}]
				},{
					xtype: 'radiogroup',
					x: 190,
					y: 2,
					fieldLabel: 'Data',
					labelWidth: 30,
					width: 190,
					vertical: true,
					columns: 1,
					labelStyle: 'color:#777;',
					items: [{
						boxLabel: 'Delta',
						name: 'dataStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('delta');
								Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(false);
								Ext.getCmp('DSS_valueTypeDisplay').setDisabled(false);
								Ext.getCmp('DSS_dataTypeDisplay').setText('Delta');
							}
						}
					},{
						boxLabel: 'File1',
						name: 'dataStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('file1');
								Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(true);
								Ext.getCmp('DSS_valueTypeDisplay').setDisabled(true);
								Ext.getCmp('DSS_dataTypeDisplay').setText('File1');
							}
						}
					},{
						boxLabel: 'File2',
						name: 'dataStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('file2');
								Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(true);
								Ext.getCmp('DSS_valueTypeDisplay').setDisabled(true);
								Ext.getCmp('DSS_dataTypeDisplay').setText('File2');
							}
						}
					}]
				},{
					xtype: 'radiogroup',
					x: 300,
					y: 2,
					fieldLabel: 'Heatmap',
					labelWidth: 50,
					width: 190,
					vertical: true,
					columns: 1,
					labelStyle: 'color:#777;',
					items: [{
						boxLabel: 'Quantile',
						name: 'heatStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').changeDataSubStyle('quantile');
								Ext.getCmp('DSS_heatmapTypeDisplay').setText('Quantile');
							}
						}
					},{
						boxLabel: 'Equal Interval',
						name: 'heatStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').changeDataSubStyle('equal');
								Ext.getCmp('DSS_heatmapTypeDisplay').setText('Equal Interval');
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
            			Ext.getCmp('DSS_reportHeaderChange').hide();
            			Ext.getCmp('DSS_reportHeaderDisplay').show();
            			Ext.resumeLayouts(true);
            		}
            	}]
			}]
	    });
        
        me.callParent(arguments);
    },
    
    //--------------------------------------------------------------------------
	// These below here to simplify changing misc things on all elements of the Report_Detail panel...
	//	ie, I just iterate over all components on that panel but this has nothing to change.
    //--------------------------------------------------------------------------
    changeDataStyleType: function(newType) {
    },
    
    changeValueStyleType: function(newType) {
    },
    
    changeDataSubStyle: function(subtype) {
    },
    
    clearFields: function() {
    },
    
    clearHeatToggle: function() {
    },
    
	setWait: function() {
    }

});

