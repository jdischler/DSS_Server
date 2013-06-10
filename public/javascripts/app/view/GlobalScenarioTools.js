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
    minHeight: 200,
    width: 300,
    title: 'Set Global Assumptions',
    
    bodyStyle: {'background-color': '#fafcff'},
    header: {
    	style: {
    		'background-image': 'none',
    		'background-color': '#ebf2ff !important',
			border: '1px dotted #d0d8e7'
    	},
    	icon: 'app/images/globe_icon.png'
    },
/*    tabBar: {
    	padding: '0 0 0 20',
    	items: [{
    			xtype: 'tbspacer',
    			width: 40
    	}],
    	style: {
    		'background-image': 'none',
    		'background-color': '#fff !important',
			border: '1px dotted #fff'
    	},
    	plain: true
    },*/
    activeTab: 1,

	tools:[{
		type: 'help',
		qtip: 'Global Scenario Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],

	//--------------------------------------------------------------------------    
	listeners: {
		afterrender: function(c) { 
			
			var spc = Ext.create('Ext.toolbar.Spacer',
			{
				width: 20
			});
			el = c.header.insert(0,spc);
		}
	},
	
   	//--------------------------------------------------------------------------    
    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
            items: [{
            	xtype: 'panel',
            	hidden: false,
            	disabled: true
            },
            {
				xtype: 'panel',
				title: 'Economic',
				icon: 'app/images/economic_icon.png',
				bodyStyle: {
					'background-image': 'none',
					'background-color': '#fff !important',
				},
				items: [{
					xtype: 'economicassumptions',
				}],
				dockedItems: [{
					xtype: 'toolbar',
					dock: 'bottom',
					items: [{
						xtype: 'tbspacer', 
						width: 20
					},
					{
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