
//------------------------------------------------------------------------------
Ext.define('MyApp.view.ReportMasterLayout', {
		
    extend: 'Ext.panel.Panel',
    alias: 'widget.report_master_layout',

	dock: 'right',
	title: 'Simulation Results / Reports',
	icon: 'app/images/magnify_icon.png',
	width: 400,
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
				xtype: 'evaluationtools'
			},
			{
				xtype: 'reporttools',
				collapsed: true
			}]
        });
        
        me.callParent(arguments);
    }
    
});
