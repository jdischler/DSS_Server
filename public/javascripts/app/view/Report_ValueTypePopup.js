
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
					y: 4,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Value Type:',
					width: 65
            	},{
					xtype: 'label',
					id: 'DSS_valueTypeDisplay',
					disabledCls: 'dss-disabled-label',
					x: 90,
					y: 4,
					text: 'Actual'
				},{
					xtype: 'label',
					x: 180,
					y: 4,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Results:',
					width: 30
            	},{
					xtype: 'label',
					id: 'DSS_dataTypeDisplay',
					x: 225,
					y: 4,
					text: 'Change'
            	},{
					xtype: 'label',
					x: 330,
					y: 4,
					labelAlign: 'right',
					style: 'color:#777;',
					text: 'Map:',
					width: 50
            	},{
					xtype: 'label',
					id: 'DSS_heatmapTypeDisplay',
					x: 362,
					y: 4,
					width: 110,
					text: 'Quantile'
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
						id: 'DSS_ActualOutputRadio',
						boxLabel: 'Actual',
						name: 'valueStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setValueStyle('absolute');
								Ext.getCmp('DSS_valueTypeDisplay').setText('Actual');
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
						hidden: true,
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setValueStyle('$');
								Ext.getCmp('DSS_valueTypeDisplay').setText('$');
							}
						}
					}]
				},{
					xtype: 'radiogroup',
					x: 180,
					y: 0,
					fieldLabel: 'Results',
					labelWidth: 40,
					width: 190,
					vertical: true,
					columns: 1,
					labelStyle: 'color:#777;',
					items: [{
						id: 'DSS_DeltaOutputRadio',
						boxLabel: 'Change',
						name: 'dataStyle',
						checked: true,
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('delta');
								Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(false);
								Ext.getCmp('DSS_valueTypeDisplay').setDisabled(false);
								Ext.getCmp('DSS_dataTypeDisplay').setText('Change');
							}
						}
					},{
						boxLabel: 'Baseline',
						id: 'DSS_BaselineOutputRadio',
						name: 'dataStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('file1');
								Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(true);
								Ext.getCmp('DSS_valueTypeDisplay').setDisabled(true);
								Ext.getCmp('DSS_dataTypeDisplay').setText('Baseline');
							}
						}
					},{
						boxLabel: 'Scenario',
						name: 'dataStyle',
						padding: '0 0 -5 0',
						handler: function(radio, checked) {
							if (checked) {
								Ext.getCmp('DSS_ReportDetail').setDataStyle('file2');
								Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(true);
								Ext.getCmp('DSS_valueTypeDisplay').setDisabled(true);
								Ext.getCmp('DSS_dataTypeDisplay').setText('Scenario');
							}
						}
					}]
				},{
					xtype: 'radiogroup',
					x: 330,
					y: 0,
					fieldLabel: 'Map',
					labelWidth: 20,
					width: 160,
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
					tooltip: {
						text: 'Show/Hide display options for this report section.'
					},
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

