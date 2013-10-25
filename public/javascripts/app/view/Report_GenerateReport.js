/*
 * File: app/view/ReportTools.js
 */

Ext.define('MyApp.view.Report_GenerateReport', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_generate_report',

    height: 60,
    width: 300,
    title: 'Save/Print Results',
	icon: 'app/images/new_icon.png',
    activeTab: 0,

    layout: 'absolute',
    
 /*   tools:[{
		type: 'help',
		qtip: 'Report Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],*/
    
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
			items: [{
				xtype: 'button',
				text: 'Save Results',
				icon: 'app/images/save_small_icon.png',
				x: 100,
				y: 10
			},{
				xtype: 'button',
				text: 'Print Results',
				icon: 'app/images/print_small_icon.png',
				x: 250,
				y: 10
			}]
        });

        me.callParent(arguments);
    }

});