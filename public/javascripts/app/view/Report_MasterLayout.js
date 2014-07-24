
//------------------------------------------------------------------------------
Ext.define('MyApp.view.Report_MasterLayout', {
		
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_master_layout',

	requires: [
		'MyApp.view.Report_ScenarioComparison',
		'MyApp.view.Report_SpiderGraph',
		'MyApp.view.Report_Detail',
		'MyApp.view.Report_GenerateReport'
	],
	
	dock: 'right',
	title: 'Simulation Results Viewer / Comparison',//Simulation Results / Reports',
	icon: 'app/images/magnify_icon.png',
	width: 500,
	autoScroll: true,
	layout: {
		fill: false,
		autoWidth: false,
		type: 'accordion',
		animate: false,
		multi: true
	},
	collapseDirection: 'right',
	collapsible: true,
	collapsed: true,
	animCollapse: false,
	bodystyle: 'border-color:#000; border-width:2px',
	/*dockedItems: [{
			xtype: 'scenariocompare' // docked top
	}],*/
	
	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) {
			// NOTE: didn't like the expander tool appearing at the BOTTOM
			//	of the rightmost collapsible panel. It is not very visible.
			//	So, I move it from the last position to the first position...
			var tool = c.reExpander.remove(c.reExpander.items.getAt(2), false);
			c.reExpander.insert(0, tool);
		},
	},
	
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
			items: [{
			// NOTE: Hidden Panel to allow all visible items to collapse.
				xtype: 'panel',
				hidden: true,
				collapsed: false
			},
			{
				xtype: 'report_spider'
			},
			{
				xtype: 'report_detail'
			},
			{
				xtype: 'report_generate_report'
			}]
        });
        
        me.callParent(arguments);
    }
    
});
