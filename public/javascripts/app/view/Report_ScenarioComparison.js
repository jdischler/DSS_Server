
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_ScenarioComparison', {
    extend: 'Ext.container.Container',
    alias: 'widget.scenariocompare',

    height: 34,
    layout: {
        type: 'absolute'
    },
	dock: 'top',
    header: false,
     bodyStyle: {
    	'background-color': '#f4f8ff'
    },
   
    //--------------------------------------------------------------------------
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'combobox',
				x: 0,
				y: 5,
				width: 230,
				fieldLabel: 'Compare Scenario',
				labelAlign: 'right',
				labelWidth: 110,
				value: 'DEFAULT'
			},
			{
				xtype: 'combobox',
				x: 220,
				y: 5,
				width: 230,
				fieldLabel: 'To Scenario',
				labelAlign: 'right',
				labelWidth: 90,
				value: 'TRANSFORM 1'
			},
			{
				xtype: 'button',
				x: 455,
				y: 5,
				width: 30,
				text: 'Go'
			}]
        });

        me.callParent(arguments);
    }

});

