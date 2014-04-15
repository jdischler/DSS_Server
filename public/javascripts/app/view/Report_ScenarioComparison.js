
var DSS_ScenarioComparison_DEFAULT = -1;

// TODO: actually save the whole scenario in here? Have a field for it but
//	need to figure out how to wire it all up for how and when to get the scenario
//	back out...
//------------------------------------------------------------------------------
var DSS_ScenarioComparisonStore = Ext.create('Ext.data.Store', {
		
    fields: ['Index', 'ScenarioName', 'Scenario'],
    data: {
        items: [{ 
        	Index: DSS_ScenarioComparison_DEFAULT, 
            ScenarioName: 'CURRENT LANDSCAPE',
            Scenario: undefined
        }]
    },
    proxy: {
        type: 'memory',
        reader: {
            type: 'json',
            root: 'items'
        }
    },
    listeners: {
    	// blah, just force the commit to happen, no reason not to save it right away IMHO
    	update: function(store, record, operation, eOps) {
    		if (operation == Ext.data.Model.EDIT) {
    			store.commitChanges();
    		}
    	}
    }
});

//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_ScenarioComparison', {
    extend: 'Ext.container.Container',
    alias: 'widget.scenariocompare',

//    id: 'DSS_ScenarioComparisonTool',
    
   	requires: [
    	'MyApp.view.Report_ComparisonTypePopup',
   	],
    
 //   height: 34,
//    layout: 'absolute'
	layout: 'vbox',
	dock: 'top',
	
    header: false,
    bodyStyle: {
    	'background-color': '#f4f8ff'
    },
//    hidden: true, // FIXME: finish so we can show this when needed...
   
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	xtype: 'report_comparison_popup'
            },{
            	xtype: 'container',
            	id: 'DSS_ScenarioComparisonTool',
            	hidden: true,
            	layout: 'absolute',
            	height: 34,
            	items: [{
					id: 'DSS_ScenarioCompareCombo_1',
					xtype: 'combobox',
					x: 0,
					y: 5,
					width: 230,
					forceSelection: true,
					allowBlank: false,
					fieldLabel: 'Compare',
					labelAlign: 'right',
					labelWidth: 60,
					store: DSS_ScenarioComparisonStore,
					valueField: 'Index',
					displayField: 'ScenarioName',
					value: DSS_ScenarioComparison_DEFAULT,
					queryMode: 'local'
				},{
					xtype: 'button',
					icon: 'app/images/switch_icon.png',
					tooltip: {
						text: 'Swap values'
					},
					x: 232,
					y: 5,
					handler: function(me,evt) {
						var combo1 = Ext.getCmp('DSS_ScenarioCompareCombo_1');
						var combo2 = Ext.getCmp('DSS_ScenarioCompareCombo_2');
						var temp = combo1.getValue();
						combo1.setValue(combo2.getValue());
						combo2.setValue(temp);
					}
				},{
					id: 'DSS_ScenarioCompareCombo_2',
					xtype: 'combobox',
					x: 250,
					y: 5,
					width: 200,
					forceSelection: true,
					allowBlank: false,
					fieldLabel: 'To',
					labelAlign: 'right',
					labelWidth: 30,
					store: DSS_ScenarioComparisonStore,
					valueField: 'Index',
					displayField: 'ScenarioName',
	//				value: DSS_ScenarioComparison_CURRENT,
					queryMode: 'local'
				},
				{
					xtype: 'button',
					x: 455,
					y: 5,
					width: 30,
					text: 'Go',
					handler: function() {
					}
				}]
			}]
        });

        me.callParent(arguments);
    }

});

