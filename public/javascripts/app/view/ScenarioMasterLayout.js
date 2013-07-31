
/*//------------------------------------------------------------------------------
Ext.define('MyApp.view.ScenarioMasterLayout', {
		
    extend: 'Ext.panel.Panel',
    alias: 'widget.scenario_master_layout',

	id: 'DSS_ScenarioPanel',
	frameHeader: false,
	border: false,
	autoScroll: true,

	dock: 'bottom',
	height: 300,
	layout: {
		type: 'accordion',
		animate: false,
		multi: true
	},
	dockedItems: [{
		xtype: 'panel',
		dock: 'top',
		title: 'Scenario Setup / Tools',
		icon: 'app/images/magnify_icon.png'
	}],
	
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
				xtype: 'transformationtools',
				collapsed: true
			},
			{
				xtype: 'globalscenariotools',
				collapsed: true
			},
			{
				xtype: 'scenariotools',
				collapsed: true
			}]
        });
        
        me.callParent(arguments);
    }
    
});
*/
// TEST of new, simpler layout.
//------------------------------------------------------------------------------
Ext.define('MyApp.view.ScenarioMasterLayout', {
		
    extend: 'Ext.panel.Panel',
    alias: 'widget.scenario_master_layout',

	id: 'DSS_ScenarioPanel',
	frameHeader: false,
	border: false,
	autoScroll: true,

	dock: 'bottom',
	height: 300,
	dockedItems: [{
		xtype: 'panel',
		dock: 'top',
		title: 'Scenario Setup / Tools',
		icon: 'app/images/magnify_icon.png'
	}],
	
    //--------------------------------------------------------------------------
    initComponent: function() {
    	
        var me = this;
        
        Ext.applyIf(me, {
			items: [{
				xtype: 'scenariotools'
			}]
        });
        
        me.callParent(arguments);
    }
    
});

