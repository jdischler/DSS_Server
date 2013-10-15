
// Values used by this WIDGET object - should be provided on the configured object...    
// DSS_Label - the label for the value field
// DSS_UnitLabel - the label that describes what units the value field are in
// DSS_GraphTitle - the title for the popup graph window
// DSS_GraphData - the server passed JSON that contains the data bins for the graphes
// DSS_FieldString - the name passed to the server heatmap function, spider graphs, etc.

//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_DetailHeader', {
    extend: 'Ext.container.Container',
    alias: 'widget.report_detail_header',

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
				id: 'DSS_ValueStyleRadioGroup',
				x: 20,
				y: 4,
				fieldLabel: 'Values',
				labelWidth: 42,
				width: 200,
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
					boxLabel: '%',
					name: 'valueStyle',
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setValueStyle('%');
						}
					}
				}]
			},{
				xtype: 'radiogroup',
				x: 210,
				y: 4,
				fieldLabel: 'Data',
				labelWidth: 30,
				width: 190,
				labelStyle: 'color:#777;',
				items: [{
					boxLabel: 'Delta',
					name: 'dataStyle',
					checked: true,
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setDataStyle('delta');
							Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(false);
						}
					}
				},{
					boxLabel: 'File1',
					name: 'dataStyle',
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setDataStyle('file1');
							Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(true);
						}
					}
				},{
					boxLabel: 'File2',
					name: 'dataStyle',
					handler: function(radio, checked) {
						if (checked) {
							Ext.getCmp('DSS_ReportDetail').setDataStyle('file2');
							Ext.getCmp('DSS_ValueStyleRadioGroup').setDisabled(true);
						}
					}
				}]
			}, {
				xtype: 'checkbox',
				x: 420,
				y: 4,
				fieldLabel: 'EqInt',
				labelWidth: 35,
				handler: function(checkbox, checked) {
					var subtype = 'quantile';
					if (checked) {
						subtype = 'equal';
					}
					Ext.getCmp('DSS_ReportDetail').changeDataSubStyle(subtype);
				}
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
    
    setWait: function() {
    }

});

