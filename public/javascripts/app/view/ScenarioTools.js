/*
 * File: app/view/ScenarioTools.js
 */

 var testGridStore = Ext.create('Ext.data.Store', {
    fields : ['active', 'query', 'transforms'],
    data   : {
        items : [
            { active: true, query: 'Prairie near Rivers In Watershed', transforms: 'To Corn / Soy'  },
            { active: true, query: 'Corn on slopes > 30 near Rivers', transforms: 'To Miscanthus'  },
        ]
    },
    proxy  : {
        type   : 'memory',
        reader : {
            type : 'json',
            root : 'items'
        }
    }
});
 
Ext.define('MyApp.view.ScenarioTools', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.scenariotools',

    height: 200,
    width: 300,
    title: 'Create / Manage Scenario',
	icon: 'app/images/scenario_icon.png',
	cls: 'my-header',
	viewConfig: {
		stripeRows: true
	},

    tools:[{
		type: 'help',
		qtip: 'Scenario Help',
		handler: function(event, target, owner, tool) {
			var help = Ext.create('MyApp.view.LayerHelpWindow').show();
		}
    }],
    
    enableColumnHide: false,
    enableColumnMove: false,

    store: testGridStore,
    
	dockedItems: [{
		xtype: 'toolbar',
		dock: 'bottom',
		items: [{
			xtype: 'button',
			icon: 'app/images/new_icon.png',
			scale: 'medium',
			text: 'New'
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
		},
		{
			xtype: 'button',
			icon: 'app/images/go_icon.png',
			scale: 'medium',
			text: 'Run'
		}]
	}],
	
	columns: [{
		dataIndex: 'query',
		text: 'Query',
		width: 225
	},
	{
		dataIndex: 'transforms',
		text: 'Transforms',
		width: 200
	},
	{
		dataIndex: 'active',
		text: 'x',
	//	xtype: 'checkcolumn',
		width: 40
	}],

    initComponent: function() {
        var me = this;

        Ext.applyIf(me, {
        });

        me.callParent(arguments);
    }

});