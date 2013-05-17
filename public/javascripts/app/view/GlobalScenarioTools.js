/*
 * File: app/view/GlobalScenario.js
 */

Ext.define('MyApp.view.GlobalScenarioTools', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.globalscenariotools',

    requires: [
    	'MyApp.view.EconomicAssumptions'
    ],
    
    height: 200,
    width: 300,
    title: 'Set Global Assumptions',
	icon: 'app/images/globe_icon.png',
    activeTab: 0,

	tools:[{
		type: 'help',
		qtip: 'Global Scenario Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
				xtype: 'panel',
				title: 'Economic',
				icon: 'app/images/economic_icon.png',
				items: [{
					xtype: 'economicassumptions',
				}],
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [{
						xtype: 'button',
						icon: 'app/images/new_icon.png',
						scale: 'medium',
						text: 'Defaults'
					},
					{
						xtype: 'button',
						icon: 'app/images/save_icon.png',
						scale: 'medium',
						text: 'Save'
					},
					{
						xtype: 'button',
						icon: 'app/images/load_icon.png',
						scale: 'medium',
						text: 'Load'
					}]
				}]
			},
			{
				xtype: 'panel',
				bodyPadding: 10,
				title: 'Climate',
				icon: 'app/images/climate_icon.png',
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [{
						xtype: 'button',
						icon: 'app/images/new_icon.png',
						scale: 'medium',
						text: 'Defaults'
					},
					{
						xtype: 'button',
						icon: 'app/images/save_icon.png',
						scale: 'medium',
						text: 'Save'
					},
					{
						xtype: 'button',
						icon: 'app/images/load_icon.png',
						scale: 'medium',
						text: 'Load'
					}]
				}]
			},
			{
				xtype: 'panel',
				bodyPadding: 10,
				title: 'Policies',
				icon: 'app/images/policy_icon.png',
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [{
						xtype: 'button',
						icon: 'app/images/new_icon.png',
						scale: 'medium',
						text: 'Defaults'
					},
					{
						xtype: 'button',
						icon: 'app/images/save_icon.png',
						scale: 'medium',
						text: 'Save'
					},
					{
						xtype: 'button',
						icon: 'app/images/load_icon.png',
						scale: 'medium',
						text: 'Load'
					}]
				}]
			}]
        });

        me.callParent(arguments);
    }

});