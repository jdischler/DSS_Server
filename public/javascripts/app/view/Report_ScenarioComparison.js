Ext.define('MyApp.view.Report_ScenarioComparison', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.scenariocompare',

    height: 60,
//    width: 500,
    layout: {
        type: 'absolute'
    },
	dock: 'top',
//    title: 'Simulation Results Viewer / Comparison',
    header: false,
     bodyStyle: {
    	'background-color': '#f4f8ff'
    },
   
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'combobox',
				x: 50,
				y: 5,
				width: 300,
				fieldLabel: 'Scenario 1',
				labelAlign: 'right',
				labelWidth: 80
			},
			{
				xtype: 'combobox',
				x: 50,
				y: 30,
				width: 300,
				fieldLabel: 'Scenario 2',
				labelAlign: 'right',
				labelWidth: 80
			},
			{
				xtype: 'checkboxfield',
				x: 360,
				y: 30,
				boxLabel: 'Compare'
			}]
        });

        me.callParent(arguments);
    }

});
