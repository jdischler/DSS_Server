Ext.define('MyApp.view.Report_ScenarioComparison', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.scenariocompare',

    height: 60,
    layout: {
        type: 'absolute'
    },
	dock: 'top',
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
				itemId: 'DSS_ScenarioCompare',
				xtype: 'combobox',
				x: 50,
				y: 30,
				width: 300,
				fieldLabel: 'Scenario 2',
				labelAlign: 'right',
				labelWidth: 80,
				value: 'DEFAULT'
			},
			{
				xtype: 'checkboxfield',
				x: 360,
				y: 30,
				boxLabel: 'Compare',
				checked: true,
				listeners: {
					'dirtychange': function(me) {
						var combo = me.up().getComponent('DSS_ScenarioCompare');
						if (me.getValue() == true) {
							combo.enable(true);
						}
						else {
							combo.disable(true);
						}
					}
				}
			}]
        });

        me.callParent(arguments);
    }

});
