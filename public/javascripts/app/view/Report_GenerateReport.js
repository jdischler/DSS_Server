/*
 * File: app/view/ReportTools.js
 */

Ext.define('MyApp.view.Report_GenerateReport', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.report_generate_report',

    height: 300,
    width: 300,
    title: 'Generate Reports',
	icon: 'app/images/new_icon.png',
    activeTab: 0,

    tools:[{
		type: 'help',
		qtip: 'Report Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],
    
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
    }

});